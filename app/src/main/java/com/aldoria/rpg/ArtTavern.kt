package com.aldoria.rpg

import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

/** A Taverna "Caneca Dourada": enxaimel (vigas escuras + reboco quente), andar de cima em balanço,
 *  porta ogival gigante com ferragens, toldo de telhas, placa de caneca pendurada e lampião de ferro. */
object ArtTavern {
    const val STYLE = 6
    const val DOOR_KIND = 6

    private val PLASTER = h(0xFFE9D3A2)
    private val PLASTER_DK = h(0xFFD3B882)
    private val BEAM = h(0xFF3A2312)
    private val STONE = h(0xFF82776B)
    private val IRON = h(0xFF24242C)
    private val IRON_HI = h(0xFF8A8A98)
    private val GOLD = h(0xFFD8A640)
    private val GOLD_HI = h(0xFFF6DC7C)
    private val OAK = h(0xFF6E4424)
    private val GLASS_A = h(0xFFB9DCEE)
    private val GLASS_B = h(0xFF6FA6C8)
    private val SHUT = h(0xFF8A2F2A)

    private fun beamH(b: PixBuf, x: Int, y: Int, w: Int, t: Int) {
        b.rect(x, y, w, t, BEAM)
        b.rect(x, y, w, 1, shadeCol(BEAM, 0.32f))
        b.rect(x, y + t - 1, w, 1, shadeCol(BEAM, -0.45f))
    }

    private fun beamV(b: PixBuf, x: Int, y: Int, t: Int, hgt: Int) {
        b.rect(x, y, t, hgt, BEAM)
        b.rect(x, y, 1, hgt, shadeCol(BEAM, 0.32f))
        b.rect(x + t - 1, y, 1, hgt, shadeCol(BEAM, -0.45f))
    }

    private fun brace(b: PixBuf, x0: Int, y0: Int, x1: Int, y1: Int) {
        b.line(x0, y0, x1, y1, BEAM)
        b.line(x0 + 1, y0, x1 + 1, y1, BEAM)
        b.line(x0, y0 + 1, x1, y1 + 1, shadeCol(BEAM, 0.18f))
    }

    private fun plaster(b: PixBuf, rn: java.util.Random, y0: Int, y1: Int) {
        for (y in y0 until y1) b.rect(0, y, 32, 1, PLASTER)
        for (i in 0 until 150) b.set(rn.nextInt(32), y0 + rn.nextInt(y1 - y0), shadeCol(PLASTER, if (rn.nextBoolean()) 0.06f else -0.09f))
        // manchas de umidade escorrendo da viga
        for (k in 0 until 4) {
            val x = 4 + rn.nextInt(24)
            val len = 3 + rn.nextInt(6)
            for (y in y0 until min(y1, y0 + len)) if (rn.nextFloat() < 0.8f) b.set(x, y, PLASTER_DK)
        }
    }

    private fun plinthStone(b: PixBuf, rn: java.util.Random) {
        val mort = shadeCol(STONE, -0.45f)
        b.rect(0, 49, 32, 15, STONE)
        var y = 49
        var row = 0
        while (y < 64) {
            b.rect(0, y, 32, 1, mort)
            var x = if (row % 2 == 1) -5 else 0
            while (x < 32) {
                val bw = if ((x / 10 + row) % 2 == 0) 11 else 9
                if (x >= 0) b.rect(x, y, 1, 5, mort)
                for (k in 0 until bw - 1) b.set(x + 1 + k, y + 1, shadeCol(STONE, 0.2f))
                b.set(x + 3 + rn.nextInt(max(1, bw - 4)), y + 2 + rn.nextInt(3), shadeCol(STONE, if (rn.nextBoolean()) -0.16f else 0.12f))
                x += bw
            }
            y += 5
            row++
        }
        b.rect(0, 49, 32, 1, shadeCol(STONE, 0.4f))
        b.rect(0, 63, 32, 1, shadeCol(STONE, -0.55f))
        val moss = intArrayOf(h(0xFF4A7A34), h(0xFF3A6A2C), h(0xFF5E8A3A))
        for (i in 0 until 14) b.set(rn.nextInt(32), 58 + rn.nextInt(5), moss[rn.nextInt(3)])
    }

    private fun window(b: PixBuf) {
        // janela retangular com vidro em losangos (chumbo), venezianas vermelhas e jardineira de lúpulo
        val x0 = 8
        val x1 = 23
        val y0 = 18
        val y1 = 38
        b.rect(x0 - 2, y0 - 2, x1 - x0 + 5, y1 - y0 + 5, BEAM)
        for (y in y0..y1) for (x in x0..x1) {
            val t = (y - y0) / (y1 - y0).toFloat()
            var c = if (t < 0.5f) GLASS_A else GLASS_B
            val d1 = (x + y) % 5
            val d2 = (x - y + 100) % 5
            if (d1 == 0 || d2 == 0) c = h(0xFF3A3A44)
            b.set(x, y, c)
        }
        // brilho do vidro
        for (k in 0 until 5) { b.set(x0 + 2 + k, y0 + 7 - k, h(0xFFFFFFFF)); b.set(x0 + 3 + k, y0 + 7 - k, h(0xFFEAF7FF)) }
        // luz quente por dentro
        for (y in y0 + 10..y1) for (x in x0 + 2..x1 - 2) if ((x + y) % 3 == 0) b.set(x, y, h(0xFFF6D88A))
        // venezianas
        for (sx in intArrayOf(x0 - 7, x1 + 3)) {
            for (y in y0 - 1..y1 + 1) for (x in sx until sx + 5) {
                var c = SHUT
                if ((y - y0) % 3 == 0) c = shadeCol(SHUT, -0.35f) else if ((y - y0) % 3 == 1) c = shadeCol(SHUT, 0.14f)
                if (x == sx) c = shadeCol(c, 0.2f)
                if (x == sx + 4) c = shadeCol(c, -0.35f)
                b.set(x, y, c)
            }
            b.set(sx + 2, y0 + 10, GOLD)
        }
        // peitoril + jardineira
        b.rect(x0 - 3, y1 + 2, x1 - x0 + 7, 2, h(0xFFB89868))
        b.rect(x0 - 2, y1 + 4, x1 - x0 + 5, 5, OAK)
        b.rect(x0 - 2, y1 + 4, x1 - x0 + 5, 1, h(0xFF9A6C3A))
        b.rect(x0 - 2, y1 + 8, x1 - x0 + 5, 1, shadeCol(OAK, -0.4f))
        val g = intArrayOf(h(0xFF3F9A4A), h(0xFF2F7A3A), h(0xFF58B85A))
        val fl = intArrayOf(h(0xFFE8505B), h(0xFFF5D547), h(0xFFFFFFFF), h(0xFFB06AE0))
        for (k in 0 until x1 - x0 + 5) {
            val xx = x0 - 2 + k
            b.set(xx, y1 + 3, g[k % 3])
            if (k % 2 == 0) b.set(xx, y1 + 2, fl[(k / 2) % 4])
        }
        // lúpulo pendurado
        for (k in 0 until 6) { b.set(x0 + 1 + k * 3, y1 + 9, g[k % 3]); if (k % 2 == 0) b.set(x0 + 1 + k * 3, y1 + 10, g[1]) }
    }

    private fun lantern(b: PixBuf, frame: Int) {
        // braço de ferro com lampião aceso e halo
        for (y in 14 until 48) for (x in 2 until 30) {
            val d = hypot(x - 22.0, y - 28.0)
            if (d < 12.0) {
                val c = b.get(x, y)
                if (c != 0) b.set(x, y, shadeCol(c, (0.2 * (1 - d / 12.0)).toFloat()))
            }
        }
        b.rect(11, 17, 3, 20, IRON); b.rect(11, 17, 1, 20, IRON_HI)         // poste
        b.rect(11, 17, 13, 2, IRON)                                          // braço
        for (k in 0 until 5) b.set(12 + k, 19 + k / 2 + 1, IRON)             // suporte curvo
        b.rect(21, 19, 1, 3, IRON)                                           // corrente
        b.rect(19, 22, 6, 2, IRON); b.rect(20, 21, 4, 1, IRON)               // tampa
        val gl = if (frame == 0) h(0xFFFFE066) else h(0xFFFFD04A)
        b.rect(19, 24, 6, 9, h(0xFFB8741A))
        b.rect(20, 25, 4, 7, gl)
        b.rect(21, 26, 2, 5, h(0xFFFFFFE0))
        b.rect(19, 24, 1, 9, IRON); b.rect(24, 24, 1, 9, IRON)
        b.rect(19, 33, 6, 2, IRON); b.set(21, 35, IRON); b.set(22, 35, IRON)
        for (k in 0 until 4) b.set(11 + k * 3, 36, IRON)                     // enrolado decorativo
        // barris pequenos com lúpulo ao pé
    }

    private fun barrels(b: PixBuf, rn: java.util.Random) {
        // dois barris empilhados + videira de lúpulo subindo pela viga
        val w = h(0xFF8A5A2B)
        for ((bx, by, rr) in arrayOf(Triple(10, 38, 8), Triple(23, 41, 6))) {
            for (y in by until by + 14) {
                val t = (y - by) / 13f
                val half = (rr * (0.85f + 0.2f * kotlin.math.sin(Math.PI * t).toFloat())).toInt()
                for (x in bx - half..bx + half) {
                    var c = w
                    if ((x - bx + 20) % 4 == 0) c = h(0xFF6B4423)
                    if (x < bx - half + 2) c = h(0xFFB07A44)
                    if (x > bx + half - 2) c = h(0xFF5A3A1E)
                    b.set(x, y, c)
                }
            }
            for (y in intArrayOf(by + 3, by + 10)) b.rect(bx - rr, y, rr * 2 + 1, 1, h(0xFF33333C))
            b.ellipse(bx.toFloat(), (by + 1).toFloat(), rr * 0.9f, 1.5f, h(0xFFB07A44), false)
            b.rect(bx - 1, by + 7, 2, 2, GOLD)
        }
        val g = intArrayOf(h(0xFF2F7A3A), h(0xFF3F9A4A), h(0xFF58B85A))
        for (y in 16 until 40) if (rn.nextFloat() < 0.8f) b.set(25 + (if (rn.nextInt(3) == 0) 1 else 0), y, g[rn.nextInt(3)])
        for (k in 0 until 14) b.set(22 + rn.nextInt(8), 17 + rn.nextInt(20), g[rn.nextInt(3)])
        for (k in 0 until 4) b.set(22 + rn.nextInt(8), 22 + rn.nextInt(14), h(0xFFC8E070))
    }

    /** Coluna da fachada 32x64. kind: 0 parede com cruz de vigas, 1 janela de losangos, 2 lampião, 3 barris e lúpulo, 4 vazia (fundo da porta). */
    fun facade(kind: Int, edge: Int, frame: Int): PixBuf {
        val b = PixBuf(32, 64)
        val rn = java.util.Random(4242L + kind * 13 + edge * 7)
        b.rect(0, 0, 32, 64, PLASTER)
        plaster(b, rn, 0, 49)
        // sombra do balcão do andar de cima
        val sh = floatArrayOf(-0.5f, -0.42f, -0.36f, -0.3f, -0.25f, -0.2f, -0.16f, -0.12f, -0.08f, -0.05f, -0.03f, -0.015f)
        for (y in sh.indices) b.rect(0, y, 32, 1, shadeCol(PLASTER, sh[y]))
        // beiral e vigas de balanço (cabeças de viga)
        b.rect(0, 0, 32, 2, shadeCol(BEAM, -0.4f))
        b.rect(0, 2, 32, 3, BEAM); b.rect(0, 2, 32, 1, shadeCol(BEAM, 0.3f)); b.rect(0, 5, 32, 1, shadeCol(BEAM, -0.3f))
        for (bx in intArrayOf(3, 15, 26)) {
            b.rect(bx, 6, 4, 5, BEAM); b.rect(bx, 6, 1, 5, shadeCol(BEAM, 0.28f)); b.rect(bx + 1, 11, 2, 1, shadeCol(BEAM, -0.35f))
        }
        // enxaimel do andar de cima (faixa 6..11): cruz pequena
        brace(b, 5, 7, 13, 10)
        brace(b, 18, 10, 25, 7)
        beamH(b, 0, 12, 32, 3)
        // viga de baixo
        beamH(b, 0, 46, 32, 3)
        b.rect(0, 45, 32, 1, shadeCol(PLASTER, -0.3f))
        // postes laterais
        beamV(b, 0, 15, 3, 31)
        beamV(b, 29, 15, 3, 31)
        when (kind) {
            0 -> {
                beamV(b, 14, 15, 4, 31)
                brace(b, 3, 17, 13, 43)
                brace(b, 19, 43, 28, 17)
                // rosetas de ferro nos cruzamentos
                for (p in arrayOf(intArrayOf(8, 30), intArrayOf(23, 30))) { b.set(p[0], p[1], GOLD); b.set(p[0] + 1, p[1], shadeCol(GOLD, -0.3f)) }
            }
            1 -> window(b)
            2 -> lantern(b, frame)
            3 -> barrels(b, rn)
            else -> {}
        }
        plinthStone(b, rn)
        if (edge == 1) {
            beamV(b, 0, 12, 5, 37)
            b.rect(0, 49, 6, 15, shadeCol(STONE, 0.12f)); b.rect(0, 49, 6, 1, shadeCol(STONE, 0.4f))
            var y = 52
            while (y < 63) { b.rect(0, y, 6, 1, shadeCol(STONE, -0.45f)); y += 5 }
        } else if (edge == 2) {
            beamV(b, 27, 12, 5, 37)
            b.rect(26, 49, 6, 15, shadeCol(STONE, -0.05f)); b.rect(26, 49, 6, 1, shadeCol(STONE, 0.3f))
            var y = 52
            while (y < 63) { b.rect(26, y, 6, 1, shadeCol(STONE, -0.45f)); y += 5 }
        }
        return b
    }

    // ------------------------------------------------------------------ porta grande

    private const val LO = 14
    private const val HI = 49
    private const val TOP = 17
    private const val DBOT = 60

    private fun doorIn(x: Int, y: Int, g: Int): Boolean {
        val fx = x + 0.5f
        val fy = y + 0.5f
        val wd = (HI - LO + 1).toFloat()
        val cx = (LO + HI + 1) / 2f
        val half = wd / 2f + g
        val spring = TOP + wd * 0.58f
        val tp = (TOP - g).toFloat()
        if (fy >= spring) return abs(fx - cx) <= half && y <= DBOT
        if (fy < tp) return false
        val t = (spring - fy) / (spring - tp)
        return abs(fx - cx) <= half * (1f - Math.pow(t.toDouble(), 1.8).toFloat())
    }

    private fun leaf(lx: Int, ly: Int, wl: Int, hh: Int): Int {
        val wood = h(0xFF6A3A22)
        var c = if (lx % 4 == 3) shadeCol(wood, -0.3f) else if (lx % 4 == 0) shadeCol(wood, 0.12f) else wood
        if ((lx * 7 + ly * 3) % 11 == 0) c = shadeCol(c, -0.1f)
        // tiras de ferro com ponta de flor-de-lis
        for (sy in intArrayOf((hh * 0.2f).toInt(), (hh * 0.58f).toInt(), (hh * 0.88f).toInt())) {
            if (ly >= sy && ly <= sy + 2 && lx <= wl - 4) {
                c = if (ly == sy) shadeCol(IRON, 0.18f) else IRON
                if (lx % 4 == 1 && ly == sy + 1) c = IRON_HI
            }
            val ex = wl - 3
            if (abs(lx - ex) <= 1 && ly >= sy - 1 && ly <= sy + 3) c = IRON
            if (lx == ex && ly == sy - 2) c = IRON
        }
        // janelinha com grade (olho-mágico)
        val wx0 = wl / 2 - 3
        if (lx >= wx0 && lx <= wx0 + 6 && ly in 7..14) {
            c = if (lx == wx0 || lx == wx0 + 6 || ly == 7 || ly == 14) IRON else h(0xFF14101A)
            if ((lx - wx0) % 2 == 0 && c != IRON) c = IRON
            if (ly in 9..10 && c != IRON) c = h(0xFFF2B040)
        }
        // aldrava de leão (anel)
        val ky = (hh * 0.5f).toInt() + 4
        val dd = hypot(lx + 0.5 - (wl - 5.0), ly + 0.5 - ky.toDouble())
        if (dd < 2.2) c = if (dd < 1.2) GOLD_HI else GOLD
        if (dd >= 3.0 && dd < 4.4 && ly > ky - 1) c = shadeCol(GOLD, -0.15f)
        // tachas
        if (ly > 20 && lx > 1 && lx < wl - 6 && lx % 4 == 1 && ly % 6 == 3) c = IRON_HI
        // pé da porta mais escuro
        if (ly > hh - 4) c = shadeCol(c, -0.3f)
        return c
    }

    /** Porta ogival dupla, 64x64. frame 0 fechada ... 4 aberta. */
    fun doorBig(frame: Int): PixBuf {
        val big = PixBuf(64, 64)
        for (c in 0..1) big.blit(facade(4, 0, 0), c * 32, 0)
        val wd = HI - LO + 1
        val cx = (LO + HI + 1) / 2f
        val spring = TOP + wd * 0.58f
        val o = frame / 4f
        val outline = h(0xFF2A1A0E)
        val mort = shadeCol(STONE, -0.45f)
        // moldura de pedra em arco com aduelas
        for (y in 0 until 64) for (x in 0 until 64) {
            if (doorIn(x, y, 5) && !doorIn(x, y, 4) && y <= DBOT + 1) big.set(x, y, outline)
            if (doorIn(x, y, 4) && !doorIn(x, y, 0)) {
                var c = STONE
                if (!doorIn(x, y, 3)) c = shadeCol(STONE, 0.22f)
                else if (doorIn(x, y, 1)) c = shadeCol(STONE, -0.22f)
                if (y < spring) {
                    val ang = Math.toDegrees(Math.atan2(x + 0.5 - cx, -(y + 0.5 - spring).toDouble()))
                    if (((ang + 90.0) % 15.0) < 1.5) c = mort
                } else if (y % 6 == 0 || (x + (y / 6) * 3) % 7 == 0) c = mort
                big.set(x, y, c)
            }
        }
        // soleira em degraus
        for (y in DBOT + 1..62) for (x in LO - 6..HI + 6) {
            big.set(x, y, if (y == DBOT + 1) shadeCol(STONE, 0.3f) else if (y == 62) shadeCol(STONE, -0.4f) else shadeCol(STONE, -0.05f))
        }
        // folhas
        val wl0 = wd / 2
        val wl = Math.round(wl0 * (1f - 0.86f * o))
        val hh = DBOT - TOP + 1
        for (y in 0 until 64) for (x in 0 until 64) {
            if (!doorIn(x, y, 0)) continue
            val ly = y - TOP
            var lx = -1
            var edge = false
            if (x < LO + wl) { lx = x - LO; edge = x == LO + wl - 1 }
            else if (x > HI - wl) { lx = HI - x; edge = x == HI - wl + 1 }
            if (lx >= 0) {
                var c = leaf(lx, ly, wl0, hh)
                if (edge) c = h(0xFF24140A)
                if (o > 0f) c = shadeCol(c, -0.12f * o)
                big.set(x, y, c)
            } else {
                // vão aberto: saguão quente, com brilho de lareira
                val a = (0xB8 * (1f - (y - 8) / 56f) + 0x28).toInt().coerceIn(0, 255)
                val warm = if ((x + y) % 5 == 0) 0x3A2008 else 0x1A0E06
                big.set(x, y, (a shl 24) or warm)
            }
        }
        // chave da ogiva com caneca em ouro
        val ix = cx.toInt()
        big.rect(ix - 2, TOP - 4, 4, 5, GOLD); big.rect(ix - 2, TOP - 4, 4, 1, GOLD_HI)
        big.rect(ix - 1, TOP - 3, 2, 3, shadeCol(GOLD, -0.4f)); big.set(ix + 2, TOP - 2, GOLD)
        // toldo de telhas (empena inclinada), com vigas e cachorros de apoio
        for (i in 0 until 11) {
            val xi = 5 + i
            val xr = 58 - i
            val y = i
            for (x in xi..xr) {
                var c = if (((x + (i / 2) * 3) / 4) % 2 == 0) h(0xFFA8432B) else h(0xFFBE5136)
                if (i % 2 == 1) c = shadeCol(c, -0.22f)
                if (x == xi || x == xr) c = h(0xFF4A1F12)
                big.set(x, y, c)
            }
        }
        big.rect(4, 11, 56, 2, BEAM); big.rect(4, 11, 56, 1, shadeCol(BEAM, 0.3f))
        for (px in intArrayOf(7, 54)) {
            big.rect(px, 13, 3, 4, BEAM)
            big.line(px, 17, px + (if (px < 30) 5 else -3), 13, shadeCol(BEAM, 0.1f))
        }
        // lampião à esquerda (aceso)
        big.rect(4, 22, 1, 4, IRON)
        big.rect(2, 26, 5, 1, IRON); big.rect(3, 27, 3, 6, h(0xFFB8741A)); big.rect(3, 28, 3, 4, h(0xFFFFE066)); big.set(4, 29, h(0xFFFFFFE0))
        big.rect(2, 33, 5, 1, IRON)
        // placa de caneca à direita, pendurada por correntes
        val sx = 55
        big.rect(sx - 2, 19, 9, 2, IRON)
        big.set(sx, 21, IRON); big.set(sx + 5, 21, IRON); big.set(sx, 22, IRON); big.set(sx + 5, 22, IRON)
        big.rect(sx - 1, 23, 8, 12, h(0xFF5A3A1E)); big.rect(sx - 1, 23, 8, 1, h(0xFF8A5A2B)); big.rect(sx - 1, 34, 8, 1, shadeCol(BEAM, -0.3f))
        big.rect(sx, 24, 6, 10, h(0xFF2E1A0C))
        // caneca dourada espumando
        big.rect(sx + 1, 27, 4, 5, GOLD); big.rect(sx + 1, 27, 1, 5, GOLD_HI)
        big.rect(sx + 1, 32, 4, 1, shadeCol(GOLD, -0.35f))
        big.set(sx + 5, 28, GOLD); big.set(sx + 5, 29, GOLD); big.set(sx + 5, 30, GOLD)
        big.rect(sx + 1, 25, 4, 2, h(0xFFFFF6E0)); big.set(sx + 2, 24, h(0xFFFFFFFF)); big.set(sx + 4, 24, h(0xFFFFFFFF))
        // vasos de flores e barrilzinho ao lado da porta
        big.rect(LO - 11, DBOT - 6, 6, 7, h(0xFFB8602E)); big.rect(LO - 11, DBOT - 6, 6, 1, h(0xFFD87A3E))
        big.set(LO - 10, DBOT - 8, h(0xFFD84A5A)); big.set(LO - 8, DBOT - 9, h(0xFFF4D060)); big.set(LO - 7, DBOT - 8, h(0xFFFFFFFF))
        big.set(LO - 9, DBOT - 7, h(0xFF3E7A3A)); big.set(LO - 8, DBOT - 7, h(0xFF3E7A3A)); big.set(LO - 10, DBOT - 7, h(0xFF3E7A3A))
        big.rect(HI + 5, DBOT - 5, 6, 6, h(0xFF8A5A2B)); big.rect(HI + 5, DBOT - 5, 1, 6, h(0xFFB07A44)); big.rect(HI + 5, DBOT - 3, 6, 1, IRON)
        return big
    }

    // ------------------------------------------------------------------ telhado

    /** Enfeites do telhado: biruta em forma de caneca e bandeirolas no beiral. [yR] = y da cumeeira. */
    fun roofExtras(b: PixBuf, bw: Int, yR: Int) {
        val cx = bw / 2
        b.rect(cx, yR - 24, 1, 24, IRON)
        b.rect(cx - 1, yR - 25, 3, 1, GOLD_HI)
        // caneca-biruta
        b.rect(cx + 1, yR - 22, 7, 6, GOLD); b.rect(cx + 1, yR - 22, 7, 1, GOLD_HI)
        b.rect(cx + 8, yR - 21, 2, 4, GOLD); b.set(cx + 10, yR - 20, GOLD); b.set(cx + 10, yR - 19, GOLD)
        b.rect(cx + 1, yR - 24, 7, 2, h(0xFFFFF6E0)); b.set(cx + 3, yR - 25, h(0xFFFFFFFF)); b.set(cx + 6, yR - 25, h(0xFFFFFFFF))
        b.rect(cx + 1, yR - 17, 7, 1, shadeCol(GOLD, -0.4f))
        // bandeirolas ao longo do beiral
        val cols = intArrayOf(h(0xFFB8372F), h(0xFFF2E6CC), h(0xFF2F8C86), h(0xFFD8A640))
        val yb = b.h - 7
        var x = 14
        var k = 0
        while (x < bw - 14) {
            b.set(x, yb, h(0xFF3A2410))
            val c = cols[k % 4]
            b.rect(x + 1, yb, 3, 1, c); b.rect(x + 1, yb + 1, 3, 1, c); b.rect(x + 2, yb + 2, 1, 1, c)
            x += 6
            k++
        }
    }

    // ------------------------------------------------------------------ interior

    /** Decoração da parede de trás por dentro (por cima da base de ArtHouse.backWall). */
    fun backDecor(b: PixBuf, kind: Int) {
        when (kind) {
            0 -> {
                // guirlanda de lúpulo e canecas penduradas
                val g = intArrayOf(h(0xFF2F7A3A), h(0xFF3F9A4A), h(0xFF8CC24A))
                for (x in 2 until 30) {
                    val y = 9 + (3.0 * kotlin.math.sin((x - 2) / 28.0 * Math.PI)).toInt()
                    b.set(x, y, g[x % 3]); b.set(x, y + 1, g[(x + 1) % 3])
                    if (x % 5 == 0) b.set(x, y + 2, h(0xFFC8E070))
                }
                for (mx in intArrayOf(7, 16, 24)) {
                    b.set(mx, 17, IRON); b.rect(mx - 1, 18, 4, 4, GOLD); b.rect(mx - 1, 18, 1, 4, GOLD_HI); b.set(mx + 3, 19, GOLD)
                }
            }
            3 -> {
                // troféu: cabeça de javali sobre escudo com espadas cruzadas
                b.rect(7, 12, 18, 22, h(0xFF3A2410)); b.rect(8, 13, 16, 20, h(0xFF7A2E2E))
                b.rect(8, 13, 16, 1, GOLD); b.rect(8, 32, 16, 1, GOLD); b.rect(8, 13, 1, 20, GOLD); b.rect(23, 13, 1, 20, GOLD)
                b.line(10, 16, 21, 29, h(0xFFC8C8D4)); b.line(21, 16, 10, 29, h(0xFFC8C8D4))
                b.rect(14, 18, 5, 5, h(0xFF6A4A2E)); b.rect(13, 20, 7, 4, h(0xFF7A5636))
                b.set(14, 20, h(0xFF14101A)); b.set(18, 20, h(0xFF14101A))
                b.set(13, 24, h(0xFFF2E6CC)); b.set(19, 24, h(0xFFF2E6CC)); b.set(13, 25, h(0xFFF2E6CC)); b.set(19, 25, h(0xFFF2E6CC))
                b.set(16, 17, h(0xFF5A3A22)); b.set(14, 17, h(0xFF5A3A22)); b.set(18, 17, h(0xFF5A3A22))
            }
        }
    }
}
