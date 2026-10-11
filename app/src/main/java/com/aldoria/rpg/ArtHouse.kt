package com.aldoria.rpg

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
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

    private fun windowTall(b: PixBuf, style: Int) {
        val ytop = 15
        val wx0 = 6
        val wx1 = 25
        val fr = h(0xFF4A2F18)
        val hi = h(0xFF7A5230)
        val r = (wx1 - wx0 + 1) / 2f
        val cx = wx0 + r
        val ycy = ytop + r
        val ybot = 40
        fun inside(x: Int, y: Int, rr: Float): Boolean {
            return if (y < ycy) {
                val dx = x + 0.5f - cx
                val dy = y + 0.5f - ycy
                dx * dx + dy * dy <= rr * rr
            } else (x + 0.5f >= cx - rr && x + 0.5f <= cx + rr && y <= ybot)
        }
        val sh = SH[style]
        for (y in (ytop + 3)..ybot) {
            for (x in intArrayOf(3, 4, 5, 26, 27, 28)) {
                var c = sh
                if ((y - ytop) % 3 == 0) c = shadeCol(sh, -0.3f)
                if (x == 3 || x == 28) c = shadeCol(sh, -0.4f)
                else if (x == 5 || x == 26) c = shadeCol(sh, 0.1f)
                b.set(x, y, c)
            }
        }
        for (y in (ytop - 1)..ybot) {
            for (x in (wx0 - 2)..(wx1 + 2)) {
                if (inside(x, y, r + 1.6f) && !inside(x, y, r - 1.5f)) b.set(x, y, fr)
                else if (inside(x, y, r - 1.5f)) {
                    val t = (y - ytop) / (ybot - ytop).toFloat()
                    b.set(x, y, mixCol(h(0xFFCFEBF8), h(0xFF86BDDC), t))
                }
            }
        }
        for (k in 0 until 9) {
            val x = (cx - 6 + k).toInt()
            val y = (ytop + 8 + (9 - k) * 1.4f).toInt()
            b.set(x, y, h(0xFFEAF7FF)); b.set(x + 1, y, h(0xFFEAF7FF))
        }
        for (y in ytop until ybot) if (inside(cx.toInt(), y, r - 1.5f)) { b.set(cx.toInt() - 1, y, fr); b.set(cx.toInt(), y, fr) }
        val my = (ycy + 4).toInt()
        for (x in wx0..wx1) if (inside(x, my, r - 1.5f)) { b.set(x, my, fr); b.set(x, my + 1, fr) }
        val cur = shadeCol(sh, 0.35f)
        for (y in (ycy.toInt() - 1) until (ybot - 3)) {
            for (k in 0 until 3) {
                if (inside(wx0 + 2 + k, y, r - 1.6f)) b.set(wx0 + 2 + k, y, if (k != 1) cur else shadeCol(cur, -0.2f))
                if (inside(wx1 - 2 - k, y, r - 1.6f)) b.set(wx1 - 2 - k, y, if (k != 1) cur else shadeCol(cur, -0.2f))
            }
        }
        for (y in (ytop - 1)..ybot) {
            for (x in (wx0 - 2)..(wx1 + 2)) {
                if (inside(x, y, r + 1.6f) && !inside(x, y, r - 1.5f) && !inside(x - 1, y, r + 1.6f)) b.set(x, y, hi)
            }
        }
        b.rect(wx0 - 3, 41, wx1 - wx0 + 7, 2, h(0xFFC4C4CE))
        b.rect(wx0 - 3, 41, wx1 - wx0 + 7, 1, h(0xFFE2E2EA))
        b.rect(wx0 - 3, 43, wx1 - wx0 + 7, 1, 0x66000000)
        val wd = h(0xFF8A5A2B)
        b.rect(wx0, 42, wx1 - wx0 + 1, 5, wd)
        b.rect(wx0, 42, wx1 - wx0 + 1, 1, h(0xFFB07A44))
        b.rect(wx0, 46, wx1 - wx0 + 1, 1, h(0xFF4A2F18))
        var x = wx0 + 4
        while (x < wx1) { b.rect(x, 43, 1, 3, h(0xFF5A3A1E)); x += 5 }
        val fc = intArrayOf(h(0xFFE8505B), h(0xFFFF8FB7), h(0xFFF5D547), h(0xFFFFFFFF), h(0xFFB06AE0))
        val rn = java.util.Random(style * 5L + 1)
        for (k in 0 until (wx1 - wx0)) {
            val xx = wx0 + 1 + k
            if (k % 2 == 0) {
                b.set(xx, 41, h(0xFF3F9A4A)); b.set(xx, 40, fc[rn.nextInt(5)])
                if (rn.nextFloat() < 0.5f) b.set(xx, 39, fc[rn.nextInt(5)])
            } else b.set(xx, 41, h(0xFF2F7A3A))
        }
        for (k in 0 until 6) b.set(wx0 + 1 + k * 4, 47, h(0xFF3F9A4A))
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
        for (i in 0 until 130) b.set(rn.nextInt(32), 10 + rn.nextInt(40), shadeCol(pl, if (rn.nextBoolean()) 0.05f else -0.07f))
        for (k in 0 until 3) {
            val sx = 4 + rn.nextInt(24)
            for (y in 16 until 46) if (rn.nextFloat() < 0.55f) b.set(sx, y, shadeCol(pl, -0.07f))
        }
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
        when (kind) {
            0 -> {
                for (t in 0 until 2) {
                    b.line(5 + t, 17, 27 + t, 43, bm)
                    b.line(27 - t, 17, 5 - t, 43, bm)
                }
                b.line(5, 17, 27, 43, bm)
                b.line(6, 17, 28, 43, shadeCol(bm, 0.15f)); b.line(26, 17, 4, 43, shadeCol(bm, 0.15f))
                b.rect(14, 28, 4, 4, shadeCol(pl, -0.12f)); b.rect(15, 29, 2, 2, bm)
            }
            1 -> windowTall(b, style)
            2 -> lanternTall(b, style, frame)
            else -> {
                for (k in -3 until 5) {
                    b.line(14 + k * 6, 16, 14 + k * 6 + 14, 44, shadeCol(bm, 0.05f))
                    b.line(14 + k * 6 + 14, 16, 14 + k * 6, 44, shadeCol(bm, 0.05f))
                }
                val gr = intArrayOf(h(0xFF2F7A3A), h(0xFF3F9A4A), h(0xFF58B85A), h(0xFF245E2E))
                for (i in 0 until 90) {
                    val x = 14 + rn.nextInt(16)
                    val y = 16 + rn.nextInt(29)
                    if (x + (y - 16) * 0.15f > 22f || rn.nextFloat() < 0.5f) b.rect(x, y, 2, 2, gr[rn.nextInt(4)])
                }
                val fl = intArrayOf(h(0xFFFF8FB7), h(0xFFF5D547), h(0xFFFFFFFF))
                for (i in 0 until 7) b.set(16 + rn.nextInt(14), 18 + rn.nextInt(26), fl[rn.nextInt(3)])
            }
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

    /** Porta dupla gigante em arco (64x64). frame 0 fechada ... 4 aberta. O vão aberto fica transparente com sombra. */
    fun doorBig(style: Int, frame: Int): PixBuf {
        val big = PixBuf(64, 64)
        for (c in 0..1) big.blit(facade(style, 0, 0, 0), c * 32, 0)
        val o = frame / 4f
        val stone = h(0xFF9B9BA6)
        val mortar = h(0xFF5A5A66)
        val wood = h(0xFF7A4B25)
        val woodDk = h(0xFF4E2F14)
        val iron = h(0xFF2E2E36)
        val wl = Math.round(24f * (1f - 0.86f * o))
        val cy = 34f
        for (y in 0 until 64) {
            for (x in 0 until 64) {
                val fx = x + 0.5f
                val fy = y + 0.5f
                val inOuter: Boolean
                val inOpen: Boolean
                if (y < cy) {
                    val dx = fx - 32f
                    val dy = fy - cy
                    val d2 = dx * dx + dy * dy
                    inOuter = d2 <= 30f * 30f
                    inOpen = d2 <= 24f * 24f
                } else {
                    inOuter = x in 2..61
                    inOpen = x in 8..55 && y <= 60
                }
                if (!inOuter) continue
                if (!inOpen) {
                    var c = stone
                    if (y < cy) {
                        val dx = fx - 32f
                        val dy = fy - cy
                        val ang = Math.toDegrees(atan2(dx.toDouble(), (-dy).toDouble()))
                        val dist = sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                        if (((ang + 90.0) % 15.0) < 1.4) c = mortar
                        else if (dist > 28.2f) c = shadeCol(stone, 0.22f)
                        else if (dist < 25.5f) c = shadeCol(stone, -0.2f)
                        if (abs(dx) <= 3f && y <= 11) c = if (abs(dx) >= 2.5f || y >= 10) mortar else shadeCol(stone, 0.3f)
                    } else {
                        c = if (x < 5 || x > 58) shadeCol(stone, 0.12f) else shadeCol(stone, -0.14f)
                        if (y % 7 == 0) c = mortar
                        else if (((x + (y / 7) * 3) % 6) == 0) c = mortar
                        if (y >= 61) c = if (y == 61) shadeCol(stone, 0.3f) else shadeCol(stone, -0.12f)
                    }
                    big.set(x, y, c)
                    continue
                }
                val onLeft = x < 8 + wl
                val onRight = x > 55 - wl
                if (onLeft || onRight) {
                    val lx = if (onLeft) x - 8 else 55 - x
                    var c = if (lx % 5 == 4) woodDk else if (lx % 5 == 0) shadeCol(wood, 0.12f) else wood
                    if ((y in 24..26) || (y in 46..48)) {
                        c = iron
                        if (lx % 6 == 2 && (y == 25 || y == 47)) c = h(0xFF8A8A96)
                    }
                    if (y < 24) {
                        val dd = sqrt(((fx - 32f) * (fx - 32f) + (fy - cy) * (fy - cy)).toDouble()).toFloat()
                        if (dd > 21.2f) c = shadeCol(iron, 0.05f)
                    }
                    val edgeX = if (onLeft) x == 8 + wl - 1 else x == 55 - wl + 1
                    if (edgeX) c = h(0xFF2A1A0E)
                    if (o > 0f) c = shadeCol(c, -0.12f * o)
                    if (o < 0.3f && y in 38..41 && (x in 29..34)) c = if (y == 38 || y == 41 || x == 29 || x == 34) iron else h(0xFFB8A070)
                    big.set(x, y, c)
                } else {
                    val a = (0xA0 * (1f - (y - 8) / 52f) + 0x30).toInt().coerceIn(0, 255)
                    big.set(x, y, a shl 24)
                }
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
