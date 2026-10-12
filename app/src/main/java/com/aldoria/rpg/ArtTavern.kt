package com.aldoria.rpg

/** A Taverna "Caneca Dourada": arte pixel art desenhada à mão (ver ArtTavernData).
 *  Fachada de dois andares em enxaimel, porta ogival com sacada, telhado de telhas, piso e móveis próprios. */
object ArtTavern {
    const val STYLE = 6
    const val DOOR_KIND = 6

    private val cache = HashMap<String, PixBuf>()

    @Synchronized
    private fun load(key: String, fw: Int, fh: Int): PixBuf {
        val hit = cache[key]
        if (hit != null) return hit
        var pb: PixBuf? = null
        val s = ArtTavernData.get(key)
        if (s != null) {
            try {
                val bytes = android.util.Base64.decode(s, android.util.Base64.DEFAULT)
                val opt = android.graphics.BitmapFactory.Options()
                opt.inPremultiplied = false
                val bmp = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opt)
                if (bmp != null) {
                    val p = PixBuf(bmp.width, bmp.height)
                    bmp.getPixels(p.px, 0, bmp.width, 0, 0, bmp.width, bmp.height)
                    bmp.recycle()
                    pb = p
                }
            } catch (e: Exception) {
            }
        }
        val res = pb ?: PixBuf(fw, fh)
        cache[key] = res
        return res
    }

    /** Coluna da fachada, 32x96 (3 quadrados de altura). kind 0 parede, 1 janelas, 2 lampião, 3 barris/sótão. edge: 1 canto esq., 2 canto dir. */
    fun facade(kind: Int, edge: Int, frame: Int): PixBuf {
        val k = if (kind in 0..3) kind else 0
        val fr = if (k == 2) (frame and 1) else 0
        return load("f$k$edge$fr", 32, 96)
    }

    /** Porta ogival dupla com sacada em cima, 64x96. frame 0 fechada ... 4 aberta. */
    fun doorBig(frame: Int): PixBuf = load("d${frame.coerceIn(0, 4)}", 64, 96)

    /** Telhado da taverna (14x9): 470x222. */
    fun roof(): PixBuf = load("roof", 470, 222)

    /** Parede de trás vista de dentro, 32x64. */
    fun backWall(kind: Int, edge: Int, frame: Int): PixBuf {
        val k = if (kind in 0..3) kind else 0
        val fr = if (k == 2) (frame and 1) else 0
        return load("b$k$edge$fr", 32, 64)
    }

    fun floor(v: Int): PixBuf = load("fl${v and 3}", 32, 32)
    fun table(half: Int): PixBuf = load("tb${half and 1}", 32, 32)
    fun stool(): PixBuf = load("stool", 32, 32)
    fun counter(part: Int): PixBuf = load("ct${part.coerceIn(0, 2)}", 32, 32)
    fun cask(): PixBuf = load("cask", 32, 32)
}
