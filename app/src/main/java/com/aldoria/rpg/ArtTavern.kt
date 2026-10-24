package com.aldoria.rpg

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory

/**
 * Caneca Dourada — fatias medidas na casa em enxaimel do tileset Calciumtrice.
 * Fachada: painéis com X (sem porta repetida). Portal: uma porta só no centro.
 * Interior: lareira, mesas e props com coordenadas conferidas no PNG.
 */
object ArtTavern {
    const val STYLE = 6
    const val DOOR_KIND = 6

    private var exterior: Bitmap? = null
    private var interior: Bitmap? = null
    private val cache = HashMap<String, PixBuf>()

    // Casa enxaimel no exterior.png (384×256)
    private const val HX = 133
    private const val HW = 117

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

    private fun blitScale(
        dst: PixBuf, src: Bitmap,
        sx: Int, sy: Int, sw: Int, sh: Int,
        dx: Int, dy: Int, dw: Int, dh: Int
    ) {
        if (sw <= 0 || sh <= 0 || dw <= 0 || dh <= 0) return
        if (sx < 0 || sy < 0 || sx + sw > src.width || sy + sh > src.height) return
        val pixels = IntArray(sw * sh)
        src.getPixels(pixels, 0, sw, sx, sy, sw, sh)
        for (y in 0 until dh) {
            val srcRow = ((y * sh) / dh) * sw
            for (x in 0 until dw) {
                val color = pixels[srcRow + (x * sw) / dw]
                if ((color ushr 24) == 0) continue
                dst.set(dx + x, dy + y, color)
            }
        }
    }

    /**
     * Coluna de fachada 32×192.
     * Usa só painéis de enxaimel (andar de baixo com X + andar de cima SEM a porta).
     * A porta aparece apenas em doorBig.
     */
    fun facadeCol(rel: Int): PixBuf = cached("facade_$rel", 32, 192) { b ->
        val src = ext()
        val n = 6
        val col = ((rel % n) + n) % n
        // evita a faixa central onde está a porta (~ colunas 2-3 do meio)
        val safe = when (col) {
            0 -> 0
            1 -> 1
            2 -> 4  // pula porta
            3 -> 5
            4 -> 0
            else -> 1
        }
        val sx = HX + safe * (HW / 6)
        val sw = (HW / 6).coerceAtLeast(12)

        // andar superior (reboco + vigas) — y 65..140, sem porta
        blitScale(b, src, sx, 65, sw, 75, 0, 32, 32, 80)
        // andar inferior (X de madeira) — y 140..202
        blitScale(b, src, sx, 140, sw, 62, 0, 112, 32, 80)
    }

    fun facadeCut(rel: Int): PixBuf = cached("cut_$rel", 32, 192) { b ->
        val src = ext()
        val n = 6
        val col = ((rel % n) + n) % n
        val safe = intArrayOf(0, 1, 4, 5, 0, 1)[col]
        val sx = HX + safe * (HW / 6)
        val sw = (HW / 6).coerceAtLeast(12)
        blitScale(b, src, sx, 170, sw, 32, 0, 160, 32, 32)
    }

    /** Portal 96×192: laterais de parede + UMA porta no centro. */
    fun doorBig(frame: Int): PixBuf = cached("door_$frame", 96, 192) { b ->
        val src = ext()
        // laterais: painéis sem porta
        blitScale(b, src, HX, 65, 18, 75, 0, 32, 20, 80)
        blitScale(b, src, HX, 140, 18, 62, 0, 112, 20, 80)
        blitScale(b, src, HX + HW - 18, 65, 18, 75, 76, 32, 20, 80)
        blitScale(b, src, HX + HW - 18, 140, 18, 62, 76, 112, 20, 80)

        // porta (região medida ~165,65 45×80)
        val shift = if (frame <= 0) 0 else frame.coerceIn(1, 4) * 5
        blitScale(b, src, 168, 68, 42, 78, 24 + shift, 40, 48, 100)
        // base sob a porta (painéis inferiores centro)
        blitScale(b, src, HX + 40, 170, 30, 32, 28, 160, 40, 32)
    }

    /** Telhado vermelho + chaminés, repetido na largura. */
    fun roof(): PixBuf = cached("roof_real", 672, 318) { b ->
        val src = ext()
        // telhado da casa y 8..64
        var x = 0
        var i = 0
        while (x < 672) {
            val sx = HX + (i % 4) * (HW / 4)
            blitScale(b, src, sx, 8, HW / 4, 56, x, 0, 56, 110)
            x += 56
            i++
        }
        // beiral / transição
        x = 0
        i = 0
        while (x < 672) {
            val sx = HX + (i % 6) * (HW / 6)
            blitScale(b, src, sx, 60, HW / 6, 20, x, 100, 32, 40)
            x += 32
            i++
        }
        // corpo sob telhado (reboco)
        x = 0
        i = 0
        while (x < 672) {
            val safe = intArrayOf(0, 1, 4, 5, 0, 1)[i % 6]
            val sx = HX + safe * (HW / 6)
            blitScale(b, src, sx, 65, HW / 6, 50, x, 140, 32, 100)
            x += 32
            i++
        }
    }

    fun backCol(rel: Int, frame: Int): PixBuf = cached("back_${rel}_$frame", 32, 96) { b ->
        val src = inside()
        // parede de madeira y 115-175
        val sx = (rel.coerceIn(0, 4) * 16).coerceAtMost(64)
        blitScale(b, src, sx, 115, 16, 60, 0, 0, 32, 96)
        when (rel % 4) {
            1 -> blitScale(b, src, 95, 130, 35, 35, 0, 20, 32, 40) // janela
            2 -> blitScale(b, src, 210, 120, 16, 40, 8, 16, 16, 48) // prateleira/prop
            3 -> {
                // pequeno brilho de tocha
                val fy = if ((frame and 1) == 0) 256 else 272
                blitScale(b, src, 0, fy, 16, 24, 8, 24, 16, 32)
            }
        }
    }

    fun floor(v: Int): PixBuf = cached("floor_$v", 32, 32) { b ->
        val src = inside()
        // usa textura de madeira da mesa/chão
        val sx = 70 + (v and 1) * 20
        blitScale(b, src, sx, 200, 16, 16, 0, 0, 32, 32)
    }

    fun loftFloor(v: Int): PixBuf = cached("loft_$v", 32, 32) { b ->
        val src = inside()
        blitScale(b, src, 80 + (v and 1) * 16, 210, 16, 16, 0, 0, 32, 32)
    }

    fun rail(kind: Int): PixBuf = cached("rail_$kind", 32, 48) { b ->
        val src = ext()
        blitScale(b, src, 256 + (kind % 3) * 20, 0, 16, 28, 0, 0, 32, 48)
    }

    fun stairs(side: Int, part: Int): PixBuf = cached("stairs_${side}_$part", 32, 48) { b ->
        val src = inside()
        blitScale(b, src, 208 + (side and 1) * 20, 0 + part * 8, 16, 40, 0, 0, 32, 48)
    }

    fun bed(kind: Int): PixBuf = cached("bed_$kind", 32, 48) { b ->
        val src = inside()
        blitScale(b, src, 70, 180, 40, 30, 0, 12, 32, 36)
    }

    fun hearth(side: Int, frame: Int): PixBuf = cached("hearth_${side}_$frame", 32, 48) { b ->
        val src = inside()
        // lareira com fogo: (0,255)-(30,310) e variante
        val fy = if ((frame and 1) == 0) 255 else 272
        blitScale(b, src, (side and 1) * 16, fy, 20, 40, 0, 0, 32, 48)
    }

    fun shelf(kind: Int): PixBuf = cached("shelf_$kind", 32, 48) { b ->
        val src = inside()
        // props / garrafas
        blitScale(b, src, 210 + (kind % 3) * 20, 120, 24, 50, 0, 0, 32, 48)
    }

    fun casks(kind: Int): PixBuf = cached("casks_$kind", 32, 48) { b ->
        val src = inside()
        blitScale(b, src, 175 + (kind and 1) * 20, 195, 24, 40, 0, 4, 32, 44)
    }

    fun counter(part: Int): PixBuf = cached("counter_$part", 32, 40) { b ->
        val src = inside()
        // bancada (50,215)-(140,255)
        val sx = 50 + (part % 4) * 22
        blitScale(b, src, sx.coerceAtMost(120), 215, 22, 40, 0, 0, 32, 40)
    }

    fun table(half: Int, variant: Int): PixBuf = cached("table_${half}_$variant", 32, 32) { b ->
        val src = inside()
        // mesa (70,180)-(140,230)
        val sx = 70 + (half and 1) * 35
        blitScale(b, src, sx, 180, 35, 40, 0, 0, 32, 32)
    }

    fun stool(): PixBuf = cached("stool", 32, 32) { b ->
        val src = inside()
        // cadeira/banqueta
        blitScale(b, src, 145, 190, 20, 40, 4, 0, 24, 32)
    }

    fun facade(kind: Int, edge: Int, frame: Int): PixBuf =
        facadeCol((kind * 2 + edge).coerceIn(0, 20))

    fun backWall(kind: Int, edge: Int, frame: Int): PixBuf =
        backCol((kind + edge).coerceIn(0, 20), frame)
}
