package com.aldoria.rpg

/** Árvores, arbustos, pedras e decorações (tudo em pixel art procedural). */
object ArtNature {

    fun mirror(src: PixBuf): PixBuf {
        val o = PixBuf(src.w, src.h)
        for (y in 0 until src.h) for (x in 0 until src.w) o.px[y * src.w + (src.w - 1 - x)] = src.px[y * src.w + x]
        return o
    }

    private fun thick(b: PixBuf, x0: Int, y0: Int, x1: Int, y1: Int, c: Int) {
        b.line(x0, y0, x1, y1, c)
        b.line(x0 + 1, y0, x1 + 1, y1, c)
    }

    // ------------------------------------------------------------------ árvores

    fun oak(v: Int): PixBuf {
        val b = PixBuf(32, 48)
        val rn = java.util.Random(2000L + v * 31L)
        val leaf = intArrayOf(h(0xFF3C8A34), h(0xFF4A9A3A), h(0xFF357F2E))[v % 3]
        val bark = h(0xFF6B4423)
        b.poly(
            floatArrayOf(14.3f, 18.7f, 19.6f, 23f, 9f, 12.4f),
            floatArrayOf(29f, 29f, 41f, 47f, 47f, 41f), bark
        )
        for (y in 30..44) {
            b.set(14, y, shadeCol(bark, 0.15f))
            b.set(15, y, shadeCol(bark, -0.25f))
            if (y % 3 != 0) b.set(17, y, shadeCol(bark, -0.2f))
            b.set(18, y, shadeCol(bark, -0.12f))
        }
        b.rect(10, 45, 3, 2, shadeCol(bark, -0.2f))
        b.rect(19, 45, 3, 2, shadeCol(bark, -0.3f))
        b.ellipse(16.5f, 38f, 1.1f, 1.6f, h(0xFF2A1A0E), false)
        val lobes = arrayOf(
            floatArrayOf(9.5f, 25f, 7.5f), floatArrayOf(22.5f, 25f, 7.5f), floatArrayOf(16f, 27f, 8.5f),
            floatArrayOf(10.5f, 16f, 8f), floatArrayOf(21.5f, 16f, 8f), floatArrayOf(16f, 10.5f, 8.5f)
        )
        for (l in lobes) {
            val j = (rn.nextFloat() - 0.5f) * 1.6f
            b.ellipse(l[0] + j, l[1], l[2], l[2] * 0.92f, leaf, true)
        }
        for (i in 0 until 160) {
            val x = 2 + rn.nextInt(28)
            val y = 2 + rn.nextInt(34)
            val c = b.get(x, y)
            if (c != 0) b.set(x, y, shadeCol(c, if (rn.nextBoolean()) 0.12f else -0.12f))
        }
        for (l in lobes) {
            val hx = (l[0] - l[2] * 0.4f).toInt()
            val hy = (l[1] - l[2] * 0.5f).toInt()
            b.rect(hx, hy, 3, 2, shadeCol(leaf, 0.35f))
            b.set(hx + 3, hy + 1, shadeCol(leaf, 0.25f))
        }
        if (v == 2) {
            for (i in 0 until 26) {
                val x = 4 + rn.nextInt(24)
                val y = 4 + rn.nextInt(30)
                if (b.get(x, y) != 0) {
                    b.set(x, y, h(0xFFFF9EC8))
                    b.set(x + 1, y, h(0xFFFFD0E4))
                }
            }
        }
        b.outline(h(0xFF1F3A1E))
        b.groundShadow(16f, 46.5f, 11f, 2.6f)
        return b
    }

    fun pine(v: Int): PixBuf {
        val b = PixBuf(32, 48)
        val dark = if (v == 0) h(0xFF2E6B3A) else h(0xFF2F6A55)
        val bark = h(0xFF5A3A1E)
        b.rect(15, 40, 3, 7, bark)
        b.rect(15, 40, 1, 7, shadeCol(bark, 0.2f))
        b.rect(17, 40, 1, 7, shadeCol(bark, -0.25f))
        val rn = java.util.Random(2500L + v)
        for (t in 3 downTo 0) {
            val apex = 2f + t * 9f
            val base = apex + 15f
            val hw = 5.5f + t * 2.4f
            val ya = apex.toInt()
            val yb = Math.min(46, base.toInt())
            for (y in ya..yb) {
                val half = hw * (y + 0.5f - apex) / (base - apex)
                if (half < 0f) continue
                val xl = 16f - half
                val xr = 16f + half
                var x = Math.ceil((xl - 0.5f).toDouble()).toInt()
                while (x + 0.5f <= xr) {
                    val fx = x + 0.5f
                    var col = dark
                    if (fx < 16f - half * 0.15f) col = shadeCol(dark, 0.16f)
                    else if (fx > 16f + half * 0.4f) col = shadeCol(dark, -0.2f)
                    if (y >= yb - 1) col = shadeCol(col, -0.15f)
                    b.set(x, y, col)
                    x++
                }
            }
            for (k in 0 until 7) {
                val y = ya + 3 + rn.nextInt(11)
                val half = hw * (y + 0.5f - apex) / (base - apex)
                val x = (16f - half + 1f + rn.nextFloat() * (half * 2f - 4f)).toInt()
                b.rect(x, y, 3, 1, shadeCol(dark, 0.3f))
                b.rect(x + 1, y + 1, 3, 1, shadeCol(dark, -0.28f))
            }
        }
        b.outline(h(0xFF16301F))
        b.groundShadow(16f, 46.5f, 10f, 2.4f)
        return b
    }

    fun dead(v: Int): PixBuf {
        val b = PixBuf(32, 48)
        val col = h(0xFF6B5B4B)
        val dk = shadeCol(col, -0.3f)
        val lt = shadeCol(col, 0.2f)
        b.poly(
            floatArrayOf(14f, 18.5f, 19.5f, 22f, 10f, 13f),
            floatArrayOf(18f, 18f, 42f, 47f, 47f, 42f), col
        )
        for (y in 19..44) {
            b.set(15, y, lt)
            b.set(17, y, dk)
            if (y % 4 == 0) b.set(16, y, dk)
        }
        thick(b, 16, 32, 9, 24, col); b.line(9, 24, 5, 15, col); b.line(8, 22, 4, 21, col)
        thick(b, 17, 27, 24, 20, col); b.line(24, 20, 27, 10, col); b.line(25, 18, 29, 17, col)
        b.line(16, 21, 14, 11, col); b.line(17, 20, 21, 7, col); b.line(15, 15, 11, 10, col)
        b.line(9, 24, 10, 25, dk)
        b.line(24, 20, 25, 21, dk)
        val o = if (v == 1) mirror(b) else b
        o.outline(h(0xFF2A2018))
        o.groundShadow(16f, 46.5f, 9f, 2.2f)
        return o
    }

    // ------------------------------------------------------------------ arbustos

    fun bush(v: Int, flowers: Boolean): PixBuf {
        val b = PixBuf(32, 32)
        val rn = java.util.Random(2700L + v * 9L + (if (flowers) 100L else 0L))
        val tones = intArrayOf(h(0xFF3F8F38), h(0xFF4A9A3A), h(0xFF37822F), h(0xFF449438))
        val leaf = tones[v and 3]
        b.ellipse(9.5f, 23f, 6.5f, 5.5f, leaf, true)
        b.ellipse(22.5f, 23f, 6.5f, 5.5f, leaf, true)
        b.ellipse(16f, 20f, 8.5f, 7f, leaf, true)
        for (i in 0 until 90) {
            val x = 3 + rn.nextInt(26)
            val y = 10 + rn.nextInt(18)
            val c = b.get(x, y)
            if (c != 0) b.set(x, y, shadeCol(c, if (rn.nextBoolean()) 0.12f else -0.12f))
        }
        if (flowers) {
            val fc = intArrayOf(h(0xFFFF7AB6), h(0xFFFFFFFF), h(0xFFB07AFF), h(0xFFFFD84A))[v and 3]
            for (i in 0 until 14) {
                val x = 5 + rn.nextInt(21)
                val y = 12 + rn.nextInt(14)
                if (b.get(x, y) != 0) {
                    b.rect(x, y, 2, 2, fc)
                    b.set(x, y, shadeCol(fc, 0.3f))
                    b.set(x + 1, y + 1, h(0xFFF5D547))
                }
            }
        } else if (v % 2 == 1) {
            for (i in 0 until 9) {
                val x = 6 + rn.nextInt(20)
                val y = 14 + rn.nextInt(12)
                if (b.get(x, y) != 0) {
                    b.rect(x, y, 2, 2, h(0xFFD63A3A))
                    b.set(x, y, h(0xFFFF8A8A))
                }
            }
        }
        b.outline(h(0xFF1F3A1E))
        b.groundShadow(16f, 29f, 12f, 2.4f)
        return b
    }

    // ------------------------------------------------------------------ pedras, troncos

    fun rock(v: Int): PixBuf {
        val b = PixBuf(32, 32)
        val rn = java.util.Random(2900L + v * 13L)
        val base = h(0xFF8E8E98)
        b.ellipse(16f, 24f, 11f, 6.5f, base, true)
        b.ellipse(10.5f + v, 22f, 6f, 5f, shadeCol(base, 0.05f), true)
        b.ellipse(21.5f - v, 23f, 7f, 5.5f, shadeCol(base, -0.04f), true)
        b.rect(8, 19, 4, 1, shadeCol(base, 0.4f))
        b.rect(7, 20, 2, 1, shadeCol(base, 0.35f))
        b.line(15, 20, 17, 25, shadeCol(base, -0.4f))
        b.line(17, 25, 16, 28, shadeCol(base, -0.4f))
        b.line(23, 21, 24, 24, shadeCol(base, -0.4f))
        for (i in 0 until 30) {
            val x = 5 + rn.nextInt(22)
            val y = 17 + rn.nextInt(12)
            val c = b.get(x, y)
            if (c != 0) b.set(x, y, shadeCol(c, if (rn.nextBoolean()) 0.1f else -0.1f))
        }
        if (v >= 1) {
            for (i in 0 until 12) {
                val x = 7 + rn.nextInt(18)
                val y = 17 + rn.nextInt(5)
                if (b.get(x, y) != 0) {
                    b.set(x, y, h(0xFF5E8A3E))
                    b.set(x + 1, y, h(0xFF6FA04A))
                }
            }
        }
        b.outline(h(0xFF30303A))
        b.groundShadow(16f, 29f, 12.5f, 2.3f)
        return b
    }

    fun boulder(v: Int): PixBuf {
        val b = PixBuf(32, 32)
        val rn = java.util.Random(3100L + v * 7L)
        val base = h(0xFF858590)
        b.ellipse(16f, 20f, 13f, 10f, base, true)
        b.ellipse(8.5f, 24f, 7f, 5.5f, shadeCol(base, -0.03f), true)
        b.ellipse(23.5f, 23f, 7f, 6f, shadeCol(base, 0.03f), true)
        b.line(10, 14, 16, 18, shadeCol(base, -0.4f))
        b.line(16, 18, 15, 26, shadeCol(base, -0.4f))
        b.line(20, 12, 24, 18, shadeCol(base, -0.4f))
        b.rect(9, 12, 5, 1, shadeCol(base, 0.4f))
        b.rect(8, 13, 3, 1, shadeCol(base, 0.35f))
        for (i in 0 until 50) {
            val x = 4 + rn.nextInt(24)
            val y = 10 + rn.nextInt(18)
            val c = b.get(x, y)
            if (c != 0) b.set(x, y, shadeCol(c, if (rn.nextBoolean()) 0.1f else -0.1f))
        }
        for (i in 0 until 22) {
            val x = 6 + rn.nextInt(20)
            val y = 10 + rn.nextInt(7)
            if (b.get(x, y) != 0) {
                b.set(x, y, h(0xFF5E8A3E))
                b.set(x + 1, y, h(0xFF6FA04A))
            }
        }
        b.outline(h(0xFF30303A))
        b.groundShadow(16f, 29.5f, 14f, 2.2f)
        return b
    }

    fun log(v: Int): PixBuf {
        val b = PixBuf(32, 32)
        val rn = java.util.Random(3300L + v)
        val bark = h(0xFF7A5230)
        for (x in 3..25) {
            for (y in 19..27) {
                val col = when (y) {
                    19, 20 -> shadeCol(bark, 0.2f)
                    21, 22, 23, 24 -> bark
                    25 -> shadeCol(bark, -0.2f)
                    else -> shadeCol(bark, -0.35f)
                }
                b.set(x, y, col)
            }
        }
        for (i in 0 until 14) {
            val x = 4 + rn.nextInt(20)
            b.rect(x, 20 + rn.nextInt(4), 1, 3, shadeCol(bark, -0.3f))
        }
        b.ellipse(25.5f, 23f, 3.4f, 4.6f, h(0xFFD9B27A), true)
        b.ring(25.5f, 23f, 2.4f, 3.4f, h(0xFFA8804C))
        b.set(25, 23, h(0xFF8A6030))
        b.rect(10, 16, 4, 4, bark)
        b.rect(10, 16, 4, 1, shadeCol(bark, 0.25f))
        b.rect(11, 15, 2, 1, h(0xFFD9B27A))
        for (i in 0 until 8) {
            val x = 5 + rn.nextInt(14)
            b.set(x, 19, h(0xFF5E8A3E))
            b.set(x + 1, 18, h(0xFF6FA04A))
        }
        val o = if (v == 1) mirror(b) else b
        o.outline(h(0xFF2A1A0E))
        o.groundShadow(15f, 28.5f, 13f, 2f)
        return o
    }

    fun stump(v: Int): PixBuf {
        val b = PixBuf(32, 32)
        val bark = h(0xFF7A5230)
        for (x in 8..24) {
            for (y in 21..27) {
                val col = if (x < 12) shadeCol(bark, 0.15f) else if (x > 20) shadeCol(bark, -0.3f) else if (x > 16) shadeCol(bark, -0.12f) else bark
                b.set(x, y, col)
            }
        }
        b.ellipse(16f, 27f, 8.5f, 2.6f, shadeCol(bark, -0.2f), false)
        b.rect(6, 26, 4, 2, shadeCol(bark, -0.1f))
        b.rect(22, 26, 4, 2, shadeCol(bark, -0.3f))
        b.ellipse(16f, 21f, 8.6f, 4.8f, h(0xFFD2A870), true)
        b.ring(16f, 21f, 6f, 3.3f, h(0xFFA8804C))
        b.ring(16f, 21f, 3.2f, 1.9f, h(0xFF9A7040))
        b.set(16, 21, h(0xFF7A5028))
        for (y in 22..26) if (y % 2 == 0) b.set(14, y, shadeCol(bark, -0.3f))
        if (v == 1) {
            b.ellipse(22.5f, 26f, 2.2f, 1.6f, h(0xFFD63A3A), true)
            b.rect(22, 27, 1, 2, h(0xFFEADCC0))
            b.set(22, 25, h(0xFFFFFFFF))
        }
        b.outline(h(0xFF2A1A0E))
        b.groundShadow(16f, 29.5f, 11f, 2f)
        return b
    }

    // ------------------------------------------------------------------ decorações pisáveis

    fun flowers(petal: Int, center: Int, variant: Int): PixBuf {
        val b = PixBuf(32, 32)
        val rn = java.util.Random(3500L + variant * 17L + (petal and 0xFF))
        val stem = h(0xFF2E7D32)
        val leaf = h(0xFF4FA14C)
        for (i in 0 until 5) {
            val x = 5 + rn.nextInt(22)
            val y = 9 + rn.nextInt(16)
            b.line(x, y, x, y + 5, stem)
            b.set(x - 1, y + 3, leaf)
            b.set(x + 1, y + 4, leaf)
            b.set(x, y - 1, petal)
            b.set(x - 1, y, petal)
            b.set(x + 1, y, petal)
            b.set(x, y + 1, petal)
            b.set(x - 1, y - 1, shadeCol(petal, 0.3f))
            b.set(x, y, center)
        }
        return b
    }

    fun tallGrass(variant: Int): PixBuf {
        val b = PixBuf(32, 32)
        val rn = java.util.Random(3700L + variant * 5L)
        for (k in 0 until 8) {
            val bx = 5 + k * 3 + rn.nextInt(2)
            val ht = 9 + rn.nextInt(8)
            val lean = (rn.nextFloat() - 0.5f) * 7f
            for (t in 0..ht) {
                val f = t.toFloat() / ht
                val x = bx + Math.round(lean * f * f)
                val y = 30 - t
                val col = if (f < 0.35f) h(0xFF2E7A2E) else if (f < 0.7f) h(0xFF4FA14C) else h(0xFF7CC85A)
                b.set(x, y, col)
                if (t < ht / 2) b.set(x + 1, y, shadeCol(col, -0.2f))
            }
        }
        return b
    }

    fun mushroom(brown: Boolean, variant: Int): PixBuf {
        val b = PixBuf(32, 32)
        val cap = if (brown) h(0xFF9A6A3A) else h(0xFFD63A3A)
        val dots = if (brown) h(0xFFD9B27A) else h(0xFFFFFFFF)
        val cx = 13f + variant * 2f
        b.rect(cx.toInt() - 1, 22, 3, 6, h(0xFFEADCC0))
        b.rect(cx.toInt() + 1, 22, 1, 6, h(0xFFC8B896))
        b.ellipse(cx, 21f, 5.5f, 3.8f, cap, true)
        b.rect(cx.toInt() - 5, 22, 11, 1, shadeCol(cap, -0.3f))
        b.set(cx.toInt() - 2, 19, dots)
        b.set(cx.toInt() + 2, 20, dots)
        b.set(cx.toInt(), 18, dots)
        val sx = (cx + 8f).toInt()
        b.rect(sx, 25, 2, 3, h(0xFFEADCC0))
        b.ellipse(sx + 0.5f, 25f, 3f, 2.2f, cap, true)
        b.set(sx, 24, dots)
        b.outline(h(0xFF3A2410))
        return b
    }

    fun pebbles(variant: Int): PixBuf {
        val b = PixBuf(32, 32)
        val rn = java.util.Random(3900L + variant * 3L)
        for (i in 0 until 3) {
            val x = 5 + rn.nextInt(20)
            val y = 8 + rn.nextInt(18)
            b.ellipse(x.toFloat(), y.toFloat(), 2.6f, 1.8f, h(0xFF9A9AA4), true)
            b.set(x - 1, y - 1, h(0xFFD6D6DE))
            b.rect(x - 1, y + 2, 4, 1, 0x44000000)
        }
        return b
    }

    fun lily(flower: Boolean, variant: Int): PixBuf {
        val b = PixBuf(32, 32)
        val cx = 14f + variant * 3f
        b.ellipse(cx, 20f, 7f, 4.2f, h(0xFF3F9A4A), true)
        for (x in cx.toInt()..cx.toInt() + 8) {
            val dy = ((x - cx.toInt()) * 0.25f).toInt()
            for (y in 20 - dy..20 + dy) b.set(x, y, 0)
        }
        b.line(cx.toInt() - 4, 19, cx.toInt() + 2, 21, h(0xFF2E7A38))
        if (flower) {
            val fx = cx.toInt() - 2
            val pink = h(0xFFFF8FC0)
            b.set(fx, 17, pink); b.set(fx - 1, 18, pink); b.set(fx + 1, 18, pink)
            b.set(fx - 2, 19, pink); b.set(fx + 2, 19, pink); b.set(fx, 19, h(0xFFFFE9F2))
            b.set(fx, 18, h(0xFFFFE066))
            b.set(fx - 1, 19, shadeCol(pink, 0.2f)); b.set(fx + 1, 19, shadeCol(pink, 0.2f))
        }
        b.outline(h(0xFF1F5A2A))
        return b
    }

    fun reed(variant: Int): PixBuf {
        val b = PixBuf(32, 32)
        val xs = intArrayOf(9, 14, 19, 24)
        val hs = intArrayOf(19, 25, 21, 17)
        for (i in 0 until 4) {
            val x = xs[(i + variant) % 4]
            val ht = hs[i]
            for (t in 0..ht) {
                b.set(x, 30 - t, if (t > ht - 3) h(0xFF7CC85A) else h(0xFF4F8A3E))
                if (t < 8) b.set(x + 1, 30 - t, h(0xFF356A2E))
            }
            if (i % 2 == 0) {
                b.rect(x - 1, 30 - ht - 4, 3, 5, h(0xFF7A4A24))
                b.set(x - 1, 30 - ht - 4, h(0xFFA06A3A))
                b.set(x, 30 - ht - 5, h(0xFF4F8A3E))
            }
        }
        b.line(11, 28, 5, 20, h(0xFF4F8A3E))
        b.line(21, 28, 28, 21, h(0xFF4F8A3E))
        b.line(12, 29, 8, 24, h(0xFF7CC85A))
        return b
    }

    fun fern(variant: Int): PixBuf {
        val b = PixBuf(32, 32)
        val rn = java.util.Random(4100L + variant)
        val dk = h(0xFF357A38)
        val lt = h(0xFF5CA84A)
        for (k in 0 until 6) {
            val ang = -2.9f + k * 0.55f + (rn.nextFloat() - 0.5f) * 0.2f
            val len = 11f + rn.nextInt(4)
            var px = 16
            var py = 28
            for (t in 1..len.toInt()) {
                val f = t / len
                val x = 16 + Math.round(Math.cos(ang.toDouble()).toFloat() * t)
                val y = 28 + Math.round(Math.sin(ang.toDouble()).toFloat() * t * 0.8f) + Math.round(f * f * 4f)
                b.set(x, y, dk)
                if (t % 2 == 0 && t > 2) {
                    b.set(x, y - 1, lt)
                    b.set(x, y + 1, dk)
                    b.set(x + (if (x > 16) -1 else 1), y, lt)
                }
                px = x
                py = y
            }
            b.set(px, py, lt)
        }
        return b
    }
}
