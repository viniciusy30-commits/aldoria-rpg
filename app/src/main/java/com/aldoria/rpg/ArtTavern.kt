package com.aldoria.rpg

import android.graphics.Color
import kotlin.math.max
import kotlin.math.min

/**
 * Caneca Dourada — reconstrução visual original.
 * Arte procedural em pixel grid: madeira escura, pedra irregular, cobre,
 * vitrais âmbar, telhas individuais e mobiliário de taverna artesanal.
 * Não depende dos sprites antigos de ArtTavernData.
 */
object ArtTavern {
    const val STYLE = 6
    const val DOOR_KIND = 6
    private val cache = HashMap<String, PixBuf>()

    private val outline = h(0xFF211820)
    private val woodDark = h(0xFF38242A)
    private val wood = h(0xFF67402E)
    private val woodMid = h(0xFF89543A)
    private val woodLight = h(0xFFB8794A)
    private val gold = h(0xFFE8B95B)
    private val goldLight = h(0xFFFFD982)
    private val stone = h(0xFF77716B)
    private val stoneLight = h(0xFFA59A87)
    private val stoneDark = h(0xFF4C4548)
    private val plaster = h(0xFFD8C49B)
    private val plasterShade = h(0xFFB79A72)
    private val roofDark = h(0xFF542A32)
    private val roof = h(0xFF843A42)
    private val roofLight = h(0xFFAD4F4B)
    private val roofHi = h(0xFFD06C55)
    private val amber = h(0xFFFFB94F)
    private val amberLight = h(0xFFFFE5A0)
    private val glassDark = h(0xFF6D382E)

    private fun shade(c: Int, amount: Float): Int = shadeCol(c, amount)
    private fun cached(key: String, fw: Int, fh: Int, draw: (PixBuf) -> Unit): PixBuf {
        cache[key]?.let { return it }
        val b = PixBuf(fw, fh)
        draw(b)
        cache[key] = b
        return b
    }
    private fun pixel(b: PixBuf, x: Int, y: Int, c: Int) {
        if (x in 0 until b.w && y in 0 until b.h) b.set(x, y, c)
    }
    private fun plank(b: PixBuf, x: Int, y: Int, w: Int, c: Int) {
        b.rect(x, y, w, 1, c)
        if (y + 1 < b.h) b.rect(x + 2, y + 1, max(1, w - 4), 1, shade(c, -0.10f))
    }

    /** Fachada de 19 colunas costuradas em uma fachada única de 608x192. */
    fun facadeCol(rel: Int): PixBuf = cached("new_fc${rel.coerceIn(0,18)}", 32, 192) { b ->
        val col = rel.coerceIn(0,18)
        val x0 = col * 32
        b.rect(0, 0, 32, 192, plaster)
        // Reboco texturizado com lascas discretas, repetição estável por coluna.
        for (i in 0 until 13) {
            val x = (col * 17 + i * 11 + 3) % 31
            val y = 13 + ((col * 29 + i * 17) % 116)
            pixel(b, x, y, if (i % 3 == 0) plasterShade else shade(plaster, 0.07f))
            if (i % 4 == 0) pixel(b, min(31,x+1), y, plasterShade)
        }
        // Travessas de carvalho escuro e pilares com brilho lateral.
        b.rect(0, 40, 32, 7, woodDark)
        b.rect(0, 40, 32, 2, woodLight)
        b.rect(0, 45, 32, 2, woodMid)
        b.rect(0, 130, 32, 8, woodDark)
        b.rect(0, 130, 32, 2, woodLight)
        b.rect(0, 136, 32, 2, woodMid)
        if (col % 3 != 1) {
            b.rect(0, 47, 4, 82, woodDark)
            b.rect(1, 48, 1, 79, woodLight)
            b.rect(27, 47, 5, 83, woodDark)
            b.rect(28, 48, 1, 79, woodMid)
        }
        // Base de alvenaria irregular, junta a junta, com highlights.
        b.rect(0, 138, 32, 54, stoneDark)
        for (row in 0 until 4) {
            val yy = 140 + row * 13
            b.rect(0, yy, 32, 1, outline)
            val offset = if ((row + col) % 2 == 0) 0 else -8
            var xx = offset
            while (xx < 32) {
                if (xx >= 0) {
                    b.rect(xx, yy + 2, 1, 9, stoneDark)
                    b.rect(xx + 1, yy + 2, 8, 1, stoneLight)
                    b.rect(xx + 1, yy + 3, 7, 1, stone)
                    b.rect(xx + 8, yy + 3, 1, 7, shade(stone, -0.16f))
                    for (k in 0..2) pixel(b, xx + 2 + ((col + row + k) % 5), yy + 5 + k, stoneLight)
                }
                xx += 10 + ((row + col) % 2) * 2
            }
        }
        b.rect(0, 189, 32, 3, outline)
        b.rect(0, 188, 32, 1, stoneLight)
        // Janelas profundas, vitrais quentes e floreiras, em dois vãos amplos.
        fun window(gx: Int, gy: Int, ww: Int, hh: Int) {
            val lx = gx - x0
            if (lx >= 32 || lx + ww <= 0) return
            b.rect(lx - 2, gy - 3, ww + 4, hh + 7, outline)
            b.rect(lx - 1, gy - 2, ww + 2, hh + 4, woodLight)
            b.rect(lx, gy - 1, ww, hh + 2, woodDark)
            b.rect(lx + 2, gy + 1, max(1,ww - 4), max(1,hh - 2), glassDark)
            for (yy in gy + 2 until gy + hh) for (xx in lx + 2 until lx + ww - 2) {
                pixel(b, xx, yy, if ((xx + yy) % 5 == 0) amberLight else amber)
            }
            b.rect(lx + ww / 2 - 1, gy, 2, hh, woodLight)
            b.rect(lx + 1, gy + hh / 2, max(1,ww - 2), 2, woodLight)
            b.rect(lx - 3, gy + hh + 3, ww + 6, 3, woodDark)
            b.rect(lx - 2, gy + hh + 3, ww + 4, 1, woodLight)
            for (xx in lx - 1..lx + ww) if (xx in 0..31) {
                pixel(b, xx, gy + hh + 1, if (xx % 3 == 0) h(0xFF497447) else h(0xFF648B4B))
                if (xx % 4 == 0) pixel(b, xx, gy + hh, h(0xFFD85C62))
            }
        }
        window(70, 67, 30, 47)
        window(418, 67, 30, 47)
        // Placa "CANeca DOURADA" pendurada em suportes de ferro.
        if (x0 < 245 && x0 + 32 > 150) {
            b.rect(151-x0, 41, 2, 14, outline)
            b.rect(181-x0, 41, 2, 14, outline)
            b.rect(146-x0, 52, 42, 25, outline)
            b.rect(148-x0, 54, 38, 21, wood)
            b.rect(150-x0, 56, 34, 17, woodMid)
            b.rect(151-x0, 57, 32, 1, gold)
            for (yy in 0..2) for (xx in 0..4) pixel(b, 155-x0+xx*5, 60+yy*3, if ((xx+yy)%2==0) goldLight else gold)
            b.rect(162-x0, 65, 10, 2, amberLight)
        }
        // Lampiões de ferro laterais com vidro âmbar.
        for (gx in intArrayOf(28, 486)) if (gx / 32 == col) {
            val lx = gx - x0
            b.rect(lx + 3, 78, 2, 8, outline)
            b.rect(lx, 84, 8, 12, outline)
            b.rect(lx + 1, 85, 6, 9, amber)
            b.rect(lx + 2, 86, 3, 6, amberLight)
            b.rect(lx - 1, 83, 10, 2, woodDark)
            b.rect(lx + 2, 96, 4, 2, outline)
        }
        // Rodapé superior de madeira e pequenos cravos de latão.
        for (x in 3..29 step 8) {
            pixel(b, x, 43, gold)
            pixel(b, x, 133, gold)
        }
    }

    // Compatibilidade com os despachantes genéricos de ArtHouse.
    fun facade(kind: Int, edge: Int, frame: Int): PixBuf = facadeCol(0)
    fun backWall(kind: Int, edge: Int, frame: Int): PixBuf = backCol(0, 0)

    /** Fachada baixa, visível por dentro: base de pedra, balcão de madeira e corrimão. */
    fun facadeCut(rel: Int): PixBuf = cached("new_cc${rel.coerceIn(0,18)}", 32, 192) { b ->
        val col = rel.coerceIn(0,18)
        b.rect(0, 143, 32, 49, stoneDark)
        for (row in 0..3) {
            val y = 145 + row * 11
            b.rect(0, y, 32, 1, outline)
            for (x in 0..31 step 11) {
                b.rect(x + ((row + col) % 2) * 4, y + 2, 1, 7, stone)
                b.rect(x + 1, y + 2, 7, 1, stoneLight)
            }
        }
        b.rect(0, 140, 32, 5, woodDark)
        b.rect(0, 140, 32, 2, woodLight)
        b.rect(0, 146, 32, 2, woodMid)
        if (col % 2 == 0) {
            b.rect(0, 132, 32, 4, woodDark)
            b.rect(0, 132, 32, 1, gold)
            b.rect(0, 136, 32, 3, woodMid)
        }
        b.rect(0, 189, 32, 3, outline)
    }

    /** Portal arqueado em 3 colunas; as folhas abrem para dentro. */
    fun doorBig(frame: Int): PixBuf = cached("new_d${frame.coerceIn(0,4)}", 96, 192) { b ->
        val fr = frame.coerceIn(0,4)
        // Portal de pedra com arco, keystone e moldura de carvalho.
        b.rect(4, 31, 88, 161, outline)
        b.rect(7, 27, 82, 165, stoneDark)
        for (i in 0..5) {
            val yy = 25 + i * 5
            val inset = when (i) { 0 -> 24; 1 -> 16; 2 -> 10; else -> 5 }
            b.rect(inset, yy, 96 - inset * 2, 5, stone)
            b.rect(inset + 2, yy, 92 - inset * 2, 1, stoneLight)
        }
        b.rect(12, 52, 72, 140, woodDark)
        b.rect(16, 55, 64, 137, woodMid)
        b.rect(19, 58, 58, 134, wood)
        // Portas com tábuas verticais, veios e ferragens.
        for (leaf in 0..1) {
            val lx = if (leaf == 0) 20 else 49
            val shift = if (fr == 0) 0 else if (leaf == 0) -fr * 5 else fr * 5
            val width = if (fr == 0) 28 else max(8, 28 - fr * 5)
            b.rect(lx + shift, 60, width, 128, woodDark)
            for (x in 0 until width step 7) {
                b.rect(lx + shift + x + 1, 62, 1, 123, woodLight)
                b.rect(lx + shift + x + 2, 62, max(1,min(3,width-x-2)), 123, wood)
                for (y in intArrayOf(76, 111, 147, 179)) pixel(b, lx + shift + x + 2, y, gold)
            }
            if (fr == 0) {
                b.rect(lx + 4, 79, 19, 31, woodDark)
                b.rect(lx + 6, 81, 15, 27, woodMid)
                b.rect(lx + 7, 82, 13, 1, gold)
                b.rect(lx + 7, 106, 13, 1, gold)
                b.rect(lx + 9, 85, 9, 20, wood)
                b.rect(lx + 7, 118, 13, 35, woodDark)
                b.rect(lx + 9, 120, 9, 31, woodMid)
            }
            b.rect(lx + shift + width - 4, 120, 3, 7, gold)
            b.rect(lx + shift + width - 3, 122, 2, 3, goldLight)
        }
        // Fechadura central e emblema de caneca.
        if (fr == 0) {
            b.rect(45, 118, 6, 9, outline)
            b.rect(46, 119, 4, 6, gold)
            b.rect(47, 120, 2, 4, goldLight)
            b.rect(36, 37, 24, 13, outline)
            b.rect(38, 39, 20, 9, gold)
            b.rect(40, 40, 11, 5, amber)
            b.rect(52, 41, 4, 4, goldLight)
        }
        b.rect(4, 188, 88, 4, outline)
        b.rect(10, 190, 76, 2, gold)
    }

    /** Telhado independente, 630x222: duas águas, telhas legíveis, cumeeira e chaminés. */
    fun roof(): PixBuf = cached("new_roof", 630, 222) { b ->
        b.rect(0, 42, 630, 180, outline)
        // Silhueta triangular em degraus para manter a estética pixel art.
        for (y in 0 until 190) {
            val inset = max(0, (y - 4) * 3 / 2)
            val left = max(0, inset)
            val right = min(629, 629 - inset)
            if (left <= right) b.rect(left, y + 24, right - left + 1, 1, roofDark)
        }
        // Planos de telha em fileiras diagonais, com contorno escuro e reflexo.
        for (row in 0..11) {
            val y = 43 + row * 14
            val inset = row * 23
            val start = inset.coerceAtMost(300)
            val end = (630 - inset).coerceAtLeast(330)
            var x = start
            while (x < end) {
                val tw = 18 + ((x / 18 + row) % 3) * 3
                b.rect(x, y, tw, 13, roofDark)
                b.rect(x + 1, y + 1, max(1,tw - 3), 9, if ((x / 18 + row) % 4 == 0) roofLight else roof)
                b.rect(x + 2, y + 2, max(1,tw - 5), 2, roofHi)
                b.rect(x + 1, y + 10, max(1,tw - 2), 1, shade(roof, -0.20f))
                x += tw
            }
        }
        // Vigas decorativas seguindo as duas águas.
        for (i in 0..8) {
            val x = 12 + i * 36
            val y = 54 + i * 8
            b.line(x, y, x + 18, y + 18, woodDark)
            b.line(x + 1, y, x + 19, y + 18, woodLight)
            b.line(618 - x, y, 600 - x, y + 18, woodDark)
        }
        // Cumeeira alta e ornamental.
        b.rect(0, 28, 630, 16, outline)
        b.rect(0, 30, 630, 7, roofDark)
        b.rect(0, 30, 630, 2, roofHi)
        for (x in 8 until 630 step 24) {
            b.rect(x, 32, 12, 3, roofLight)
            pixel(b, x + 4, 31, gold)
        }
        // Beiral de madeira com suportes e pontas esculpidas.
        b.rect(0, 203, 630, 19, outline)
        b.rect(0, 205, 630, 5, wood)
        b.rect(0, 205, 630, 2, woodLight)
        b.rect(0, 214, 630, 4, woodDark)
        for (x in 8 until 630 step 32) {
            b.rect(x, 210, 4, 8, woodMid)
            pixel(b, x + 1, 211, gold)
        }
        // Chaminés de pedra com capas de cobre.
        for (cx in intArrayOf(110, 510)) {
            b.rect(cx, 2, 34, 72, outline)
            b.rect(cx + 4, 5, 26, 66, stone)
            for (y in 10..65 step 14) {
                b.rect(cx + 4, y, 26, 1, stoneDark)
                b.rect(cx + 5, y + 2, 8, 1, stoneLight)
            }
            b.rect(cx - 4, 0, 42, 8, woodDark)
            b.rect(cx - 2, 1, 38, 3, gold)
            b.rect(cx + 2, 4, 30, 2, woodMid)
        }
        // Pequenas placas de cobre nas pontas do telhado.
        for (x in intArrayOf(12, 598)) {
            b.rect(x, 180, 20, 10, woodDark)
            b.rect(x + 2, 182, 16, 6, gold)
            pixel(b, x + 9, 184, goldLight)
        }
    }

    /** Parede de fundo do salão: madeira, vigas, quadros e luzes animadas. */
    fun backCol(rel: Int, frame: Int): PixBuf = cached("new_bk${rel.coerceIn(0,18)}${frame and 1}", 32, 96) { b ->
        val col = rel.coerceIn(0,18)
        b.rect(0, 0, 32, 96, woodDark)
        for (x in 0..31) {
            b.rect(x, 7, 1, 80, if (x % 8 == 0) woodLight else wood)
            if (x % 8 == 1) b.rect(x, 7, 1, 80, woodMid)
        }
        b.rect(0, 0, 32, 7, outline)
        b.rect(0, 1, 32, 2, woodLight)
        b.rect(0, 88, 32, 8, outline)
        b.rect(0, 88, 32, 2, gold)
        // Painéis em relevo e rebites.
        if (col % 2 == 0) {
            b.rect(4, 20, 24, 48, outline)
            b.rect(6, 22, 20, 44, woodMid)
            b.rect(8, 24, 16, 40, wood)
            b.rect(8, 24, 16, 2, woodLight)
            for (y in 28..60 step 8) b.rect(10, y, 12, 1, woodDark)
            pixel(b, 7, 23, gold); pixel(b, 24, 23, gold)
            if (col == 4 || col == 14) {
                b.rect(10, 29, 12, 28, outline)
                b.rect(12, 31, 8, 24, amber)
                b.rect(13, 32, 3, 19, amberLight)
                b.rect(11, 56, 10, 2, gold)
                if (frame == 1) { pixel(b, 15, 34, h(0xFFFFF5CF)); pixel(b, 17, 38, goldLight) }
            }
        }
        // Brasões intercalados para quebrar a repetição de parede.
        if (col == 2 || col == 8 || col == 10 || col == 16) {
            b.rect(12, 12, 8, 2, gold)
            b.rect(10, 14, 12, 17, outline)
            b.rect(12, 16, 8, 12, roof)
            pixel(b, 15, 18, goldLight)
            b.rect(14, 21, 4, 4, gold)
        }
    }

    fun floor(v: Int): PixBuf = cached("new_fl${v and 3}", 32, 32) { b ->
        b.rect(0, 0, 32, 32, woodDark)
        val base = when (v and 3) { 0 -> h(0xFF98613F); 1 -> h(0xFF865238); 2 -> h(0xFFAA7149); else -> h(0xFF75442F) }
        for (row in 0..3) {
            val y = row * 8
            b.rect(0, y, 32, 7, base)
            b.rect(0, y, 32, 1, woodDark)
            val offset = if (row % 2 == 0) 0 else 10
            for (x in offset..31 step 16) {
                b.rect(x, y + 1, 1, 5, shade(base, -0.24f))
                b.rect(x + 1, y + 1, 1, 1, shade(base, 0.22f))
                b.rect(x + 2, y + 2, 8, 1, shade(base, 0.08f))
                pixel(b, min(31,x+11), y+5, woodLight)
            }
        }
        b.rect(0, 31, 32, 1, outline)
    }

    fun loftFloor(v: Int): PixBuf = cached("new_lf${v and 3}", 32, 32) { b ->
        b.rect(0, 0, 32, 32, outline)
        val base = if ((v and 1) == 0) h(0xFF6E4939) else h(0xFF79503B)
        for (y in 0..31) {
            b.rect(0, y, 32, 1, if (y % 8 == 0) woodDark else base)
            if (y % 8 == 1) b.rect(0, y, 32, 1, woodLight)
        }
        for (x in 0..31 step 8) b.rect(x, 0, 1, 32, woodDark)
        for (x in intArrayOf(4, 20)) { pixel(b, x, 3, gold); pixel(b, x + 1, 4, gold) }
    }

    fun rail(kind: Int): PixBuf = cached("new_rl${kind.coerceIn(0,3)}", 32, 48) { b ->
        val k = kind.coerceIn(0,3)
        b.rect(0, 8, 32, 5, outline)
        b.rect(0, 9, 32, 2, woodLight)
        for (x in if (k == 0) intArrayOf(3,27) else if (k == 2) intArrayOf(4,28) else intArrayOf(4,14,26)) {
            b.rect(x, 10, 4, 31, outline)
            b.rect(x+1, 11, 2, 27, wood)
            b.rect(x+1, 11, 1, 25, woodLight)
            b.rect(x, 38, 4, 3, gold)
        }
        b.rect(0, 39, 32, 5, woodDark)
        b.rect(0, 39, 32, 2, woodLight)
        if (k == 3) {
            b.rect(13, 0, 6, 14, outline); b.rect(14, 2, 4, 9, amber)
            b.rect(15, 3, 2, 5, amberLight); b.rect(12, 10, 8, 2, gold)
        }
    }

    fun stairs(side: Int, part: Int): PixBuf = cached("new_st${side and 1}${part.coerceIn(0,2)}", 32, 48) { b ->
        val s = side and 1; val p = part.coerceIn(0,2)
        b.rect(0, 0, 32, 48, h(0x00000000))
        for (step in 0..5) {
            val y = 7 + step * 6
            val x = if (s == 0) 2 + step * 3 else 12 - step * 2
            val ww = 19 + (step % 2) * 2
            b.rect(x, y, ww, 6, outline)
            b.rect(x+1, y+1, ww-2, 2, woodLight)
            b.rect(x+1, y+3, ww-2, 2, wood)
            b.rect(x+1, y+5, ww-2, 1, woodDark)
        }
        b.rect(if (s==0) 0 else 27, 0, 5, 48, outline)
        b.rect(if (s==0) 1 else 28, 0, 2, 48, wood)
        if (p == 0) b.rect(0, 41, 32, 5, gold)
        if (p == 2) b.rect(0, 3, 32, 3, gold)
    }

    fun bed(kind: Int): PixBuf = cached("new_bd${kind and 1}", 32, 48) { b ->
        val blue = (kind and 1) == 1
        b.rect(3, 3, 26, 43, outline)
        b.rect(5, 5, 22, 39, wood)
        b.rect(6, 7, 20, 11, if (blue) h(0xFF426B91) else h(0xFF9D3F48))
        b.rect(7, 8, 18, 8, if (blue) h(0xFF6D9EC1) else h(0xFFD56868))
        b.rect(7, 18, 18, 23, if (blue) h(0xFF24445F) else h(0xFF6B2934))
        for (y in 20..38 step 4) b.rect(8, y, 16, 1, if (blue) h(0xFF527EA0) else h(0xFFB95159))
        b.rect(7, 18, 18, 2, gold)
        b.rect(7, 41, 18, 2, woodDark)
        b.rect(4, 44, 4, 4, woodDark); b.rect(24, 44, 4, 4, woodDark)
    }

    fun hearth(side: Int, frame: Int): PixBuf = cached("new_h${side and 1}${frame and 1}", 32, 48) { b ->
        val sidePart = side and 1
        b.rect(2, 1, 28, 47, outline)
        b.rect(4, 3, 24, 43, stoneDark)
        for (y in 5..42 step 8) {
            b.rect(4, y, 24, 1, stoneLight)
            b.rect(4 + (y % 3) * 3, y+1, 1, 5, stone)
        }
        b.rect(8, 17, 16, 27, outline)
        b.rect(10, 19, 12, 23, h(0xFF331E25))
        b.rect(12, 30, 8, 10, h(0xFFE15A31))
        b.poly(floatArrayOf(12f,16f,20f), floatArrayOf(37f,if ((frame and 1)==0) 22f else 25f,37f), amber)
        b.poly(floatArrayOf(14f,16f,18f), floatArrayOf(36f,if ((frame and 1)==0) 27f else 24f,36f), amberLight)
        b.rect(7, 41, 18, 4, woodDark)
        b.rect(10, 42, 12, 2, woodLight)
        if (sidePart == 0) b.rect(2, 8, 4, 34, stone) else b.rect(26, 8, 4, 34, stone)
    }

    fun shelf(kind: Int): PixBuf = cached("new_sf${kind.coerceIn(0,2)}", 32, 48) { b ->
        val k = kind.coerceIn(0,2)
        b.rect(2, 3, 28, 43, outline)
        b.rect(4, 5, 24, 39, wood)
        for (y in intArrayOf(15,27,39)) {
            b.rect(4, y, 24, 3, woodDark); b.rect(4, y, 24, 1, woodLight)
        }
        if (k == 0) {
            for ((x,c) in arrayOf(6 to h(0xFFB58B52),12 to h(0xFF6D8F9A),19 to h(0xFF8A4D37),24 to h(0xFFB58B52))) {
                b.rect(x, 7, 4, 7, c); b.rect(x+1, 5, 2, 3, shade(c,0.2f)); pixel(b,x+1,8,gold)
            }
            for (x in intArrayOf(8,17,25)) { b.rect(x,19,3,6,woodLight); b.rect(x,17,3,3,gold) }
        } else if (k == 1) {
            for (i in 0..5) {
                val x=6+i*4; val c=when(i%3){0->h(0xFF7C3441);1->h(0xFF3E697C);else->h(0xFF8E653B)}
                b.rect(x,7,3,6,c); b.rect(x,5,3,3,gold); pixel(b,x+1,8,shade(c,0.25f))
                b.rect(x,19,3,6,c); b.rect(x,17,3,3,gold)
            }
        } else {
            b.rect(6,7,8,6,h(0xFFD6B36B)); b.rect(7,6,6,2,h(0xFFF4D88A))
            b.rect(18,8,8,5,h(0xFF9E483D)); b.rect(19,7,6,2,h(0xFFC9694D))
            b.rect(7,19,5,6,h(0xFFB58B52)); b.rect(17,20,8,5,h(0xFF7B4930))
        }
    }

    fun casks(kind: Int): PixBuf = cached("new_ck${kind and 1}", 32, 48) { b ->
        if ((kind and 1) == 0) {
            b.rect(2, 12, 28, 30, outline); b.rect(4, 14, 24, 26, wood)
            b.rect(4, 17, 24, 3, woodDark); b.rect(4, 34, 24, 3, woodDark)
            b.rect(6, 15, 20, 1, woodLight); b.rect(6, 38, 20, 1, woodLight)
            b.rect(7, 20, 2, 12, woodLight); b.rect(23, 20, 2, 12, woodDark)
            b.rect(0, 39, 32, 5, woodDark); b.rect(2, 40, 28, 2, woodLight)
        } else {
            b.rect(5, 2, 22, 44, outline); b.rect(7, 4, 18, 40, wood)
            b.rect(6, 9, 20, 3, gold); b.rect(6, 35, 20, 3, gold)
            b.rect(8, 6, 3, 31, woodLight); b.rect(21, 6, 2, 31, woodDark)
            b.ellipse(16f,5f,9f,3f,woodDark,false); b.ellipse(16f,5f,6f,2f,woodLight,false)
        }
    }

    fun counter(part: Int): PixBuf = cached("new_cn${part.coerceIn(0,5)}", 32, 40) { b ->
        val k = part.coerceIn(0,5)
        b.rect(0, 7, 32, 33, outline)
        b.rect(2, 10, 28, 28, wood)
        for (x in 3..28 step 6) {
            b.rect(x, 12, 1, 22, woodLight)
            b.rect(x+2, 13, 2, 20, woodMid)
        }
        b.rect(0, 6, 32, 7, woodDark)
        b.rect(1, 7, 30, 3, woodLight)
        b.rect(2, 10, 28, 2, gold)
        b.rect(0, 36, 32, 3, woodDark)
        if (k == 0) b.rect(0, 5, 5, 9, woodLight)
        if (k == 2) b.rect(27, 5, 5, 9, woodLight)
        if (k == 3) {
            b.rect(11, 0, 10, 13, outline); b.rect(13, 2, 6, 8, stone)
            b.rect(14, 3, 4, 6, gold); b.rect(15, 4, 2, 4, amberLight)
        }
        if (k == 4) {
            for (x in intArrayOf(7,15,23)) {
                b.rect(x, 0, 5, 7, gold); b.rect(x+1, 1, 3, 4, amber)
                b.rect(x+4, 2, 2, 3, gold)
            }
        }
        if (k == 5) {
            b.rect(7, 0, 18, 8, woodDark); b.rect(9, 1, 14, 5, h(0xFFD6C7A0))
            b.rect(11, 2, 10, 1, wood); b.rect(11, 4, 7, 1, wood)
            pixel(b,24,3,amber)
        }
    }

    fun table(half: Int, variant: Int): PixBuf = cached("new_tb${(variant and 1)*2 + (half and 1)}", 32, 32) { b ->
        val v = variant and 1
        b.rect(2, 5, 28, 24, outline)
        b.rect(4, 7, 24, 20, if (v==0) woodMid else wood)
        b.rect(5, 8, 22, 3, woodLight)
        for (x in 7..26 step 6) {
            b.rect(x, 11, 1, 13, woodDark)
            b.rect(x+1, 12, 2, 11, wood)
        }
        b.rect(4, 25, 24, 2, gold)
        b.rect(6, 27, 4, 5, woodDark); b.rect(22, 27, 4, 5, woodDark)
        if ((half and 1)==0) {
            b.rect(10, 14, 7, 5, h(0xFF6C8D91)); b.rect(11, 13, 5, 2, gold)
            b.rect(20, 14, 5, 4, h(0xFFB98C51))
        } else {
            b.rect(8, 15, 5, 5, h(0xFFB98C51)); b.rect(9, 14, 3, 2, gold)
            b.rect(19, 13, 6, 7, h(0xFF6C8D91)); b.rect(20, 12, 4, 2, gold)
        }
    }

    fun stool(): PixBuf = cached("new_stool", 32, 32) { b ->
        b.rect(7, 7, 18, 17, outline)
        b.rect(9, 9, 14, 12, woodMid)
        b.rect(10, 10, 12, 2, woodLight)
        b.rect(10, 21, 3, 9, woodDark); b.rect(20, 21, 3, 9, woodDark)
        b.rect(8, 19, 16, 3, woodDark)
        pixel(b, 12, 13, gold); pixel(b, 19, 16, gold)
    }
}
