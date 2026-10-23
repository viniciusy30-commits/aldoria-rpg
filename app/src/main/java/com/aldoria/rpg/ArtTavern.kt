package com.aldoria.rpg

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory

/**
 * Caneca Dourada: sprites reais de um tileset medieval modular.
 * As folhas PNG são usadas com nearest-neighbour para preservar pixels nítidos.
 * Créditos e licença em assets/PIXEL_ART_CREDITS.txt.
 */
object ArtTavern {
    const val STYLE = 6
    const val DOOR_KIND = 6
    private var exterior: Bitmap? = null
    private var interior: Bitmap? = null
    private val cache = HashMap<String, PixBuf>()

    @Synchronized
    fun init(context: Context) {
        if (exterior != null && interior != null) return
        exterior = context.assets.open("tavern_medieval_exterior.png").use { BitmapFactory.decodeStream(it) }
        interior = context.assets.open("tavern_medieval_interior.png").use { BitmapFactory.decodeStream(it) }
        cache.clear()
    }

    private fun ext(): Bitmap = exterior ?: error("ArtTavern.init(context) must be called first")
    private fun inside(): Bitmap = interior ?: error("ArtTavern.init(context) must be called first")

    private fun cached(key: String, w: Int, h: Int, draw: (PixBuf) -> Unit): PixBuf {
        cache[key]?.let { return it }
        return PixBuf(w, h).also { draw(it); cache[key] = it }
    }

    /** Copia pixels da folha original com ampliação 2x, sem suavização. */
    private fun blit2(dst: PixBuf, src: Bitmap, sx: Int, sy: Int, sw: Int, sh: Int, dx: Int, dy: Int) {
        if (sx < 0 || sy < 0 || sw <= 0 || sh <= 0 || sx + sw > src.width || sy + sh > src.height) return
        val pixels = IntArray(sw * sh)
        src.getPixels(pixels, 0, sw, sx, sy, sw, sh)
        for (y in 0 until sh) for (x in 0 until sw) {
            val color = pixels[y * sw + x]
            if ((color ushr 24) == 0) continue
            val ox = dx + x * 2
            val oy = dy + y * 2
            dst.set(ox, oy, color); dst.set(ox + 1, oy, color)
            dst.set(ox, oy + 1, color); dst.set(ox + 1, oy + 1, color)
        }
    }

    private fun wallColumn(rel: Int): Int = when (rel) {
        3, 15 -> 144
        7, 12 -> 128
        else -> when (rel % 5) { 1, 4 -> 128; else -> 96 }
    }

    /** Coluna de fachada: quatro fiadas reais de parede e área superior transparente para o telhado. */
    fun facadeCol(rel: Int): PixBuf = cached("facade_" + rel, 32, 192) { b ->
        val sx = wallColumn(rel.coerceAtLeast(0))
        for (row in 0..3) blit2(b, ext(), sx, 48 + row * 16, 16, 16, 0, 64 + row * 32)
    }

    /** Parede baixa ao entrar: deixa o salão visível e mantém a base da fachada. */
    fun facadeCut(rel: Int): PixBuf = cached("cut_" + rel, 32, 192) { b ->
        blit2(b, ext(), wallColumn(rel.coerceAtLeast(0)), 96, 16, 16, 0, 160)
    }

    /** Portal de três tiles, com ombreiras de pedra e porta de madeira do tileset. */
    fun doorBig(frame: Int): PixBuf = cached("door_" + frame, 96, 192) { b ->
        val src = ext()
        for (row in 0..5) {
            val sy = 48 + row.coerceAtMost(3) * 16
            blit2(b, src, 160, sy, 16, 16, 0, row * 32)
            blit2(b, src, 176, sy, 16, 16, 64, row * 32)
        }
        blit2(b, src, 160, 48, 16, 16, 32, 0)
        blit2(b, src, 176, 48, 16, 16, 32, 32)
        val shift = if (frame == 0) 0 else frame.coerceIn(1, 4) * 3
        blit2(b, src, 112, 80, 16, 32, 32 + shift, 128)
    }

    /** Silhueta nova: telhado de duas águas em telhas reais, com empena de madeira abaixo. */
    fun roof(): PixBuf = cached("roof_real", 694, 222) { b ->
        val src = ext()
        val tiles = arrayOf(intArrayOf(176, 16), intArrayOf(192, 16), intArrayOf(176, 32), intArrayOf(192, 32))
        // Quatro fiadas triangulares: a largura cresce de forma escalonada até os beirais.
        for (row in 0..3) {
            val y = row * 32
            val half = ((row + 1) * 87).toInt()
            var col = 0
            var x = 0
            while (x < 694) {
                if (kotlin.math.abs((x + 16) - 347) <= half) {
                    val t = tiles[(col + row * 2) % tiles.size]
                    blit2(b, src, t[0], t[1], 16, 16, x, y)
                }
                x += 32
                col++
            }
        }
        // Empena e travessas da parte alta: madeira e reboco retirados do mesmo tileset.
        for (row in 0..2) {
            val y = 128 + row * 32
            var col = 0
            var x = 0
            while (x < 694) {
                blit2(b, src, wallColumn(col), 48 + row * 16, 16, 16, x, y)
                x += 32
                col++
            }
        }
    }

    /** Parede traseira de madeira feita com peças modulares da folha interior. */
    fun backCol(rel: Int, frame: Int): PixBuf = cached("back_" + rel + "_" + (frame and 1), 32, 96) { b ->
        val src = inside()
        val sx = (rel.coerceIn(0, 18) % 8) * 16
        blit2(b, src, sx, 96, 16, 16, 0, 0)
        blit2(b, src, sx, 112, 16, 16, 0, 32)
        blit2(b, src, sx, 112, 16, 16, 0, 64)
    }

    fun floor(v: Int): PixBuf = cached("floor_real_" + (v and 3), 32, 32) { b ->
        blit2(b, inside(), (v and 3) * 16, 160, 16, 16, 0, 0)
    }

    fun loftFloor(v: Int): PixBuf = cached("loft_real_" + (v and 3), 32, 32) { b ->
        blit2(b, inside(), ((v and 3) + 2) * 16, 160, 16, 16, 0, 0)
    }

    fun rail(kind: Int): PixBuf = cached("rail_real_" + kind.coerceIn(0, 3), 32, 48) { b ->
        // Os segmentos de cerca do tileset exterior são reaproveitados como balaustrada.
        blit2(b, ext(), 224 + (kind.coerceIn(0, 3) % 3) * 16, 0, 16, 24, 0, 0)
    }

    fun stairs(side: Int, part: Int): PixBuf = cached("stairs_real_" + (side and 1) + "_" + part.coerceIn(0, 2), 32, 48) { b ->
        blit2(b, inside(), 256 + (side and 1) * 16, (part.coerceIn(0, 2) * 8).coerceAtMost(24), 16, 24, 0, 0)
    }

    /** Leitos compostos por madeira e tecido do tileset, sem reutilizar os sprites antigos. */
    fun bed(kind: Int): PixBuf = cached("bed_real_" + (kind and 1), 32, 48) { b ->
        val src = inside()
        blit2(b, src, 96 + (kind and 1) * 16, 160, 16, 16, 0, 16)
        blit2(b, src, 176 + (kind and 1) * 16, 224, 16, 16, 0, 0)
    }

    fun hearth(side: Int, frame: Int): PixBuf = cached("hearth_real_" + (side and 1) + "_" + (frame and 1), 32, 48) { b ->
        blit2(b, inside(), (side and 1) * 16, 208 + (frame and 1) * 16, 16, 24, 0, 0)
    }

    fun shelf(kind: Int): PixBuf = cached("shelf_real_" + kind.coerceIn(0, 2), 32, 48) { b ->
        blit2(b, inside(), 96 + kind.coerceIn(0, 2) * 16, 208, 16, 24, 0, 0)
    }

    fun casks(kind: Int): PixBuf = cached("casks_real_" + (kind and 1), 32, 48) { b ->
        blit2(b, inside(), 176 + (kind and 1) * 16, 144, 16, 24, 0, 0)
    }

    fun counter(part: Int): PixBuf = cached("counter_real_" + part.coerceIn(0, 5), 32, 40) { b ->
        blit2(b, inside(), 96 + (part.coerceIn(0, 5) % 4) * 16, 160, 16, 20, 0, 0)
    }

    fun table(half: Int, variant: Int): PixBuf = cached("table_real_" + (half and 1) + "_" + (variant and 1), 32, 32) { b ->
        blit2(b, inside(), 96 + (variant and 1) * 32 + (half and 1) * 16, 160, 16, 16, 0, 0)
    }

    fun stool(): PixBuf = cached("stool_real", 32, 32) { b ->
        blit2(b, inside(), 160, 144, 16, 16, 0, 0)
    }

    // Compatibilidade de assinatura para chamadas genéricas da arte de casas.
    fun facade(kind: Int, edge: Int, frame: Int): PixBuf = facadeCol((kind + edge + frame).coerceIn(0, 18))
    fun backWall(kind: Int, edge: Int, frame: Int): PixBuf = backCol((kind + edge).coerceIn(0, 18), frame)
}
