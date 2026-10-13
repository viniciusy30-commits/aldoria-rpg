package com.aldoria.rpg

/** Texturas de chão em 32 x 32 pixels. */
object ArtGround {

    private fun pebble(b: PixBuf, x: Int, y: Int, shadow: Int) {
        b.rect(x, y, 3, 2, h(0xFFA8A8B0))
        b.set(x, y, h(0xFFD6D6DE))
        b.set(x + 1, y, h(0xFFC4C4CC))
        b.rect(x + 1, y + 2, 3, 1, shadow)
    }

    fun grass(v: Int): PixBuf {
        val b = PixBuf(32, 32)
        val tones = intArrayOf(h(0xFF4E9C36), h(0xFF58A83D), h(0xFF47922F), h(0xFF53A13A))
        val base = tones[v and 3]
        b.fill(base)
        val dark = shadeCol(base, -0.16f)
        val light = shadeCol(base, 0.15f)
        val hi = shadeCol(base, 0.3f)
        val rn = java.util.Random(700L + v * 13L)
        for (i in 0 until 80) b.set(rn.nextInt(32), rn.nextInt(32), if (rn.nextBoolean()) light else dark)
        for (i in 0 until 3) b.rect(rn.nextInt(28), rn.nextInt(29), 3, 2, dark)
        for (i in 0 until 14) {
            val x = 1 + rn.nextInt(30)
            val y = 3 + rn.nextInt(28)
            b.set(x, y, light)
            b.set(x, y - 1, hi)
            b.set(x + 1, y + 1, dark)
            if (i % 2 == 0) b.set(x - 1, y - 1, hi)
        }
        when (v) {
            4 -> for (i in 0 until 3) pebble(b, 4 + rn.nextInt(24), 4 + rn.nextInt(24), shadeCol(base, -0.3f))
            5 -> {
                for (k in 0 until 3) {
                    val x = 5 + rn.nextInt(22)
                    val y = 5 + rn.nextInt(22)
                    b.set(x, y - 1, h(0xFFFFFFFF))
                    b.set(x - 1, y, h(0xFFFFFFFF))
                    b.set(x + 1, y, h(0xFFFFFFFF))
                    b.set(x, y + 1, h(0xFFFFFFFF))
                    b.set(x, y, h(0xFFF5D547))
                }
            }
            6 -> for (i in 0 until 4) {
                val x = 3 + rn.nextInt(26)
                val y = 3 + rn.nextInt(26)
                b.rect(x, y, 2, 2, dark)
                b.rect(x + 2, y - 1, 2, 2, dark)
                b.rect(x + 2, y + 2, 2, 2, dark)
                b.set(x + 2, y + 1, light)
            }
            7 -> for (i in 0 until 10) {
                val x = rn.nextInt(31)
                val y = rn.nextInt(28) + 3
                b.set(x, y, h(0xFFA3A03A))
                b.set(x, y - 1, h(0xFFC2BE55))
            }
        }
        return b
    }

    fun dirt(v: Int): PixBuf {
        val b = PixBuf(32, 32)
        val tones = intArrayOf(h(0xFFA37B4F), h(0xFFA97F52), h(0xFF9A7249), h(0xFFA67D50))
        val base = tones[v and 3]
        b.fill(base)
        val dark = shadeCol(base, -0.17f)
        val light = shadeCol(base, 0.15f)
        val rn = java.util.Random(800L + v * 11L)
        for (i in 0 until 110) b.set(rn.nextInt(32), rn.nextInt(32), if (rn.nextBoolean()) light else dark)
        for (i in 0 until 4) {
            val x = 2 + rn.nextInt(26)
            val y = 2 + rn.nextInt(26)
            b.rect(x, y, 3, 2, h(0xFFC4A57E))
            b.set(x, y, h(0xFFDEC59C))
            b.rect(x + 1, y + 2, 3, 1, shadeCol(base, -0.3f))
        }
        for (i in 0 until 2) {
            val x = 3 + rn.nextInt(22)
            val y = 3 + rn.nextInt(26)
            b.line(x, y, x + 4, y + 1, dark)
            b.line(x + 4, y + 1, x + 6, y + 3, dark)
        }
        if (v == 3) {
            for (row in intArrayOf(9, 21)) {
                for (x in 0 until 32) {
                    if (((x / 2) and 1) == 0) b.set(x, row, dark)
                    b.set(x, row + 1, light)
                }
            }
        }
        return b
    }

    fun sand(v: Int): PixBuf {
        val b = PixBuf(32, 32)
        val tones = intArrayOf(h(0xFFE6D08F), h(0xFFE9D496), h(0xFFE2CB88), h(0xFFE7D091))
        val base = tones[v and 3]
        b.fill(base)
        val dark = shadeCol(base, -0.1f)
        val light = shadeCol(base, 0.18f)
        val rn = java.util.Random(900L + v * 5L)
        for (i in 0 until 90) b.set(rn.nextInt(32), rn.nextInt(32), if (rn.nextBoolean()) light else dark)
        for (r in 0 until 3) {
            val y0 = 6 + r * 10 + (v * 3) % 5
            val ph = r * 1.7f + v
            for (x in 0 until 32) {
                val y = y0 + Math.round(Math.sin((x * 0.4f + ph).toDouble()).toFloat() * 1.3f)
                b.set(x, y, h(0xFFF4E4B0))
                b.set(x, y + 1, shadeCol(base, -0.14f))
            }
        }
        if (v == 3) {
            b.ellipse(21f, 21f, 2.4f, 1.8f, h(0xFFF2D9E0), true)
            b.set(20, 21, h(0xFFD9A0B0))
            b.set(22, 21, h(0xFFD9A0B0))
        }
        return b
    }

    /** Pedras arredondadas de calçada (diagrama de Voronoi sem emendas). */
    fun stone(v: Int): PixBuf {
        val b = PixBuf(32, 32)
        val rn = java.util.Random(1000L + v * 17L)
        val n = 16
        val sxs = FloatArray(n)
        val sys = FloatArray(n)
        for (i in 0 until n) {
            sxs[i] = (i % 4) * 8f + 4f + (rn.nextFloat() - 0.5f) * 5f
            sys[i] = (i / 4) * 8f + 4f + (rn.nextFloat() - 0.5f) * 5f
        }
        val tone = IntArray(n) { shadeCol(h(0xFF9B9BA6), (rn.nextFloat() - 0.5f) * 0.22f) }
        val mortar = h(0xFF666673)
        for (y in 0 until 32) {
            for (x in 0 until 32) {
                var d1 = 999f
                var d2 = 999f
                var i1 = 0
                for (i in 0 until n) {
                    var dx = Math.abs(x + 0.5f - sxs[i])
                    if (dx > 16f) dx = 32f - dx
                    var dy = Math.abs(y + 0.5f - sys[i])
                    if (dy > 16f) dy = 32f - dy
                    val d = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                    if (d < d1) {
                        d2 = d1
                        d1 = d
                        i1 = i
                    } else if (d < d2) {
                        d2 = d
                    }
                }
                if (d2 - d1 < 1.3f) {
                    b.set(x, y, mortar)
                } else {
                    val rel = (x + 0.5f - sxs[i1]) + (y + 0.5f - sys[i1])
                    var col = tone[i1]
                    if (rel < -2.2f) col = shadeCol(col, 0.14f) else if (rel > 3.2f) col = shadeCol(col, -0.14f)
                    b.set(x, y, col)
                }
            }
        }
        for (i in 0 until 40) {
            val x = rn.nextInt(32)
            val y = rn.nextInt(32)
            val c = b.get(x, y)
            if (c != mortar) b.set(x, y, shadeCol(c, if (rn.nextBoolean()) 0.07f else -0.07f))
        }
        if (v >= 2) {
            for (i in 0 until 30) {
                val x = rn.nextInt(32)
                val y = rn.nextInt(32)
                if (b.get(x, y) == mortar) {
                    b.set(x, y - 1, h(0xFF5E8A3E))
                    b.set(x + 1, y, h(0xFF6FA04A))
                }
            }
        }
        return b
    }

    fun wood(v: Int): PixBuf {
        val b = PixBuf(32, 32)
        val rn = java.util.Random(1100L + v * 7L)
        val tones = intArrayOf(h(0xFFB07A44), h(0xFFA56F3B), h(0xFFB98350), h(0xFFA06A38))
        val line = h(0xFF6B4423)
        for (p in 0 until 4) {
            val y0 = p * 8
            val base = tones[(p + v) and 3]
            b.rect(0, y0, 32, 8, base)
            b.rect(0, y0, 32, 1, shadeCol(base, 0.18f))
            b.rect(0, y0 + 7, 32, 1, line)
            for (k in 0 until 4) {
                b.rect(rn.nextInt(24), y0 + 2 + rn.nextInt(4), 3 + rn.nextInt(5), 1, shadeCol(base, -0.13f))
            }
            val jx = 2 + rn.nextInt(28)
            b.rect(jx, y0, 1, 8, line)
            b.set(jx - 2, y0 + 2, h(0xFF3A2A1E))
            b.set(jx + 2, y0 + 5, h(0xFF3A2A1E))
            if (rn.nextInt(3) == 0) b.ellipse(4f + rn.nextInt(24), y0 + 4f, 1.8f, 1.1f, shadeCol(base, -0.3f), false)
        }
        return b
    }

    fun carpet(): PixBuf {
        val b = PixBuf(32, 32)
        val base = h(0xFF8E2A33)
        val dk = h(0xFF6E1F27)
        val lt = h(0xFFAA3640)
        val gold = h(0xFFD9A441)
        b.fill(base)
        val rn = java.util.Random(1200L)
        for (i in 0 until 90) b.set(rn.nextInt(32), rn.nextInt(32), if (rn.nextBoolean()) lt else dk)
        b.rect(0, 0, 32, 1, dk)
        b.rect(0, 31, 32, 1, dk)
        b.rect(0, 0, 1, 32, dk)
        b.rect(31, 0, 1, 32, dk)
        for (t in 0..8) {
            b.set(16 + t, 16 - (8 - t), gold)
            b.set(16 + t, 16 + (8 - t), gold)
            b.set(16 - t, 16 - (8 - t), gold)
            b.set(16 - t, 16 + (8 - t), gold)
        }
        for (t in 0..3) {
            b.set(16 + t, 16 - (3 - t), lt)
            b.set(16 + t, 16 + (3 - t), lt)
            b.set(16 - t, 16 - (3 - t), lt)
            b.set(16 - t, 16 + (3 - t), lt)
        }
        b.set(16, 16, gold)
        b.rect(3, 3, 2, 2, gold)
        b.rect(27, 3, 2, 2, gold)
        b.rect(3, 27, 2, 2, gold)
        b.rect(27, 27, 2, 2, gold)
        return b
    }

    /** Ponte de madeira (para andar da esquerda para a direita), com corrimões. */
    fun bridge(): PixBuf {
        val b = PixBuf(32, 32)
        b.fill(h(0xFF4A2F18))
        val tones = intArrayOf(h(0xFFB07A44), h(0xFFA06C3A))
        for (i in 0 until 8) {
            val x0 = i * 4
            val col = tones[i and 1]
            b.rect(x0, 5, 3, 22, col)
            b.rect(x0, 5, 1, 22, shadeCol(col, 0.16f))
            b.rect(x0 + 2, 5, 1, 22, shadeCol(col, -0.15f))
            b.set(x0 + 1, 8, h(0xFF3A2A1E))
            b.set(x0 + 1, 23, h(0xFF3A2A1E))
        }
        val rail = h(0xFF7A4A24)
        b.rect(0, 0, 32, 5, rail)
        b.rect(0, 0, 32, 1, h(0xFFA06C3A))
        b.rect(0, 4, 32, 1, h(0xFF3A2410))
        b.rect(0, 27, 32, 5, rail)
        b.rect(0, 27, 32, 1, h(0xFFA06C3A))
        b.rect(0, 31, 32, 1, h(0xFF3A2410))
        for (x in intArrayOf(1, 13, 25)) {
            b.rect(x, 0, 4, 6, h(0xFF5A3A1E))
            b.rect(x, 0, 4, 1, h(0xFF9A6A3A))
            b.rect(x, 26, 4, 6, h(0xFF5A3A1E))
            b.rect(x, 26, 4, 1, h(0xFF9A6A3A))
        }
        return b
    }

    /** Água animada: 4 quadros que se repetem sem emenda. */
    fun water(frame: Int, shallow: Boolean): PixBuf {
        val b = PixBuf(32, 32)
        val deep = if (shallow) h(0xFF4B9ADC) else h(0xFF2461C2)
        val mid = if (shallow) h(0xFF5BA9E8) else h(0xFF2D70D4)
        val dk = if (shallow) h(0xFF3F88C8) else h(0xFF1D53A8)
        val lt = if (shallow) h(0xFF8CCBF5) else h(0xFF5C9CF2)
        val hi = h(0xFFD6ECFF)
        val rn = java.util.Random(if (shallow) 1350L else 1300L)
        b.fill(deep)
        for (i in 0 until 110) b.set(rn.nextInt(32), rn.nextInt(32), if (rn.nextBoolean()) mid else dk)
        if (shallow) {
            for (i in 0 until 26) b.set(rn.nextInt(32), rn.nextInt(32), h(0xFFC9B88A))
        }
        val rw = java.util.Random(if (shallow) 1409L else 1400L)
        for (i in 0 until 7) {
            val wy = 3 + i * 4 + rw.nextInt(2)
            val wx = rw.nextInt(32)
            val len = 6 + rw.nextInt(5)
            val x0 = (wx + frame * 8) % 32
            for (k in 0 until len) {
                val x = (x0 + k) % 32
                val arc = if (k < len / 2) k / 3 else (len - k) / 3
                val yy = wy - arc
                b.set(x, yy, lt)
                if (k >= 2 && k < len - 2) b.set(x, yy - 1, hi)
                b.set(x, yy + 1, dk)
            }
        }
        val rs = java.util.Random(1500L + frame * 3L + (if (shallow) 7L else 0L))
        for (i in 0 until 3) {
            val x = rs.nextInt(32)
            val y = rs.nextInt(32)
            b.set(x, y, hi)
            if (i == 0) {
                b.set(x - 1, y, lt)
                b.set(x + 1, y, lt)
            }
        }
        return b
    }

    /**
     * Borda irregular de um terreno "mais forte" invadindo o vizinho.
     * kind: 0 terra, 1 areia, 2 pedra, 3 água rasa, 4 água funda. side: 0 cima, 1 direita, 2 baixo, 3 esquerda.
     */
    fun edge(kind: Int, side: Int, variant: Int): PixBuf {
        val animated = kind >= 3
        val tex = when (kind) {
            0 -> dirt(variant)
            1 -> sand(variant)
            2 -> stone(variant)
            3 -> water(variant, true)
            else -> water(variant, false)
        }
        val o = PixBuf(32, 32)
        val seed = kind * 7 + side * 3 + (if (animated) 0 else variant)
        val depth = IntArray(32)
        for (t in 0 until 32) {
            var d = 3 + (smoothNoise(t / 4.5f, seed * 3.1f, 61) * 5.5f).toInt()
            if (animated && ((t / 3 + variant) % 3 == 0)) d += 1
            depth[t] = d
        }
        for (y in 0 until 32) {
            for (x in 0 until 32) {
                var d = 0
                var t = 0
                when (side) {
                    0 -> { d = y; t = x }
                    1 -> { d = 31 - x; t = y }
                    2 -> { d = 31 - y; t = x }
                    else -> { d = x; t = y }
                }
                val dp = depth[t]
                if (d >= dp) continue
                var col = tex.get(x, y)
                if (d == dp - 1) {
                    col = if (animated) h(0xFFE8F6FF) else shadeCol(col, -0.28f)
                } else if (d == dp - 2 && animated) {
                    col = shadeCol(col, 0.14f)
                }
                o.set(x, y, col)
            }
        }
        return o
    }
}
