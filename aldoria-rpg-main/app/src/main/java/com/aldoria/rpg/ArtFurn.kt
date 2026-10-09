package com.aldoria.rpg

/** Móveis e objetos de dentro das casas. */
object ArtFurn {

    fun bed(): PixBuf {
        val b = PixBuf(32, 32)
        val wood = h(0xFF7A4A24)
        b.rect(4, 2, 24, 28, wood)
        b.rect(4, 2, 24, 4, shadeCol(wood, -0.2f))
        b.rect(4, 2, 24, 1, shadeCol(wood, 0.25f))
        b.rect(6, 6, 20, 22, h(0xFFF2EAD6))
        b.rect(6, 6, 20, 1, h(0xFFFFFFFF))
        b.ellipse(16f, 10.5f, 7.5f, 3.4f, h(0xFFFFFFFF), true)
        b.rect(6, 14, 20, 14, h(0xFFB03A3A))
        b.rect(6, 14, 20, 2, h(0xFFD25A5A))
        for (k in 0 until 4) b.rect(6, 18 + k * 3, 20, 1, h(0xFF8E2A2A))
        b.rect(6, 14, 20, 1, h(0xFFF2C94C))
        b.rect(6, 27, 20, 1, h(0xFF7A2020))
        b.rect(4, 28, 3, 2, shadeCol(wood, -0.3f))
        b.rect(25, 28, 3, 2, shadeCol(wood, -0.3f))
        b.outline(h(0xFF2A1A0E))
        return b
    }

    fun table(v: Int): PixBuf {
        val b = PixBuf(32, 32)
        val wood = h(0xFFA06A38)
        b.ellipse(16f, 23f, 12.5f, 6.5f, shadeCol(wood, -0.35f), false)
        b.ellipse(16f, 20f, 12.5f, 7.5f, wood, true)
        for (k in 0 until 3) b.rect(7 + k * 7, 15 + k, 1, 10 - k * 2, shadeCol(wood, -0.2f))
        when (v) {
            0 -> {
                b.rect(14, 15, 4, 5, h(0xFFF2EAD6))
                b.rect(15, 12, 2, 3, h(0xFFFFE066))
                b.set(15, 11, h(0xFFFF8A2D))
                b.ellipse(10f, 22f, 3f, 1.6f, h(0xFFE8E8F0), true)
            }
            1 -> {
                b.rect(9, 17, 4, 5, h(0xFFB07A44))
                b.rect(9, 17, 4, 1, h(0xFFF2EAD6))
                b.rect(18, 19, 4, 5, h(0xFFB07A44))
                b.rect(18, 19, 4, 1, h(0xFFF2EAD6))
                b.rect(13, 18, 2, 3, h(0xFF5CC85A))
            }
            else -> {
                b.rect(8, 15, 16, 10, h(0xFFE6D2A0))
                b.rect(8, 15, 16, 1, h(0xFFF4E6BC))
                b.line(10, 18, 20, 18, h(0xFF8A6A45))
                b.line(10, 21, 17, 21, h(0xFF8A6A45))
                b.set(22, 20, h(0xFFB03A3A))
                b.rect(21, 19, 3, 3, h(0xFFB03A3A))
            }
        }
        b.outline(h(0xFF2A1A0E))
        return b
    }

    fun chair(): PixBuf {
        val b = PixBuf(32, 32)
        val wood = h(0xFF8A5A2B)
        b.rect(10, 9, 12, 8, shadeCol(wood, -0.1f))
        b.rect(10, 9, 12, 1, shadeCol(wood, 0.25f))
        b.rect(12, 11, 8, 1, shadeCol(wood, -0.3f))
        b.rect(10, 17, 12, 9, wood)
        b.rect(11, 18, 10, 6, h(0xFFB03A3A))
        b.rect(11, 18, 10, 1, h(0xFFD25A5A))
        b.rect(10, 26, 2, 3, shadeCol(wood, -0.3f))
        b.rect(20, 26, 2, 3, shadeCol(wood, -0.3f))
        b.outline(h(0xFF2A1A0E))
        b.groundShadow(16f, 29.5f, 8f, 1.6f)
        return b
    }

    fun shelf(v: Int): PixBuf {
        val b = PixBuf(32, 32)
        val rn = java.util.Random(5000L + v * 7L)
        val wood = h(0xFF7A4A24)
        b.rect(2, 2, 28, 28, wood)
        b.rect(2, 2, 28, 1, shadeCol(wood, 0.25f))
        val cols = intArrayOf(h(0xFFB03A3A), h(0xFF3A6AB0), h(0xFF3A9A5A), h(0xFFD0A030), h(0xFF8A4AA8), h(0xFFE0E0D0))
        for (r in 0 until 3) {
            val y0 = 4 + r * 9
            b.rect(4, y0, 24, 8, h(0xFF3A2410))
            var x = 4
            while (x < 26) {
                val bw = 2 + rn.nextInt(3)
                val bh = 5 + rn.nextInt(3)
                val c = cols[rn.nextInt(cols.size)]
                if (x + bw > 28) break
                b.rect(x, y0 + 8 - bh, bw, bh, c)
                b.rect(x, y0 + 8 - bh, 1, bh, shadeCol(c, 0.25f))
                b.set(x + bw - 1, y0 + 8 - bh, shadeCol(c, -0.2f))
                if (bw > 2) b.rect(x + 1, y0 + 8 - bh + 2, bw - 1, 1, h(0xFFF2C94C))
                x += bw
                if (rn.nextInt(6) == 0) x += 2
            }
            b.rect(3, y0 + 8, 26, 1, shadeCol(wood, 0.15f))
        }
        b.rect(2, 29, 28, 1, shadeCol(wood, -0.4f))
        b.outline(h(0xFF2A1A0E))
        return b
    }

    fun shelfPotions(v: Int): PixBuf {
        val b = PixBuf(32, 32)
        val rn = java.util.Random(5200L + v * 11L)
        val wood = h(0xFF6A3E1E)
        b.rect(2, 2, 28, 28, wood)
        b.rect(2, 2, 28, 1, shadeCol(wood, 0.25f))
        val cols = intArrayOf(h(0xFFE8505B), h(0xFF5CC85A), h(0xFF5AA8F0), h(0xFFB07AFF), h(0xFFF5D547))
        for (r in 0 until 3) {
            val y0 = 4 + r * 9
            b.rect(4, y0, 24, 8, h(0xFF2A1A0E))
            var x = 5
            while (x < 25) {
                val c = cols[rn.nextInt(cols.size)]
                val tall = rn.nextBoolean()
                val by = y0 + 8 - (if (tall) 3 else 2)
                b.ellipse(x + 1.5f, by.toFloat(), 2.2f, if (tall) 2.4f else 1.9f, c, true)
                b.rect(x + 1, by - (if (tall) 4 else 3), 1, 2, h(0xFFDDE6E8))
                b.set(x + 1, by - (if (tall) 5 else 4), h(0xFF8A5A2B))
                b.set(x, by - 1, h(0xFFFFFFFF))
                x += 4 + rn.nextInt(2)
            }
            b.rect(3, y0 + 8, 26, 1, shadeCol(wood, 0.2f))
        }
        b.outline(h(0xFF2A1A0E))
        return b
    }

    fun barrel(v: Int): PixBuf {
        val b = PixBuf(32, 32)
        val wood = if (v == 0) h(0xFF9A6A3A) else h(0xFF8A5A2B)
        b.poly(floatArrayOf(7f, 25f, 27f, 5f), floatArrayOf(13f, 13f, 28f, 28f), wood)
        for (x in 5..26) {
            val c = if (x < 10) shadeCol(wood, 0.2f) else if (x > 21) shadeCol(wood, -0.3f) else if (x > 16) shadeCol(wood, -0.12f) else wood
            for (y in 13..28) if (b.get(x, y) != 0) b.set(x, y, c)
        }
        for (x in intArrayOf(10, 14, 18, 22)) for (y in 14..27) if (b.get(x, y) != 0) b.set(x, y, shadeCol(wood, -0.28f))
        b.ellipse(16f, 13f, 9.5f, 4.6f, shadeCol(wood, 0.15f), true)
        b.ellipse(16f, 13f, 7.4f, 3.4f, shadeCol(wood, -0.05f), false)
        b.ring(16f, 13f, 7.4f, 3.4f, shadeCol(wood, -0.35f))
        val band = h(0xFF3A3A44)
        b.rect(5, 17, 22, 2, band)
        b.rect(5, 17, 22, 1, h(0xFF6A6A74))
        b.rect(5, 24, 22, 2, band)
        b.rect(5, 24, 22, 1, h(0xFF6A6A74))
        if (v == 1) {
            b.ellipse(16f, 12.6f, 6.2f, 2.6f, h(0xFF3A6AB0), false)
            b.set(13, 12, h(0xFF8CC4F5))
        }
        b.outline(h(0xFF2A1A0E))
        b.groundShadow(16f, 29.5f, 11f, 2f)
        return b
    }

    fun crate(v: Int): PixBuf {
        val b = PixBuf(32, 32)
        if (v == 0) {
            val wood = h(0xFFB07A44)
            b.rect(5, 8, 22, 21, wood)
            b.rect(5, 8, 22, 2, shadeCol(wood, 0.25f))
            b.rect(5, 27, 22, 2, shadeCol(wood, -0.3f))
            b.rect(5, 8, 3, 21, shadeCol(wood, -0.15f))
            b.rect(24, 8, 3, 21, shadeCol(wood, -0.15f))
            b.line(8, 10, 24, 26, shadeCol(wood, -0.35f))
            b.line(24, 10, 8, 26, shadeCol(wood, -0.35f))
            b.set(6, 9, h(0xFF3A3A44)); b.set(25, 9, h(0xFF3A3A44)); b.set(6, 27, h(0xFF3A3A44)); b.set(25, 27, h(0xFF3A3A44))
        } else {
            val sack = h(0xFFD2B88A)
            b.ellipse(11f, 22f, 7f, 7f, sack, true)
            b.ellipse(22f, 23f, 6.5f, 6.5f, shadeCol(sack, -0.05f), true)
            b.ellipse(16f, 17f, 6.5f, 6f, shadeCol(sack, 0.05f), true)
            b.rect(14, 10, 4, 2, shadeCol(sack, -0.3f))
            b.line(11, 17, 12, 22, shadeCol(sack, -0.3f))
            b.line(22, 18, 21, 23, shadeCol(sack, -0.3f))
        }
        b.outline(h(0xFF2A1A0E))
        b.groundShadow(16f, 29.5f, 12f, 1.8f)
        return b
    }

    fun chest(): PixBuf {
        val b = PixBuf(32, 32)
        val wood = h(0xFF8A5A2B)
        val iron = h(0xFF3A3A44)
        b.rect(5, 14, 22, 14, wood)
        b.ellipse(16f, 14f, 11f, 5f, shadeCol(wood, 0.1f), true)
        b.rect(5, 18, 22, 1, shadeCol(wood, -0.4f))
        b.rect(5, 14, 2, 14, iron)
        b.rect(25, 14, 2, 14, iron)
        b.rect(5, 26, 22, 2, shadeCol(wood, -0.35f))
        b.rect(14, 16, 4, 6, h(0xFFF2C94C))
        b.rect(15, 18, 2, 2, h(0xFF3A2A10))
        b.rect(14, 16, 4, 1, h(0xFFFFF0A0))
        for (k in 0 until 3) b.rect(8 + k * 6, 20, 1, 7, shadeCol(wood, -0.2f))
        b.outline(h(0xFF2A1A0E))
        b.groundShadow(16f, 29.5f, 12f, 1.8f)
        return b
    }

    fun fireplace(frame: Int): PixBuf {
        val b = PixBuf(32, 32)
        val st = h(0xFF8E8E98)
        b.rect(2, 4, 28, 26, st)
        for (y in 4..29) {
            for (x in 2..29) if (((x / 5 + y / 4) % 2) == 0) b.set(x, y, shadeCol(st, -0.1f))
        }
        b.rect(2, 4, 28, 2, shadeCol(st, 0.3f))
        b.rect(0, 2, 32, 3, h(0xFF7A4A24))
        b.rect(0, 2, 32, 1, h(0xFFB07A44))
        b.rect(7, 10, 18, 20, h(0xFF14100C))
        b.ellipse(16f, 10f, 9f, 3f, h(0xFF14100C), false)
        b.rect(10, 26, 12, 3, h(0xFF4A2F18))
        b.rect(9, 27, 14, 2, h(0xFF3A2410))
        b.rect(10, 26, 12, 1, h(0xFF8A5A2B))
        val fy = 22
        if (frame == 0) {
            b.ellipse(16f, fy.toFloat(), 5f, 6f, h(0xFFFF6A1D), false)
            b.ellipse(16f, fy + 1f, 3.2f, 4.2f, h(0xFFFFA82D), false)
            b.ellipse(16f, fy + 2f, 1.6f, 2.6f, h(0xFFFFF0A0), false)
            b.set(12, 16, h(0xFFFF8A2D)); b.set(20, 15, h(0xFFFFB04D))
        } else {
            b.ellipse(16.4f, fy - 0.6f, 4.6f, 6.6f, h(0xFFFF6A1D), false)
            b.ellipse(16f, fy + 0.8f, 3f, 4.6f, h(0xFFFFA82D), false)
            b.ellipse(16f, fy + 2f, 1.5f, 2.8f, h(0xFFFFF0A0), false)
            b.set(13, 14, h(0xFFFF8A2D)); b.set(19, 17, h(0xFFFFB04D))
        }
        b.set(8, 29, h(0xFFFF4A1D)); b.set(23, 29, h(0xFFFF4A1D))
        b.outline(h(0xFF2A1A0E))
        return b
    }

    fun cauldron(frame: Int): PixBuf {
        val b = PixBuf(32, 32)
        b.rect(7, 26, 18, 3, h(0xFF4A4A54))
        b.set(10, 27, h(0xFFFF4A1D)); b.set(15, 27, h(0xFFFF8A2D)); b.set(21, 27, h(0xFFFF4A1D))
        b.ellipse(16f, 21f, 11f, 8f, h(0xFF2A2A34), true)
        b.rect(5, 15, 22, 3, h(0xFF3A3A44))
        b.ellipse(16f, 15f, 11.5f, 4.4f, h(0xFF3A3A44), true)
        b.ellipse(16f, 15.2f, 9.6f, 3.4f, h(0xFF4AD060), false)
        b.ellipse(16f, 15.6f, 7.6f, 2.4f, h(0xFF7CFF8A), false)
        for (k in 0 until 4) {
            val ph = (frame * 0.5f + k * 0.25f) % 1f
            b.set(10 + k * 4, (15 - ph * 3f).toInt(), h(0xFFD6FFD9))
        }
        b.set(12, 11 - frame, h(0xFFB8F0C0))
        b.set(20, 10 + frame, h(0xFFB8F0C0))
        b.rect(5, 17, 2, 4, h(0xFF3A3A44))
        b.rect(25, 17, 2, 4, h(0xFF3A3A44))
        b.outline(h(0xFF14141A))
        b.groundShadow(16f, 29.5f, 12f, 1.8f)
        return b
    }

    fun counter(v: Int): PixBuf {
        val b = PixBuf(32, 32)
        val wood = h(0xFF8A5A2B)
        b.rect(0, 10, 32, 6, shadeCol(wood, 0.2f))
        b.rect(0, 10, 32, 1, shadeCol(wood, 0.4f))
        b.rect(0, 16, 32, 15, wood)
        b.rect(0, 16, 32, 1, shadeCol(wood, -0.35f))
        for (k in 0 until 4) {
            b.rect(k * 8 + 1, 18, 6, 11, shadeCol(wood, -0.1f))
            b.rect(k * 8 + 1, 18, 6, 1, shadeCol(wood, 0.15f))
            b.rect(k * 8 + 1, 28, 6, 1, shadeCol(wood, -0.35f))
        }
        b.rect(0, 30, 32, 1, h(0xFF2A1A0E))
        b.rect(0, 9, 32, 1, h(0xFF2A1A0E))
        if (v == 0) {
            b.rect(6, 4, 4, 6, h(0xFFB07A44)); b.rect(6, 4, 4, 1, h(0xFFF2EAD6)); b.rect(10, 5, 2, 3, h(0xFFB07A44))
            b.rect(21, 5, 4, 5, h(0xFFB07A44)); b.rect(21, 5, 4, 1, h(0xFFF2EAD6))
        } else {
            b.ellipse(10f, 8f, 4f, 2f, h(0xFFE8E8F0), true)
            b.rect(9, 5, 2, 3, h(0xFFD25A3A))
            b.rect(20, 3, 5, 7, h(0xFF5CA050)); b.rect(20, 3, 5, 1, h(0xFF8A5A2B))
        }
        return b
    }
}
