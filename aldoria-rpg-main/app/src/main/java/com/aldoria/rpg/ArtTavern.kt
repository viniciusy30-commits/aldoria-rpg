package com.aldoria.rpg

/** A Taverna "Caneca Dourada" v3: fachada de 4 quadrados (128 px) desenhada como uma peça única e fatiada em colunas,
 *  corte baixo da fachada para quando você está dentro, parede de fundo contínua, lareira de pedra e móveis próprios. */
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

    /** Coluna [rel] (0..13) da fachada vista de fora, 32x128. */
    fun facadeCol(rel: Int): PixBuf = load("fc${rel.coerceIn(0, 13)}", 32, 128)

    /** Coluna [rel] do murinho baixo que substitui a fachada quando você está dentro, 32x128 (quase tudo transparente). */
    fun facadeCut(rel: Int): PixBuf = load("cc${rel.coerceIn(0, 13)}", 32, 128)

    /** Porta ogival dupla (colunas 6 e 7) com sacada em cima, 64x128. frame 0 fechada ... 4 aberta. */
    fun doorBig(frame: Int): PixBuf = load("d${frame.coerceIn(0, 4)}", 64, 128)

    /** Telhado da taverna (14x9): 470x190. */
    fun roof(): PixBuf = load("roof", 470, 190)

    /** Parede de fundo vista de dentro, coluna [rel] (0..13), 32x64. frame 0/1 anima as tochas. */
    fun backCol(rel: Int, frame: Int): PixBuf = load("bk${rel.coerceIn(0, 13)}${frame and 1}", 32, 64)

    // compatibilidade com ArtHouse (não usados pelo desenho da taverna)
    fun facade(kind: Int, edge: Int, frame: Int): PixBuf = facadeCol(0)
    fun backWall(kind: Int, edge: Int, frame: Int): PixBuf = backCol(0, 0)

    fun floor(v: Int): PixBuf = load("fl${v and 3}", 32, 32)

    /** Lareira de pedra (2 quadrados): lado 0 esquerdo, 1 direito; frame 0/1 anima as chamas. 32x48. */
    fun hearth(side: Int, frame: Int): PixBuf = load((if (side == 0) "hl" else "hr") + (frame and 1), 32, 48)

    /** Prateleira de parede 32x48: 0 canecas e jarras, 1 garrafas, 2 queijos, pão e enchidos. */
    fun shelf(kind: Int): PixBuf = load("sf${kind.coerceIn(0, 2)}", 32, 48)

    /** Tonéis empilhados em cavalete (0) ou barril em pé (1), 32x48. */
    fun casks(kind: Int): PixBuf = load("ck${kind and 1}", 32, 48)

    /** Balcão 32x40: 0 ponta esquerda, 1 meio, 2 ponta direita, 3 torre de chope, 4 canecas, 5 livro-caixa e vela. */
    fun counter(part: Int): PixBuf = when (part) {
        0 -> load("cnL", 32, 40)
        2 -> load("cnR", 32, 40)
        3 -> load("cnT", 32, 40)
        4 -> load("cnC", 32, 40)
        5 -> load("cnB", 32, 40)
        else -> load("cnM", 32, 40)
    }

    /** Mesa comprida: [half] 0 esquerda/1 direita, [variant] 0 ou 1. */
    fun table(half: Int, variant: Int): PixBuf = load("tb${(variant and 1) * 2 + (half and 1)}", 32, 32)

    fun stool(): PixBuf = load("stool", 32, 32)
}
