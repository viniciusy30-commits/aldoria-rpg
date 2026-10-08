package com.aldoria.rpg

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint

/** Pincel de pixel art: desenha numa grade de 16 x 16 que é esticada para o tamanho do tile. */
private class Px(val c: Canvas, size: Int) {
    val u: Float = size / 16f
    val p = Paint()

    fun r(x: Int, y: Int, w: Int, h: Int, col: Int) {
        p.color = col
        c.drawRect(x * u, y * u, (x + w) * u, (y + h) * u, p)
    }
}

object Art {

    // ------------------------------------------------------------------ terrenos

    fun tiles(size: Int): Array<Array<Bitmap>> {
        return Array(Tl.COUNT) { t -> Array(4) { v -> makeTile(t, v, size) } }
    }

    private fun makeTile(t: Int, v: Int, size: Int): Bitmap {
        val b = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val g = Px(Canvas(b), size)
        val rn = java.util.Random(1000L + t * 31L + v * 7L)
        when (t) {
            Tl.GRASS -> grass(g, rn)
            Tl.FLOWER -> {
                grass(g, rn)
                flowers(g, v, rn)
            }
            Tl.TREE -> {
                grass(g, rn)
                tree(g, v)
            }
            Tl.BUSH -> {
                grass(g, rn)
                bush(g, v)
            }
            Tl.ROCK -> {
                grass(g, rn)
                rock(g)
            }
            Tl.DIRT -> dirt(g, rn)
            Tl.SAND -> sand(g, rn)
            Tl.WATER -> water(g, v)
            Tl.STONE -> stone(g, rn)
            Tl.WOOD -> wood(g)
            Tl.WALL -> wall(g)
            Tl.DOOR -> {
                wood(g)
                door(g)
            }
            Tl.FOUNTAIN -> {
                stone(g, rn)
                fountain(g, v)
            }
        }
        return b
    }

    private fun grass(g: Px, rn: java.util.Random) {
        g.r(0, 0, 16, 16, 0xFF4C9A3A.toInt())
        for (i in 0 until 18) {
            val x = rn.nextInt(16)
            val y = rn.nextInt(16)
            g.r(x, y, 1, 1, if (rn.nextBoolean()) 0xFF5BAA45.toInt() else 0xFF3F8531.toInt())
        }
        for (i in 0 until 3) {
            val x = rn.nextInt(13)
            val y = 1 + rn.nextInt(14)
            val col = 0xFF6FC456.toInt()
            g.r(x, y, 1, 1, col)
            g.r(x + 1, y - 1, 1, 1, col)
            g.r(x + 2, y, 1, 1, col)
        }
    }

    private fun flowers(g: Px, v: Int, rn: java.util.Random) {
        val fc = intArrayOf(0xFFE8505B.toInt(), 0xFFF5D547.toInt(), 0xFFFFFFFF.toInt(), 0xFFFF8FB7.toInt())
        for (k in 0 until 3) {
            val x = 2 + rn.nextInt(12)
            val y = 3 + rn.nextInt(10)
            val col = fc[(v + k) % 4]
            g.r(x, y + 1, 1, 2, 0xFF2E7D32.toInt())
            g.r(x - 1, y, 3, 1, col)
            g.r(x, y - 1, 1, 3, col)
            g.r(x, y, 1, 1, 0xFFFFE066.toInt())
        }
    }

    private fun tree(g: Px, v: Int) {
        // sombra no chão
        g.r(4, 13, 8, 2, 0x44000000)
        // tronco
        g.r(7, 10, 2, 5, 0xFF6B4423.toInt())
        g.r(7, 10, 1, 5, 0xFF8A5A33.toInt())
        // copa
        val hws = intArrayOf(2, 3, 4, 5, 5, 5, 5, 4, 3, 2)
        for (i in hws.indices) {
            g.r(8 - hws[i], 1 + i, hws[i] * 2, 1, 0xFF2E6B2E.toInt())
        }
        for (i in 1..6) {
            val hw = hws[i]
            g.r(8 - hw + 1, 1 + i, if (hw - 1 > 1) hw - 1 else 1, 1, 0xFF3F8F3B.toInt())
        }
        val rn = java.util.Random(50L + v)
        for (i in 0 until 9) {
            val yy = 2 + rn.nextInt(5)
            val hw = hws[yy - 1]
            val xx = 8 - hw + 1 + rn.nextInt(hw)
            g.r(xx, yy, 1, 1, 0xFF5DB85A.toInt())
        }
        g.r(5, 9, 6, 1, 0xFF245624.toInt())
        if (v == 3) {
            g.r(6, 4, 1, 1, 0xFFD94B4B.toInt())
            g.r(10, 6, 1, 1, 0xFFD94B4B.toInt())
            g.r(8, 8, 1, 1, 0xFFD94B4B.toInt())
        }
    }

    private fun bush(g: Px, v: Int) {
        g.r(3, 13, 10, 2, 0x44000000)
        val hws = intArrayOf(2, 3, 4, 4, 4, 3, 2)
        for (i in hws.indices) {
            g.r(8 - hws[i], 7 + i, hws[i] * 2, 1, 0xFF357A35.toInt())
        }
        for (i in 0 until 4) {
            g.r(8 - hws[i] + 1, 7 + i, 2, 1, 0xFF4FA14C.toInt())
        }
        g.r(5, 12, 6, 1, 0xFF28602A.toInt())
        if (v % 2 == 1) {
            g.r(6, 9, 1, 1, 0xFFD94B4B.toInt())
            g.r(10, 10, 1, 1, 0xFFD94B4B.toInt())
            g.r(8, 11, 1, 1, 0xFFD94B4B.toInt())
        }
    }

    private fun rock(g: Px) {
        g.r(3, 13, 10, 2, 0x44000000)
        val hws = intArrayOf(2, 3, 4, 5, 5, 4)
        for (i in hws.indices) {
            g.r(8 - hws[i], 8 + i, hws[i] * 2, 1, 0xFF8A8A92.toInt())
        }
        g.r(6, 9, 3, 1, 0xFFB5B5BD.toInt())
        g.r(5, 10, 2, 1, 0xFFB5B5BD.toInt())
        g.r(5, 13, 7, 1, 0xFF5F5F66.toInt())
        g.r(10, 11, 2, 2, 0xFF70707A.toInt())
    }

    private fun dirt(g: Px, rn: java.util.Random) {
        g.r(0, 0, 16, 16, 0xFFA67C52.toInt())
        for (i in 0 until 20) {
            val x = rn.nextInt(16)
            val y = rn.nextInt(16)
            g.r(x, y, 1, 1, if (rn.nextBoolean()) 0xFF8F6A43.toInt() else 0xFFB88F62.toInt())
        }
        for (i in 0 until 2) {
            g.r(rn.nextInt(14), rn.nextInt(15), 2, 1, 0xFFC9A87A.toInt())
        }
    }

    private fun sand(g: Px, rn: java.util.Random) {
        g.r(0, 0, 16, 16, 0xFFE3CC8B.toInt())
        for (i in 0 until 18) {
            val x = rn.nextInt(16)
            val y = rn.nextInt(16)
            g.r(x, y, 1, 1, if (rn.nextBoolean()) 0xFFD2B877.toInt() else 0xFFF0DDA2.toInt())
        }
    }

    private fun water(g: Px, frame: Int) {
        g.r(0, 0, 16, 16, 0xFF2F6FD0.toInt())
        val r2 = java.util.Random(55L)
        for (k in 0 until 7) {
            val bx = r2.nextInt(13)
            val by = r2.nextInt(15)
            val x = (bx + frame * 3) % 13
            g.r(x, by, 3, 1, 0xFF6FA8F5.toInt())
            g.r(x + 1, by + 1, 1, 1, 0xFF4A86E0.toInt())
        }
        for (k in 0 until 4) {
            val bx = r2.nextInt(14)
            val by = r2.nextInt(15)
            g.r(bx, by, 2, 1, 0xFF2A63BE.toInt())
        }
    }

    private fun stone(g: Px, rn: java.util.Random) {
        g.r(0, 0, 16, 16, 0xFF9A9AA2.toInt())
        val mortar = 0xFF7C7C85.toInt()
        for (row in 0 until 4) {
            val y = row * 4
            g.r(0, y, 16, 1, mortar)
            val off = if (row % 2 == 0) 0 else 4
            var x = off
            while (x < 16) {
                g.r(x, y, 1, 4, mortar)
                x += 8
            }
        }
        for (i in 0 until 12) {
            g.r(rn.nextInt(16), rn.nextInt(16), 1, 1, if (rn.nextBoolean()) 0xFFA9A9B1.toInt() else 0xFF8E8E96.toInt())
        }
    }

    private fun wood(g: Px) {
        g.r(0, 0, 16, 16, 0xFFA8743C.toInt())
        val line = 0xFF8A5C2D.toInt()
        for (row in 0 until 4) {
            g.r(0, row * 4, 16, 1, line)
            g.r(if (row % 2 == 0) 5 else 11, row * 4 + 1, 1, 3, line)
            g.r(if (row % 2 == 0) 2 else 8, row * 4 + 2, 1, 1, 0xFFC08A4E.toInt())
        }
    }

    private fun wall(g: Px) {
        g.r(0, 0, 16, 16, 0xFF7B5B45.toInt())
        val mortar = 0xFF54402F.toInt()
        for (row in 0 until 4) {
            val y = row * 4
            g.r(0, y, 16, 1, mortar)
            var x = if (row % 2 == 0) 0 else 4
            while (x < 16) {
                g.r(x, y, 1, 4, mortar)
                x += 8
            }
        }
        g.r(0, 1, 16, 1, 0xFF9C7C62.toInt())
        g.r(0, 15, 16, 1, 0xFF3A2C20.toInt())
        g.r(0, 0, 16, 1, 0xFF3A2C20.toInt())
    }

    private fun door(g: Px) {
        g.r(2, 1, 12, 15, 0xFF4A2F18.toInt())
        g.r(3, 2, 10, 14, 0xFF7A4B25.toInt())
        g.r(5, 2, 1, 14, 0xFF5E3A1C.toInt())
        g.r(8, 2, 1, 14, 0xFF5E3A1C.toInt())
        g.r(11, 2, 1, 14, 0xFF5E3A1C.toInt())
        g.r(10, 9, 2, 2, 0xFFF2C94C.toInt())
        g.r(3, 5, 10, 1, 0xFF3A2410.toInt())
        g.r(3, 11, 10, 1, 0xFF3A2410.toInt())
    }

    private fun fountain(g: Px, v: Int) {
        val rim = 0xFFB8B8C0.toInt()
        val hws = intArrayOf(3, 5, 6, 7, 7, 7, 7, 7, 7, 6, 5, 3)
        for (i in hws.indices) {
            g.r(8 - hws[i], 2 + i, hws[i] * 2, 1, rim)
        }
        for (i in 1 until hws.size - 1) {
            g.r(8 - hws[i] + 1, 2 + i, (hws[i] - 1) * 2, 1, 0xFF5BA0F0.toInt())
        }
        g.r(7, 5, 2, 6, 0xFF8E8E98.toInt())
        g.r(6, 10, 4, 2, 0xFF8E8E98.toInt())
        g.r(7, 3 + (v % 2), 2, 2, 0xFFBFE0FF.toInt())
        g.r(4 + v, 7, 1, 1, 0xFFE0F0FF.toInt())
        g.r(11 - v, 9, 1, 1, 0xFFE0F0FF.toInt())
    }

    // ------------------------------------------------------------------ sprites

    private fun sprite(rows: Array<String>, altRow: Int, alt: String, pal: Map<Char, Int>, size: Int): Bitmap {
        val b = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val g = Px(Canvas(b), size)
        for (yy in 0 until 16) {
            val src = if (yy == altRow && alt.isNotEmpty()) alt else if (yy < rows.size) rows[yy] else ""
            val row = src.padEnd(16, '.')
            for (xx in 0 until 16) {
                val ch = row[xx]
                if (ch == '.') continue
                val col = pal[ch] ?: continue
                g.r(xx, yy, 1, 1, col)
            }
        }
        return b
    }

    private fun flip(src: Bitmap): Bitmap {
        val m = Matrix()
        m.preScale(-1f, 1f)
        return Bitmap.createBitmap(src, 0, 0, src.width, src.height, m, false)
    }

    private val MAGE_PAL: Map<Char, Int> = mapOf(
        'h' to 0xFF2F55C8.toInt(), 'H' to 0xFF1E3A94.toInt(), 'y' to 0xFFF2C94C.toInt(),
        's' to 0xFFF0C090.toInt(), 'w' to 0xFFEFEFEF.toInt(), 'r' to 0xFF3E68DE.toInt(),
        'R' to 0xFF2A4CAE.toInt(), 'b' to 0xFF8A5A2B.toInt(), 'g' to 0xFFF2C94C.toInt(),
        'e' to 0xFF222222.toInt(), 'f' to 0xFF5A3B22.toInt()
    )

    private val MAGE_FRONT = arrayOf(
        "......hh........",
        ".....hhhH.......",
        ".....hhhhH......",
        "....hhhhhhH.....",
        "...yyyyyyyyyy...",
        ".....sesses.....",
        ".....swwwws.....",
        "....rrwwwwrr....",
        "...rrrwwwwrrr...",
        "...sRrrrrrrRs...",
        "....rrrbbrrr....",
        "....rrrggrrr....",
        "....rrrrrrrr....",
        "...rRrrrrrrRr...",
        "...RRRRRRRRRR...",
        "....ff....ff...."
    )

    private val MAGE_BACK = arrayOf(
        "......hh........",
        ".....hhhH.......",
        ".....hhhhH......",
        "....hhhhhhH.....",
        "...yyyyyyyyyy...",
        ".....HHHHHH.....",
        ".....wwwwww.....",
        "....rrwwwwrr....",
        "...rrrwwwwrrr...",
        "...sRrrrrrrRs...",
        "....rrrrrrrr....",
        "....rrbbbbrr....",
        "....rrrrrrrr....",
        "...rRrrrrrrRr...",
        "...RRRRRRRRRR...",
        "....ff....ff...."
    )

    private val MAGE_SIDE = arrayOf(
        ".......hh.......",
        "......hhhH......",
        "......hhhhH.....",
        ".....hhhhhhH....",
        "....yyyyyyyyy...",
        ".....sssesss....",
        ".....ssswwww....",
        "....rrrrwwwr....",
        "...rrrrrwwrrr...",
        "...rRrrrrrrsr...",
        "....rrrrbbrr....",
        "....rrrrbgrr....",
        "....rrrrrrrr....",
        "....rrRrrrrr....",
        "....RRRRRRRR....",
        "....ff...ff....."
    )

    /** [direção 0..3][quadro 0..1]; direções: cima, direita, baixo, esquerda. */
    fun mage(size: Int): Array<Array<Bitmap>> {
        val up = Array(2) { f -> sprite(MAGE_BACK, 15, if (f == 1) ".....ff..ff....." else "", MAGE_PAL, size) }
        val down = Array(2) { f -> sprite(MAGE_FRONT, 15, if (f == 1) ".....ff..ff....." else "", MAGE_PAL, size) }
        val right = Array(2) { f -> sprite(MAGE_SIDE, 15, if (f == 1) ".....ff.ff......" else "", MAGE_PAL, size) }
        val left = Array(2) { f -> flip(right[f]) }
        return arrayOf(up, right, down, left)
    }

    private val RAT_PAL: Map<Char, Int> = mapOf(
        'g' to 0xFF9A9A9A.toInt(), 'G' to 0xFF616161.toInt(), 'p' to 0xFFE8A0A8.toInt(),
        'e' to 0xFFD92B2B.toInt(), 'w' to 0xFFF2F2F2.toInt(), 'x' to 0xFF222222.toInt()
    )

    private val RAT_SIDE = arrayOf(
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "..........pg....",
        "....ggggggggg...",
        "...gggggggggge..",
        ".pGggggggggggwp.",
        "pp.gggggggggg...",
        "p..GggggggggG...",
        "....GG....GG....",
        "................",
        "................"
    )

    private val RAT_FRONT = arrayOf(
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "....pg....gp....",
        "....gggggggg....",
        "....geggggeg....",
        "....ggggppgg....",
        ".....gggggg.....",
        "....gggggggg....",
        "....ggggggggpp..",
        "....GggggggG....",
        "....GG....GG....",
        "................"
    )

    private val RAT_BACK = arrayOf(
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "....pg....gp....",
        "....gggggggg....",
        "....gggggggg....",
        "....gggggggg....",
        ".....gggggg.....",
        "....gggggggg....",
        "....gggggggg....",
        ".....gGppGg.....",
        "....GG.pp.GG....",
        "................"
    )

    private val RAT_DEAD = arrayOf(
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "......GGGG......",
        "....GggggggG....",
        ".pGggggggggxgp..",
        "pp.GggggggggG...",
        "p...GG....GG....",
        "................"
    )

    fun rat(size: Int): Array<Array<Bitmap>> {
        val up = Array(2) { f -> sprite(RAT_BACK, 14, if (f == 1) "....G..pp..G...." else "", RAT_PAL, size) }
        val down = Array(2) { f -> sprite(RAT_FRONT, 14, if (f == 1) ".....GG..GG....." else "", RAT_PAL, size) }
        val right = Array(2) { f -> sprite(RAT_SIDE, 13, if (f == 1) ".....GG..GG....." else "", RAT_PAL, size) }
        val left = Array(2) { f -> flip(right[f]) }
        return arrayOf(up, right, down, left)
    }

    fun ratDead(size: Int): Bitmap = sprite(RAT_DEAD, -1, "", RAT_PAL, size)
}
