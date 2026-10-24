package com.aldoria.rpg

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlin.math.abs

/**
 * Caneca Dourada — arte a partir do tileset medieval (Calciumtrice / CC BY 3.0).
 * Usa a casa em enxaimel inteira da folha exterior e os props da folha interior,
 * fatiados com coordenadas medidas no sprite (não grid modular errado).
 */
object ArtTavern {
    const val STYLE = 6
    const val DOOR_KIND = 6

    private var exterior: Bitmap? = null
    private var interior: Bitmap? = null
    private val cache = HashMap<String, PixBuf>()

    // Bounding boxes medidas no PNG exterior 384×256
    // Casa em enxaimel (creme + madeira + telhado vermelho)
    private const val HX = 133   // left of plaster
    private const val HY = 8     // top of chimneys/roof
    private const val HW = 120   // width of house body
    private const val HH = 198   // full height including roof

    // Interior sheet 320×320 — regiões úteis
    // wood wall band ~ y 112-176, furniture lower

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

    /** Copia com scale nearest-neighbour (src → dst scale). */
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
            val syy = sy + (y * sh) / dh
            val srcRow = ((y * sh) / dh) * sw
            for (x in 0 until dw) {
                val sxx = (x * sw) / dw
                val color = pixels[srcRow + sxx]
                if ((color ushr 24) == 0) continue
                dst.set(dx + x, dy + y, color)
            }
        }
    }

    private fun blit2(dst: PixBuf, src: Bitmap, sx: Int, sy: Int, sw: Int, sh: Int, dx: Int, dy: Int) {
        blitScale(dst, src, sx, sy, sw, sh, dx, dy, sw * 2, sh * 2)
    }

    /** Coluna de fachada: fatia vertical da casa em enxaimel. */
    fun facadeCol(rel: Int): PixBuf = cached("facade_$rel", 32, 192) { b ->
        val src = ext()
        // 15 colunas cobrem a fachada; cicla pela largura da casa
        val cols = 8
        val col = ((rel % cols) + cols) % cols
        val sx = HX + (col * HW) / cols
        val sw = maxOf(1, HW / cols)
        // corpo da casa (sem telhado no topo do facade — telhado é sprite separado)
        // house body starts ~ y 65 in sheet
        val bodyY = 65
        val bodyH = 137
        blitScale(b, src, sx, bodyY, sw, bodyH, 0, 192 - (bodyH * 32 / 16), 32, bodyH * 2)
        // se a escala ficou estranha, força 32x192 preenchendo de baixo
        // Redesenha de forma estável: scale body to fill lower 160px
        b.fill(0)
        blitScale(b, src, sx, bodyY, sw, bodyH, 0, 32, 32, 160)
    }

    fun facadeCut(rel: Int): PixBuf = cached("cut_$rel", 32, 192) { b ->
        val src = ext()
        val cols = 8
        val col = ((rel % cols) + cols) % cols
        val sx = HX + (col * HW) / cols
        val sw = maxOf(1, HW / cols)
        // só a base da fachada
        blitScale(b, src, sx, 65 + 100, sw, 37, 0, 160, 32, 32)
    }

    /** Portal 3 tiles: centro da casa com a porta de madeira. */
    fun doorBig(frame: Int): PixBuf = cached("door_$frame", 96, 192) { b ->
        val src = ext()
        // Centro da casa (~ onde está a porta no sprite)
        val doorCx = HX + HW / 2 - 20
        blitScale(b, src, doorCx, 65, 40, 137, 8, 32, 80, 160)
        // Laterais com mais parede
        blitScale(b, src, HX, 65, 20, 137, 0, 32, 16, 160)
        blitScale(b, src, HX + HW - 20, 65, 20, 137, 80, 32, 16, 160)
        // Shift porta aberta: empurra faixa central
        if (frame > 0) {
            val shift = frame.coerceIn(1, 4) * 4
            // limpa e redesenha com offset
            b.fill(0)
            blitScale(b, src, HX, 65, 20, 137, 0, 32, 16, 160)
            blitScale(b, src, HX + HW - 20, 65, 20, 137, 80, 32, 16, 160)
            blitScale(b, src, doorCx, 65, 40, 137, 8 + shift, 32, 80 - shift / 2, 160)
        }
    }

    /** Telhado vermelho da casa, repetido na largura da taverna. */
    fun roof(): PixBuf = cached("roof_real", 672, 318) { b ->
        val src = ext()
        // telhado + chaminés: y 8..64 approx, x HX..HX+HW
        val roofY = 8
        val roofH = 58
        // tile across
        var x = 0
        var col = 0
        while (x < 672) {
            val sx = HX + (col % 4) * (HW / 4)
            blitScale(b, src, sx, roofY, HW / 4, roofH, x, 0, 48, 100)
            x += 48
            col++
        }
        // empena / corpo superior sob o beiral
        x = 0
        col = 0
        while (x < 672) {
            val sx = HX + (col % 8) * (HW / 8)
            blitScale(b, src, sx, 65, HW / 8, 40, x, 100, 32, 80)
            x += 32
            col++
        }
        // faixa inferior de madeira
        x = 0
        while (x < 672) {
            blitScale(b, src, HX + 10, 100, 20, 20, x, 180, 32, 40)
            x += 32
        }
    }

    fun backCol(rel: Int, frame: Int): PixBuf = cached("back_${rel}_$frame", 32, 96) { b ->
        val src = inside()
        // parede de madeira interior: banda y≈112-160
        val sx = (rel.coerceIn(0, 9) % 8) * 16
        blitScale(b, src, sx.coerceAtMost(144), 112, 16, 48, 0, 0, 32, 96)
        // detalhe: janela ou prateleira ocasional
        when (rel % 5) {
            1 -> blitScale(b, src, 96, 128, 32, 24, 0, 16, 32, 48) // janela
            2 -> {
                // tocha / prop pequeno
                if ((frame and 1) == 0) blitScale(b, src, 240, 144, 16, 16, 8, 20, 16, 24)
                else blitScale(b, src, 256, 144, 16, 16, 8, 20, 16, 24)
            }
            3 -> blitScale(b, src, 208, 128, 16, 32, 8, 16, 16, 48) // prateleira
        }
    }

    fun floor(v: Int): PixBuf = cached("floor_$v", 32, 32) { b ->
        val src = inside()
        // piso de madeira / pedra na área inferior
        val sx = (v and 3) * 16
        blitScale(b, src, sx.coerceAtMost(64), 176, 16, 16, 0, 0, 32, 32)
    }

    fun loftFloor(v: Int): PixBuf = cached("loft_$v", 32, 32) { b ->
        val src = inside()
        blitScale(b, src, 16 + (v and 3) * 16, 176, 16, 16, 0, 0, 32, 32)
    }

    fun rail(kind: Int): PixBuf = cached("rail_$kind", 32, 48) { b ->
        val src = ext()
        // cerca do sheet exterior (topo-direita)
        blitScale(b, src, 256 + (kind % 3) * 16, 0, 16, 24, 0, 0, 32, 48)
    }

    fun stairs(side: Int, part: Int): PixBuf = cached("stairs_${side}_$part", 32, 48) { b ->
        val src = inside()
        // escada no topo do interior sheet
        blitScale(b, src, 208 + (side and 1) * 16, 0, 16, 48, 0, 0, 32, 48)
    }

    fun bed(kind: Int): PixBuf = cached("bed_$kind", 32, 48) { b ->
        val src = inside()
        // usa mesa/madeira como base + prop
        blitScale(b, src, 64, 176, 32, 24, 0, 16, 32, 32)
        blitScale(b, src, 176 + (kind and 1) * 16, 208, 16, 16, 8, 0, 16, 16)
    }

    fun hearth(side: Int, frame: Int): PixBuf = cached("hearth_${side}_$frame", 32, 48) { b ->
        val src = inside()
        // lareiras no canto inferior esquerdo
        val fy = if ((frame and 1) == 0) 256 else 272
        blitScale(b, src, (side and 1) * 16, fy, 16, 32, 0, 0, 32, 48)
    }

    fun shelf(kind: Int): PixBuf = cached("shelf_$kind", 32, 48) { b ->
        val src = inside()
        // prateleiras / props
        blitScale(b, src, 208 + (kind % 3) * 16, 112, 16, 48, 0, 0, 32, 48)
    }

    fun casks(kind: Int): PixBuf = cached("casks_$kind", 32, 48) { b ->
        val src = inside()
        // barril
        blitScale(b, src, 176 + (kind and 1) * 16, 192, 16, 24, 0, 8, 32, 40)
    }

    fun counter(part: Int): PixBuf = cached("counter_$part", 32, 40) { b ->
        val src = inside()
        // bancada de madeira
        blitScale(b, src, 64 + (part % 4) * 16, 208, 16, 20, 0, 0, 32, 40)
    }

    fun table(half: Int, variant: Int): PixBuf = cached("table_${half}_$variant", 32, 32) { b ->
        val src = inside()
        blitScale(b, src, 64 + (half and 1) * 32, 176, 32, 32, 0, 0, 32, 32)
    }

    fun stool(): PixBuf = cached("stool", 32, 32) { b ->
        val src = inside()
        blitScale(b, src, 144, 192, 16, 24, 0, 4, 32, 28)
    }

    fun facade(kind: Int, edge: Int, frame: Int): PixBuf =
        facadeCol((kind + edge + frame).coerceIn(0, 20))

    fun backWall(kind: Int, edge: Int, frame: Int): PixBuf =
        backCol((kind + edge).coerceIn(0, 20), frame)
}
