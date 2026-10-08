package com.aldoria.rpg

object Tl {
    const val GRASS = 0
    const val DIRT = 1
    const val WATER = 2
    const val SAND = 3
    const val STONE = 4
    const val WOOD = 5
    const val WALL = 6
    const val TREE = 7
    const val ROCK = 8
    const val FLOWER = 9
    const val BUSH = 10
    const val DOOR = 11
    const val FOUNTAIN = 12
    const val COUNT = 13
}

class World {
    val w = Cfg.MAP_W
    val h = Cfg.MAP_H
    val tiles = IntArray(w * h)
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

    fun tile(x: Int, y: Int): Int = if (x < 0 || y < 0 || x >= w || y >= h) Tl.TREE else tiles[y * w + x]

    fun solid(t: Int): Boolean =
        t == Tl.WATER || t == Tl.WALL || t == Tl.TREE || t == Tl.ROCK || t == Tl.BUSH || t == Tl.FOUNTAIN

    fun walkable(x: Int, y: Int): Boolean = !solid(tile(x, y))

    fun isPz(x: Int, y: Int): Boolean = x >= 0 && y >= 0 && x < w && y < h && pz[y * w + x]

    private fun put(x: Int, y: Int, t: Int) {
        if (x >= 0 && y >= 0 && x < w && y < h) tiles[y * w + x] = t
    }

    private fun fillRect(x0: Int, y0: Int, x1: Int, y1: Int, t: Int) {
        for (yy in y0..y1) for (xx in x0..x1) put(xx, yy, t)
    }

    private fun house(x0: Int, y0: Int, hw: Int, hh: Int, doorX: Int, doorY: Int) {
        fillRect(x0, y0, x0 + hw - 1, y0 + hh - 1, Tl.WOOD)
        for (xx in x0 until x0 + hw) {
            put(xx, y0, Tl.WALL)
            put(xx, y0 + hh - 1, Tl.WALL)
        }
        for (yy in y0 until y0 + hh) {
            put(x0, yy, Tl.WALL)
            put(x0 + hw - 1, yy, Tl.WALL)
        }
        put(doorX, doorY, Tl.DOOR)
    }

    private fun generate() {
        // terreno base: lagos, areia, florestas, arbustos, flores e pedras
        for (y in 0 until h) {
            for (x in 0 until w) {
                var t = Tl.GRASS
                val dLake = Math.hypot(x - 54.0, y - 9.0) / 10.0 + (smoothNoise(x / 4f, y / 4f, 11) - 0.5f) * 0.5f
                val dPond = Math.hypot(x - 10.0, y - 38.0) / 6.0 + (smoothNoise(x / 3f, y / 3f, 12) - 0.5f) * 0.5f
                val d = Math.min(dLake, dPond)
                if (d < 0.85) {
                    t = Tl.WATER
                } else if (d < 1.1) {
                    t = Tl.SAND
                } else {
                    val f = smoothNoise(x / 5f, y / 5f, 7)
                    val r = rand01(x, y, 3)
                    if (f > 0.62f && r < 0.85f) t = Tl.TREE
                    else if (f > 0.56f && r < 0.25f) t = Tl.BUSH
                    else if (r < 0.05f) t = Tl.FLOWER
                    else if (r > 0.992f) t = Tl.ROCK
                }
                tiles[y * w + x] = t
            }
        }
        // borda do mundo: floresta fechada
        for (x in 0 until w) {
            for (k in 0..1) {
                put(x, k, Tl.TREE)
                put(x, h - 1 - k, Tl.TREE)
            }
        }
        for (y in 0 until h) {
            for (k in 0..1) {
                put(k, y, Tl.TREE)
                put(w - 1 - k, y, Tl.TREE)
            }
        }
        // cidade: limpa a área
        fillRect(20, 12, 44, 36, Tl.GRASS)
        // estradas de terra
        for (x in 0 until w) {
            val inTown = x in 20..44
            val ry = if (inTown) 24 else 24 + Math.round((smoothNoise(x / 9f, 2f, 3) - 0.5f) * 5f)
            for (k in 0..1) if (tile(x, ry + k) != Tl.WATER) put(x, ry + k, Tl.DIRT)
        }
        for (y in 0 until h) {
            val inTown = y in 12..36
            val rx = if (inTown) 32 else 32 + Math.round((smoothNoise(3f, y / 9f, 4) - 0.5f) * 5f)
            for (k in 0..1) if (tile(rx + k, y) != Tl.WATER) put(rx + k, y, Tl.DIRT)
        }
        // praça de pedra com fonte
        fillRect(27, 20, 37, 28, Tl.STONE)
        put(32, 24, Tl.FOUNTAIN)
        // casas
        house(22, 14, 6, 5, 24, 18)
        house(36, 14, 6, 5, 38, 18)
        house(22, 30, 6, 5, 24, 30)
        house(36, 30, 7, 5, 39, 30)
        for (yy in 19..23) {
            put(24, yy, Tl.DIRT)
            put(38, yy, Tl.DIRT)
        }
        for (yy in 26..29) {
            put(24, yy, Tl.DIRT)
            put(39, yy, Tl.DIRT)
        }
        // algumas flores na cidade
        val rn = java.util.Random(5L)
        for (i in 0 until 26) {
            val fx = 21 + rn.nextInt(23)
            val fy = 13 + rn.nextInt(23)
            if (tile(fx, fy) == Tl.GRASS) put(fx, fy, Tl.FLOWER)
        }
        // zona de proteção: a cidade toda
        for (y in 13..35) for (x in 21..43) pz[y * w + x] = true

        // pontos onde os monstros nascem: fora da cidade, no gramado
        val sr = java.util.Random(77L)
        var tries = 0
        while (spawns.size < 18 && tries < 4000) {
            tries++
            val x = 4 + sr.nextInt(w - 8)
            val y = 4 + sr.nextInt(h - 8)
            val t = tile(x, y)
            if (t != Tl.GRASS && t != Tl.FLOWER) continue
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
