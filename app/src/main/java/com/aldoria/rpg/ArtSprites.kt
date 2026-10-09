package com.aldoria.rpg

/** Personagens em 32 x 32 pixels. Direções: 0 costas, 1 direita, 2 frente (a esquerda é o espelho da direita). */
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

    private fun staff(b: PixBuf, x: Int) {
        b.rect(x, 7, 2, 23, WOOD)
        b.rect(x, 7, 1, 23, WOOD_L)
        var y = 9
        while (y < 29) {
            b.set(x + 1, y, shadeCol(WOOD, -0.35f))
            b.set(x, y + 2, shadeCol(WOOD, -0.2f))
            y += 4
        }
        b.rect(x - 1, 7, 4, 1, GOLD)
        b.rect(x - 1, 8, 4, 1, GOLD_D)
        val cx = x + 1f
        b.poly(floatArrayOf(cx, cx + 3.2f, cx, cx - 3.2f), floatArrayOf(0.6f, 4f, 7.6f, 4f), CRYSTAL)
        b.poly(floatArrayOf(cx, cx + 1.6f, cx, cx - 1.6f), floatArrayOf(2.2f, 4f, 6f, 4f), CRYSTAL_L)
        b.set(x - 1, 3, h(0xFFFFFFFF))
    }

    private fun boot(b: PixBuf, x: Int, y: Int, w: Int) {
        b.rect(x, y, w, 3, LEATHER)
        b.rect(x, y, w, 1, shadeCol(LEATHER, 0.3f))
        b.rect(x, y + 2, w, 1, LEATHER_D)
    }

    private fun hat(b: PixBuf, cx: Float, star: Boolean) {
        b.ellipse(cx, 8.2f, 9.6f, 2.7f, HAT, true)
        b.poly(
            floatArrayOf(cx - 4.8f, cx - 1.8f, cx + 2.8f, cx + 5.2f, cx + 2.6f, cx + 4.8f),
            floatArrayOf(8.4f, 3.2f, 0.6f, 1.6f, 4.0f, 8.4f), HAT
        )
        for (y in 0..8) {
            for (x in 0..31) {
                if (b.get(x, y) == HAT && y < 8) {
                    if (x < cx - 1f) b.set(x, y, HAT_L) else if (x > cx + 2f) b.set(x, y, HAT_D)
                }
            }
        }
        b.rect((cx - 4.8f).toInt(), 6, 10, 1, GOLD)
        b.rect((cx - 4.8f).toInt(), 7, 10, 1, GOLD_D)
        if (star) {
            val sx = (cx - 1f).toInt()
            b.set(sx, 4, GOLD); b.set(sx - 1, 4, GOLD); b.set(sx + 1, 4, GOLD)
            b.set(sx, 3, GOLD); b.set(sx, 5, GOLD); b.set(sx, 4, h(0xFFFFF6C8))
        }
    }

    /** dir: 0 costas, 1 direita, 2 frente. frame: 0 parado, 1 passo A, 2 parado, 3 passo B. */
    fun mage(dir: Int, frame: Int): PixBuf {
        val body = PixBuf(32, 32)
        val sw = if (frame == 1) 1 else if (frame == 3) -1 else 0
        if (dir == 1) {
            staff(body, 24)
            // capa que balança atrás
            body.poly(
                floatArrayOf(11f, 13f, 13f, 6f + sw, 8f + sw),
                floatArrayOf(15f, 15f, 28f, 28f, 22f), ROBE_D
            )
            body.poly(
                floatArrayOf(12.5f, 20f, 22.5f, 20f, 15f, 10.5f, 9.5f),
                floatArrayOf(15f, 15f, 27f, 28.6f, 27.6f, 28.6f, 27f), ROBE
            )
            for (y in 16..27) for (x in 9..23) {
                if (body.get(x, y) == ROBE) body.set(x, y, if (x <= 13) ROBE_L else if (x >= 19) ROBE_D else ROBE)
            }
            for (x in 10..23) {
                if (body.get(x, 27) != 0) body.set(x, 27, GOLD)
                if (body.get(x, 28) != 0) body.set(x, 28, GOLD_D)
            }
            body.rect(12, 19, 9, 2, LEATHER)
            body.rect(12, 19, 9, 1, shadeCol(LEATHER, 0.3f))
            body.rect(17, 19, 2, 2, GOLD)
            body.rect(12, 21, 3, 3, LEATHER_D)
            // braço da frente
            body.rect(15, 16, 5, 4, ROBE_L)
            body.rect(19, 18, 3, 3, ROBE_L)
            body.rect(19, 20, 3, 1, GOLD)
            body.rect(21, 20, 4, 2, SKIN)
            body.rect(21, 20, 4, 1, shadeCol(SKIN, 0.2f))
            // cabeça, cabelo e barba
            body.rect(12, 10, 4, 6, WHITE_D)
            body.ellipse(17.2f, 12.3f, 4f, 4.3f, SKIN, true)
            body.rect(21, 12, 1, 2, SKIN)
            body.set(22, 13, SKIN_D)
            body.set(19, 12, DARK)
            body.rect(18, 10, 3, 1, WHITE)
            body.poly(floatArrayOf(18f, 21.5f, 20.5f, 18f, 16f), floatArrayOf(14.5f, 14.5f, 19f, 23.5f, 19f), WHITE)
            body.rect(18, 14, 4, 1, WHITE)
            body.line(19, 17, 18, 21, WHITE_D)
            body.line(17, 17, 17, 20, WHITE_D)
            for (y in 14..22) body.set(20, y, if (body.get(20, y) == WHITE) WHITE_D else body.get(20, y))
            hat(body, 17f, true)
        } else {
            staff(body, 25)
            if (dir == 2) {
                body.poly(
                    floatArrayOf(10.5f, 21.5f, 23.5f, 21f, 16f, 11f, 8.5f),
                    floatArrayOf(15f, 15f, 27f, 28.5f, 27.6f, 28.5f, 27f), ROBE
                )
            } else {
                body.poly(
                    floatArrayOf(10.5f, 21.5f, 23.5f, 21f, 16f, 11f, 8.5f),
                    floatArrayOf(15f, 15f, 27f, 28.5f, 27.6f, 28.5f, 27f), ROBE
                )
            }
            for (y in 16..27) for (x in 8..24) {
                if (body.get(x, y) == ROBE) {
                    body.set(x, y, if (x <= 11) ROBE_L else if (x >= 20) ROBE_D else if (y > 21 && (x == 14 || x == 18)) ROBE_D else ROBE)
                }
            }
            if (dir == 0) {
                for (y in 16..27) body.set(16, y, ROBE_DD)
            }
            for (x in 8..24) {
                if (body.get(x + sw, 27) != 0 && body.get(x, 27) != 0) body.set(x, 27, GOLD)
                if (body.get(x, 28) != 0) body.set(x, 28, GOLD_D)
            }
            // braços
            body.rect(8, 16, 3, 6, ROBE_L)
            body.rect(8, 22, 3, 1, GOLD)
            body.rect(8, 23, 3, 2, SKIN)
            body.rect(21, 16, 3, 5, ROBE_D)
            body.rect(21, 21, 3, 1, GOLD)
            body.rect(22, 22, 4, 2, SKIN)
            body.rect(22, 22, 4, 1, shadeCol(SKIN, 0.2f))
            // cinto
            body.rect(11, 19, 10, 2, LEATHER)
            body.rect(11, 19, 10, 1, shadeCol(LEATHER, 0.3f))
            if (dir == 2) {
                body.rect(15, 19, 2, 2, GOLD)
                body.rect(10, 21, 3, 3, LEATHER_D)
                body.rect(10, 21, 3, 1, LEATHER)
            }
            if (dir == 2) {
                body.rect(11, 9, 2, 5, WHITE)
                body.rect(20, 9, 2, 5, WHITE_D)
                body.ellipse(16f, 12.3f, 4.3f, 4.3f, SKIN, true)
                body.set(14, 12, DARK); body.set(18, 12, DARK)
                body.rect(13, 11, 3, 1, WHITE); body.rect(17, 11, 3, 1, WHITE)
                body.set(16, 13, SKIN_D)
                body.poly(floatArrayOf(12f, 20f, 19.5f, 16f, 12.5f), floatArrayOf(14.5f, 14.5f, 18.5f, 25f, 18.5f), WHITE)
                body.rect(13, 14, 6, 1, WHITE)
                for (y in 15..24) for (x in 12..20) if (body.get(x, y) == WHITE && x >= 17) body.set(x, y, WHITE_D)
                body.line(14, 18, 14, 21, WHITE_D)
                body.line(16, 19, 16, 23, WHITE_D)
            } else {
                body.poly(floatArrayOf(12f, 20f, 19f, 16f, 13f), floatArrayOf(10f, 10f, 20f, 22.5f, 20f), WHITE)
                body.ellipse(16f, 12.3f, 4.3f, 4.3f, WHITE, true)
                for (y in 11..21) for (x in 12..19) if (body.get(x, y) == WHITE && x >= 16) body.set(x, y, WHITE_D)
                body.line(14, 14, 14, 20, WHITE_D)
            }
            hat(body, 16f, true)
        }
        // monta o quadro: corpo sobe 1 pixel nos passos; botas ficam no chão
        val out = PixBuf(32, 32)
        val bob = if (frame == 1 || frame == 3) -1 else 0
        out.blit(body, 0, bob)
        if (dir == 1) {
            if (frame == 1) {
                boot(out, 12, 28, 4); boot(out, 19, 27, 5)
            } else if (frame == 3) {
                boot(out, 10, 27, 4); boot(out, 17, 28, 5)
            } else {
                boot(out, 12, 28, 4); boot(out, 17, 28, 5)
            }
        } else {
            val l = if (frame == 1) 27 else 28
            val r = if (frame == 3) 27 else 28
            boot(out, 11, l, 4)
            boot(out, 17, r, 4)
        }
        out.outline(h(0xFF14183A))
        return out
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
