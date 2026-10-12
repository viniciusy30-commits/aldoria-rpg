package com.aldoria.rpg

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt

/** Arte das casas da vila: fachada alta, porta dupla, telhado, parede de fundo, parede lateral e tapetes.
 *  Estilos: 0 creme/terracota, 1 branca/ardósia, 2 areia/musgo, 3 branca/laranja, 4 pedra/escura (ferraria), 5 lilás/roxa (mago). */
object ArtHouse {
    const val STYLES = 6

    private val PL = intArrayOf(h(0xFFE9DEC4), h(0xFFC9D0D8), h(0xFFE8CF98), h(0xFFF3EEE4), h(0xFF9A9AA4), h(0xFFE4DCEE))
    private val BM = intArrayOf(h(0xFF5A3A22), h(0xFF343A52), h(0xFF6B4423), h(0xFF7A3B2A), h(0xFF3A3028), h(0xFF3A2A5A))
    private val SH = intArrayOf(h(0xFF3F7A66), h(0xFFA8413A), h(0xFF3F6FA8), h(0xFF2F8C86), h(0xFF8A5A2B), h(0xFF4A7AC8))
    private val ST = intArrayOf(h(0xFF8E8E98), h(0xFF7C8294), h(0xFF9A9486), h(0xFF8E8E98), h(0xFF74747E), h(0xFF8A8498))
    private val IWA = intArrayOf(h(0xFF5E7F5A), h(0xFF7A2F3A), h(0xFFC8A46A), h(0xFF3C4E7A), h(0xFF6A6258), h(0xFF5A3A7A))
    private val IWB = intArrayOf(h(0xFF71936A), h(0xFF923F4B), h(0xFFD9B97F), h(0xFF4D628F), h(0xFF7C7468), h(0xFF6E4C90))
    private val RUG_BASE = intArrayOf(h(0xFF9A2E3A), h(0xFF2E4A9A), h(0xFF2E7A3E), h(0xFFB8602E), h(0xFF7A4A24), h(0xFF7A2E9A))
    private val RUG_LITE = intArrayOf(h(0xFFB5424F), h(0xFF4262BA), h(0xFF429A55), h(0xFFD87A3E), h(0xFF98622E), h(0xFF9A42BA))
    private val RUG_DARK = intArrayOf(h(0xFF6E1E28), h(0xFF1E2E6E), h(0xFF1E5028), h(0xFF8A4420), h(0xFF4E2E14), h(0xFF501E6E))
    private val GOLD = h(0xFFD8A640)
    private val CURT = intArrayOf(h(0xFF8E2A33), h(0xFF2E4A9A), h(0xFF2E7A3E), h(0xFF7A2E9A), h(0xFF8E2A33), h(0xFF7A2E9A))

    val ROOF = arrayOf(
        intArrayOf(h(0xFFB5482F), h(0xFFC2523A), h(0xFFA93F29), h(0xFFBC4B32)),
        intArrayOf(h(0xFF4A5F86), h(0xFF55698F), h(0xFF3F5278), h(0xFF4E6389)),
        intArrayOf(h(0xFF5E8A48), h(0xFF6A9650), h(0xFF527A3E), h(0xFF74A058)),
        intArrayOf(h(0xFFB8742F), h(0xFFC48038), h(0xFFA8652A), h(0xFFBE7A34)),
        intArrayOf(h(0xFF4A4A58), h(0xFF55556A), h(0xFF3E3E4C), h(0xFF4E4E5E)),
        intArrayOf(h(0xFF6A4A8A), h(0xFF76539A), h(0xFF5E4079), h(0xFF6C4C8C))
    )

    /** Cor de telhado para o minimapa. */
    fun roofColor(style: Int): Int = ROOF[style % STYLES][0]

    private fun mixCol(a: Int, c: Int, t: Float): Int {
        val k = if (t < 0f) 0f else if (t > 1f) 1f else t
        val r = (android.graphics.Color.red(a) * (1f - k) + android.graphics.Color.red(c) * k).toInt()
        val g = (android.graphics.Color.green(a) * (1f - k) + android.graphics.Color.green(c) * k).toInt()
        val b = (android.graphics.Color.blue(a) * (1f - k) + android.graphics.Color.blue(c) * k).toInt()
        return (0xFF shl 24) or (r shl 16) or (g shl 8) or b
    }

    // ------------------------------------------------------------------ fachada

    private fun plinth(b: PixBuf, y0: Int, style: Int, rn: java.util.Random) {
        val st = ST[style]
        val mortar = shadeCol(st, -0.42f)
        b.rect(0, y0, 32, 64 - y0, st)
        val ys = intArrayOf(y0, y0 + 7)
        val hs = intArrayOf(7, 64 - (y0 + 7))
        for (ci in 0..1) {
            val yy = ys[ci]
            val hh = hs[ci]
            b.rect(0, yy, 32, 1, mortar)
            var x = if (ci == 0) 0 else -6
            while (x < 32) {
                val bw = if (((x / 12) + ci) % 2 == 0) 12 else 10
                if (x >= 0) b.rect(x, yy, 1, hh, mortar)
                for (k in 0 until bw - 2) {
                    val px = x + 1 + k
                    if (px in 0..31) b.set(px, yy + 1, shadeCol(st, 0.16f))
                }
                if (x + bw - 2 in 0..31) b.rect(x + bw - 2, yy + 1, 1, hh - 1, shadeCol(st, -0.12f))
                x += bw
            }
        }
        b.rect(0, y0, 32, 1, shadeCol(st, 0.34f))
        b.rect(0, 63, 32, 1, shadeCol(st, -0.5f))
        for (i in 0 until 14) b.set(rn.nextInt(32), y0 + 8 + rn.nextInt(63 - (y0 + 8)), shadeCol(st, if (rn.nextBoolean()) -0.2f else 0.12f))
        val moss = intArrayOf(h(0xFF4A7A34), h(0xFF3A6A2C), h(0xFF5E8A3A))
        for (i in 0 until 16) b.set(rn.nextInt(32), 59 + rn.nextInt(4), moss[rn.nextInt(3)])
    }

    private fun wallTexture(b: PixBuf, style: Int, rn: java.util.Random) {
        val pl = PL[style]
        when (style) {
            1 -> { // tábuas horizontais
                for (y in 15 until 46) {
                    val k = (y - 15) % 5
                    if (k == 0) b.rect(0, y, 32, 1, shadeCol(pl, -0.2f)) else if (k == 1) b.rect(0, y, 32, 1, shadeCol(pl, 0.12f))
                }
                for (i in 0 until 60) b.set(rn.nextInt(32), 16 + rn.nextInt(29), shadeCol(pl, -0.08f))
            }
            4 -> { // pedra aparelhada
                val mort = shadeCol(pl, -0.4f)
                var y = 15
                var row = 0
                while (y < 46) {
                    val hh = min(8, 46 - y)
                    b.rect(0, y, 32, 1, mort)
                    var x = if (row % 2 == 1) -6 else 0
                    while (x < 32) {
                        val bw = if ((x / 12 + row) % 2 == 0) 12 else 10
                        if (x >= 0) b.rect(x, y, 1, hh, mort)
                        for (k in 0 until bw - 1) b.set(x + 1 + k, y + 1, shadeCol(pl, 0.14f))
                        b.rect(x + bw - 1, y + 1, 1, hh - 1, shadeCol(pl, -0.1f))
                        for (q in 0 until 2) b.set(x + 2 + rn.nextInt(max(1, bw - 3)), y + 2 + rn.nextInt(max(1, hh - 3)), shadeCol(pl, if (rn.nextBoolean()) -0.12f else 0.1f))
                        x += bw
                    }
                    y += 8
                    row++
                }
            }
            2 -> { // reboco de areia com manchas de tijolo
                for (i in 0 until 150) b.set(rn.nextInt(32), 15 + rn.nextInt(31), shadeCol(pl, if (rn.nextBoolean()) 0.06f else -0.08f))
                val patches = arrayOf(intArrayOf(2, 36, 9, 6), intArrayOf(20, 19, 8, 5))
                for (pt in patches) {
                    for (yy in pt[1] until pt[1] + pt[3]) for (xx in pt[0] until pt[0] + pt[2]) {
                        var c = h(0xFFB5683C)
                        if ((yy - pt[1]) % 2 == 1) c = shadeCol(c, -0.18f)
                        else if ((xx - pt[0] + (yy - pt[1]) % 2 * 2) % 4 == 0) c = shadeCol(c, -0.25f)
                        b.set(xx, yy, c)
                    }
                }
            }
            else -> { // reboco liso com rachaduras e manchas
                for (i in 0 until 170) b.set(rn.nextInt(32), 15 + rn.nextInt(31), shadeCol(pl, if (rn.nextBoolean()) 0.05f else -0.07f))
                for (k in 0 until 2) {
                    var x = 3 + rn.nextInt(26)
                    val y = 18 + rn.nextInt(8)
                    for (j in 0 until 10) { b.set(x, y + j, shadeCol(pl, -0.18f)); x += rn.nextInt(3) - 1 }
                }
                for (k in 0 until 3) {
                    val x = 4 + rn.nextInt(24)
                    for (y in 36 until 46) if (rn.nextFloat() < 0.5f) b.set(x, y, shadeCol(pl, -0.09f))
                }
            }
        }
    }

    /** Parede medieval: base de alvenaria, trave de madeira, reboco em cima e uma seteira de pedra. */
    private fun plainWall(b: PixBuf, style: Int, rn: java.util.Random) {
        val bm = BM[style]
        val st = ST[style]
        if (style != 4) {
            val mort = shadeCol(st, -0.42f)
            for (row in 0 until 2) {
                val y = 33 + row * 7
                val rh = if (row == 0) 7 else 6
                b.rect(0, y, 32, rh, st)
                b.rect(0, y, 32, 1, mort)
                var x = if (row == 1) -5 else 0
                while (x < 32) {
                    val bw = if ((x / 11 + row) % 2 == 0) 11 else 9
                    if (x >= 0) b.rect(x, y, 1, rh, mort)
                    for (k in 0 until bw - 1) b.set(x + 1 + k, y + 1, shadeCol(st, 0.18f))
                    b.rect(x + bw - 1, y + 1, 1, 5, shadeCol(st, -0.12f))
                    for (q in 0 until 2) {
                        val px = min(31, max(0, x + 2 + rn.nextInt(bw - 3)))
                        b.set(px, y + 2 + rn.nextInt(4), shadeCol(st, if (rn.nextBoolean()) -0.15f else 0.12f))
                    }
                    x += bw
                }
            }
            b.rect(0, 30, 32, 3, bm); b.rect(0, 30, 32, 1, shadeCol(bm, 0.3f)); b.rect(0, 32, 32, 1, shadeCol(bm, -0.4f))
        }
        val sx = 15
        val top = 17
        val bot = if (style != 4) 29 else 33
        val sd = shadeCol(st, -0.25f)
        val sl = shadeCol(st, 0.25f)
        b.rect(sx - 3, top - 2, 8, bot - top + 5, st)
        b.rect(sx - 3, top - 2, 8, 1, sl); b.rect(sx - 3, top - 2, 1, bot - top + 5, sl)
        b.rect(sx + 4, top - 2, 1, bot - top + 5, sd); b.rect(sx - 3, bot + 2, 8, 1, sd)
        val slit = h(0xFF14121C)
        b.rect(sx, top, 2, bot - top + 1, slit)
        b.rect(sx - 1, top + (bot - top) / 2, 4, 2, slit)
        b.set(sx, top, h(0xFF2A2840)); b.set(sx + 1, bot, h(0xFF2A2840))
        var y = top
        while (y < bot) { b.set(sx + 1, y, h(0xFF22203A)); y += 2 }
    }

    /** Hera subindo pela parede, com florzinhas. */
    private fun ivy(b: PixBuf, rn: java.util.Random) {
        val gr = intArrayOf(h(0xFF2F7A3A), h(0xFF3F9A4A), h(0xFF58B85A), h(0xFF245E2E))
        for (i in 0 until 150) {
            val x = 4 + rn.nextInt(28)
            val y = 16 + rn.nextInt(30)
            val p = 0.12f + (y - 16) / 45f * 0.5f + (x - 4) / 60f
            if (rn.nextFloat() < p) b.rect(x, y, 2, 2, gr[rn.nextInt(4)])
        }
        for (k in 0 until 3) {
            val x = 14 + k * 7
            for (y in 18 + k * 4 until 46) if (rn.nextFloat() < 0.7f) b.set(x + (if (rn.nextInt(3) == 2) 1 else 0), y, gr[3])
        }
        val fl = intArrayOf(h(0xFFFF8FB7), h(0xFFF5D547), h(0xFFFFFFFF))
        for (i in 0 until 7) b.set(14 + rn.nextInt(17), 24 + rn.nextInt(20), fl[rn.nextInt(3)])
    }

    /** Postes de madeira nas laterais de cada pano de parede (estrutura de enxaimel). */
    private fun posts(b: PixBuf, style: Int) {
        val bm = BM[style]
        b.rect(0, 15, 3, 31, bm); b.rect(0, 15, 1, 31, shadeCol(bm, 0.3f)); b.rect(2, 15, 1, 31, shadeCol(bm, -0.4f))
        b.rect(29, 15, 3, 31, bm); b.rect(29, 15, 1, 31, shadeCol(bm, 0.3f)); b.rect(31, 15, 1, 31, shadeCol(bm, -0.4f))
    }

    private fun windowTall(b: PixBuf, style: Int) {
        val ytop = 15
        val wx0 = 7
        val wx1 = 24
        val ybot = 39
        val stone = shadeCol(ST[style], 0.25f)
        val stoneDk = shadeCol(ST[style], -0.2f)
        val fr = h(0xFF4A2F18)
        val r = (wx1 - wx0 + 1) / 2f
        val cx = wx0 + r
        val ycy = ytop + r
        fun ins(x: Int, y: Int, rr: Float): Boolean {
            return if (y < ycy) {
                val dx = x + 0.5f - cx
                val dy = y + 0.5f - ycy
                dx * dx + dy * dy <= rr * rr
            } else (x + 0.5f >= cx - rr && x + 0.5f <= cx + rr && y <= ybot)
        }
        val sh = SH[style]
        for (sx0 in intArrayOf(2, 25)) {
            for (y in ytop + 4..ybot) for (x in sx0 until sx0 + 5) {
                var c = sh
                if ((y - ytop) % 3 == 0) c = shadeCol(sh, -0.32f) else if ((y - ytop) % 3 == 1) c = shadeCol(sh, 0.12f)
                if (x == sx0) c = shadeCol(c, 0.2f)
                if (x == sx0 + 4) c = shadeCol(c, -0.35f)
                b.set(x, y, c)
            }
            for (y in intArrayOf(ytop + 6, ybot - 2)) b.rect(sx0, y, 5, 1, h(0xFF2A2A32))
        }
        for (y in ytop - 3..ybot) {
            for (x in wx0 - 3..wx1 + 3) {
                if (ins(x, y, r + 2.6f) && !ins(x, y, r + 0.6f)) {
                    var c = stone
                    if (y < ycy) {
                        val ang = Math.toDegrees(atan2(x + 0.5 - cx, -(y + 0.5 - ycy).toDouble()))
                        if ((ang + 90.0) % 18.0 < 1.8) c = stoneDk
                    } else {
                        if (x == wx0 - 2) c = shadeCol(stone, 0.1f)
                        if (y % 5 == 0) c = stoneDk
                    }
                    b.set(x, y, c)
                } else if (ins(x, y, r + 0.6f) && !ins(x, y, r - 0.4f)) b.set(x, y, fr)
                else if (ins(x, y, r - 0.4f)) {
                    val t = (y - ytop) / (ybot - ytop).toFloat()
                    b.set(x, y, mixCol(h(0xFFCFEBF8), h(0xFF7FB4D6), t))
                }
            }
        }
        val kx = cx.toInt()
        b.rect(kx - 2, ytop - 4, 4, 4, shadeCol(stone, 0.15f)); b.rect(kx - 2, ytop - 4, 4, 1, h(0xFFFFFFFF)); b.rect(kx - 2, ytop - 1, 4, 1, stoneDk)
        val cur = CURT[style]
        for (y in ycy.toInt() + 1 until ybot - 4) for (k in 0 until 4) {
            val cc = if (k % 2 == 0) cur else shadeCol(cur, -0.25f)
            if (ins(wx0 + 1 + k, y, r - 1f)) b.set(wx0 + 1 + k, y, cc)
            if (ins(wx1 - k, y, r - 1f)) b.set(wx1 - k, y, cc)
        }
        for (k in 0 until 7) {
            val x = (cx - 4 + k).toInt()
            val y = (ytop + 6 + (7 - k) * 1.5f).toInt()
            if (ins(x, y, r - 1.5f)) { b.set(x, y, h(0xFFEAF7FF)); b.set(x + 1, y, h(0xFFEAF7FF)) }
        }
        for (y in ytop until ybot) if (ins(kx, y, r - 0.4f)) { b.set(kx - 1, y, fr); b.set(kx, y, fr) }
        val my = (ycy + 4).toInt()
        for (x in wx0..wx1) if (ins(x, my, r - 0.4f)) { b.set(x, my, fr); b.set(x, my + 1, fr) }
        val ay = (ycy - 3).toInt()
        for (x in wx0..wx1) if (ins(x, ay, r - 0.4f)) b.set(x, ay, fr)
        b.rect(wx0 - 4, ybot + 1, wx1 - wx0 + 9, 2, h(0xFFD0D0DA))
        b.rect(wx0 - 4, ybot + 1, wx1 - wx0 + 9, 1, h(0xFFF2F2F8))
        b.rect(wx0 - 4, ybot + 3, wx1 - wx0 + 9, 1, 0x66000000)
        for (bx in intArrayOf(wx0 - 2, wx1)) { b.rect(bx, ybot + 3, 2, 3, stone); b.rect(bx, ybot + 5, 2, 1, stoneDk) }
        val wd = h(0xFF8A5A2B)
        val bw = wx1 - wx0 + 3
        b.rect(wx0 - 1, ybot + 3, bw, 5, wd); b.rect(wx0 - 1, ybot + 3, bw, 1, h(0xFFB07A44)); b.rect(wx0 - 1, ybot + 7, bw, 1, h(0xFF4A2F18))
        var px = wx0 + 2
        while (px < wx1) { b.rect(px, ybot + 4, 1, 3, h(0xFF5A3A1E)); px += 5 }
        val fc = intArrayOf(h(0xFFE8505B), h(0xFFFF8FB7), h(0xFFF5D547), h(0xFFFFFFFF), h(0xFFB06AE0))
        val rn = java.util.Random(style * 5L + 1)
        for (k in 0 until wx1 - wx0 + 2) {
            val xx = wx0 - 1 + k
            b.set(xx, ybot + 2, if (k % 2 == 0) h(0xFF3F9A4A) else h(0xFF2F7A3A))
            if (k % 2 == 0) {
                b.set(xx, ybot + 1, fc[rn.nextInt(5)])
                if (rn.nextFloat() < 0.6f) b.set(xx, ybot, fc[rn.nextInt(5)])
            }
        }
        for (k in 0 until 5) {
            b.set(wx0 + k * 4, ybot + 8, h(0xFF3F9A4A))
            if (k % 2 == 0) b.set(wx0 + k * 4, ybot + 9, h(0xFF2F7A3A))
        }
    }

    private fun lanternTall(b: PixBuf, style: Int, frame: Int) {
        val iron = h(0xFF26262E)
        for (y in 12 until 46) {
            for (x in 3 until 29) {
                val d = hypot(x - 23.0, y - 27.0)
                if (d < 11.0) {
                    val c = b.get(x, y)
                    if (c != 0) b.set(x, y, shadeCol(c, (0.16 * (1 - d / 11.0)).toFloat()))
                }
            }
        }
        b.rect(13, 16, 4, 7, iron); b.rect(13, 16, 1, 7, h(0xFF4A4A56))
        b.rect(16, 19, 8, 2, iron); b.set(24, 21, iron); b.set(24, 22, iron)
        b.rect(23, 22, 1, 2, iron)
        b.rect(20, 24, 7, 2, iron); b.rect(21, 23, 5, 1, iron)
        val gl = if (frame == 0) h(0xFFFFE066) else h(0xFFFFD14A)
        b.rect(21, 26, 5, 9, h(0xFFB8741A))
        b.rect(22, 27, 3, 7, gl)
        b.rect(23, 28, 1, 5, h(0xFFFFFFE0))
        b.set(21, 26, iron); b.set(25, 26, iron)
        b.rect(20, 35, 7, 2, iron); b.set(23, 37, iron)
        for (x in intArrayOf(21, 25)) b.rect(x, 26, 1, 9, iron)
        for (k in 0 until 5) b.set(21 + k, 19 - k / 2, shadeCol(PL[style], -0.2f))
    }

    /** Uma coluna da fachada, 32x64 (2 quadrados de altura). kind: 0 liso com cruz, 1 janela, 2 lanterna, 3 hera e barril.
     *  edge: 1 canto esquerdo, 2 canto direito. */
    fun facade(style: Int, kind: Int, edge: Int, frame: Int): PixBuf {
        val b = PixBuf(32, 64)
        val pl = PL[style]
        val bm = BM[style]
        val st = ST[style]
        val rn = java.util.Random(1234L + style * 31 + kind * 13 + edge * 7)
        b.rect(0, 0, 32, 64, pl)
        wallTexture(b, style, rn)
        val sh = floatArrayOf(-0.5f, -0.42f, -0.36f, -0.3f, -0.25f, -0.2f, -0.16f, -0.12f, -0.08f, -0.05f, -0.03f, -0.015f)
        for (y in sh.indices) b.rect(0, y, 32, 1, shadeCol(pl, sh[y]))
        b.rect(0, 0, 32, 2, shadeCol(bm, -0.45f)); b.rect(0, 2, 32, 3, bm); b.rect(0, 2, 32, 1, shadeCol(bm, 0.28f)); b.rect(0, 5, 32, 1, shadeCol(bm, -0.3f))
        for (bx in intArrayOf(4, 24)) {
            b.rect(bx, 6, 4, 5, bm); b.rect(bx, 6, 1, 5, shadeCol(bm, 0.25f)); b.rect(bx + 1, 11, 2, 1, shadeCol(bm, -0.3f))
        }
        b.rect(0, 12, 32, 3, bm); b.rect(0, 12, 32, 1, shadeCol(bm, 0.3f)); b.rect(0, 14, 32, 1, shadeCol(bm, -0.35f))
        b.rect(0, 45, 32, 1, shadeCol(pl, -0.25f))
        b.rect(0, 46, 32, 3, bm); b.rect(0, 46, 32, 1, shadeCol(bm, 0.3f)); b.rect(0, 48, 32, 1, shadeCol(bm, -0.4f))
        b.rect(3, 15, 26, 1, shadeCol(pl, -0.15f))
        if (kind != 4) posts(b, style)
        when (kind) {
            0 -> plainWall(b, style, rn)
            1 -> windowTall(b, style)
            2 -> lanternTall(b, style, frame)
            3 -> ivy(b, rn)
            else -> {}
        }
        plinth(b, 49, style, rn)
        if (kind == 3) {
            for (y in 36 until 55) {
                val hw = 5.5 + 1.6 * sin(PI * (y - 36) / 18.0)
                for (x in (9.5 - hw).toInt()..(9.5 + hw).toInt()) {
                    var c = h(0xFF8A5A2B)
                    if (x % 4 == 0) c = h(0xFF6B4423)
                    if (x < (9.5 - hw).toInt() + 2) c = h(0xFFB07A44)
                    if (x > (9.5 + hw).toInt() - 2) c = h(0xFF5A3A1E)
                    b.set(x, y, c)
                }
            }
            for (y in intArrayOf(40, 46, 51)) {
                val hwid = 5.5 + 1.6 * sin(PI * (y - 36) / 18.0)
                b.rect((9.5 - hwid).toInt(), y, (hwid * 2).toInt() + 1, 1, h(0xFF3A3A44))
            }
            b.rect(4, 34, 12, 3, h(0xFF4A2F18))
            val fc = intArrayOf(h(0xFFE8505B), h(0xFFFF8FB7), h(0xFFF5D547), h(0xFF58B85A))
            for (k in 0 until 14) b.set(4 + rn.nextInt(12), 30 + rn.nextInt(5), fc[rn.nextInt(4)])
            for (k in 0 until 8) b.set(5 + rn.nextInt(10), 33 + rn.nextInt(2), h(0xFF3F9A4A))
            b.rect(3, 55, 14, 2, 0x55000000)
        }
        if (edge == 1) {
            b.rect(0, 12, 5, 37, bm); b.rect(0, 12, 1, 37, shadeCol(bm, 0.3f)); b.rect(4, 12, 1, 37, shadeCol(bm, -0.4f))
            b.rect(0, 49, 6, 15, shadeCol(st, 0.1f)); b.rect(0, 49, 6, 1, shadeCol(st, 0.4f))
            var y = 52
            while (y < 63) { b.rect(0, y, 6, 1, shadeCol(st, -0.42f)); y += 5 }
        } else if (edge == 2) {
            b.rect(27, 12, 5, 37, bm); b.rect(27, 12, 1, 37, shadeCol(bm, 0.3f)); b.rect(31, 12, 1, 37, shadeCol(bm, -0.4f))
            b.rect(26, 49, 6, 15, shadeCol(st, -0.05f)); b.rect(26, 49, 6, 1, shadeCol(st, 0.3f))
            var y = 52
            while (y < 63) { b.rect(26, y, 6, 1, shadeCol(st, -0.42f)); y += 5 }
        } else {
            b.rect(0, 15, 3, 31, bm); b.rect(0, 15, 1, 31, shadeCol(bm, 0.3f)); b.rect(2, 15, 1, 31, shadeCol(bm, -0.35f))
        }
        return b
    }

    // ------------------------------------------------------------------ portas
    // Cada casa tem a sua porta (tipo 0..5): 0 portal arqueado do mago, 1 portão de ferraria, 2 porta azul com cornija,
    // 3 porta de taverna com toldo, 4 porta redonda do alquimista, 5 porta rústica do chalé.
    const val DOOR_KINDS = 6
    private val DS_SHAPE = intArrayOf(2, 0, 0, 1, 3, 1)       // 0 reta, 1 arco, 2 ogiva, 3 círculo
    private val DS_LO = intArrayOf(15, 10, 20, 11, 19, 23)
    private val DS_HI = intArrayOf(48, 53, 43, 52, 44, 40)
    private val DS_TOP = intArrayOf(10, 28, 20, 24, 0, 30)
    private val DS_DBL = booleanArrayOf(true, true, false, true, false, false)
    private val DS_STONE = intArrayOf(h(0xFF8A8498), h(0xFF6E6E78), h(0xFFF2EBDD), h(0xFFA8987E), h(0xFF8E9A7A), h(0xFF9A9486))
    private val DS_WOOD = intArrayOf(h(0xFF45315F), h(0xFF4A3220), h(0xFF4A78B8), h(0xFF8A3A2A), h(0xFF4F8F4A), h(0xFF9A6A3A))
    private const val DBOT = 60

    private fun doorIn(kind: Int, x: Int, y: Int, g: Int): Boolean {
        val shape = DS_SHAPE[kind]
        val lo = DS_LO[kind]
        val hi = DS_HI[kind]
        val top = DS_TOP[kind]
        val fx = x + 0.5f
        val fy = y + 0.5f
        val wd = (hi - lo + 1).toFloat()
        val cx = (lo + hi + 1) / 2f
        when (shape) {
            0 -> return x >= lo - g && x <= hi + g && y >= top - g && y <= DBOT
            1 -> {
                val r = wd / 2f + g
                val cyc = top + wd / 2f
                return if (fy < cyc) (fx - cx) * (fx - cx) + (fy - cyc) * (fy - cyc) <= r * r
                else abs(fx - cx) <= r && y <= DBOT
            }
            2 -> {
                val half = wd / 2f + g
                val spring = top + wd * 0.62f
                val tp = (top - g).toFloat()
                if (fy >= spring) return abs(fx - cx) <= half && y <= DBOT
                if (fy < tp) return false
                val t = (spring - fy) / (spring - tp)
                return abs(fx - cx) <= half * (1f - Math.pow(t.toDouble(), 1.9).toFloat())
            }
            else -> {
                val r = wd / 2f + g
                val cyc = DBOT + 1 - wd / 2f
                return (fx - cx) * (fx - cx) + (fy - cyc) * (fy - cyc) <= r * r
            }
        }
    }

    /** Cor da folha da porta no ponto (lx, ly): lx conta a partir da dobradiça, ly a partir do topo do vão. */
    private fun doorLeaf(kind: Int, lx: Int, ly: Int, wl: Int, hh: Int): Int {
        val wood = DS_WOOD[kind]
        val gold = GOLD
        val goldHi = h(0xFFF4D878)
        val iron = h(0xFF2E2E38)
        val ironHi = h(0xFF8A8A98)
        var c = wood
        when (kind) {
            0 -> {
                c = if (lx % 4 == 3) shadeCol(wood, -0.28f) else if (lx % 4 == 0) shadeCol(wood, 0.1f) else wood
                if (lx == 2) c = gold else if (lx == 3 || lx == wl - 3) c = shadeCol(gold, -0.3f)
                val dd = hypot(lx + 0.5 - wl, ly + 0.5 - hh * 0.5)
                if (dd >= 8.0 && dd < 9.6) c = gold
                if (dd < 2.4) c = goldHi
                if (dd > 2.4 && dd < 6.5 && (abs(lx + 0.5 - wl) < 0.9 || abs(ly + 0.5 - hh * 0.5) < 0.9)) c = gold
                if (abs(ly + 0.5 - hh * 0.5) > 13 && (lx + ly) % 5 == 0 && lx < wl - 3) c = shadeCol(gold, -0.1f)
                if (ly > hh - 6) c = if (lx % 4 != 0) shadeCol(wood, -0.35f) else shadeCol(wood, -0.2f)
                val ky = (hh * 0.62f).toInt()
                if (abs(lx - (wl - 5)) <= 1 && abs(ly - ky) <= 1) c = if (lx == wl - 5 && ly == ky) goldHi else gold
            }
            1 -> {
                c = if (lx % 6 == 5) shadeCol(wood, -0.35f) else if (lx % 6 == 0) shadeCol(wood, 0.1f) else wood
                for (sy in intArrayOf((hh * 0.2f).toInt(), (hh * 0.52f).toInt(), (hh * 0.82f).toInt())) {
                    if (ly >= sy && ly <= sy + 2) {
                        c = if (ly != sy) iron else shadeCol(iron, 0.15f)
                        if (lx % 7 == 3 && ly == sy + 1) c = ironHi
                    }
                }
                if (lx == wl - 1) c = shadeCol(wood, -0.3f)
                val hx = wl - 6.0
                val hy = (hh * 0.52f).toInt() + 7.0
                val dd = hypot(lx + 0.5 - hx, ly + 0.5 - hy)
                if (dd >= 3.0 && dd < 4.5) c = iron
                if (dd < 1.4) c = ironHi
            }
            2 -> {
                if (ly < 11) {
                    c = mixCol(h(0xFF9AD4F0), h(0xFFDCF2FB), ly / 11f)
                    if (lx % 7 == 3 || ly == 5) c = shadeCol(wood, -0.2f)
                    if (lx < 2 || lx >= wl - 2) c = shadeCol(wood, 0.1f)
                } else if (ly == 11) c = shadeCol(wood, 0.2f)
                else if (ly == 12) c = shadeCol(wood, -0.3f)
                else {
                    for (k in 0..1) {
                        val p0 = if (k == 0) 15 else 30
                        val p1 = if (k == 0) 27 else hh - 5
                        if (lx >= 4 && lx <= wl - 5 && ly >= p0 && ly <= p1) {
                            c = if (lx == 4 || ly == p0) shadeCol(wood, -0.3f)
                            else if (lx == wl - 5 || ly == p1) shadeCol(wood, 0.28f)
                            else shadeCol(wood, 0.07f)
                        }
                    }
                    if (ly >= hh - 3) c = shadeCol(wood, -0.25f)
                }
                val ky = (hh * 0.56f).toInt()
                if (abs(lx - (wl - 4)) <= 1 && abs(ly - ky) <= 1) c = if (lx == wl - 4 && ly == ky) goldHi else gold
            }
            3 -> {
                c = if (lx % 5 == 4) shadeCol(wood, -0.3f) else if (lx % 5 == 0) shadeCol(wood, 0.1f) else wood
                if (ly in 3 until 15 && lx >= 3 && lx <= wl - 3) {
                    c = mixCol(h(0xFFF6C860), h(0xFFE08A30), ly / 15f)
                    if (lx == wl / 2 || ly == 9) c = shadeCol(wood, -0.45f)
                    if (lx == 3 || ly == 3 || lx == wl - 3) c = shadeCol(wood, -0.4f)
                }
                if (ly > hh - 8) {
                    c = if (ly > hh - 3) shadeCol(h(0xFFB88A3A), -0.2f) else h(0xFFC89A48)
                    if (ly == hh - 7) c = goldHi
                }
                if (lx >= wl - 8 && lx <= wl - 3 && ly in 24..34)
                    c = if (lx == wl - 8 || lx == wl - 3 || ly == 24 || ly == 34) gold else shadeCol(wood, -0.1f)
                if ((lx == wl - 5 || lx == wl - 4) && ly in 36..47) c = if (lx == wl - 5) goldHi else gold
            }
            4 -> {
                c = if (lx % 5 == 4) shadeCol(wood, -0.3f) else if (lx % 5 == 0) shadeCol(wood, 0.1f) else wood
                for (sy in intArrayOf((hh * 0.28f).toInt(), (hh * 0.68f).toInt())) {
                    if (lx <= 10 && ly >= sy && ly <= sy + 1) c = gold
                }
                val dd = hypot(lx + 0.5 - (wl - 6.5), ly + 0.5 - hh * 0.5)
                if (dd < 3.0) c = if (dd < 1.6) goldHi else if (dd >= 2.2) shadeCol(gold, -0.35f) else gold
            }
            else -> {
                c = if (lx % 5 == 4) shadeCol(wood, -0.3f) else if (lx % 5 == 0) shadeCol(wood, 0.12f) else wood
                for (sy in intArrayOf((hh * 0.22f).toInt(), (hh * 0.68f).toInt())) {
                    if (lx <= wl * 0.75f && ly >= sy && ly <= sy + 1) c = iron
                    if (lx <= wl * 0.75f && ly == sy && lx % 6 == 2) c = ironHi
                    if (abs(lx - wl * 0.75f) <= 1f && ly >= sy - 1 && ly <= sy + 2) c = iron
                }
                if (lx >= 5 && lx <= wl - 6 && ly in 6..11)
                    c = if (lx == (wl - 1) / 2 || ly == 8) shadeCol(wood, -0.2f) else h(0xFF2E2230)
                val dd = hypot(lx + 0.5 - (wl - 5.0), ly + 0.5 - (hh * 0.58f).toInt())
                if (dd >= 1.6 && dd < 3.0) c = iron
                if (dd < 1.2) c = ironHi
            }
        }
        return c
    }

    /** Porta da casa (64x64 = 2 quadrados). Cada [kind] tem forma, tamanho e enfeites próprios. frame 0 fechada ... 4 aberta.
     *  O vão aberto fica transparente com sombra para você ver o saguão. */
    fun doorBig(style: Int, kind: Int, frame: Int): PixBuf {
        val big = PixBuf(64, 64)
        for (c in 0..1) big.blit(facade(style, 4, 0, 0), c * 32, 0)
        val k = kind % DOOR_KINDS
        val lo = DS_LO[k]
        val hi = DS_HI[k]
        val top = DS_TOP[k]
        val wd = hi - lo + 1
        val cx = (lo + hi + 1) / 2f
        val o = frame / 4f
        val dbl = DS_DBL[k]
        val stone = DS_STONE[k]
        val mort = shadeCol(stone, -0.42f)
        val outline = h(0xFF2A2230)
        val gold = GOLD
        val goldHi = h(0xFFF4D878)
        val iron = h(0xFF2E2E38)
        val ironHi = h(0xFF8A8A98)
        val shape = DS_SHAPE[k]
        val ringY = if (shape == 2) top + wd * 0.62f else if (shape == 1) top + wd / 2f else if (shape == 3) DBOT + 1 - wd / 2f else -1000f
        // moldura de pedra
        for (y in 0 until 64) {
            for (x in 0 until 64) {
                if (doorIn(k, x, y, 4) && !doorIn(k, x, y, 3) && y <= DBOT + 1) big.set(x, y, outline)
                if (doorIn(k, x, y, 3) && !doorIn(k, x, y, 0)) {
                    var c = stone
                    if (k == 2) {
                        if (!doorIn(k, x, y, 2)) c = shadeCol(stone, -0.08f)
                        if (doorIn(k, x, y, 1)) c = shadeCol(stone, -0.25f)
                    } else {
                        if (!doorIn(k, x, y, 2)) c = shadeCol(stone, 0.2f)
                        else if (doorIn(k, x, y, 1)) c = shadeCol(stone, -0.2f)
                        if (y < ringY) {
                            val ang = Math.toDegrees(atan2(x + 0.5 - cx, -(y + 0.5 - ringY).toDouble()))
                            if (((ang + 90.0) % 16.0) < 1.6) c = mort
                        } else if (y % 6 == 0 || (x + (y / 6) * 3) % 7 == 0) c = mort
                    }
                    big.set(x, y, c)
                }
            }
        }
        // soleira
        for (y in DBOT + 1..62) for (x in lo - 3..hi + 3) big.set(x, y, if (y == DBOT + 1) shadeCol(stone, 0.1f) else shadeCol(stone, -0.2f))
        // folhas
        val wl0 = if (dbl) wd / 2 else wd
        val wl = Math.round(wl0 * (1f - 0.86f * o))
        val hh = DBOT - top + 1
        for (y in 0 until 64) {
            for (x in 0 until 64) {
                if (!doorIn(k, x, y, 0)) continue
                val ly = y - top
                var lx = -1
                var edge = false
                if (dbl) {
                    if (x < lo + wl) { lx = x - lo; edge = x == lo + wl - 1 }
                    else if (x > hi - wl) { lx = hi - x; edge = x == hi - wl + 1 }
                } else if (x < lo + wl) { lx = x - lo; edge = x == lo + wl - 1 }
                if (lx >= 0) {
                    var c = doorLeaf(k, lx, ly, wl0, hh)
                    if (edge) c = h(0xFF2A1A0E)
                    if (o > 0f) c = shadeCol(c, -0.12f * o)
                    big.set(x, y, c)
                } else {
                    val a = (0xA0 * (1f - (y - 8) / 52f) + 0x30).toInt().coerceIn(0, 255)
                    big.set(x, y, a shl 24)
                }
            }
        }
        // enfeites
        val ix = cx.toInt()
        when (k) {
            0 -> {
                big.rect(ix - 2, top, 4, 3, goldHi); big.rect(ix - 1, top + 3, 2, 1, gold); big.set(ix, top - 3, goldHi)
                big.set(ix - 1, top - 3, gold); big.set(ix + 1, top - 3, gold)
            }
            1 -> {
                big.rect(lo - 4, top - 6, wd + 8, 4, h(0xFF5A3A22)); big.rect(lo - 4, top - 6, wd + 8, 1, h(0xFF7A5230)); big.rect(lo - 4, top - 3, wd + 8, 1, h(0xFF3A2410))
                big.rect(lo - 3, top - 6, 2, 4, iron); big.rect(hi + 1, top - 6, 2, 4, iron)
                val pts = intArrayOf(-3, 0, -3, 1, -3, 2, -2, 3, -1, 3, 0, 3, 1, 3, 2, 3, 3, 2, 3, 1, 3, 0)
                var i = 0
                while (i < pts.size) {
                    val dy = pts[i + 1]
                    big.set(ix + pts[i], top - 15 + dy, if (dy < 2) h(0xFFB8B8C4) else h(0xFF8A8A96))
                    i += 2
                }
            }
            2 -> {
                for (i in 0 until 8) {
                    big.rect(lo - 1 - i, top - 10 + i, wd + 2 + 2 * i, 1, shadeCol(stone, if (i == 0) 0.1f else if (i < 7) -0.05f else -0.3f))
                    big.set(lo - 1 - i, top - 10 + i, outline); big.set(hi + 1 + i, top - 10 + i, outline)
                }
                big.rect(lo + 3, DBOT + 1, wd - 6, 2, h(0xFFB8602E)); big.rect(lo + 3, DBOT + 1, wd - 6, 1, h(0xFFD87A3E))
            }
            3 -> {
                val yt = top - 8
                for (y in yt until top + 4) {
                    val inset = max(0, (yt + 3 - y) * 2)
                    for (x in lo - 5 + inset..hi + 5 - inset) {
                        var c = if (((x - lo + 5) / 4) % 2 == 0) h(0xFFB8372F) else h(0xFFF2E6CC)
                        if (y == yt) c = shadeCol(c, 0.25f)
                        if (y >= top + 1) {
                            val kk = (x - lo + 5) % 4
                            val bump = if (kk == 1 || kk == 2) top + 3 else top + 2
                            if (y > bump) continue
                            if (y == bump) c = shadeCol(c, -0.25f)
                        }
                        big.set(x, y, c)
                    }
                }
            }
            4 -> {
                val rn = java.util.Random(7L)
                val cyc = DBOT + 1 - wd / 2f
                val leaf = intArrayOf(h(0xFF3E7A3A), h(0xFF5AA050), h(0xFF2E5E2E), h(0xFF7AC060))
                for (i in 0 until 34) {
                    val ang = Math.toRadians(160.0 + rn.nextDouble() * 170.0)
                    val r = wd / 2.0 + 3 + (rn.nextDouble() * 2.5 - 1.0)
                    val x = (cx + cos(ang) * r).toInt()
                    val y = (cyc + sin(ang) * r).toInt()
                    val cc = leaf[rn.nextInt(4)]
                    if (y <= DBOT - 8) big.set(x, y, cc)
                }
                big.set(lo - 2, DBOT - 8, leaf[0]); big.set(lo - 3, DBOT - 5, leaf[0]); big.set(lo - 2, DBOT - 2, leaf[0])
            }
            else -> {
                val px0 = lo - 9
                big.rect(px0, DBOT - 6, 6, 7, h(0xFFB8602E)); big.rect(px0, DBOT - 6, 6, 1, h(0xFFD87A3E)); big.rect(px0 + 1, DBOT, 4, 1, h(0xFF8A4420))
                big.set(px0 + 1, DBOT - 13, h(0xFF3E7A3A)); big.set(px0 + 2, DBOT - 14, h(0xFFD84A5A)); big.set(px0 + 3, DBOT - 13, h(0xFF3E7A3A))
                big.set(px0 + 4, DBOT - 14, h(0xFFF4D060)); big.set(px0 + 2, DBOT - 12, h(0xFF3E7A3A)); big.set(px0 + 3, DBOT - 12, h(0xFF3E7A3A)); big.set(px0, DBOT - 12, h(0xFFD84A5A))
                big.rect(hi + 5, DBOT - 3, 5, 4, h(0xFF7A7A82)); big.rect(hi + 5, DBOT - 3, 5, 1, h(0xFF9A9AA4))
                big.set(hi + 6, DBOT - 4, h(0xFF3E7A3A)); big.set(hi + 8, DBOT - 4, h(0xFF4E9A4A))
            }
        }
        return big
    }

    /** Recorta um quadrado 32x32 (ou 32x64) de uma imagem maior. */
    fun crop(src: PixBuf, ox: Int, oy: Int, cw: Int, ch: Int): PixBuf {
        val t = PixBuf(cw, ch)
        for (y in 0 until ch) for (x in 0 until cw) t.px[y * cw + x] = src.get(ox + x, oy + y)
        return t
    }

    // ------------------------------------------------------------------ interior

    /** Parede de trás vista de dentro, 32x64. kind: 0 lisa, 1 janela com cortinas, 2 arandela, 3 quadro. */
    fun backWall(style: Int, kind: Int, edge: Int, frame: Int): PixBuf {
        val b = PixBuf(32, 64)
        val ia = IWA[style]
        val ib = IWB[style]
        val wood = h(0xFF7A5230)
        b.rect(0, 0, 32, 64, ia)
        for (y in 8..40) {
            for (x in 0 until 32) {
                if (x % 8 < 2) b.set(x, y, ib)
                if ((x % 16 == 7 || x % 16 == 8) && ((y - 8) % 14) in 5..8) b.set(x, y, shadeCol(ib, 0.12f))
                if ((x % 16 == 3 || x % 16 == 4 || x % 16 == 11 || x % 16 == 12) && ((y - 8) % 14) in 6..7) b.set(x, y, shadeCol(ia, -0.08f))
            }
        }
        b.rect(0, 0, 32, 4, h(0xFF3A2410)); b.rect(0, 0, 32, 1, h(0xFF5A3A22))
        b.rect(0, 4, 32, 3, h(0xFFB8854A)); b.rect(0, 4, 32, 1, h(0xFFD8A870)); b.rect(0, 6, 32, 1, h(0xFF7A5230))
        val sa = floatArrayOf(-0.28f, -0.2f, -0.12f, -0.06f)
        for (y in sa.indices) b.rect(0, 7 + y, 32, 1, shadeCol(ia, sa[y]))
        b.rect(0, 41, 32, 1, h(0xFF5A3A22)); b.rect(0, 42, 32, 2, h(0xFFB8854A)); b.rect(0, 42, 32, 1, h(0xFFD8A870)); b.rect(0, 44, 32, 1, h(0xFF5A3A22))
        for (y in 38..40) b.rect(0, y, 32, 1, shadeCol(ia, -0.1f - (y - 37) * 0.04f))
        b.rect(0, 45, 32, 19, wood)
        for (px in intArrayOf(1, 17)) {
            b.rect(px, 47, 14, 13, shadeCol(wood, -0.18f)); b.rect(px + 1, 48, 12, 11, wood)
            b.rect(px + 1, 48, 12, 1, shadeCol(wood, 0.22f)); b.rect(px + 1, 48, 1, 11, shadeCol(wood, 0.14f))
            b.rect(px + 1, 58, 12, 1, shadeCol(wood, -0.3f)); b.rect(px + 12, 48, 1, 11, shadeCol(wood, -0.3f))
        }
        b.rect(0, 60, 32, 4, h(0xFF4A2F18)); b.rect(0, 60, 32, 1, h(0xFF7A5230))
        when (kind) {
            1 -> {
                val fr = h(0xFF8A5A2B)
                val r = 8.5f
                val cx = 16f
                val ycy = 19.5f
                fun inside(x: Int, y: Int, rr: Float): Boolean {
                    return if (y < ycy) {
                        val dx = x + 0.5f - cx
                        val dy = y + 0.5f - ycy
                        dx * dx + dy * dy <= rr * rr
                    } else (x + 0.5f >= cx - rr && x + 0.5f <= cx + rr && y <= 37)
                }
                for (y in 9..38) for (x in 4..28) {
                    if (inside(x, y, r + 2f)) b.set(x, y, h(0xFF3A2410))
                    if (inside(x, y, r + 1.1f) && !inside(x, y, r - 0.2f)) b.set(x, y, fr)
                    if (inside(x, y, r - 0.3f)) b.set(x, y, mixCol(h(0xFF9AD4F0), h(0xFFDCF2FB), (y - 11) / 27f))
                }
                for (x in 8..24) for (y in 25..33) if (inside(x, y, r - 0.3f) && (x + y * 2) % 11 == 0) b.set(x, y, h(0xFFFFFFFF))
                for (y in 11..37) if (inside(16, y, r - 0.3f)) { b.set(15, y, fr); b.set(16, y, fr) }
                for (x in 8..24) if (inside(x, 26, r - 0.3f)) { b.set(x, 25, fr); b.set(x, 26, fr) }
                val cc = CURT[style]
                for (y in 10..34) {
                    val wv = if (y < 28) 4 else 5
                    for (k in 0 until wv) {
                        val c = if (k % 2 == 0) cc else shadeCol(cc, -0.25f)
                        if (y < 30 || k >= 1) { b.set(5 + k, y, c); b.set(26 - k, y, c) }
                    }
                }
                b.rect(4, 10, 24, 2, h(0xFF3A2410)); b.rect(3, 10, 2, 2, GOLD); b.rect(27, 10, 2, 2, GOLD)
                b.rect(8, 29, 2, 2, GOLD); b.rect(22, 29, 2, 2, GOLD)
                b.rect(5, 38, 22, 2, h(0xFFB8854A)); b.rect(5, 38, 22, 1, h(0xFFD8A870))
            }
            2 -> {
                val iron = h(0xFF26262E)
                for (y in 10 until 40) for (x in 3 until 29) {
                    val d = hypot(x - 16.0, y - 22.0)
                    if (d < 12.0) b.set(x, y, shadeCol(b.get(x, y), (0.2 * (1 - d / 12.0)).toFloat()))
                }
                b.rect(12, 24, 8, 2, iron); b.rect(14, 26, 4, 6, iron); b.rect(15, 32, 2, 2, iron)
                b.rect(14, 20, 4, 4, h(0xFFF2E8C8)); b.rect(14, 20, 1, 4, h(0xFFFFFFFF))
                if (frame == 0) b.ellipse(16f, 17f, 1.8f, 2.8f, h(0xFFFF8A2D), false)
                else b.ellipse(16.5f, 16.7f, 1.6f, 3f, h(0xFFFF8A2D), false)
                b.ellipse(16f, 17.6f, 0.9f, 1.6f, h(0xFFFFE066), false)
                b.rect(9, 28, 14, 1, iron)
            }
            3 -> {
                b.rect(7, 13, 18, 22, h(0xFF3A2410)); b.rect(8, 14, 16, 20, GOLD); b.rect(9, 15, 14, 18, h(0xFF3A2410))
                for (y in 16 until 32) for (x in 10 until 22) {
                    val c = if (y < 24) mixCol(h(0xFF8EC8F0), h(0xFFF6E3B0), (y - 16) / 15f) else mixCol(h(0xFF5A8A3A), h(0xFF2F5A2A), (y - 24) / 8f)
                    b.set(x, y, c)
                }
                b.ellipse(17f, 20f, 2.2f, 2.2f, h(0xFFFFE066), false)
                b.poly(floatArrayOf(10f, 16f, 22f), floatArrayOf(31f, 23f, 31f), h(0xFF3F6A3A))
                b.poly(floatArrayOf(14f, 22f, 30f), floatArrayOf(31f, 25f, 31f), h(0xFF2F5A2A))
                b.set(16, 12, h(0xFF3A2410)); b.line(16, 12, 10, 13, h(0xFF3A2410)); b.line(16, 12, 22, 13, h(0xFF3A2410))
            }
        }
        if (edge == 1) { b.rect(0, 0, 3, 64, h(0xFF3A2410)); b.rect(2, 7, 1, 38, shadeCol(ia, -0.3f)) }
        if (edge == 2) { b.rect(29, 0, 3, 64, h(0xFF3A2410)); b.rect(29, 7, 1, 38, shadeCol(ia, -0.3f)) }
        return b
    }

    /** Parede lateral de dentro (32x32). */
    fun sideWall(left: Boolean): PixBuf {
        val b = PixBuf(32, 32)
        val bm = h(0xFF4A2F18)
        val wd = h(0xFF7A5230)
        val rn = java.util.Random(if (left) 100L else 101L)
        b.rect(0, 0, 32, 32, bm)
        for (y in 0 until 32) for (x in 0 until 32) {
            if (y % 8 == 0) b.set(x, y, shadeCol(bm, -0.3f)) else if (y % 8 == 1) b.set(x, y, shadeCol(bm, 0.12f))
        }
        for (i in 0 until 30) b.set(rn.nextInt(32), rn.nextInt(32), shadeCol(bm, -0.15f))
        if (left) {
            b.rect(20, 0, 12, 32, wd); b.rect(20, 0, 1, 32, shadeCol(wd, 0.3f))
            for (y in intArrayOf(0, 16)) { b.rect(22, y + 3, 8, 11, shadeCol(wd, -0.15f)); b.rect(23, y + 4, 6, 9, wd) }
            b.rect(31, 0, 1, 32, shadeCol(wd, -0.45f))
        } else {
            b.rect(0, 0, 12, 32, wd); b.rect(11, 0, 1, 32, shadeCol(wd, -0.4f))
            for (y in intArrayOf(0, 16)) { b.rect(2, y + 3, 8, 11, shadeCol(wd, -0.15f)); b.rect(3, y + 4, 6, 9, wd) }
            b.rect(0, 0, 1, 32, shadeCol(wd, 0.3f))
        }
        return b
    }

    /** Tapete: v = estilo*9 + posição (linha*3 + coluna; 0 = canto/borda de cima-esquerda ... 8 = baixo-direita). */
    fun rug(v: Int): PixBuf {
        val style = (v / 9) % STYLES
        val pos = v % 9
        val r = pos / 3
        val c = pos % 3
        val base = RUG_BASE[style]
        val lite = RUG_LITE[style]
        val dark = RUG_DARK[style]
        val b = PixBuf(32, 32)
        b.rect(0, 0, 32, 32, base)
        for (y in 0 until 32) for (x in 0 until 32) {
            val m = (abs(x - 16) + abs(y - 16)) % 16
            if (m == 0 || m == 8) b.set(x, y, shadeCol(base, 0.12f))
            val dd = abs(x - 16) + abs(y - 16)
            if (dd <= 4) b.set(x, y, lite)
            if (dd == 5) b.set(x, y, dark)
        }
        val top = r == 0
        val bot = r == 2
        val lef = c == 0
        val rig = c == 2
        val depth = intArrayOf(h(0xFF3A0E14), GOLD, GOLD, dark, dark, shadeCol(dark, 0.15f))
        for (d in 0 until 6) {
            val col = depth[d]
            if (top) b.rect(0, d, 32, 1, col)
            if (bot) b.rect(0, 31 - d, 32, 1, col)
            if (lef) b.rect(d, 0, 1, 32, col)
            if (rig) b.rect(31 - d, 0, 1, 32, col)
        }
        return b
    }

    // ------------------------------------------------------------------ telhado

    /** Altura (em pixels) da cumeeira dentro da imagem do telhado. */
    fun ridgeY(hh: Int): Int = 16 + (((hh - 2) * 32 + 14) * 0.40f).toInt()

    /** Posições x (em pixels, na imagem do telhado) das chaminés. */
    fun chimneys(hw: Int, vr: Int): IntArray {
        val w = (hw * 32 + 22).toFloat()
        return when (vr % 3) {
            0 -> intArrayOf((w * 0.60f).toInt())
            1 -> intArrayOf((w * 0.22f).toInt(), (w * 0.80f).toInt())
            else -> intArrayOf((w * 0.35f).toInt(), (w * 0.66f).toInt())
        }
    }

    private fun dormers(vr: Int): FloatArray = when (vr % 3) {
        0 -> floatArrayOf(0.22f, 0.78f)
        1 -> floatArrayOf(0.5f)
        else -> floatArrayOf(0.30f, 0.70f)
    }

    /** Telhado da casa inteira. Tamanho: largura hw*32+22, altura 16 (chaminé) + 14 (beiral) + (hh-2)*32. */
    fun roof(style: Int, hw: Int, hh: Int, vr: Int): PixBuf {
        val ovl = 11
        val mt = 16
        val ov = 14
        val bw = hw * 32 + ovl * 2
        val bodyH = (hh - 2) * 32 + ov
        val hgt = mt + bodyH
        val b = PixBuf(bw, hgt)
        val tones = ROOF[style]
        val yTop = mt
        val yR = ridgeY(hh)
        val rn = java.util.Random(900L + style)
        val fascia = shadeCol(tones[0], -0.5f)
        for (y in yTop until hgt - 1) {
            val back = y < yR
            val rh = if (back) 4 else 5
            val ys = if (back) yTop else yR
            val r = (y - ys) / rh
            val oy = (y - ys) % rh
            val inset = if (back) Math.max(0, 8 - (y - yTop)) else 0
            for (x in (1 + inset)..(bw - 2 - inset)) {
                val cx = x + (r % 2) * 5
                val idx = ((r * 374761393 + (cx / 10) * 668265263 + style * 977) ushr 13) and 3
                var col = tones[idx]
                if (back) col = shadeCol(col, -0.2f) else col = shadeCol(col, 0.05f - 0.13f * (y - yR) / (hgt - yR).toFloat())
                if (oy == rh - 1) col = shadeCol(col, -0.28f)
                else if (oy == 0) col = shadeCol(col, 0.12f)
                if (cx % 10 == 0) col = shadeCol(col, -0.22f)
                if (x <= 2 + inset || x >= bw - 3 - inset) col = fascia
                b.set(x, y, col)
            }
        }
        if (style == 2) {
            for (k in 0 until 70) {
                val mx = 6 + rn.nextInt(bw - 12)
                val my = yR + 4 + rn.nextInt(hgt - yR - 10)
                val mc = if (rn.nextBoolean()) h(0xFF4A7A34) else h(0xFF86B35A)
                b.rect(mx, my, 2 + rn.nextInt(3), 1 + rn.nextInt(2), mc)
            }
        }
        val rc = shadeCol(tones[2], -0.15f)
        for (x in 1..bw - 2) {
            b.set(x, yR - 2, shadeCol(tones[1], 0.28f))
            b.set(x, yR - 1, shadeCol(rc, 0.12f))
            b.set(x, yR, rc)
            b.set(x, yR + 1, shadeCol(rc, -0.3f))
            b.set(x, yR + 2, shadeCol(rc, -0.5f))
        }
        for (x in 1..bw - 2) {
            b.set(x, hgt - 3, shadeCol(tones[2], -0.4f))
            b.set(x, hgt - 2, h(0xFF3A2410))
        }
        val dy0 = yR + 22
        val pl = PL[style]
        val bm = BM[style]
        val gabled = style == 0 || style == 2 || style == 3 || style == 5
        for (fx in dormers(vr)) {
            val dcx = (bw * fx).toInt()
            if (gabled) {
                b.rect(dcx - 11, dy0 + 9, 22, 19, bm)
                b.rect(dcx - 10, dy0 + 10, 20, 17, pl)
                b.rect(dcx - 6, dy0 + 12, 12, 11, h(0xFF4A2F18))
                b.rect(dcx - 5, dy0 + 13, 10, 9, h(0xFF8EC8E8))
                b.rect(dcx - 5, dy0 + 18, 10, 4, h(0xFF6AA8CC))
                b.rect(dcx - 1, dy0 + 13, 2, 9, pl)
                b.rect(dcx - 5, dy0 + 17, 10, 1, pl)
                b.poly(floatArrayOf((dcx - 14).toFloat(), (dcx + 14).toFloat(), dcx.toFloat()), floatArrayOf((dy0 + 10).toFloat(), (dy0 + 10).toFloat(), (dy0 - 5).toFloat()), shadeCol(tones[2], -0.1f))
                for (yy in (dy0 - 4)..(dy0 + 9)) {
                    val half = 14f * (yy - (dy0 - 5)) / 15f
                    for (xx in (dcx - half.toInt())..(dcx + half.toInt())) {
                        if ((yy + xx / 2) % 4 == 0) b.set(xx, yy, shadeCol(tones[2], -0.3f))
                    }
                }
                b.rect(dcx - 14, dy0 + 10, 29, 2, shadeCol(tones[2], -0.5f))
                b.rect(dcx - 11, dy0 + 28, 22, 2, 0x55000000)
            } else {
                b.rect(dcx - 11, dy0 + 6, 22, 16, bm)
                b.rect(dcx - 9, dy0 + 8, 18, 12, h(0xFF8EC8E8))
                for (k in 0 until 5) b.set(dcx - 7 + k * 2, dy0 + 17 - k, h(0xFFD6F0FC))
                b.rect(dcx - 1, dy0 + 8, 2, 12, bm)
                b.rect(dcx - 11, dy0 + 22, 22, 2, 0x55000000)
            }
        }
        for (chx in chimneys(hw, vr)) {
            val st = h(0xFF8E8E98)
            val ct = yR - 34
            val chBottom = yR + 6
            val cw = 15
            b.rect(chx, ct, cw, chBottom - ct, st)
            for (y in ct until chBottom) {
                if ((y - ct) % 4 == 0) b.rect(chx, y, cw, 1, shadeCol(st, -0.28f))
                val off = (((y - ct) / 4) % 2) * 4
                b.set(chx + 4 + off, y, shadeCol(st, -0.28f))
                b.set(chx + 10 + off - (if (off > 0) 4 else 0), y, shadeCol(st, -0.28f))
            }
            b.rect(chx, ct, 1, chBottom - ct, shadeCol(st, 0.22f))
            b.rect(chx + cw - 2, ct, 2, chBottom - ct, shadeCol(st, -0.32f))
            b.rect(chx - 2, ct - 3, cw + 4, 3, shadeCol(st, -0.2f))
            b.rect(chx - 2, ct - 3, cw + 4, 1, shadeCol(st, 0.1f))
            b.rect(chx + 2, ct - 2, cw - 4, 2, h(0xFF1A1210))
        }
        b.outline(h(0xFF2A1A0E))
        return b
    }
}
