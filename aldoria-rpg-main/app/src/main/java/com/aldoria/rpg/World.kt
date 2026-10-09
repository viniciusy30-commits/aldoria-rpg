package com.aldoria.rpg

/** Tipos de chão. */
object Gd {
    const val GRASS = 0
    const val DIRT = 1
    const val SAND = 2
    const val STONE = 3
    const val SHALLOW = 4
    const val WATER = 5
    const val WOOD = 6
    const val CARPET = 7
    const val BRIDGE = 8
    const val COUNT = 9
}

/** Objetos que ficam em cima do chão. */
object Ob {
    const val NONE = 0
    const val OAK = 1
    const val PINE = 2
    const val DEAD = 3
    const val BUSH = 4
    const val BUSHF = 5
    const val ROCK = 6
    const val BOULDER = 7
    const val LOG = 8
    const val STUMP = 9
    const val WALL = 10
    const val DOOR = 11
    const val FOUNTAIN = 12
    const val STATUE = 13
    const val LAMP = 14
    const val BENCH = 15
    const val PLANTER = 16
    const val STALL = 17
    const val WELL = 18
    const val SIGN = 19
    const val FENCE = 20
    const val BED = 21
    const val TABLE = 22
    const val CHAIR = 23
    const val SHELF = 24
    const val SHELFP = 25
    const val BARREL = 26
    const val CRATE = 27
    const val CHEST = 28
    const val FIRE = 29
    const val CAULDRON = 30
    const val COUNTER = 31
    const val COUNT = 32

    fun solid(o: Int): Boolean = o != NONE && o != DOOR && o != CHAIR
}

/** Decorações pisáveis (flores, mato, cogumelos...). */
object Dc {
    const val NONE = 0
    const val FLOWER_R = 1
    const val FLOWER_Y = 2
    const val FLOWER_B = 3
    const val FLOWER_W = 4
    const val TALL = 5
    const val MUSH = 6
    const val MUSH2 = 7
    const val PEBBLE = 8
    const val LILY = 9
    const val LILYF = 10
    const val REED = 11
    const val FERN = 12
    const val COUNT = 13
}

class World {
    val w = Cfg.MAP_W
    val h = Cfg.MAP_H
    val ground = IntArray(w * h)
    val vari = IntArray(w * h)
    val obj = IntArray(w * h)
    val ovar = IntArray(w * h)
    val deco = IntArray(w * h)
    val pz = BooleanArray(w * h)
    val spawns = ArrayList<IntArray>()
    val startX = 32
    val startY = 26

    private val prev = IntArray(w * h)
    private val queue = IntArray(w * h)
    private val seen = IntArray(w * h)
    private var stamp = 0

    init {
        generate()
    }

    fun inside(x: Int, y: Int): Boolean = x >= 0 && y >= 0 && x < w && y < h

    fun groundAt(x: Int, y: Int): Int = if (inside(x, y)) ground[y * w + x] else Gd.WATER
    fun objAt(x: Int, y: Int): Int = if (inside(x, y)) obj[y * w + x] else Ob.NONE

    fun solidAt(x: Int, y: Int): Boolean {
        if (!inside(x, y)) return true
        val i = y * w + x
        if (ground[i] == Gd.WATER) return true
        return Ob.solid(obj[i])
    }

    fun walkable(x: Int, y: Int): Boolean = !solidAt(x, y)

    fun isPz(x: Int, y: Int): Boolean = inside(x, y) && pz[y * w + x]

    private fun setG(x: Int, y: Int, g: Int) {
        if (inside(x, y)) {
            val i = y * w + x
            ground[i] = g
            vari[i] = hash2(x, y, 17) and 3
        }
    }

    private fun setO(x: Int, y: Int, o: Int, v: Int) {
        if (inside(x, y)) {
            val i = y * w + x
            obj[i] = o
            ovar[i] = v
            deco[i] = Dc.NONE
        }
    }

    private fun grassTone(x: Int, y: Int): Int {
        val tone = (smoothNoise(x / 7f, y / 7f, 31) * 4f).toInt().coerceIn(0, 3)
        return if (rand01(x, y, 19) < 0.07f) 4 + (hash2(x, y, 23) and 3) else tone
    }

    private fun house(x0: Int, y0: Int, hw: Int, hh: Int, doorX: Int, doorY: Int) {
        for (yy in y0 until y0 + hh) for (xx in x0 until x0 + hw) setG(xx, yy, Gd.WOOD)
        for (xx in x0 until x0 + hw) {
            setO(xx, y0, Ob.WALL, 0)
            setO(xx, y0 + hh - 1, Ob.WALL, 0)
        }
        for (yy in y0 until y0 + hh) {
            setO(x0, yy, Ob.WALL, 0)
            setO(x0 + hw - 1, yy, Ob.WALL, 0)
        }
        setO(doorX, doorY, Ob.DOOR, 0)
    }

    private fun wallKind(x: Int, y: Int, kind: Int) {
        if (inside(x, y) && obj[y * w + x] == Ob.WALL) ovar[y * w + x] = kind
    }

    private fun generate() {
        // 1) água, areia e grama
        for (y in 0 until h) {
            for (x in 0 until w) {
                val i = y * w + x
                var g = Gd.GRASS
                val dLake = Math.hypot(x - 54.0, y - 9.0) / 10.0 + (smoothNoise(x / 4f, y / 4f, 11) - 0.5f) * 0.5f
                val dPond = Math.hypot(x - 10.0, y - 38.0) / 6.0 + (smoothNoise(x / 3f, y / 3f, 12) - 0.5f) * 0.5f
                val d = Math.min(dLake, dPond)
                if (d < 0.62) g = Gd.WATER else if (d < 0.85) g = Gd.SHALLOW else if (d < 1.1) g = Gd.SAND
                if (y >= 13) {
                    val rcx = 49f + (smoothNoise(0f, y / 7f, 21) - 0.5f) * 4f
                    val dr = Math.abs(x - rcx)
                    if (dr < 1.5f) g = Gd.WATER
                    else if (dr < 2.6f && g != Gd.WATER) g = Gd.SHALLOW
                    else if (dr < 3.4f && g == Gd.GRASS) g = Gd.SAND
                }
                ground[i] = g
                vari[i] = if (g == Gd.GRASS) grassTone(x, y) else (hash2(x, y, 17) and 3)
            }
        }
        // 2) borda do mundo: floresta fechada
        for (y in 0 until h) {
            for (x in 0 until w) {
                if (x >= 2 && y >= 2 && x < w - 2 && y < h - 2) continue
                val g = ground[y * w + x]
                if (g != Gd.GRASS && g != Gd.SAND) continue
                val pine = smoothNoise(x / 9f, y / 9f, 41) > 0.5f
                val hv = hash2(x, y, 29)
                if (pine) setO(x, y, Ob.PINE, hv and 1) else setO(x, y, Ob.OAK, ((hv and 3) % 3))
            }
        }
        // 3) cidade: limpa a área
        for (y in 12..36) {
            for (x in 20..44) {
                val i = y * w + x
                ground[i] = Gd.GRASS
                vari[i] = grassTone(x, y)
                obj[i] = Ob.NONE
                deco[i] = Dc.NONE
            }
        }
        // 4) estradas de terra (com ponte onde cruzam o rio)
        for (x in 0 until w) {
            val inTown = x in 20..44
            val ry = if (inTown) 24 else 24 + Math.round((smoothNoise(x / 9f, 2f, 3) - 0.5f) * 5f)
            for (k in 0..1) {
                val yy = ry + k
                val g = groundAt(x, yy)
                if (g == Gd.WATER || g == Gd.SHALLOW || g == Gd.BRIDGE) setG(x, yy, Gd.BRIDGE) else setG(x, yy, Gd.DIRT)
                if (inside(x, yy)) {
                    obj[yy * w + x] = Ob.NONE
                    deco[yy * w + x] = Dc.NONE
                }
            }
        }
        for (y in 0 until h) {
            val inTown = y in 12..36
            val rx = if (inTown) 32 else 32 + Math.round((smoothNoise(3f, y / 9f, 4) - 0.5f) * 5f)
            for (k in 0..1) {
                val xx = rx + k
                val g = groundAt(xx, y)
                if (g != Gd.WATER && g != Gd.SHALLOW && g != Gd.BRIDGE) setG(xx, y, Gd.DIRT)
                if (inside(xx, y)) {
                    obj[y * w + xx] = Ob.NONE
                    deco[y * w + xx] = Dc.NONE
                }
            }
        }
        // 5) praça de pedra
        for (y in 20..28) for (x in 27..37) setG(x, y, Gd.STONE)

        // 6) casas
        house(22, 14, 6, 5, 24, 18)
        house(36, 14, 6, 5, 38, 18)
        house(22, 30, 6, 5, 24, 30)
        house(36, 30, 7, 5, 39, 30)
        for (yy in 19..23) {
            setG(24, yy, Gd.DIRT)
            setG(38, yy, Gd.DIRT)
        }
        for (yy in 26..29) {
            setG(24, yy, Gd.DIRT)
            setG(39, yy, Gd.DIRT)
        }
        // janelas e tochas
        wallKind(24, 14, 1); wallKind(26, 14, 2); wallKind(22, 16, 1); wallKind(27, 16, 1)
        wallKind(26, 18, 1); wallKind(25, 18, 2); wallKind(23, 18, 1)
        wallKind(38, 14, 1); wallKind(40, 14, 2); wallKind(36, 16, 1); wallKind(41, 16, 1)
        wallKind(40, 18, 1); wallKind(39, 18, 2); wallKind(37, 18, 1)
        wallKind(24, 34, 1); wallKind(26, 34, 2); wallKind(22, 32, 1); wallKind(27, 32, 1)
        wallKind(23, 30, 2); wallKind(25, 30, 2); wallKind(26, 30, 1)
        wallKind(38, 34, 1); wallKind(41, 34, 2); wallKind(36, 32, 1); wallKind(42, 32, 1)
        wallKind(38, 30, 2); wallKind(40, 30, 2); wallKind(41, 30, 1)

        // 7) mobília das casas
        for (yy in 16..17) for (xx in 24..25) setG(xx, yy, Gd.CARPET)
        setO(23, 15, Ob.BED, 0); setO(25, 15, Ob.SHELF, 0); setO(26, 15, Ob.CHEST, 0); setO(26, 17, Ob.CHAIR, 0)
        for (xx in 37..40) setO(xx, 15, Ob.SHELF, xx % 3)
        setO(39, 16, Ob.TABLE, 2); setO(38, 16, Ob.CHAIR, 0); setO(40, 17, Ob.CHEST, 0)
        setG(37, 17, Gd.CARPET); setG(38, 17, Gd.CARPET)
        setO(23, 31, Ob.FIRE, 0); setO(25, 32, Ob.TABLE, 1); setO(24, 32, Ob.CHAIR, 0)
        setO(23, 33, Ob.COUNTER, 0); setO(24, 33, Ob.COUNTER, 1); setO(25, 33, Ob.COUNTER, 0)
        setO(26, 33, Ob.BARREL, 0); setO(26, 32, Ob.BARREL, 1)
        for (xx in 37..39) setO(xx, 33, Ob.SHELFP, xx % 3)
        setO(41, 32, Ob.CAULDRON, 0); setO(37, 31, Ob.TABLE, 0); setO(41, 31, Ob.CRATE, 0)
        setG(39, 32, Gd.CARPET); setG(40, 32, Gd.CARPET)
        setO(25, 19, Ob.SIGN, 3); setO(39, 19, Ob.SIGN, 2); setO(25, 29, Ob.SIGN, 0); setO(40, 29, Ob.SIGN, 1)

        // 8) cerca da cidade (com portões nas estradas) e lampiões
        for (x in 21..43) {
            if (x !in 31..34) {
                setO(x, 13, Ob.FENCE, 0)
                setO(x, 35, Ob.FENCE, 0)
            }
        }
        for (y in 13..35) {
            if (y !in 23..26) {
                setO(21, y, Ob.FENCE, 0)
                setO(43, y, Ob.FENCE, 0)
            }
        }
        setO(30, 13, Ob.LAMP, 0); setO(35, 13, Ob.LAMP, 0); setO(30, 35, Ob.LAMP, 0); setO(35, 35, Ob.LAMP, 0)
        setO(21, 22, Ob.LAMP, 0); setO(21, 27, Ob.LAMP, 0); setO(43, 22, Ob.LAMP, 0); setO(43, 27, Ob.LAMP, 0)

        // 9) praça: fonte, estátua do mago, bancos, vasos, barracas e poço
        setO(32, 24, Ob.FOUNTAIN, 0)
        setO(32, 21, Ob.STATUE, 0)
        setO(28, 21, Ob.LAMP, 0); setO(36, 21, Ob.LAMP, 0); setO(28, 27, Ob.LAMP, 0); setO(36, 27, Ob.LAMP, 0)
        setO(29, 24, Ob.BENCH, 0); setO(35, 24, Ob.BENCH, 0)
        setO(30, 21, Ob.PLANTER, 0); setO(34, 21, Ob.PLANTER, 1); setO(30, 27, Ob.PLANTER, 2); setO(34, 27, Ob.PLANTER, 0)
        setO(23, 22, Ob.STALL, 0); setO(41, 22, Ob.STALL, 1); setO(23, 27, Ob.STALL, 2); setO(41, 27, Ob.STALL, 3)
        setO(39, 26, Ob.WELL, 0)
        setO(22, 20, Ob.CRATE, 1); setO(42, 20, Ob.BARREL, 0)
        // flores no gramado da cidade
        val rt = java.util.Random(5L)
        for (i in 0 until 60) {
            val fx = 22 + rt.nextInt(21)
            val fy = 14 + rt.nextInt(21)
            val idx = fy * w + fx
            if (ground[idx] == Gd.GRASS && obj[idx] == Ob.NONE) deco[idx] = Dc.FLOWER_R + rt.nextInt(4)
        }

        // 10) natureza fora da cidade
        for (y in 0 until h) {
            for (x in 0 until w) {
                val i = y * w + x
                if (x in 20..44 && y in 12..36) continue
                if (obj[i] != Ob.NONE) continue
                val g = ground[i]
                val f = smoothNoise(x / 5f, y / 5f, 7)
                val r = rand01(x, y, 3)
                val hv = hash2(x, y, 29)
                if (g == Gd.GRASS) {
                    val pine = smoothNoise(x / 11f, y / 11f, 41) > 0.52f
                    if (f > 0.62f && r < 0.8f) {
                        if (pine) setO(x, y, Ob.PINE, hv and 1) else setO(x, y, Ob.OAK, ((hv ushr 2) and 3) % 3)
                    } else if (f > 0.56f && r < 0.22f) {
                        setO(x, y, if ((hv and 1) == 0) Ob.BUSH else Ob.BUSHF, (hv ushr 3) and 3)
                    } else if (r < 0.012f) {
                        setO(x, y, Ob.ROCK, ((hv ushr 2) and 3) % 3)
                    } else if (r < 0.016f) {
                        setO(x, y, Ob.BOULDER, hv and 1)
                    } else if (r < 0.019f) {
                        setO(x, y, Ob.LOG, hv and 1)
                    } else if (r < 0.022f) {
                        setO(x, y, Ob.STUMP, hv and 1)
                    } else if (r < 0.025f) {
                        setO(x, y, Ob.DEAD, hv and 1)
                    }
                    if (obj[i] == Ob.NONE) {
                        val r2 = rand01(x, y, 8)
                        val patch = smoothNoise(x / 6f, y / 6f, 51)
                        if (r2 < 0.05f) deco[i] = Dc.FLOWER_R + (hv and 3)
                        else if (r2 < 0.13f && patch > 0.55f) deco[i] = Dc.TALL
                        else if (r2 < 0.16f && f > 0.5f) deco[i] = if ((hv and 1) == 0) Dc.MUSH else Dc.MUSH2
                        else if (r2 < 0.19f && f > 0.45f) deco[i] = Dc.FERN
                        else if (r2 < 0.2f) deco[i] = Dc.PEBBLE
                    }
                } else if (g == Gd.SAND || g == Gd.DIRT) {
                    if (r < 0.06f) deco[i] = Dc.PEBBLE
                } else if (g == Gd.SHALLOW) {
                    var land = false
                    for (k in 0 until 4) {
                        val ng = groundAt(x + DX[k], y + DY[k])
                        if (ng != Gd.WATER && ng != Gd.SHALLOW && ng != Gd.BRIDGE) land = true
                    }
                    if (land && r < 0.4f) deco[i] = Dc.REED
                } else if (g == Gd.WATER) {
                    var all = true
                    for (k in 0 until 4) {
                        val ng = groundAt(x + DX[k], y + DY[k])
                        if (ng != Gd.WATER && ng != Gd.SHALLOW) all = false
                    }
                    if (all && r < 0.07f) deco[i] = if (r < 0.025f) Dc.LILYF else Dc.LILY
                }
            }
        }

        // 11) zona de proteção: a cidade toda
        for (y in 13..35) for (x in 21..43) pz[y * w + x] = true

        // 12) pontos onde os monstros nascem: fora da cidade, no gramado
        val sr = java.util.Random(77L)
        var tries = 0
        while (spawns.size < 18 && tries < 4000) {
            tries++
            val x = 4 + sr.nextInt(w - 8)
            val y = 4 + sr.nextInt(h - 8)
            val i = y * w + x
            if (ground[i] != Gd.GRASS || obj[i] != Ob.NONE) continue
            if (Math.hypot(x - 32.0, y - 24.0) < 16.0) continue
            var ok = true
            for (s in spawns) {
                if (cheb(s[0], s[1], x, y) < 5) {
                    ok = false
                    break
                }
            }
            if (ok) spawns.add(intArrayOf(x, y))
        }
    }

    /** Cor do minimapa para um quadrado. */
    fun miniColor(x: Int, y: Int): Int {
        val i = y * w + x
        when (obj[i]) {
            Ob.OAK, Ob.PINE, Ob.DEAD -> return 0xFF1F5A24.toInt()
            Ob.BUSH, Ob.BUSHF -> return 0xFF2E7A33.toInt()
            Ob.ROCK, Ob.BOULDER -> return 0xFF7A7A82.toInt()
            Ob.WALL -> return 0xFFB5482F.toInt()
            Ob.DOOR -> return 0xFF6B4423.toInt()
            Ob.FENCE -> return 0xFF8A6A45.toInt()
            Ob.FOUNTAIN, Ob.WELL -> return 0xFF5BA0F0.toInt()
            Ob.NONE -> {
            }
            else -> return 0xFFD0B070.toInt()
        }
        return when (ground[i]) {
            Gd.WATER -> 0xFF2461C2.toInt()
            Gd.SHALLOW -> 0xFF4B9ADC.toInt()
            Gd.SAND -> 0xFFE6D08F.toInt()
            Gd.DIRT -> 0xFF8A6A45.toInt()
            Gd.STONE -> 0xFFA0A0A8.toInt()
            Gd.WOOD, Gd.BRIDGE -> 0xFF8A5C2D.toInt()
            Gd.CARPET -> 0xFF8E2A33.toInt()
            else -> 0xFF3F8531.toInt()
        }
    }

    /**
     * Caminho mais curto (4 direções) de [sx,sy] até [tx,ty].
     * Com [adjacentGoal], para ao lado do alvo. Devolve as posições (y * w + x) do primeiro passo até o fim.
     */
    fun path(
        sx: Int, sy: Int, tx: Int, ty: Int, maxNodes: Int, adjacentGoal: Boolean,
        passable: (Int, Int) -> Boolean
    ): IntArray? {
        stamp++
        var head = 0
        var tail = 0
        val start = sy * w + sx
        queue[tail++] = start
        seen[start] = stamp
        prev[start] = -1
        var found = -1
        while (head < tail && head < maxNodes) {
            val cur = queue[head++]
            val cx = cur % w
            val cy = cur / w
            val dd = Math.abs(cx - tx) + Math.abs(cy - ty)
            if ((adjacentGoal && dd == 1) || (!adjacentGoal && dd == 0)) {
                found = cur
                break
            }
            for (k in 0 until 4) {
                val nx = cx + DX[k]
                val ny = cy + DY[k]
                if (nx < 0 || ny < 0 || nx >= w || ny >= h) continue
                val ni = ny * w + nx
                if (seen[ni] == stamp) continue
                if (!passable(nx, ny)) continue
                seen[ni] = stamp
                prev[ni] = cur
                queue[tail++] = ni
            }
        }
        if (found < 0) return null
        val list = ArrayList<Int>()
        var c = found
        while (c != start && c >= 0) {
            list.add(c)
            c = prev[c]
        }
        list.reverse()
        return IntArray(list.size) { list[it] }
    }
}
