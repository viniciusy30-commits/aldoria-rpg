package com.aldoria.rpg

/** Carrega os PNGs de terreno/natureza (ArtNatureData) como PixBuf. Devolve null se algo falhar, e aí vale o desenho procedural antigo. */
object ArtPack {
    private val cache = HashMap<String, PixBuf?>()

    @Synchronized
    fun get(key: String, w: Int, h: Int): PixBuf? {
        if (!cache.containsKey(key)) cache[key] = decode(key, w, h)
        val src = cache[key] ?: return null
        val o = PixBuf(src.w, src.h)
        System.arraycopy(src.px, 0, o.px, 0, src.px.size)
        return o
    }

    private fun decode(key: String, w: Int, h: Int): PixBuf? {
        val s = ArtNatureData.get(key) ?: return null
        try {
            val bytes = android.util.Base64.decode(s, android.util.Base64.DEFAULT)
            val opt = android.graphics.BitmapFactory.Options()
            opt.inPremultiplied = false
            val bmp = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opt) ?: return null
            if (bmp.width != w || bmp.height != h) {
                bmp.recycle()
                return null
            }
            val p = PixBuf(w, h)
            bmp.getPixels(p.px, 0, w, 0, 0, w, h)
            bmp.recycle()
            return p
        } catch (e: Exception) {
            return null
        }
    }
}
