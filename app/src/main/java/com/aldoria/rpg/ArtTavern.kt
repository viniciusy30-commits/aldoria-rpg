package com.aldoria.rpg

import android.content.Context

/**
 * Caneca Dourada — pixel art premium (reconstruida do zero).
 * Fachada de pedra + enxaimel, portal ogival, salão com lareira, balcão, mesas e galeria.
 * Não depende mais dos tilesets externos (que ficavam desalinhados e bugados).
 */
object ArtTavern {
    const val STYLE = 6
    const val DOOR_KIND = 6

    private val STONE = h(0xFF8A8480)
    private val STONE_LT = h(0xFFA8A29C)
    private val STONE_DK = h(0xFF5E5854)
    private val PLASTER = h(0xFFE8D9B8)
    private val PLASTER_DK = h(0xFFC9B896)
    private val BEAM = h(0xFF4A2E16)
    private val BEAM_LT = h(0xFF6B4423)
    private val WOOD = h(0xFF8A5A2B)
    private val WOOD_LT = h(0xFFB07A44)
    private val WOOD_DK = h(0xFF5A3A1A)
    private val TILE_R = h(0xFF8A3A28)
    private val TILE_R2 = h(0xFF9A4630)
    private val TILE_R3 = h(0xFF7A3022)
    private val IRON = h(0xFF2A2A32)
    private val GOLD = h(0xFFF2C94C)
    private val AMBER = h(0xFFFFC66A)
    private val GLASS = h(0xFF8EC8E8)
    private val GLASS_DK = h(0xFF5A90B0)
    private val MUG = h(0xFFE8C070)
    private val FLOOR_A = h(0xFF6B4423)
    private val FLOOR_B = h(0xFF7A5230)
    private val FLOOR_C = h(0xFF5A3A1E)

    fun init(@Suppress("UNUSED_PARAMETER") context: Context) {
        // procedural — nada a carregar
    }

    private fun cached(key: String, w: Int, h: Int, draw: (PixBuf) -> Unit): PixBuf {
        // cache local simples por sessão de render (o Renderer já cacheia bitmaps)
        return PixBuf(w, h).also(draw)
    }

    private fun stoneBase(b: PixBuf, y0: Int, y1: Int, seed: Int) {
        val rn = java.util.Random(4200L + seed)
        b.rect(0, y0, 32, y1 - y0, STONE)
        var row = 0
        var y = y0
        while (y < y1) {
            val hh = minOf(6, y1 - y)
            b.rect(0, y, 32, 1, STONE_DK)
            var x = if (row % 2 == 0) 0 else -5
            while (x < 32) {
                val bw = if ((x / 10 + row) % 2 == 0) 11 else 9
                if (x >= 0) b.rect(x, y, 1, hh, STONE_DK)
                for (k in 1 until bw) {
                    val px = x + k
                    if (px in 0..31) {
                        val c = if (k == 1) STONE_LT else if (k == bw - 1) STONE_DK else STONE
                        b.set(px, y + 1, shadeCol(c, 0.08f))
                        if (rn.nextInt(6) == 0) b.set(px, y + 2 + rn.nextInt(maxOf(1, hh - 3)), shadeCol(c, if (rn.nextBoolean()) -0.12f else 0.1f))
                    }
                }
                x += bw
            }
            y += hh
            row++
        }
        // musgo na base
        for (i in 0 until 10) b.set(rn.nextInt(32), y1 - 1 - rn.nextInt(3), h(0xFF3A6A2C))
    }

    private fun timberFrame(b: PixBuf, y0: Int, y1: Int) {
        b.rect(0, y0, 3, y1 - y0, BEAM)
        b.rect(0, y0, 1, y1 - y0, BEAM_LT)
        b.rect(29, y0, 3, y1 - y0, BEAM)
        b.rect(31, y0, 1, y1 - y0, WOOD_DK)
        b.rect(0, y0, 32, 2, BEAM)
        b.rect(0, y0, 32, 1, BEAM_LT)
        // cruz diagonal leve
        for (i in 0 until 18) {
            val x = 4 + i
            val y = y0 + 4 + i
            if (y < y1 - 2) {
                b.set(x, y, BEAM)
                b.set(x + 1, y, shadeCol(BEAM, -0.2f))
            }
        }
    }

    private fun windowAmber(b: PixBuf, cx: Int, cy: Int, lit: Boolean) {
        val fr = BEAM
        b.rect(cx - 5, cy - 1, 11, 14, fr)
        b.rect(cx - 4, cy, 9, 12, if (lit) AMBER else GLASS)
        b.rect(cx - 4, cy, 9, 1, if (lit) h(0xFFFFE0A0) else h(0xFFD6F0FC))
        b.rect(cx - 1, cy, 2, 12, fr)
        b.rect(cx - 4, cy + 5, 9, 1, fr)
        // brilho
        if (lit) {
            b.set(cx - 3, cy + 2, h(0xFFFFFFC8))
            b.set(cx - 2, cy + 3, h(0xFFFFFFC8))
        } else {
            b.set(cx - 3, cy + 2, h(0xFFEAF7FF))
        }
        // peitoril + floreira
        b.rect(cx - 6, cy + 13, 13, 2, WOOD)
        b.rect(cx - 6, cy + 13, 13, 1, WOOD_LT)
        val fc = intArrayOf(h(0xFFE8505B), h(0xFFF5D547), h(0xFFFF8FB7))
        for (k in 0 until 5) {
            b.set(cx - 4 + k * 2, cy + 12, h(0xFF3F9A4A))
            b.set(cx - 4 + k * 2, cy + 11, fc[k % 3])
        }
    }

    private fun lantern(b: PixBuf, x: Int, y: Int, frame: Int) {
        b.rect(x, y, 2, 10, IRON)
        b.rect(x - 2, y + 8, 6, 2, IRON)
        b.rect(x - 1, y + 10, 4, 6, if (frame == 0) AMBER else GOLD)
        b.rect(x, y + 11, 2, 4, h(0xFFFFF0C0))
        b.rect(x - 1, y + 16, 4, 1, IRON)
        b.set(x, y + 9, h(0xFF6A6A74))
    }

    private fun mugSign(b: PixBuf, cx: Int, cy: Int) {
        // placa de madeira
        b.rect(cx - 8, cy - 6, 17, 14, WOOD)
        b.rect(cx - 8, cy - 6, 17, 1, WOOD_LT)
        b.rect(cx - 8, cy + 7, 17, 1, WOOD_DK)
        b.rect(cx - 8, cy - 6, 1, 14, WOOD_LT)
        // caneca
        b.rect(cx - 3, cy - 2, 7, 8, MUG)
        b.rect(cx - 3, cy - 2, 7, 1, h(0xFFF8E0A0))
        b.rect(cx - 2, cy - 1, 5, 6, h(0xFFD4A050))
        // espuma
        b.rect(cx - 2, cy - 3, 5, 2, h(0xFFF5F0E0))
        b.set(cx - 1, cy - 4, h(0xFFFFFFFF))
        b.set(cx + 1, cy - 4, h(0xFFFFFFFF))
        // alça
        b.rect(cx + 4, cy, 3, 1, MUG)
        b.rect(cx + 6, cy + 1, 1, 3, MUG)
        b.rect(cx + 4, cy + 4, 3, 1, MUG)
        // pregos
        b.set(cx - 7, cy - 5, IRON)
        b.set(cx + 7, cy - 5, IRON)
        b.set(cx - 7, cy + 6, IRON)
        b.set(cx + 7, cy + 6, IRON)
    }

    /** Coluna de fachada 32×192 (6 tiles de altura). */
    fun facadeCol(rel: Int): PixBuf = cached("facade_$rel", 32, 192) { b ->
        val rn = java.util.Random(9000L + rel * 17)
        // pedra térreo
        stoneBase(b, 128, 192, rel)
        // reboco + enxaimel no 1º andar
        b.rect(0, 48, 32, 80, PLASTER)
        for (i in 0 until 40) b.set(rn.nextInt(32), 50 + rn.nextInt(74), shadeCol(PLASTER, if (rn.nextBoolean()) 0.05f else -0.07f))
        timberFrame(b, 48, 128)
        // viga entre andares
        b.rect(0, 120, 32, 8, BEAM)
        b.rect(0, 120, 32, 2, BEAM_LT)
        b.rect(0, 126, 32, 2, WOOD_DK)
        // detalhes por coluna
        when (rel % 7) {
            0 -> {
                for (i in 0 until 45) {
                    val x = 1 + rn.nextInt(12)
                    val y = 55 + rn.nextInt(60)
                    b.set(x, y, intArrayOf(h(0xFF2F7A3A), h(0xFF3F9A4A), h(0xFF245E2E), h(0xFF4AAA3A))[rn.nextInt(4)])
                }
                b.ellipse(10f, 170f, 7f, 9f, WOOD, true)
                b.rect(4, 168, 13, 2, IRON)
            }
            1, 5 -> {
                windowAmber(b, 16, 62, true)
                lantern(b, 15, 95, 0)
            }
            2, 4 -> {
                windowAmber(b, 16, 62, rel % 2 == 0)
            }
            3 -> {
                mugSign(b, 16, 72)
                lantern(b, 6, 98, 1)
                lantern(b, 24, 98, 0)
            }
            6 -> {
                for (i in 0 until 40) {
                    val x = 18 + rn.nextInt(12)
                    val y = 55 + rn.nextInt(60)
                    b.set(x, y, intArrayOf(h(0xFF2F7A3A), h(0xFF3F9A4A), h(0xFF245E2E))[rn.nextInt(3)])
                }
                b.ellipse(22f, 172f, 7f, 9f, WOOD, true)
                b.rect(16, 170, 13, 2, IRON)
            }
        }
        // sombra superior (telhado começa acima)
        for (y in 0 until 48) {
            val t = 1f - y / 48f
            b.rect(0, y, 32, 1, shadeCol(PLASTER, -0.15f * t))
        }
        // base escura
        b.rect(0, 190, 32, 2, STONE_DK)
    }

    /** Parede baixa ao entrar (só a base da fachada). */
    fun facadeCut(rel: Int): PixBuf = cached("cut_$rel", 32, 192) { b ->
        stoneBase(b, 160, 192, rel + 50)
        // viga do peitoril
        b.rect(0, 156, 32, 4, BEAM)
        b.rect(0, 156, 32, 1, BEAM_LT)
        // um pouco de reboco residual
        b.rect(0, 148, 32, 8, PLASTER_DK)
        if (rel % 3 == 1) {
            // jarro / vaso no peitoril
            b.rect(12, 140, 8, 10, WOOD)
            b.rect(12, 140, 8, 1, WOOD_LT)
            b.set(14, 138, h(0xFF3F9A4A)); b.set(17, 137, h(0xFFE8505B))
        }
    }

    /** Portal de 3 tiles (96×192) com arco de pedra e porta de madeira. */
    fun doorBig(frame: Int): PixBuf = cached("door_$frame", 96, 192) { b ->
        // laterais de pedra
        for (side in 0..1) {
            val ox = side * 64
            stoneBase(b, 64, 192, 200 + side)
            b.rect(ox, 48, 32, 16, PLASTER)
            timberFrame(b, 48, 64)
        }
        // vão central
        b.rect(32, 48, 32, 144, STONE)
        // arco ogival
        val cx = 48
        for (y in 48 until 80) {
            val dy = y - 48
            val half = 14 - dy / 3
            for (x in (cx - half)..(cx + half)) {
                if (x in 32..63) {
                    val edge = x == cx - half || x == cx + half
                    b.set(x, y, if (edge) STONE_DK else STONE_LT)
                }
            }
        }
        // porta de madeira (abre para a direita conforme frame)
        val shift = (frame.coerceIn(0, 4) * 4)
        val doorX = 36 + shift
        val doorW = 24 - shift / 2
        if (doorW > 4) {
            b.rect(doorX, 80, doorW, 100, WOOD)
            b.rect(doorX, 80, doorW, 2, WOOD_LT)
            b.rect(doorX, 178, doorW, 2, WOOD_DK)
            // tábuas
            for (k in 0 until 4) {
                val x = doorX + 2 + k * (doorW / 4)
                if (x < doorX + doorW - 1) b.rect(x, 82, 1, 96, WOOD_DK)
            }
            // ferragens
            b.rect(doorX + 2, 100, doorW - 4, 3, IRON)
            b.rect(doorX + 2, 140, doorW - 4, 3, IRON)
            // aldrava / maçaneta
            if (shift < 8) {
                b.ellipse((doorX + doorW - 6).toFloat(), 130f, 2.5f, 2.5f, GOLD, true)
                b.set(doorX + doorW - 6, 130, h(0xFFFFF0A0))
            }
        }
        // soleira
        b.rect(32, 180, 32, 4, STONE_LT)
        b.rect(32, 180, 32, 1, h(0xFFC4C0BA))
        // lampião sobre a porta
        lantern(b, 46, 52, frame and 1)
        // placa pequena acima
        b.rect(40, 40, 16, 8, WOOD)
        b.rect(42, 42, 5, 5, MUG)
        b.set(47, 43, h(0xFFF5F0E0))
    }

    /** Parede de fundo do salão (32×96). */
    fun backCol(rel: Int, frame: Int): PixBuf = cached("back_${rel}_$frame", 32, 96) { b ->
        val rn = java.util.Random(11000L + rel)
        // madeira escura vertical
        b.rect(0, 0, 32, 96, WOOD_DK)
        for (x in 0 until 32 step 4) {
            b.rect(x, 0, 1, 96, shadeCol(WOOD_DK, -0.15f))
            b.rect(x + 1, 0, 2, 96, WOOD)
            b.rect(x + 1, 0, 1, 96, WOOD_LT)
        }
        // moldura superior
        b.rect(0, 0, 32, 4, BEAM)
        b.rect(0, 0, 32, 1, BEAM_LT)
        when (rel % 6) {
            0, 5 -> {
                // tapeçaria
                b.rect(6, 12, 20, 28, h(0xFF8A2A2A))
                b.rect(6, 12, 20, 2, h(0xFFB03A3A))
                for (k in 0 until 5) b.rect(8 + k * 3, 16, 2, 20, h(0xFFC49A2A))
                b.rect(6, 38, 20, 2, h(0xFF5A1818))
            }
            1, 4 -> {
                // janela com luz âmbar
                b.rect(8, 14, 16, 20, BEAM)
                b.rect(10, 16, 12, 16, if ((frame and 1) == 0) AMBER else h(0xFFFFD080))
                b.rect(15, 16, 2, 16, BEAM)
                b.rect(10, 23, 12, 2, BEAM)
                b.set(11, 18, h(0xFFFFFFC8))
            }
            2 -> {
                // tocha
                b.rect(15, 30, 2, 18, IRON)
                b.rect(13, 28, 6, 3, IRON)
                if ((frame and 1) == 0) {
                    b.ellipse(16f, 24f, 3f, 5f, h(0xFFFF6A1D), false)
                    b.ellipse(16f, 23f, 1.6f, 3f, h(0xFFFFE066), false)
                } else {
                    b.ellipse(16.5f, 23.5f, 2.8f, 5.5f, h(0xFFFF6A1D), false)
                    b.ellipse(16f, 22.5f, 1.5f, 3.2f, h(0xFFFFE066), false)
                }
            }
            3 -> {
                // escudo com espadas
                b.ellipse(16f, 28f, 8f, 10f, h(0xFF3A5A8A), true)
                b.ellipse(16f, 28f, 6f, 8f, h(0xFF5A7AB0), true)
                b.rect(15, 20, 2, 16, GOLD)
                b.rect(10, 22, 12, 2, h(0xFFC0C0C8))
                b.rect(10, 32, 12, 2, h(0xFFC0C0C8))
            }
        }
        // rodapé
        b.rect(0, 90, 32, 6, BEAM)
        b.rect(0, 90, 32, 1, BEAM_LT)
    }

    fun floor(v: Int): PixBuf = cached("floor_$v", 32, 32) { b ->
        val base = if ((v and 1) == 0) FLOOR_A else FLOOR_B
        b.rect(0, 0, 32, 32, base)
        for (y in 0 until 32 step 4) {
            b.rect(0, y, 32, 1, FLOOR_C)
            val off = if ((y / 4) % 2 == 0) 0 else 8
            for (x in off until 32 step 16) b.rect(x, y, 1, 4, shadeCol(base, -0.2f))
        }
        for (i in 0 until 8) {
            val rn = java.util.Random(v * 31L + i)
            b.set(rn.nextInt(32), rn.nextInt(32), shadeCol(base, if (rn.nextBoolean()) 0.08f else -0.1f))
        }
    }

    fun loftFloor(v: Int): PixBuf = cached("loft_$v", 32, 32) { b ->
        b.rect(0, 0, 32, 32, WOOD)
        for (y in 0 until 32 step 3) b.rect(0, y, 32, 1, WOOD_DK)
        for (x in 0 until 32 step 8) b.rect(x, 0, 1, 32, shadeCol(WOOD, -0.15f))
        if ((v and 1) == 1) b.rect(0, 0, 32, 1, BEAM)
    }

    fun rail(kind: Int): PixBuf = cached("rail_$kind", 32, 48) { b ->
        // balaustrada de madeira
        b.rect(0, 20, 32, 3, WOOD)
        b.rect(0, 20, 32, 1, WOOD_LT)
        b.rect(0, 32, 32, 2, WOOD_DK)
        when (kind) {
            0 -> { // canto esquerdo
                b.rect(2, 8, 4, 28, WOOD)
                b.rect(2, 8, 1, 28, WOOD_LT)
            }
            1, 2 -> { // meio
                for (x in intArrayOf(4, 14, 24)) {
                    b.rect(x, 10, 3, 22, WOOD)
                    b.rect(x, 10, 1, 22, WOOD_LT)
                    b.set(x + 1, 9, WOOD)
                }
            }
            else -> { // canto direito
                b.rect(26, 8, 4, 28, WOOD)
                b.rect(29, 8, 1, 28, WOOD_DK)
            }
        }
        b.outline(h(0xFF2A1A0E))
        b.groundShadow(16f, 46f, 12f, 1.6f)
    }

    fun stairs(side: Int, part: Int): PixBuf = cached("stairs_${side}_$part", 32, 48) { b ->
        val dir = if ((side and 1) == 0) 1 else -1
        val baseX = if (dir > 0) 4 else 20
        for (k in 0..part) {
            val y = 36 - k * 8
            val x = baseX + dir * k * 4
            b.rect(x, y, 12, 6, WOOD)
            b.rect(x, y, 12, 1, WOOD_LT)
            b.rect(x, y + 5, 12, 1, WOOD_DK)
        }
        // corrimão
        b.rect(if (dir > 0) 2 else 28, 8, 2, 36, BEAM)
        b.outline(h(0xFF2A1A0E))
        b.groundShadow(16f, 46f, 10f, 1.5f)
    }

    fun bed(kind: Int): PixBuf = cached("bed_$kind", 32, 48) { b ->
        b.rect(4, 18, 24, 22, WOOD)
        b.rect(4, 18, 24, 2, WOOD_LT)
        b.rect(6, 22, 20, 14, if (kind == 0) h(0xFF8A2A2A) else h(0xFF2A4A8A))
        b.rect(6, 22, 20, 2, if (kind == 0) h(0xFFB03A3A) else h(0xFF3A6AB0))
        b.ellipse(16f, 20f, 7f, 3f, h(0xFFF2EAD6), true)
        b.rect(6, 20, 20, 3, h(0xFFE8E0D0))
        b.rect(4, 38, 3, 4, WOOD_DK)
        b.rect(25, 38, 3, 4, WOOD_DK)
        b.outline(h(0xFF2A1A0E))
        b.groundShadow(16f, 44f, 12f, 1.8f)
    }

    fun hearth(side: Int, frame: Int): PixBuf = cached("hearth_${side}_$frame", 32, 48) { b ->
        // lareira de pedra
        b.rect(2, 8, 28, 36, STONE)
        for (y in 8 until 44 step 5) {
            b.rect(2, y, 28, 1, STONE_DK)
            var x = if ((y / 5) % 2 == 0) 2 else -3
            while (x < 30) {
                if (x >= 2) b.rect(x, y, 1, 5, STONE_DK)
                x += 10
            }
        }
        b.rect(6, 14, 20, 26, h(0xFF14100C))
        b.ellipse(16f, 14f, 10f, 4f, h(0xFF14100C), false)
        // fogo
        val fy = 32f
        if ((frame and 1) == 0) {
            b.ellipse(16f, fy, 6f, 8f, h(0xFFFF6A1D), false)
            b.ellipse(16f, fy + 1f, 3.5f, 5f, h(0xFFFFA82D), false)
            b.ellipse(16f, fy + 2f, 1.8f, 3f, h(0xFFFFF0A0), false)
        } else {
            b.ellipse(16.5f, fy - 1f, 5.5f, 9f, h(0xFFFF6A1D), false)
            b.ellipse(16f, fy + 0.5f, 3.2f, 5.5f, h(0xFFFFA82D), false)
            b.ellipse(16f, fy + 2f, 1.6f, 3.2f, h(0xFFFFF0A0), false)
        }
        // mantos
        b.rect(4, 8, 24, 4, BEAM)
        b.rect(4, 8, 24, 1, BEAM_LT)
        // brasas
        b.set(10, 40, h(0xFFFF4A1D)); b.set(22, 41, h(0xFFFF8A2D))
        b.outline(h(0xFF2A1A0E))
    }

    fun shelf(kind: Int): PixBuf = cached("shelf_$kind", 32, 48) { b ->
        b.rect(2, 4, 28, 40, WOOD_DK)
        b.rect(2, 4, 28, 1, WOOD_LT)
        val rn = java.util.Random(7000L + kind)
        val cols = intArrayOf(h(0xFFE8505B), h(0xFF5CC85A), h(0xFF5AA8F0), h(0xFFF5D547), h(0xFFB07AFF), MUG)
        for (r in 0 until 3) {
            val y0 = 8 + r * 12
            b.rect(4, y0, 24, 10, h(0xFF2A1A0E))
            var x = 5
            while (x < 26) {
                val c = cols[rn.nextInt(cols.size)]
                val tall = rn.nextBoolean()
                b.ellipse(x + 1.5f, (y0 + 8).toFloat(), 2.2f, if (tall) 2.6f else 2f, c, true)
                b.rect(x + 1, y0 + 2, 1, 2, h(0xFFDDE6E8))
                x += 4 + rn.nextInt(2)
            }
            b.rect(3, y0 + 10, 26, 1, WOOD)
        }
        b.outline(h(0xFF2A1A0E))
    }

    fun casks(kind: Int): PixBuf = cached("casks_$kind", 32, 48) { b ->
        // dois barris deitados em cavalete
        fun barrel(cx: Float, cy: Float) {
            b.ellipse(cx, cy, 10f, 7f, WOOD, true)
            for (x in (cx - 9).toInt()..(cx + 9).toInt()) {
                val t = (x - cx) / 10f
                val c = if (t < -0.4f) WOOD_LT else if (t > 0.4f) WOOD_DK else WOOD
                for (y in (cy - 6).toInt()..(cy + 6).toInt()) {
                    if (b.get(x, y) != 0) b.set(x, y, c)
                }
            }
            b.rect((cx - 9).toInt(), (cy - 1).toInt(), 18, 2, IRON)
            b.rect((cx - 9).toInt(), (cy - 1).toInt(), 18, 1, h(0xFF6A6A74))
        }
        barrel(12f, 28f)
        if (kind == 0) barrel(22f, 22f) else barrel(20f, 30f)
        // cavalete
        b.rect(4, 34, 24, 3, BEAM)
        b.rect(6, 37, 3, 8, BEAM)
        b.rect(23, 37, 3, 8, BEAM)
        b.outline(h(0xFF2A1A0E))
        b.groundShadow(16f, 46f, 13f, 1.8f)
    }

    fun counter(part: Int): PixBuf = cached("counter_$part", 32, 40) { b ->
        b.rect(0, 14, 32, 6, WOOD_LT)
        b.rect(0, 14, 32, 1, h(0xFFC08A52))
        b.rect(0, 20, 32, 18, WOOD)
        b.rect(0, 20, 32, 1, WOOD_DK)
        for (k in 0 until 4) {
            b.rect(k * 8 + 1, 22, 6, 14, shadeCol(WOOD, -0.08f))
            b.rect(k * 8 + 1, 22, 6, 1, shadeCol(WOOD, 0.12f))
        }
        b.rect(0, 38, 32, 2, WOOD_DK)
        when (part % 6) {
            0, 2, 4 -> {
                // torneira
                b.rect(14, 6, 4, 8, IRON)
                b.rect(12, 6, 8, 2, IRON)
                b.rect(18, 8, 4, 2, IRON)
                b.set(20, 10, h(0xFF8CC4F5))
            }
            1 -> {
                // canecas
                b.rect(8, 8, 5, 6, MUG)
                b.rect(8, 7, 5, 2, h(0xFFF5F0E0))
                b.rect(20, 9, 5, 5, MUG)
                b.rect(20, 8, 5, 2, h(0xFFF5F0E0))
            }
            3 -> {
                // prato / comida
                b.ellipse(16f, 10f, 6f, 3f, h(0xFFE8E8F0), true)
                b.rect(13, 7, 6, 3, h(0xFFD25A3A))
            }
            else -> {
                // caixa registradora / pote de ouro
                b.rect(12, 6, 8, 8, GOLD)
                b.rect(12, 6, 8, 1, h(0xFFFFF0A0))
                b.rect(14, 9, 4, 3, WOOD_DK)
            }
        }
        b.outline(h(0xFF2A1A0E))
    }

    fun table(half: Int, variant: Int): PixBuf = cached("table_${half}_$variant", 32, 32) { b ->
        val wood = if (variant == 0) WOOD else WOOD_LT
        b.rect(2, 14, 28, 4, wood)
        b.rect(2, 14, 28, 1, shadeCol(wood, 0.25f))
        b.rect(4, 18, 3, 12, WOOD_DK)
        b.rect(25, 18, 3, 12, WOOD_DK)
        // toalha / canecas
        if (half == 0) {
            b.rect(6, 12, 10, 3, h(0xFF8A2A2A))
            b.ellipse(10f, 11f, 2.5f, 2f, MUG, true)
            b.set(10, 9, h(0xFFF5F0E0))
        } else {
            b.rect(16, 12, 10, 3, h(0xFF8A2A2A))
            b.ellipse(22f, 11f, 2.5f, 2f, MUG, true)
            b.set(22, 9, h(0xFFF5F0E0))
            b.ellipse(18f, 12f, 2f, 1.5f, h(0xFFE8E8F0), true)
        }
        b.outline(h(0xFF2A1A0E))
        b.groundShadow(16f, 30f, 13f, 1.6f)
    }

    fun stool(): PixBuf = cached("stool", 32, 32) { b ->
        b.ellipse(16f, 14f, 8f, 4f, WOOD, true)
        b.ellipse(16f, 14f, 8f, 4f, shadeCol(WOOD, 0.15f), false)
        b.rect(10, 16, 2, 12, WOOD_DK)
        b.rect(20, 16, 2, 12, WOOD_DK)
        b.rect(14, 18, 4, 10, WOOD_DK)
        b.outline(h(0xFF2A1A0E))
        b.groundShadow(16f, 29f, 8f, 1.4f)
    }


    /** Telhado da taverna (21 tiles de largura): telhas, empena e duas chaminés. */
    fun roof(): PixBuf = cached("roof_tavern", 672, 318) { b ->
        val tiles = intArrayOf(TILE_R, TILE_R2, TILE_R3, h(0xFF92402C))
        // quatro fiadas triangulares
        for (row in 0..3) {
            val y = row * 28
            val half = ((row + 1) * 84)
            var col = 0
            var x = 0
            while (x < 672) {
                if (kotlin.math.abs((x + 16) - 336) <= half) {
                    val c = tiles[(col + row * 2) % tiles.size]
                    b.rect(x, y, 32, 28, c)
                    b.rect(x, y, 32, 2, shadeCol(c, 0.2f))
                    b.rect(x, y + 26, 32, 2, shadeCol(c, -0.25f))
                    b.rect(x, y, 1, 28, shadeCol(c, -0.3f))
                }
                x += 32
                col++
            }
        }
        // empena / wall under roof peak
        for (row in 0..5) {
            val y = 112 + row * 28
            var x = 0
            var col = 0
            while (x < 672) {
                val c = if ((col + row) % 3 == 0) PLASTER_DK else PLASTER
                b.rect(x, y, 32, 28, c)
                b.rect(x, y, 3, 28, BEAM)
                b.rect(x + 29, y, 3, 28, BEAM)
                x += 32
                col++
            }
        }
        // cumeeira
        b.rect(0, 0, 672, 4, BEAM)
        b.rect(300, 0, 72, 8, BEAM_LT)
        // duas chaminés
        for (cx in intArrayOf(120, 520)) {
            b.rect(cx, 8, 28, 48, STONE)
            b.rect(cx, 8, 28, 2, STONE_LT)
            b.rect(cx + 2, 4, 24, 8, STONE_DK)
            b.rect(cx + 4, 0, 20, 6, STONE)
            for (k in 0 until 3) b.rect(cx, 16 + k * 12, 28, 1, STONE_DK)
        }
        // beiral
        b.rect(0, 108, 672, 6, BEAM)
        b.rect(0, 108, 672, 2, BEAM_LT)
    }

    // Compatibilidade
    fun facade(kind: Int, edge: Int, frame: Int): PixBuf = facadeCol((kind + edge + frame).coerceIn(0, 20))
    fun backWall(kind: Int, edge: Int, frame: Int): PixBuf = backCol((kind + edge).coerceIn(0, 20), frame)
}
