package com.aldoria.rpg

/** Personagens em 32 x 32 pixels (o mago é 64 x 64). Direções: 0 costas, 1 direita, 2 frente (a esquerda é o espelho da direita). */
object ArtSprites {
    private val ROBE = h(0xFF3B62D6)
    private val ROBE_L = h(0xFF5B84F0)
    private val ROBE_D = h(0xFF2A45A8)
    private val ROBE_DD = h(0xFF1E3280)
    private val GOLD = h(0xFFF2C94C)
    private val GOLD_D = h(0xFFC49A2A)
    private val SKIN = h(0xFFF2C29B)
    private val SKIN_D = h(0xFFD9A07A)
    private val WHITE = h(0xFFF4F4F4)
    private val WHITE_D = h(0xFFC9CCD6)
    private val HAT = h(0xFF3552C9)
    private val HAT_L = h(0xFF5575EA)
    private val HAT_D = h(0xFF24388F)
    private val LEATHER = h(0xFF7A4A24)
    private val LEATHER_D = h(0xFF4F2E14)
    private val WOOD = h(0xFF8B5E3C)
    private val WOOD_L = h(0xFFB07F55)
    private val CRYSTAL = h(0xFFFF9A3D)
    private val CRYSTAL_L = h(0xFFFFE0A0)
    private val DARK = h(0xFF2A1A10)

    /** Mago em 64 x 64 pixels (dados em MageData). dir: 0 costas, 1 direita, 2 frente. frame: 0 parado, 1 passo A, 2 parado, 3 passo B. */
    private var mageCols: IntArray? = null

    fun mage(dir: Int, frame: Int): PixBuf {
        var cols = mageCols
        if (cols == null) {
            val n = MageData.HEX.length / 6
            cols = IntArray(n + 1)
            for (i in 0 until n) {
                cols[i + 1] = 0xFF000000.toInt() or Integer.parseInt(MageData.HEX.substring(i * 6, i * 6 + 6), 16)
            }
            mageCols = cols
        }
        val bs = MageData.CH.length
        val b = PixBuf(64, 64)
        val s = MageData.F[dir * 4 + frame]
        var i = 0
        var pos = 0
        while (i + 1 < s.length) {
            val idx = MageData.CH.indexOf(s[i]) * bs + MageData.CH.indexOf(s[i + 1])
            i += 2
            var n = 0
            var has = false
            while (i < s.length && s[i] in '0'..'9') {
                n = n * 10 + (s[i] - '0')
                i++
                has = true
            }
            if (!has) n = 1
            val c = if (idx <= 0 || idx >= cols.size) 0 else cols[idx]
            var k = 0
            while (k < n && pos < b.px.size) {
                b.px[pos] = c
                pos++
                k++
            }
        }
        return b
    }

    // ------------------------------------------------------------------ rato

    private val FUR = h(0xFF9C9CA6)
    private val BELLY = h(0xFFC8C8D2)
    private val PINK = h(0xFFE59AA8)
    private val PINK_D = h(0xFFC27888)
    private val BLACK = h(0xFF1A1018)

    private fun furTexture(b: PixBuf, seed: Long, x0: Int, y0: Int, x1: Int, y1: Int) {
        val rn = java.util.Random(seed)
        for (i in 0 until 60) {
            val x = x0 + rn.nextInt(x1 - x0 + 1)
            val y = y0 + rn.nextInt(y1 - y0 + 1)
            val c = b.get(x, y)
            if (c != 0 && c != BLACK && c != PINK) b.set(x, y, shadeCol(c, if (rn.nextBoolean()) 0.1f else -0.12f))
        }
    }

    private fun tail(b: PixBuf, x0: Int, y0: Int, dx: Float, frame: Int, len: Int) {
        for (i in 0..len) {
            val x = x0 + Math.round(dx * i)
            val y = y0 + Math.round(Math.sin((i * 0.55f + frame * 1.57f).toDouble()).toFloat() * 2.2f) - i / 6
            b.set(x, y, PINK)
            if (i < len * 2 / 3) b.set(x, y + 1, PINK_D)
        }
    }

    fun rat(dir: Int, frame: Int): PixBuf {
        val b = PixBuf(32, 32)
        val by = if (frame == 1 || frame == 3) -1 else 0
        val lx = if (frame == 1) 1 else if (frame == 3) -1 else 0
        if (dir == 1) {
            tail(b, 6, 23, -0.8f, frame, 14)
            b.ellipse(15f, 21.5f + by, 9.2f, 5.4f, FUR, true)
            b.ellipse(10.5f, 25f + by, 3.4f, 3f, shadeCol(FUR, -0.05f), true)
            b.ellipse(16f, 24f + by, 7f, 2f, BELLY, false)
            for (x in 8..24) for (y in 20..26) if (b.get(x, y) == BELLY && b.get(x, y - 3) == 0) b.set(x, y, FUR)
            b.rect(21 + lx, 26, 2, 3, shadeCol(FUR, -0.1f))
            b.rect(20 + lx, 28, 4, 2, PINK)
            b.rect(8 - lx, 28, 4, 2, PINK)
            b.rect(13 + lx, 27, 2, 2, shadeCol(FUR, -0.15f))
            b.ellipse(24.5f, 20f + by, 5f, 4.2f, FUR, true)
            b.ellipse(28f, 21.2f + by, 3f, 2.4f, shadeCol(FUR, 0.1f), true)
            b.rect(30, 20 + by, 2, 2, PINK)
            b.set(30, 20 + by, shadeCol(PINK, 0.3f))
            b.rect(29, 23 + by, 1, 2, WHITE)
            b.ellipse(22.3f, 15.8f + by, 2.8f, 3.3f, FUR, true)
            b.ellipse(22.5f, 16.2f + by, 1.5f, 2.1f, PINK, false)
            b.rect(26, 18 + by, 2, 2, BLACK)
            b.set(26, 18 + by, h(0xFFFFFFFF))
            b.line(29, 22 + by, 31, 20 + by, WHITE_D)
            b.line(29, 22 + by, 31, 23 + by, WHITE_D)
            furTexture(b, 6100L + frame, 7, 14, 28, 27)
        } else if (dir == 2) {
            tail(b, 24, 27, 0.7f, frame, 8)
            b.ellipse(16f, 24f + by, 8.3f, 5f, FUR, true)
            b.ellipse(16f, 26f + by, 5f, 2.4f, BELLY, false)
            b.ellipse(10.5f, 12.8f + by, 3.4f, 3.6f, FUR, true)
            b.ellipse(21.5f, 12.8f + by, 3.4f, 3.6f, FUR, true)
            b.ellipse(10.7f, 13.2f + by, 1.9f, 2.2f, PINK, false)
            b.ellipse(21.3f, 13.2f + by, 1.9f, 2.2f, PINK, false)
            b.ellipse(16f, 17f + by, 6.2f, 5.2f, FUR, true)
            b.ellipse(16f, 19.6f + by, 3.4f, 2.4f, shadeCol(FUR, 0.2f), false)
            b.rect(15, 19 + by, 3, 2, PINK)
            b.set(15, 19 + by, shadeCol(PINK, 0.3f))
            b.rect(15, 21 + by, 1, 2, WHITE)
            b.rect(17, 21 + by, 1, 2, WHITE)
            b.rect(12, 15 + by, 2, 2, BLACK)
            b.rect(19, 15 + by, 2, 2, BLACK)
            b.set(12, 15 + by, h(0xFFFFFFFF))
            b.set(19, 15 + by, h(0xFFFFFFFF))
            b.line(12, 20 + by, 7, 18 + by, WHITE_D)
            b.line(12, 21 + by, 7, 22 + by, WHITE_D)
            b.line(21, 20 + by, 26, 18 + by, WHITE_D)
            b.line(21, 21 + by, 26, 22 + by, WHITE_D)
            b.rect(10 - lx, 28, 3, 2, PINK)
            b.rect(19 + lx, 28, 3, 2, PINK)
            furTexture(b, 6200L + frame, 8, 12, 24, 28)
        } else {
            b.ellipse(16f, 21f + by, 8.6f, 7f, FUR, true)
            b.ellipse(10.5f, 13.5f + by, 3.4f, 3.6f, FUR, true)
            b.ellipse(21.5f, 13.5f + by, 3.4f, 3.6f, FUR, true)
            b.ellipse(16f, 15.5f + by, 5.6f, 4.2f, shadeCol(FUR, -0.05f), true)
            for (y in 13..25) b.set(16, y + by, shadeCol(FUR, -0.25f))
            val sx = if (frame == 1) 1 else if (frame == 3) -1 else 0
            for (i in 0..7) {
                b.set(16 + Math.round(Math.sin((i * 0.7f + frame * 1.57f).toDouble()).toFloat() * 1.6f) + sx, 26 + i / 2 + (if (i > 4) 1 else 0), PINK)
                b.set(17 + Math.round(Math.sin((i * 0.7f + frame * 1.57f).toDouble()).toFloat() * 1.6f) + sx, 26 + i / 2, PINK_D)
            }
            b.rect(10 - lx, 28, 3, 2, PINK)
            b.rect(19 + lx, 28, 3, 2, PINK)
            furTexture(b, 6300L + frame, 8, 13, 24, 27)
        }
        b.outline(h(0xFF2A2430))
        return b
    }

    fun ratDead(): PixBuf {
        val b = PixBuf(32, 32)
        b.ellipse(14f, 24f, 10f, 4.6f, FUR, true)
        b.ellipse(14f, 26f, 7f, 2f, BELLY, false)
        b.ellipse(25f, 25f, 4.6f, 3.8f, FUR, true)
        b.ellipse(22f, 21.5f, 2.4f, 2.6f, FUR, true)
        b.ellipse(22f, 22f, 1.2f, 1.6f, PINK, false)
        b.set(26, 23, BLACK); b.set(27, 24, BLACK); b.set(27, 23, BLACK); b.set(26, 24, BLACK)
        b.rect(28, 25, 2, 1, PINK)
        b.rect(28, 27, 2, 1, PINK_D)
        for (k in 0 until 4) {
            val x = 8 + k * 4
            b.line(x, 21, x - 1 + (k % 2) * 2, 18, shadeCol(FUR, -0.1f))
            b.set(x - 1 + (k % 2) * 2, 17, PINK)
        }
        for (i in 0..12) b.set(4 - i / 3, 25 + Math.round(Math.sin((i * 0.6f).toDouble()).toFloat() * 2f) + i / 5, PINK)
        furTexture(b, 6400L, 4, 19, 29, 28)
        b.outline(h(0xFF2A2430))
        // poça de sangue
        for (y in 22..30) {
            for (x in 1..31) {
                val nx = (x + 0.5f - 15f) / 14f
                val ny = (y + 0.5f - 27f) / 3.4f
                if (nx * nx + ny * ny <= 1f && b.get(x, y) == 0) b.set(x, y, 0xAA8A1616.toInt())
            }
        }
        return b
    }
}
