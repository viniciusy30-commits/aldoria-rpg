package com.aldoria.rpg

/** Casas, praça e mobiliário da cidade. */
object ArtBuild {

    private val PLASTER = h(0xFFE9DEC4)
    private val BEAM = h(0xFF5A3A22)

    private fun roof(b: PixBuf) {
        val tones = intArrayOf(h(0xFFB5482F), h(0xFFC2523A), h(0xFFA93F29), h(0xFFBC4B32))
        for (r in 0 until 3) {
            val y = r * 3
            val off = if (r % 2 == 0) 0 else 4
            var sx = off - 8
            var idx = r
            while (sx < 32) {
                val base = tones[idx and 3]
                b.rect(sx, y, 8, 3, base)
                b.rect(sx, y, 8, 1, shadeCol(base, 0.18f))
                b.rect(sx, y + 2, 8, 1, shadeCol(base, -0.22f))
                b.rect(sx, y, 1, 3, shadeCol(base, -0.3f))
                b.set(sx + 3, y + 1, shadeCol(base, 0.1f))
                sx += 8
                idx++
            }
        }
        b.rect(0, 9, 32, 1, h(0xFF7A2F1E))
        b.rect(0, 10, 32, 1, h(0xFFC9B79A))
    }

    private fun foundation(b: PixBuf, y0: Int) {
        val st = h(0xFF8E8E98)
        val mortar = h(0xFF5A5A66)
        b.rect(0, y0, 32, 32 - y0, st)
        var row = 0
        var y = y0
        while (y < 32) {
            val hgt = if (y + 3 <= 32) 3 else 32 - y
            b.rect(0, y, 32, 1, mortar)
            val off = if (row % 2 == 0) 0 else 5
            var x = off
            while (x < 32) {
                b.rect(x, y, 1, hgt, mortar)
                b.rect(x + 1, y + 1, 4, 1, shadeCol(st, 0.14f))
                x += 10
            }
            y += 3
            row++
        }
        b.rect(0, 31, 32, 1, h(0xFF3A3A46))
    }

    private fun windowOn(b: PixBuf, wy: Int) {
        val frame = h(0xFF4A2F18)
        val shutter = h(0xFF3F7A66)
        b.rect(4, wy, 3, 11, shutter)
        b.rect(25, wy, 3, 11, shutter)
        for (k in 0 until 4) {
            b.rect(4, wy + 1 + k * 3, 3, 1, shadeCol(shutter, -0.25f))
            b.rect(25, wy + 1 + k * 3, 3, 1, shadeCol(shutter, -0.25f))
        }
        b.rect(8, wy - 1, 16, 12, frame)
        b.rect(10, wy + 1, 12, 8, h(0xFF8EC8E8))
        b.rect(10, wy + 6, 12, 3, h(0xFF6AA8CC))
        for (k in 0 until 4) {
            b.set(11 + k * 2, wy + 7 - k, h(0xFFD6F0FC))
            b.set(12 + k * 2, wy + 7 - k, h(0xFFD6F0FC))
        }
        b.rect(15, wy + 1, 2, 8, h(0xFFE9DEC4))
        b.rect(10, wy + 4, 12, 2, h(0xFFE9DEC4))
        b.rect(7, wy + 10, 18, 2, h(0xFF7A5230))
        b.rect(7, wy + 10, 18, 1, h(0xFFA67A4A))
        // floreira
        b.rect(8, wy + 12, 16, 3, h(0xFF8A5A2B))
        b.rect(8, wy + 12, 16, 1, h(0xFFB07A44))
        val fc = intArrayOf(h(0xFFE8505B), h(0xFFFF8FB7), h(0xFFF5D547))
        for (k in 0 until 8) {
            val x = 9 + k * 2
            b.set(x, wy + 11, h(0xFF3F9A4A))
            b.set(x, wy + 10, fc[k % 3])
        }
    }

    private fun torchOn(b: PixBuf, p0: Int, frame: Int) {
        val y = p0 + 14
        b.rect(15, y, 2, 5, h(0xFF2A2A30))
        b.rect(13, y + 4, 6, 2, h(0xFF2A2A30))
        b.rect(15, y - 6, 2, 7, h(0xFF7A4A24))
        b.rect(14, y - 7, 4, 2, h(0xFF3A3A44))
        b.set(15, y - 5, h(0xFFA06A3A))
        // fuligem
        for (k in 0 until 5) b.set(14 + (k % 3) * 2, y - 14 - k, h(0xFFC9B79A))
        val fy = (y - 11).toFloat()
        if (frame == 0) {
            b.ellipse(16f, fy, 2.4f, 3.4f, h(0xFFFF8A2D), false)
            b.ellipse(16f, fy + 0.6f, 1.2f, 2f, h(0xFFFFE066), false)
            b.set(16, (fy - 4f).toInt(), h(0xFFFFB04D))
        } else {
            b.ellipse(16.4f, fy - 0.3f, 2.1f, 3.8f, h(0xFFFF8A2D), false)
            b.ellipse(16.2f, fy + 0.4f, 1.1f, 2.2f, h(0xFFFFE066), false)
            b.set(17, (fy - 5f).toInt(), h(0xFFFFB04D))
            b.set(15, (fy - 3f).toInt(), h(0xFFFFB04D))
        }
    }

    /** kind: 0 liso, 1 janela, 2 tocha. hasN/hasS: tem parede em cima/embaixo. */
    fun wall(kind: Int, hasN: Boolean, hasS: Boolean, frame: Int): PixBuf {
        val b = PixBuf(32, 32)
        val rn = java.util.Random(4200L)
        var p0 = 0
        if (!hasN) {
            roof(b)
            p0 = 11
        }
        val bottom = if (hasS) 32 else 26
        b.rect(0, p0, 32, bottom - p0, PLASTER)
        for (i in 0 until 80) {
            val x = 3 + rn.nextInt(26)
            val y = p0 + rn.nextInt(bottom - p0)
            b.set(x, y, shadeCol(PLASTER, if (rn.nextBoolean()) 0.06f else -0.08f))
        }
        // vigas
        b.rect(0, p0, 3, bottom - p0, BEAM)
        b.rect(29, p0, 3, bottom - p0, BEAM)
        b.rect(0, p0, 1, bottom - p0, shadeCol(BEAM, 0.2f))
        b.rect(29, p0, 1, bottom - p0, shadeCol(BEAM, 0.2f))
        if (!hasN) {
            b.rect(0, p0, 32, 2, BEAM)
            b.rect(3, p0 + 2, 26, 1, shadeCol(PLASTER, -0.2f))
        }
        if (!hasS) {
            b.rect(0, bottom - 2, 32, 2, BEAM)
            b.rect(0, bottom - 2, 32, 1, shadeCol(BEAM, 0.2f))
        }
        if (kind == 0) {
            val ya = p0 + (if (!hasN) 3 else 0)
            val yb = bottom - (if (!hasS) 4 else 1)
            b.line(4, ya, 27, yb, BEAM)
            b.line(5, ya, 28, yb, BEAM)
        }
        if (kind == 1) windowOn(b, p0 + 3)
        if (kind == 2) torchOn(b, p0, frame)
        if (!hasS) foundation(b, 26)
        return b
    }

    fun door(hasN: Boolean): PixBuf {
        val b = wall(0, hasN, false, 0)
        val p0 = if (hasN) 0 else 11
        val top = p0 + 3
        val stone = h(0xFF9B9BA6)
        b.rect(6, top, 20, 32 - top, stone)
        b.rect(6, top, 20, 1, shadeCol(stone, 0.2f))
        b.rect(8, top + 2, 16, 30 - top, h(0xFF7A4B25))
        val dk = h(0xFF5E3A1C)
        for (k in 0 until 4) {
            b.rect(8 + k * 4 + 3, top + 2, 1, 30 - top, dk)
            b.rect(8 + k * 4, top + 2, 1, 30 - top, shadeCol(h(0xFF7A4B25), 0.12f))
        }
        b.rect(8, top + 2, 16, 1, h(0xFF3A2410))
        val iron = h(0xFF2A2A30)
        b.rect(8, top + 6, 16, 2, iron)
        b.rect(8, top + 15, 16, 2, iron)
        b.set(9, top + 6, h(0xFF6A6A74))
        b.set(22, top + 6, h(0xFF6A6A74))
        b.set(9, top + 15, h(0xFF6A6A74))
        b.set(22, top + 15, h(0xFF6A6A74))
        b.ellipse(20.5f, (top + 12).toFloat(), 1.7f, 1.7f, h(0xFFF2C94C), true)
        b.rect(4, 30, 24, 2, h(0xFFB8B8C0))
        b.rect(4, 30, 24, 1, h(0xFFD6D6DE))
        return b
    }

    // ------------------------------------------------------------------ praça

    fun fountain(frame: Int): PixBuf {
        val b = PixBuf(32, 40)
        val stone = h(0xFFC4C4CC)
        b.ellipse(16f, 34f, 15f, 6.4f, shadeCol(stone, -0.28f), false)
        b.rect(1, 30, 30, 5, shadeCol(stone, -0.28f))
        b.ellipse(16f, 31f, 15f, 7.6f, stone, true)
        b.ellipse(16f, 31.5f, 12.6f, 6f, h(0xFF3E8EE0), false)
        b.ellipse(16f, 32.2f, 12f, 5.2f, h(0xFF2F78C8), false)
        for (i in 0 until 5) {
            val ph = ((frame * 2 + i * 7) % 20) / 20f
            val rx = 3f + ph * 8f
            b.ring(16f, 32f, rx, rx * 0.42f, if (ph < 0.5f) h(0xFF8CC4F5) else h(0xFF5BA0E8))
        }
        b.rect(14, 15, 4, 17, h(0xFF9A9AA4))
        b.rect(14, 15, 1, 17, h(0xFFC4C4CC))
        b.rect(17, 15, 1, 17, h(0xFF6A6A76))
        b.ellipse(16f, 19f, 7.5f, 3.2f, stone, true)
        b.ellipse(16f, 18.6f, 5.8f, 2.2f, h(0xFF3E8EE0), false)
        b.ellipse(16f, 13f, 2.4f, 2.4f, stone, true)
        b.rect(15, 9, 2, 4, h(0xFF9A9AA4))
        for (k in 0 until 8) {
            val ph = (frame / 4f + k / 8f) % 1f
            val dir = if (k % 2 == 0) 1f else -1f
            val x = 16f + dir * ph * 9f
            val y = 9f - 3.5f * ph + 13f * ph * ph
            b.rect(x.toInt(), y.toInt(), 1, 2, if (k % 3 == 0) h(0xFFFFFFFF) else h(0xFFBFE0FF))
        }
        for (y in 20..29) {
            if (((y + frame) % 3) != 0) {
                b.set(10, y, h(0xFFA6D0FF))
                b.set(22, y, h(0xFFA6D0FF))
            }
        }
        b.outline(h(0xFF2A3548))
        return b
    }

    fun statue(): PixBuf {
        val b = PixBuf(32, 48)
        val stone = h(0xFFB6B6C0)
        val ped = h(0xFF8E8E98)
        b.poly(floatArrayOf(8f, 24f, 22.5f, 9.5f), floatArrayOf(47f, 47f, 36f, 36f), ped)
        b.rect(8, 44, 16, 3, shadeCol(ped, -0.2f))
        b.rect(9, 35, 14, 3, shadeCol(ped, 0.2f))
        b.rect(13, 40, 6, 3, h(0xFFC49A2A))
        b.rect(13, 40, 6, 1, h(0xFFF2C94C))
        // figura: manto
        b.poly(floatArrayOf(10f, 22f, 20f, 12f), floatArrayOf(35f, 35f, 21f, 21f), stone)
        b.ellipse(16f, 18f, 3.2f, 3.4f, stone, true)
        b.poly(floatArrayOf(12.5f, 19.5f, 17.5f, 20.5f, 15.5f), floatArrayOf(16f, 16f, 9f, 3.5f, 3f), stone)
        b.poly(floatArrayOf(13f, 19f, 16f), floatArrayOf(19f, 19f, 26f), shadeCol(stone, 0.25f))
        b.rect(23, 11, 2, 25, h(0xFF8A7A66))
        b.ellipse(24f, 9f, 2.4f, 2.4f, h(0xFF9BD8FF), true)
        b.set(23, 8, h(0xFFFFFFFF))
        for (y in 4..35) for (x in 9..22) {
            val c = b.get(x, y)
            if (c == stone) b.set(x, y, if (x < 14) shadeCol(stone, 0.12f) else if (x > 18) shadeCol(stone, -0.18f) else stone)
        }
        for (k in 0 until 6) b.set(10 + k * 2, 46 - (k % 2), h(0xFF5E8A3E))
        b.outline(h(0xFF3A3A48))
        b.groundShadow(16f, 46.5f, 11f, 2.4f)
        return b
    }

    fun lamp(): PixBuf {
        val b = PixBuf(32, 48)
        val iron = h(0xFF3A3A44)
        b.ellipse(16f, 45f, 5f, 2.4f, iron, true)
        b.rect(15, 20, 2, 26, iron)
        b.rect(15, 20, 1, 26, h(0xFF6A6A76))
        b.rect(13, 40, 6, 2, iron)
        b.line(16, 24, 21, 20, iron)
        b.rect(10, 5, 12, 13, iron)
        b.rect(12, 7, 8, 9, h(0xFFFFE08A))
        b.rect(14, 9, 4, 5, h(0xFFFFF6C8))
        b.poly(floatArrayOf(9f, 23f, 16f), floatArrayOf(5f, 5f, 0.5f), iron)
        b.rect(10, 17, 12, 2, iron)
        b.set(16, 0, h(0xFF8A8A96))
        b.outline(h(0xFF14141A))
        b.groundShadow(16f, 46.5f, 8f, 2f)
        return b
    }

    fun bench(): PixBuf {
        val b = PixBuf(32, 32)
        val wood = h(0xFFB07A44)
        val dk = h(0xFF6B4423)
        b.rect(5, 22, 3, 7, dk)
        b.rect(24, 22, 3, 7, dk)
        b.rect(4, 11, 24, 3, shadeCol(wood, -0.1f))
        b.rect(4, 15, 24, 3, shadeCol(wood, -0.18f))
        b.rect(4, 11, 24, 1, shadeCol(wood, 0.2f))
        for (k in 0 until 3) {
            val y = 19 + k * 2
            b.rect(3, y, 26, 2, wood)
            b.rect(3, y, 26, 1, shadeCol(wood, 0.2f))
        }
        b.rect(3, 24, 26, 1, shadeCol(wood, -0.3f))
        b.rect(3, 12, 2, 12, dk)
        b.rect(27, 12, 2, 12, dk)
        b.outline(h(0xFF2A1A0E))
        b.groundShadow(16f, 30f, 13f, 1.8f)
        return b
    }

    fun planter(v: Int): PixBuf {
        val b = PixBuf(32, 32)
        val rn = java.util.Random(4400L + v)
        val wood = h(0xFF8A5A2B)
        b.rect(6, 20, 20, 9, wood)
        b.rect(6, 20, 20, 1, shadeCol(wood, 0.25f))
        b.rect(6, 28, 20, 1, shadeCol(wood, -0.35f))
        for (k in 0 until 4) b.rect(6 + k * 6, 21, 1, 7, shadeCol(wood, -0.25f))
        b.rect(7, 18, 18, 3, h(0xFF4A3220))
        val fc = intArrayOf(h(0xFFE8505B), h(0xFFF5D547), h(0xFFB07AFF))[v % 3]
        for (k in 0 until 11) {
            val x = 8 + rn.nextInt(16)
            val y = 10 + rn.nextInt(7)
            b.line(x, 19, x, y, h(0xFF2E7D32))
            b.set(x - 1, y + 2, h(0xFF4FA14C))
            b.set(x + 1, y + 3, h(0xFF4FA14C))
            b.rect(x - 1, y - 1, 3, 2, fc)
            b.set(x, y - 2, fc)
            b.set(x, y - 1, shadeCol(fc, 0.35f))
        }
        b.outline(h(0xFF2A1A0E))
        b.groundShadow(16f, 30f, 12f, 1.8f)
        return b
    }

    fun stall(v: Int): PixBuf {
        val b = PixBuf(32, 48)
        val cols = intArrayOf(h(0xFFC83A3A), h(0xFF3A7AC8), h(0xFF3AA860), h(0xFFE0A030))
        val c = cols[v and 3]
        val wood = h(0xFF7A4A24)
        b.rect(3, 17, 2, 25, wood)
        b.rect(27, 17, 2, 25, wood)
        b.rect(3, 17, 1, 25, shadeCol(wood, 0.25f))
        // balcão
        b.rect(3, 31, 26, 11, h(0xFF9A6A3A))
        b.rect(3, 31, 26, 2, h(0xFFC08A52))
        b.rect(3, 41, 26, 1, h(0xFF4A2F18))
        for (k in 0 until 5) b.rect(5 + k * 5, 34, 1, 7, h(0xFF7A4A24))
        // mercadoria
        val rn = java.util.Random(4500L + v)
        for (k in 0 until 7) {
            val x = 6 + k * 3.4f
            val y = 28f + (k % 2)
            val gc = when (v and 3) {
                0 -> h(0xFFD63A3A)
                1 -> h(0xFF8EA8C0)
                2 -> h(0xFF7A4A24)
                else -> h(0xFF5CC85A)
            }
            b.ellipse(x, y, 2.1f, 2f, if (rn.nextInt(4) == 0) shadeCol(gc, 0.2f) else gc, true)
        }
        b.rect(4, 29, 24, 2, h(0xFF6B4423))
        // toldo listrado
        b.poly(floatArrayOf(1f, 31f, 28f, 4f), floatArrayOf(18f, 18f, 5f, 5f), c)
        for (k in 0 until 8) {
            val x0 = 1 + k * 3.75f
            if (k % 2 == 1) {
                b.poly(
                    floatArrayOf(x0, x0 + 3.75f, 4f + k * 3.1f + 3.1f, 4f + k * 3.1f),
                    floatArrayOf(18f, 18f, 5f, 5f), h(0xFFF2EAD6)
                )
            }
        }
        for (k in 0 until 8) {
            val x = 1 + k * 3.75f + 1.9f
            b.ellipse(x, 18f, 1.9f, 1.8f, if (k % 2 == 1) h(0xFFF2EAD6) else c, true)
        }
        b.rect(4, 4, 24, 1, shadeCol(c, 0.3f))
        b.outline(h(0xFF2A1A0E))
        b.groundShadow(16f, 44f, 14f, 2.4f)
        return b
    }

    fun well(): PixBuf {
        val b = PixBuf(32, 48)
        val stone = h(0xFF9A9AA4)
        val wood = h(0xFF7A4A24)
        b.rect(7, 16, 2, 22, wood)
        b.rect(23, 16, 2, 22, wood)
        b.rect(7, 16, 1, 22, shadeCol(wood, 0.25f))
        b.rect(10, 29, 12, 1, h(0xFF8A6A45))
        b.line(16, 17, 16, 28, h(0xFFC9A87A))
        b.rect(14, 28, 5, 4, h(0xFF8A5A2B))
        b.rect(14, 28, 5, 1, h(0xFFB07A44))
        // base de pedra
        b.ellipse(16f, 41.5f, 11.5f, 5f, shadeCol(stone, -0.3f), false)
        b.rect(5, 35, 22, 7, stone)
        for (x in 5..26) {
            val c = if (x < 10) shadeCol(stone, 0.14f) else if (x > 21) shadeCol(stone, -0.22f) else stone
            for (y in 35..41) b.set(x, y, c)
        }
        for (k in 0 until 3) b.rect(5, 37 + k * 2, 22, 1, shadeCol(stone, -0.25f))
        for (k in 0 until 5) b.rect(7 + k * 5 + (k % 2), 35, 1, 7, shadeCol(stone, -0.25f))
        b.ellipse(16f, 35f, 11.5f, 5f, h(0xFFB6B6C0), true)
        b.ellipse(16f, 35f, 8.5f, 3.4f, h(0xFF1F3A5C), false)
        b.ellipse(16f, 35.5f, 6.5f, 2.2f, h(0xFF2F5A8A), false)
        // telhado
        b.poly(floatArrayOf(3f, 29f, 16f), floatArrayOf(17f, 17f, 5f), h(0xFF8A4A2F))
        for (y in 6..16) {
            val half = 13f * (y - 5f) / 12f
            for (x in (16 - half.toInt())..(16 + half.toInt())) {
                if (((x + y * 2) % 5) == 0) b.set(x, y, h(0xFF6A3520))
                if (x < 15 && ((y + x) % 4) == 0) b.set(x, y, h(0xFFA55E3E))
            }
        }
        b.rect(3, 17, 26, 2, h(0xFF5A3A22))
        b.outline(h(0xFF2A1A0E))
        b.groundShadow(16f, 44f, 14f, 2.4f)
        return b
    }

    fun sign(v: Int): PixBuf {
        val b = PixBuf(32, 32)
        val wood = h(0xFFA06A38)
        val dk = h(0xFF5A3A1E)
        b.rect(7, 8, 2, 22, dk)
        b.rect(7, 8, 1, 22, h(0xFF8A5A2E))
        b.rect(7, 7, 18, 2, dk)
        b.set(11, 9, h(0xFF2A2A30))
        b.set(21, 9, h(0xFF2A2A30))
        b.line(11, 9, 11, 11, h(0xFF2A2A30))
        b.line(21, 9, 21, 11, h(0xFF2A2A30))
        b.rect(9, 11, 20, 12, wood)
        b.rect(9, 11, 20, 1, shadeCol(wood, 0.25f))
        b.rect(9, 22, 20, 1, shadeCol(wood, -0.35f))
        b.rect(9, 11, 1, 12, shadeCol(wood, -0.2f))
        b.rect(28, 11, 1, 12, shadeCol(wood, -0.3f))
        val cx = 19
        val cy = 17
        when (v) {
            0 -> {
                b.rect(cx - 3, cy - 3, 6, 7, h(0xFFF2C94C))
                b.rect(cx - 3, cy - 3, 6, 2, h(0xFFFFF6C8))
                b.rect(cx + 3, cy - 2, 2, 4, h(0xFFC49A2A))
                b.rect(cx + 4, cy - 1, 1, 2, wood)
            }
            1 -> {
                b.ellipse(cx.toFloat(), (cy + 2).toFloat(), 3.4f, 3.2f, h(0xFF5CC85A), true)
                b.rect(cx - 1, cy - 4, 3, 4, h(0xFFDDE6E8))
                b.rect(cx - 1, cy - 5, 3, 1, h(0xFF8A5A2B))
            }
            2 -> {
                b.rect(cx - 4, cy - 3, 8, 7, h(0xFFB03A3A))
                b.rect(cx - 3, cy - 2, 6, 5, h(0xFFF2EAD6))
                b.rect(cx, cy - 2, 1, 5, h(0xFFB03A3A))
            }
            else -> {
                b.rect(cx - 4, cy - 1, 8, 4, h(0xFFB03A3A))
                b.rect(cx - 4, cy - 3, 3, 3, h(0xFFF2EAD6))
                b.rect(cx - 4, cy + 3, 1, 2, dk)
                b.rect(cx + 3, cy + 3, 1, 2, dk)
            }
        }
        b.outline(h(0xFF2A1A0E))
        b.groundShadow(8f, 30.5f, 5f, 1.4f)
        return b
    }

    /** Cerca: [mask] = bit 0 norte, 1 leste, 2 sul, 3 oeste (vizinhos que também são cerca/poste). */
    fun fence(mask: Int): PixBuf {
        val b = PixBuf(32, 32)
        val wood = h(0xFFB8844A)
        val dk = h(0xFF6B4423)
        if ((mask and 2) != 0 || (mask and 8) != 0) {
            val x0 = if ((mask and 8) != 0) 0 else 14
            val x1 = if ((mask and 2) != 0) 31 else 17
            b.rect(x0, 17, x1 - x0 + 1, 2, wood)
            b.rect(x0, 17, x1 - x0 + 1, 1, shadeCol(wood, 0.2f))
            b.rect(x0, 23, x1 - x0 + 1, 2, shadeCol(wood, -0.1f))
            var x = x0 + 2
            while (x < x1 - 1) {
                if (x < 13 || x > 18) {
                    b.rect(x, 12, 3, 16, wood)
                    b.rect(x, 12, 1, 16, shadeCol(wood, 0.2f))
                    b.rect(x + 2, 12, 1, 16, shadeCol(wood, -0.25f))
                    b.set(x + 1, 11, wood)
                }
                x += 5
            }
        }
        if ((mask and 1) != 0) {
            b.rect(14, 0, 4, 16, shadeCol(wood, -0.05f))
            b.rect(14, 0, 1, 16, shadeCol(wood, 0.2f))
            b.rect(17, 0, 1, 16, shadeCol(wood, -0.3f))
            for (y in 0 until 16 step 5) b.rect(14, y, 4, 1, dk)
        }
        if ((mask and 4) != 0) {
            b.rect(14, 16, 4, 14, shadeCol(wood, -0.05f))
            b.rect(14, 16, 1, 14, shadeCol(wood, 0.2f))
            b.rect(17, 16, 1, 14, shadeCol(wood, -0.3f))
            for (y in 16 until 30 step 5) b.rect(14, y, 4, 1, dk)
        }
        // poste central
        b.rect(13, 10, 6, 20, wood)
        b.rect(13, 10, 2, 20, shadeCol(wood, 0.22f))
        b.rect(17, 10, 2, 20, shadeCol(wood, -0.28f))
        b.rect(14, 8, 4, 2, shadeCol(wood, 0.1f))
        b.rect(15, 7, 2, 1, shadeCol(wood, 0.3f))
        b.outline(h(0xFF3A2410))
        b.groundShadow(16f, 30f, 8f + (if ((mask and 10) != 0) 6f else 0f), 1.6f)
        return b
    }
}
