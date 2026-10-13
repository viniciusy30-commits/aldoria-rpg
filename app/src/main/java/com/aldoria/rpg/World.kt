package com.aldoria.rpg

/** Tipos de chão. */
object Gd {
    const val GRASS = 0
    const val DIRT = 1
    const val SAND = 2
    const val STONE = 3
    const val SHALLOW = 4
    const val WATER = 5
    const val WOOD = 6
    const val CARPET = 7
    const val BRIDGE = 8
    const val RUG = 9
    const val COUNT = 10
}

/** Objetos que ficam em cima do chão. */
object Ob {
    const val NONE = 0
    const val OAK = 1
    const val PINE = 2
    const val DEAD = 3
    const val BUSH = 4
    const val BUSHF = 5
    const val ROCK = 6
    const val BOULDER = 7
    const val LOG = 8
    const val STUMP = 9
    const val WALL = 10
    const val DOOR = 11
    const val FOUNTAIN = 12
    const val STATUE = 13
    const val LAMP = 14
    const val BENCH = 15
    const val PLANTER = 16
    const val STALL = 17
    const val WELL = 18
    const val SIGN = 19
    const val FENCE = 20
    const val BED = 21
    const val TABLE = 22
    const val CHAIR = 23
    const val SHELF = 24
    const val SHELFP = 25
    const val BARREL = 26
    const val CRATE = 27
    const val CHEST = 28
    const val FIRE = 29
    const val CAULDRON = 30
    const val COUNTER = 31
    const val FAC = 32
    const val COUNT = 33

    fun solid(o: Int): Boolean = o != NONE && o != DOOR && o != CHAIR
}

/** Decorações pisáveis (flores, mato, cogumelos...). */
object Dc {
    const val NONE = 0
    const val FLOWER_R = 1
    const val FLOWER_Y = 2
    const val FLOWER_B = 3
    const val FLOWER_W = 4
    const val TALL = 5
    const val MUSH = 6
    const val MUSH2 = 7
    const val PEBBLE = 8
    const val LILY = 9
    const val LILYF = 10
    const val REED = 11
    const val FERN = 12
    const val COUNT = 13
}

/** Uma casa: [x0,y0] canto de cima/esquerda. A frente (virada pra baixo) é a última linha; o desenho dela é alto. */
class House(val x0: Int, val y0: Int, val hw: Int, val hh: Int, val style: Int, val vr: Int) {
    /** largura da porta em quadrados: a taverna tem um portal de 3 quadrados (o boneco passa pelo meio, sobrando espaço dos dois lados) */
    val doorW = if (style == ArtTavern.STYLE) 3 else 2
    /** quadrado da esquerda da porta (a porta ocupa doorX .. doorX + doorW - 1) */
    val doorX = x0 + (hw - doorW) / 2
    /** linha da fachada e da porta */
    val fl = y0 + hh - 1
    /** última linha de dentro (o saguão, junto da porta) */
    val fu = fl - 1
}

class World {
    val w = Cfg.MAP_W
    val h = Cfg.MAP_H
    val ground = IntArray(w * h)
    val vari = IntArray(w * h)
    val obj = IntArray(w * h)
    val ovar = IntArray(w * h)
    val deco = IntArray(w * h)
    val pz = BooleanArray(w * h)
    val houses = ArrayList<House>()
    /** id da casa que ocupa cada quadrado (-1 = nenhuma) */
    val hid = IntArray(w * h) { -1 }
    val spawns = ArrayList<IntArray>()
    val startX = 32
    val startY = 26

    private val prev = IntArray(w * h)
    private val queue = IntArray(w * h)
    private val seen = IntArray(w * h)
    private var stamp = 0

    init {
        generate()
    }

    fun inside(x: Int, y: Int): Boolean = x >= 0 && y >= 0 && x < w && y < h

    fun groundAt(x: Int, y: Int): Int = if (inside(x, y)) ground[y * w + x] else Gd.WATER
    fun objAt(x: Int, y: Int): Int = if (inside(x, y)) obj[y * w + x] else Ob.NONE

    fun solidAt(x: Int, y: Int): Boolean {
        if (!inside(x, y)) return true
        val i = y * w + x
        if (ground[i] == Gd.WATER) return true
        return Ob.solid(obj[i])
    }

    fun walkable(x: Int, y: Int): Boolean = !solidAt(x, y)

    fun isPz(x: Int, y: Int): Boolean = inside(x, y) && pz[y * w + x]

    private fun setG(x: Int, y: Int, g: Int) {
        if (inside(x, y)) {
            val i = y * w + x
            ground[i] = g
            vari[i] = hash2(x, y, 17) and 3
        }
    }

    private fun setO(x: Int, y: Int, o: Int, v: Int) {
        if (inside(x, y)) {
            val i = y * w + x
            obj[i] = o
            ovar[i] = v
            deco[i] = Dc.NONE
        }
    }

    private fun grassTone(x: Int, y: Int): Int {
        val tone = (smoothNoise(x / 7f, y / 7f, 31) * 4f).toInt().coerceIn(0, 3)
        return if (rand01(x, y, 19) < 0.07f) 4 + (hash2(x, y, 23) and 3) else tone
    }

    /** Constrói uma casa: paredes por dentro, uma linha de fachada (alta) com porta dupla por fora. */
    private fun buildHouse(x0: Int, y0: Int, hw: Int, hh: Int, style: Int, vr: Int): House {
        val hs = House(x0, y0, hw, hh, style, vr)
        val id = houses.size
        houses.add(hs)
        for (yy in y0 until y0 + hh) for (xx in x0 until x0 + hw) {
            setG(xx, yy, Gd.WOOD)
            hid[yy * w + xx] = id
        }
        // parede de trás (janelas, quadros e arandelas)
        for (xx in x0 until x0 + hw) {
            val rel = xx - x0
            var kind = 0
            if (rel == 2 || rel == hw - 3 || (hw >= 12 && (rel == 5 || rel == hw - 6))) kind = 1
            else if (hw % 2 == 0 && (rel == hw / 2 - 1 || rel == hw / 2)) kind = 3
            else if (hw % 2 == 1 && rel == hw / 2) kind = 3
            else if (hw % 2 == 0 && (rel == hw / 2 - 2 || rel == hw / 2 + 1)) kind = 2
            else if (hw % 2 == 1 && (rel == hw / 2 - 1 || rel == hw / 2 + 1)) kind = 2
            setO(xx, y0, Ob.WALL, kind)
        }
        // paredes laterais
        for (yy in y0 + 1 until hs.fl) {
            setO(x0, yy, Ob.WALL, 0)
            setO(x0 + hw - 1, yy, Ob.WALL, 0)
        }
        // fachada: porta dupla no meio, janelas, lanternas e hera
        val doorL = hs.doorX - x0
        for (xx in x0 until x0 + hw) {
            val rel = xx - x0
            if (rel >= doorL && rel < doorL + hs.doorW) {
                setO(xx, hs.fl, Ob.DOOR, ((rel - doorL) and 1) or (style shl 1) or ((if (style == ArtTavern.STYLE) ArtTavern.DOOR_KIND else id % ArtHouse.DOOR_KINDS) shl 4))
                setG(xx, hs.fl, Gd.STONE)
            } else {
                val edge = if (rel == 0) 1 else if (rel == hw - 1) 2 else 0
                val kind = if (rel == doorL - 1 || rel == doorL + hs.doorW) 2
                else if (rel == 0 || rel == hw - 1) (if (id % 2 == 0) 3 else 0)
                else if ((rel + id) % 2 == 1) 1
                else if ((rel * 7 + id) % 3 == 0) 3 else 0
                setO(xx, hs.fl, Ob.FAC, kind or (edge shl 2) or (style shl 4))
            }
        }
        return hs
    }

    /** Tapete com borda e franja: [rw] e [rh] precisam ser 2 ou mais. */
    private fun rug(x0: Int, y0: Int, rw: Int, rh: Int, style: Int) {
        for (yy in y0 until y0 + rh) for (xx in x0 until x0 + rw) {
            val r = if (yy == y0) 0 else if (yy == y0 + rh - 1) 2 else 1
            val c = if (xx == x0) 0 else if (xx == x0 + rw - 1) 2 else 1
            val i = yy * w + xx
            ground[i] = Gd.RUG
            vari[i] = style * 9 + r * 3 + c
        }
    }

    private fun wallKind(x: Int, y: Int, kind: Int) {
        if (inside(x, y) && obj[y * w + x] == Ob.WALL) ovar[y * w + x] = kind
    }

    private fun generate() {
        // 1) água, areia e grama
        for (y in 0 until h) {
            for (x in 0 until w) {
                val i = y * w + x
                var g = Gd.GRASS
                val dLake = Math.hypot(x - 54.0, y - 9.0) / 10.0 + (smoothNoise(x / 4f, y / 4f, 11) - 0.5f) * 0.5f
                val dPond = Math.hypot(x - 56.0, y - 38.0) / 6.0 + (smoothNoise(x / 3f, y / 3f, 12) - 0.5f) * 0.5f
                val d = Math.min(dLake, dPond)
                if (d < 0.62) g = Gd.WATER else if (d < 0.85) g = Gd.SHALLOW else if (d < 1.1) g = Gd.SAND
                if (y >= 13) {
                    val rcx = 49f + (smoothNoise(0f, y / 7f, 21) - 0.5f) * 4f
                    val dr = Math.abs(x - rcx)
                    if (dr < 1.5f) g = Gd.WATER
                    else if (dr < 2.6f && g != Gd.WATER) g = Gd.SHALLOW
                    else if (dr < 3.4f && g == Gd.GRASS) g = Gd.SAND
                }
                ground[i] = g
                vari[i] = if (g == Gd.GRASS) grassTone(x, y) else (hash2(x, y, 17) and 3)
            }
        }
        // 2) borda do mundo: floresta fechada
        for (y in 0 until h) {
            for (x in 0 until w) {
                if (x >= 2 && y >= 2 && x < w - 2 && y < h - 2) continue
                val g = ground[y * w + x]
                if (g != Gd.GRASS && g != Gd.SAND) continue
                val pine = smoothNoise(x / 9f, y / 9f, 41) > 0.5f
                val hv = hash2(x, y, 29)
                if (pine) setO(x, y, Ob.PINE, hv and 1) else setO(x, y, Ob.OAK, ((hv and 3) % 3))
            }
        }
        // 3) cidade: limpa a área
        for (y in 5..42) {
            for (x in 5..44) {
                val i = y * w + x
                ground[i] = Gd.GRASS
                vari[i] = grassTone(x, y)
                obj[i] = Ob.NONE
                deco[i] = Dc.NONE
            }
        }
        // 4) estradas de terra (com ponte onde cruzam o rio)
        for (x in 0 until w) {
            val inTown = x in 5..44
            val ry = if (inTown) 24 else 24 + Math.round((smoothNoise(x / 9f, 2f, 3) - 0.5f) * 5f)
            for (k in 0..1) {
                val yy = ry + k
                val g = groundAt(x, yy)
                if (g == Gd.WATER || g == Gd.SHALLOW || g == Gd.BRIDGE) setG(x, yy, Gd.BRIDGE) else setG(x, yy, Gd.DIRT)
                if (inside(x, yy)) {
                    obj[yy * w + x] = Ob.NONE
                    deco[yy * w + x] = Dc.NONE
                }
            }
        }
        for (y in 0 until h) {
            val inTown = y in 5..42
            val rx = if (inTown) 32 else 32 + Math.round((smoothNoise(3f, y / 9f, 4) - 0.5f) * 5f)
            for (k in 0..1) {
                val xx = rx + k
                val g = groundAt(xx, y)
                if (g != Gd.WATER && g != Gd.SHALLOW && g != Gd.BRIDGE) setG(xx, y, Gd.DIRT)
                if (inside(xx, y)) {
                    obj[y * w + xx] = Ob.NONE
                    deco[y * w + xx] = Dc.NONE
                }
            }
        }
        // ruas internas (Rua Norte e Rua Sul)
        for (x in 7..43) for (k in 0..1) {
            setG(x, 20 + k, Gd.DIRT)
            setG(x, 37 + k, Gd.DIRT)
        }
        // 5) praça de pedra no cruzamento
        for (y in 20..28) for (x in 25..40) setG(x, y, Gd.STONE)

        // 6) casas, todas diferentes (x0, y0, largura, altura, estilo, variação do telhado)
        buildHouse(9, 9, 12, 11, 5, 1)    // Torre-casarão do mago
        buildHouse(22, 11, 9, 9, 4, 2)    // Ferraria
        buildHouse(35, 10, 8, 10, 1, 0)   // Casa de família
        buildHouse(8, 26, 15, 11, 6, 1)   // Taverna "Caneca Dourada" v4: 2 andares (salão + galeria), portal de 3 quadrados
        buildHouse(23, 29, 8, 8, 2, 2)    // Alquimista
        buildHouse(35, 30, 6, 7, 0, 0)    // Chalé

        // 7) mobília
        // Mansão do mago: quarto nas pontas, lareira dupla no centro, mesa grande
        rug(12, 12, 6, 4, 5)
        setO(10, 10, Ob.BED, 0); setO(11, 10, Ob.CHEST, 0); setO(12, 10, Ob.SHELF, 0); setO(13, 10, Ob.SHELF, 1)
        setO(14, 10, Ob.FIRE, 0); setO(15, 10, Ob.FIRE, 0)
        setO(16, 10, Ob.SHELF, 2); setO(17, 10, Ob.SHELF, 0); setO(18, 10, Ob.CHEST, 0); setO(19, 10, Ob.BED, 0)
        setO(14, 13, Ob.TABLE, 2); setO(15, 13, Ob.TABLE, 2)
        setO(14, 12, Ob.CHAIR, 0); setO(15, 12, Ob.CHAIR, 0); setO(14, 14, Ob.CHAIR, 0); setO(15, 14, Ob.CHAIR, 0)
        setO(13, 13, Ob.CHAIR, 0); setO(16, 13, Ob.CHAIR, 0)
        setO(10, 12, Ob.SHELF, 1); setO(10, 13, Ob.SHELF, 2); setO(19, 12, Ob.SHELF, 1); setO(19, 13, Ob.SHELF, 2)
        setO(10, 15, Ob.CAULDRON, 0); setO(10, 16, Ob.BARREL, 0); setO(19, 15, Ob.CAULDRON, 0); setO(19, 16, Ob.BARREL, 1)
        setO(10, 17, Ob.PLANTER, 0); setO(19, 17, Ob.PLANTER, 1)
        // Ferraria: forja no fundo, balcão e barris
        rug(24, 16, 4, 2, 4)
        setO(23, 12, Ob.SHELFP, 0); setO(24, 12, Ob.SHELFP, 1); setO(25, 12, Ob.CAULDRON, 0); setO(26, 12, Ob.FIRE, 0)
        setO(27, 12, Ob.CAULDRON, 1); setO(28, 12, Ob.SHELFP, 2); setO(29, 12, Ob.SHELFP, 0)
        for (xx in 24..27) setO(xx, 15, Ob.COUNTER, xx % 2)
        setO(23, 13, Ob.CHEST, 0); setO(23, 14, Ob.BARREL, 0); setO(23, 15, Ob.BARREL, 1); setO(23, 16, Ob.CRATE, 0)
        setO(29, 14, Ob.BARREL, 1); setO(29, 15, Ob.BARREL, 0); setO(29, 16, Ob.CRATE, 1); setO(29, 13, Ob.CRATE, 0)
        // Casa de família: lareira dupla, camas nos cantos, mesa de jantar
        rug(37, 13, 4, 4, 1)
        setO(36, 11, Ob.BED, 0); setO(37, 11, Ob.CHEST, 0); setO(38, 11, Ob.FIRE, 0); setO(39, 11, Ob.FIRE, 0)
        setO(40, 11, Ob.CHEST, 0); setO(41, 11, Ob.BED, 0)
        setO(38, 14, Ob.TABLE, 0); setO(39, 14, Ob.TABLE, 0)
        setO(37, 14, Ob.CHAIR, 0); setO(40, 14, Ob.CHAIR, 0); setO(38, 13, Ob.CHAIR, 0); setO(39, 13, Ob.CHAIR, 0)
        setO(38, 15, Ob.CHAIR, 0); setO(39, 15, Ob.CHAIR, 0)
        setO(36, 13, Ob.SHELF, 0); setO(36, 14, Ob.SHELF, 1); setO(41, 13, Ob.SHELF, 2)
        setO(41, 14, Ob.CAULDRON, 0); setO(41, 15, Ob.BARREL, 0); setO(41, 16, Ob.CRATE, 0)
        setO(36, 16, Ob.BARREL, 1); setO(36, 17, Ob.CRATE, 1)
        // Taverna "Caneca Dourada" v4 (x0=8, y0=26): fundo com parede alta, galeria (andar de cima) com camas e balaustrada,
        // duas escadas laterais, salão com lareira, balcão de bar, mesas compridas e portal de 3 quadrados no meio da frente.
        run {
            val th = houses[3]
            for (yy in th.y0 until th.y0 + th.hh) for (xx in th.x0 until th.x0 + th.hw) {
                val ii = yy * w + xx
                if (ground[ii] != Gd.WOOD) continue
                val r = yy - th.y0
                vari[ii] = (if (r in 1..3) 8 else 4) + (vari[ii] and 3)   // 8.. = piso da galeria, 4.. = piso do salão
            }
        }
        rug(14, 33, 3, 3, 6)
        // andar de cima (galeria): camas, baús e prateleiras junto da parede; as portas dos quartos ficam livres (x=13 e x=17)
        setO(9, 27, Ob.SHELF, 0); setO(10, 27, Ob.BED, 1); setO(11, 27, Ob.BED, 2); setO(12, 27, Ob.CHEST, 0)
        setO(14, 27, Ob.SHELF, 1); setO(16, 27, Ob.CHEST, 0)
        setO(18, 27, Ob.BED, 1); setO(19, 27, Ob.BED, 2); setO(20, 27, Ob.CHEST, 0); setO(21, 27, Ob.SHELF, 2)
        // balaustrada na frente da galeria (SHELFP 6 ponta esq., 7 meio, 8 ponta dir., 9 meio com lampião)
        setO(10, 29, Ob.SHELFP, 6)
        for (xx in 11..19) setO(xx, 29, Ob.SHELFP, if (xx == 15) 9 else 7)
        setO(20, 29, Ob.SHELFP, 8)
        // escadas (CHAIR 2..4 parede à esquerda: base, meio, topo; 5..7 parede à direita)
        setO(9, 29, Ob.CHAIR, 4); setO(9, 30, Ob.CHAIR, 3); setO(9, 31, Ob.CHAIR, 2)
        setO(21, 29, Ob.CHAIR, 7); setO(21, 30, Ob.CHAIR, 6); setO(21, 31, Ob.CHAIR, 5)
        // fundo do salão (debaixo da galeria): barris, prateleiras (3 canecas/jarras, 4 garrafas, 5 queijos e pão) e a lareira (2 e 3)
        setO(10, 30, Ob.BARREL, 3); setO(20, 30, Ob.BARREL, 3)
        setO(11, 30, Ob.SHELFP, 3); setO(12, 30, Ob.SHELFP, 4); setO(13, 30, Ob.SHELFP, 5); setO(14, 30, Ob.SHELFP, 4)
        setO(15, 30, Ob.FIRE, 2); setO(16, 30, Ob.FIRE, 3)
        setO(17, 30, Ob.SHELFP, 4); setO(18, 30, Ob.SHELFP, 5); setO(19, 30, Ob.SHELFP, 3)
        // atrás do balcão: barris de chope; cantos: baús
        setO(11, 31, Ob.BARREL, 4); setO(19, 31, Ob.BARREL, 4)
        setO(10, 31, Ob.CHEST, 0); setO(20, 31, Ob.CHEST, 0)
        // balcão (2 ponta esq., 3 meio, 4 ponta dir., 5 torre de chope, 6 canecas, 7 livro-caixa e vela)
        setO(11, 32, Ob.COUNTER, 2); setO(12, 32, Ob.COUNTER, 3); setO(13, 32, Ob.COUNTER, 6); setO(14, 32, Ob.COUNTER, 5)
        setO(15, 32, Ob.COUNTER, 3); setO(16, 32, Ob.COUNTER, 6); setO(17, 32, Ob.COUNTER, 7); setO(18, 32, Ob.COUNTER, 3)
        setO(19, 32, Ob.COUNTER, 4)
        // bancos do balcão (a passagem do portão, x=14..16, fica livre)
        setO(12, 33, Ob.CHAIR, 1); setO(13, 33, Ob.CHAIR, 1); setO(17, 33, Ob.CHAIR, 1); setO(18, 33, Ob.CHAIR, 1)
        // mesa comprida da esquerda (3 e 4) e da direita (5 e 6), com banquetas dos dois lados e nas pontas
        setO(10, 34, Ob.TABLE, 3); setO(11, 34, Ob.TABLE, 4); setO(19, 34, Ob.TABLE, 5); setO(20, 34, Ob.TABLE, 6)
        setO(10, 33, Ob.CHAIR, 1); setO(11, 33, Ob.CHAIR, 1); setO(10, 35, Ob.CHAIR, 1); setO(11, 35, Ob.CHAIR, 1)
        setO(9, 34, Ob.CHAIR, 1); setO(12, 34, Ob.CHAIR, 1)
        setO(19, 33, Ob.CHAIR, 1); setO(20, 33, Ob.CHAIR, 1); setO(19, 35, Ob.CHAIR, 1); setO(20, 35, Ob.CHAIR, 1)
        setO(18, 34, Ob.CHAIR, 1); setO(21, 34, Ob.CHAIR, 1)
        // cantos da frente: barris em pé e vasos de flores
        setO(9, 35, Ob.BARREL, 4); setO(21, 35, Ob.BARREL, 4)
        setO(9, 33, Ob.PLANTER, 2); setO(21, 33, Ob.PLANTER, 0)
        // Alquimista: prateleiras, caldeirões e mesa de trabalho
        rug(25, 33, 4, 2, 2)
        setO(24, 30, Ob.SHELFP, 0); setO(25, 30, Ob.SHELFP, 1); setO(26, 30, Ob.SHELF, 0)
        setO(27, 30, Ob.SHELF, 1); setO(28, 30, Ob.SHELFP, 2); setO(29, 30, Ob.SHELFP, 0)
        setO(26, 32, Ob.TABLE, 0); setO(27, 32, Ob.TABLE, 0); setO(25, 32, Ob.CHAIR, 0); setO(28, 32, Ob.CHAIR, 0)
        setO(24, 31, Ob.CAULDRON, 1); setO(29, 31, Ob.CAULDRON, 1); setO(24, 32, Ob.CAULDRON, 0); setO(29, 32, Ob.CAULDRON, 0)
        setO(24, 33, Ob.BARREL, 0); setO(29, 33, Ob.BARREL, 1); setO(24, 34, Ob.CRATE, 0); setO(29, 34, Ob.CRATE, 1)
        // Chalé: aconchegante
        rug(37, 33, 2, 2, 0)
        setO(36, 31, Ob.BED, 0); setO(37, 31, Ob.SHELF, 0); setO(38, 31, Ob.FIRE, 0); setO(39, 31, Ob.CHEST, 0)
        setO(36, 32, Ob.CRATE, 0); setO(39, 32, Ob.CAULDRON, 0)
        setO(39, 33, Ob.TABLE, 1); setO(39, 34, Ob.CHAIR, 0); setO(36, 33, Ob.BARREL, 0); setO(36, 34, Ob.BARREL, 1)

        // enfeites na frente das casas: vasos, lampiões e placas
        val signs = intArrayOf(2, 0, -1, 3, 1, -1)
        for (k in houses.indices) {
            val hs = houses[k]
            val ly = hs.fl + 1
            setO(hs.doorX - 1, ly, Ob.PLANTER, k % 3)
            setO(hs.doorX + hs.doorW, ly, Ob.PLANTER, (k + 1) % 3)
            setO(hs.x0 + 1, ly, Ob.LAMP, 0)
            if (signs[k] >= 0) setO(hs.x0 + hs.hw - 2, ly, Ob.SIGN, signs[k])
        }

        // 8) cerca da vila (com portões nas estradas) e lampiões
        for (x in 7..43) {
            if (x !in 31..34) {
                setO(x, 6, Ob.FENCE, 0)
                setO(x, 41, Ob.FENCE, 0)
            }
        }
        for (y in 6..41) {
            if (y !in 23..26) {
                setO(7, y, Ob.FENCE, 0)
                setO(43, y, Ob.FENCE, 0)
            }
        }
        setO(30, 6, Ob.LAMP, 0); setO(35, 6, Ob.LAMP, 0); setO(30, 41, Ob.LAMP, 0); setO(35, 41, Ob.LAMP, 0)
        setO(7, 22, Ob.LAMP, 0); setO(7, 27, Ob.LAMP, 0); setO(43, 22, Ob.LAMP, 0); setO(43, 27, Ob.LAMP, 0)

        // 9) praça: estátua no cruzamento, duas fontes, bancos, vasos, barracas de feira e poço
        setO(32, 24, Ob.STATUE, 0)
        setO(29, 23, Ob.FOUNTAIN, 0); setO(36, 23, Ob.FOUNTAIN, 0)
        setO(24, 21, Ob.LAMP, 0); setO(41, 21, Ob.LAMP, 0); setO(26, 28, Ob.LAMP, 0); setO(39, 28, Ob.LAMP, 0)
        setO(31, 21, Ob.LAMP, 0); setO(34, 21, Ob.LAMP, 0)
        setO(27, 22, Ob.BENCH, 0); setO(38, 22, Ob.BENCH, 0); setO(27, 27, Ob.BENCH, 0); setO(38, 27, Ob.BENCH, 0)
        setO(30, 22, Ob.PLANTER, 0); setO(35, 22, Ob.PLANTER, 1); setO(30, 27, Ob.PLANTER, 2); setO(35, 27, Ob.PLANTER, 0)
        setO(26, 23, Ob.STALL, 0); setO(39, 23, Ob.STALL, 1); setO(26, 26, Ob.STALL, 2); setO(39, 26, Ob.STALL, 3)
        setO(24, 26, Ob.WELL, 0)
        setO(41, 26, Ob.BARREL, 0); setO(42, 26, Ob.CRATE, 1); setO(41, 22, Ob.CRATE, 0)
        // árvores e arbustos nos gramados da vila
        val oaks = arrayOf(
            intArrayOf(9, 7), intArrayOf(13, 7), intArrayOf(17, 7), intArrayOf(21, 7), intArrayOf(25, 7), intArrayOf(29, 7),
            intArrayOf(37, 7), intArrayOf(41, 7), intArrayOf(9, 22), intArrayOf(13, 23), intArrayOf(17, 22), intArrayOf(21, 23),
            intArrayOf(42, 23), intArrayOf(9, 40), intArrayOf(13, 39),
            intArrayOf(17, 40), intArrayOf(21, 39), intArrayOf(25, 40), intArrayOf(29, 39), intArrayOf(37, 40), intArrayOf(41, 39)
        )
        for ((n, p) in oaks.withIndex()) setO(p[0], p[1], Ob.OAK, n % 3)
        val bushes = arrayOf(
            intArrayOf(11, 22), intArrayOf(15, 23), intArrayOf(19, 22), 
            intArrayOf(11, 7), intArrayOf(19, 7), intArrayOf(27, 7), intArrayOf(39, 7), intArrayOf(15, 40), intArrayOf(23, 39), intArrayOf(30, 40)
        )
        for ((n, p) in bushes.withIndex()) setO(p[0], p[1], if (n % 2 == 0) Ob.BUSHF else Ob.BUSH, n % 4)
        // flores no gramado da vila
        val rt = java.util.Random(5L)
        for (i in 0 until 160) {
            val fx = 8 + rt.nextInt(35)
            val fy = 7 + rt.nextInt(34)
            val idx = fy * w + fx
            if (ground[idx] == Gd.GRASS && obj[idx] == Ob.NONE) deco[idx] = Dc.FLOWER_R + rt.nextInt(4)
        }

        // 10) natureza fora da cidade
        for (y in 0 until h) {
            for (x in 0 until w) {
                val i = y * w + x
                if (x in 5..44 && y in 5..42) continue
                if (obj[i] != Ob.NONE) continue
                val g = ground[i]
                val f = smoothNoise(x / 5f, y / 5f, 7)
                val r = rand01(x, y, 3)
                val hv = hash2(x, y, 29)
                if (g == Gd.GRASS) {
                    val pine = smoothNoise(x / 11f, y / 11f, 41) > 0.52f
                    if (f > 0.62f && r < 0.8f) {
                        if (pine) setO(x, y, Ob.PINE, hv and 1) else setO(x, y, Ob.OAK, ((hv ushr 2) and 3) % 3)
                    } else if (f > 0.56f && r < 0.22f) {
                        setO(x, y, if ((hv and 1) == 0) Ob.BUSH else Ob.BUSHF, (hv ushr 3) and 3)
                    } else if (r < 0.012f) {
                        setO(x, y, Ob.ROCK, ((hv ushr 2) and 3) % 3)
                    } else if (r < 0.016f) {
                        setO(x, y, Ob.BOULDER, hv and 1)
                    } else if (r < 0.019f) {
                        setO(x, y, Ob.LOG, hv and 1)
                    } else if (r < 0.022f) {
                        setO(x, y, Ob.STUMP, hv and 1)
                    } else if (r < 0.025f) {
                        setO(x, y, Ob.DEAD, hv and 1)
                    }
                    if (obj[i] == Ob.NONE) {
                        val r2 = rand01(x, y, 8)
                        val patch = smoothNoise(x / 6f, y / 6f, 51)
                        if (r2 < 0.05f) deco[i] = Dc.FLOWER_R + (hv and 3)
                        else if (r2 < 0.13f && patch > 0.55f) deco[i] = Dc.TALL
                        else if (r2 < 0.16f && f > 0.5f) deco[i] = if ((hv and 1) == 0) Dc.MUSH else Dc.MUSH2
                        else if (r2 < 0.19f && f > 0.45f) deco[i] = Dc.FERN
                        else if (r2 < 0.2f) deco[i] = Dc.PEBBLE
                    }
                } else if (g == Gd.SAND || g == Gd.DIRT) {
                    if (r < 0.06f) deco[i] = Dc.PEBBLE
                } else if (g == Gd.SHALLOW) {
                    var land = false
                    for (k in 0 until 4) {
                        val ng = groundAt(x + DX[k], y + DY[k])
                        if (ng != Gd.WATER && ng != Gd.SHALLOW && ng != Gd.BRIDGE) land = true
                    }
                    if (land && r < 0.4f) deco[i] = Dc.REED
                } else if (g == Gd.WATER) {
                    var all = true
                    for (k in 0 until 4) {
                        val ng = groundAt(x + DX[k], y + DY[k])
                        if (ng != Gd.WATER && ng != Gd.SHALLOW) all = false
                    }
                    if (all && r < 0.07f) deco[i] = if (r < 0.025f) Dc.LILYF else Dc.LILY
                }
            }
        }

        // 11) zona de proteção: a cidade toda
        for (y in 6..41) for (x in 7..43) pz[y * w + x] = true

        // 12) pontos onde os monstros nascem: fora da cidade, no gramado
        val sr = java.util.Random(77L)
        var tries = 0
        while (spawns.size < 18 && tries < 4000) {
            tries++
            val x = 4 + sr.nextInt(w - 8)
            val y = 4 + sr.nextInt(h - 8)
            val i = y * w + x
            if (ground[i] != Gd.GRASS || obj[i] != Ob.NONE) continue
            if (Math.hypot(x - 32.0, y - 24.0) < 16.0) continue
            if (x in 3..47 && y in 3..45) continue
            var ok = true
            for (s in spawns) {
                if (cheb(s[0], s[1], x, y) < 5) {
                    ok = false
                    break
                }
            }
            if (ok) spawns.add(intArrayOf(x, y))
        }
    }

    /** Cor do minimapa para um quadrado. */
    fun miniColor(x: Int, y: Int): Int {
        val i = y * w + x
        val hd = hid[i]
        if (hd >= 0) {
            val hs = houses[hd]
            if (obj[i] == Ob.DOOR) return 0xFF6B4423.toInt()
            if (y >= hs.fl) return 0xFFD9CBB0.toInt()
            return ArtHouse.roofColor(hs.style)
        }
        when (obj[i]) {
            Ob.OAK, Ob.PINE, Ob.DEAD -> return 0xFF1F5A24.toInt()
            Ob.BUSH, Ob.BUSHF -> return 0xFF2E7A33.toInt()
            Ob.ROCK, Ob.BOULDER -> return 0xFF7A7A82.toInt()
            Ob.WALL, Ob.FAC -> return 0xFFB5482F.toInt()
            Ob.DOOR -> return 0xFF6B4423.toInt()
            Ob.FENCE -> return 0xFF8A6A45.toInt()
            Ob.FOUNTAIN, Ob.WELL -> return 0xFF5BA0F0.toInt()
            Ob.NONE -> {
            }
            else -> return 0xFFD0B070.toInt()
        }
        return when (ground[i]) {
            Gd.WATER -> 0xFF2461C2.toInt()
            Gd.SHALLOW -> 0xFF4B9ADC.toInt()
            Gd.SAND -> 0xFFE6D08F.toInt()
            Gd.DIRT -> 0xFF8A6A45.toInt()
            Gd.STONE -> 0xFFA0A0A8.toInt()
            Gd.WOOD, Gd.BRIDGE -> 0xFF8A5C2D.toInt()
            Gd.RUG -> 0xFF9A2E3A.toInt()
            Gd.CARPET -> 0xFF8E2A33.toInt()
            else -> 0xFF3F8531.toInt()
        }
    }

    /**
     * Caminho mais curto (4 direções) de [sx,sy] até [tx,ty].
     * Com [adjacentGoal], para ao lado do alvo. Devolve as posições (y * w + x) do primeiro passo até o fim.
     */
    fun path(
        sx: Int, sy: Int, tx: Int, ty: Int, maxNodes: Int, adjacentGoal: Boolean,
        passable: (Int, Int) -> Boolean
    ): IntArray? {
        stamp++
        var head = 0
        var tail = 0
        val start = sy * w + sx
        queue[tail++] = start
        seen[start] = stamp
        prev[start] = -1
        var found = -1
        while (head < tail && head < maxNodes) {
            val cur = queue[head++]
            val cx = cur % w
            val cy = cur / w
            val dd = Math.abs(cx - tx) + Math.abs(cy - ty)
            if ((adjacentGoal && dd == 1) || (!adjacentGoal && dd == 0)) {
                found = cur
                break
            }
            for (k in 0 until 4) {
                val nx = cx + DX[k]
                val ny = cy + DY[k]
                if (nx < 0 || ny < 0 || nx >= w || ny >= h) continue
                val ni = ny * w + nx
                if (seen[ni] == stamp) continue
                if (!passable(nx, ny)) continue
                seen[ni] = stamp
                prev[ni] = cur
                queue[tail++] = ni
            }
        }
        if (found < 0) return null
        val list = ArrayList<Int>()
        var c = found
        while (c != start && c >= 0) {
            list.add(c)
            c = prev[c]
        }
        list.reverse()
        return IntArray(list.size) { list[it] }
    }
}
