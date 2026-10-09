package com.aldoria.rpg

import android.graphics.Bitmap

/** Monta todos os bitmaps do jogo a partir da pixel art procedural. [size] é o tamanho do tile, múltiplo de 32. */
object Art {
    /** Variantes de chão: grama 8, terra 4, areia 4, pedra 4, água rasa 4, água funda 4, madeira 4, tapete 1, ponte 1. */
    fun ground(size: Int): Array<Array<Bitmap>> {
        val s = size / 32
        val cache = HashMap<String, Bitmap>()
        return Array(Gd.COUNT) { g ->
            Array(8) { v ->
                val n = when (g) {
                    Gd.GRASS -> 8
                    Gd.CARPET, Gd.BRIDGE -> 1
                    else -> 4
                }
                val vv = v % n
                val key = "$g:$vv"
                cache.getOrPut(key) {
                    val pb = when (g) {
                        Gd.GRASS -> ArtGround.grass(vv)
                        Gd.DIRT -> ArtGround.dirt(vv)
                        Gd.SAND -> ArtGround.sand(vv)
                        Gd.STONE -> ArtGround.stone(vv)
                        Gd.SHALLOW -> ArtGround.water(vv, true)
                        Gd.WATER -> ArtGround.water(vv, false)
                        Gd.WOOD -> ArtGround.wood(vv)
                        Gd.CARPET -> ArtGround.carpet()
                        else -> ArtGround.bridge()
                    }
                    pb.toBitmap(s)
                }
            }
        }
    }

    /** Bordas de transição: [tipo 0..4][lado 0..3][variante 0..3]. */
    fun edges(size: Int): Array<Array<Array<Bitmap>>> {
        val s = size / 32
        return Array(5) { k -> Array(4) { side -> Array(4) { v -> ArtGround.edge(k, side, v).toBitmap(s) } } }
    }

    /** Objetos: [objeto][variante 0..15] (null onde não existe). Bitmaps altos têm 48 pixels de altura. */
    fun objects(size: Int): Array<Array<Bitmap?>> {
        val s = size / 32
        val out = Array(Ob.COUNT) { arrayOfNulls<Bitmap>(16) }
        for (v in 0 until 3) out[Ob.OAK][v] = ArtNature.oak(v).toBitmap(s)
        for (v in 0 until 2) {
            out[Ob.PINE][v] = ArtNature.pine(v).toBitmap(s)
            out[Ob.DEAD][v] = ArtNature.dead(v).toBitmap(s)
            out[Ob.BOULDER][v] = ArtNature.boulder(v).toBitmap(s)
            out[Ob.LOG][v] = ArtNature.log(v).toBitmap(s)
            out[Ob.STUMP][v] = ArtNature.stump(v).toBitmap(s)
        }
        for (v in 0 until 4) {
            out[Ob.BUSH][v] = ArtNature.bush(v, false).toBitmap(s)
            out[Ob.BUSHF][v] = ArtNature.bush(v, true).toBitmap(s)
        }
        for (v in 0 until 3) out[Ob.ROCK][v] = ArtNature.rock(v).toBitmap(s)
        // paredes: 0..3 liso, 4..7 janela, 8..15 tocha (quadro 0: 8..11, quadro 1: 12..15); índice = combinação (norte/sul)
        for (combo in 0 until 4) {
            val hasN = (combo and 1) != 0
            val hasS = (combo and 2) != 0
            out[Ob.WALL][combo] = ArtBuild.wall(0, hasN, hasS, 0).toBitmap(s)
            out[Ob.WALL][4 + combo] = ArtBuild.wall(1, hasN, hasS, 0).toBitmap(s)
            out[Ob.WALL][8 + combo] = ArtBuild.wall(2, hasN, hasS, 0).toBitmap(s)
            out[Ob.WALL][12 + combo] = ArtBuild.wall(2, hasN, hasS, 1).toBitmap(s)
        }
        out[Ob.DOOR][0] = ArtBuild.door(false).toBitmap(s)
        out[Ob.DOOR][1] = ArtBuild.door(true).toBitmap(s)
        for (f in 0 until 4) out[Ob.FOUNTAIN][f] = ArtBuild.fountain(f).toBitmap(s)
        out[Ob.STATUE][0] = ArtBuild.statue().toBitmap(s)
        out[Ob.LAMP][0] = ArtBuild.lamp().toBitmap(s)
        out[Ob.BENCH][0] = ArtBuild.bench().toBitmap(s)
        for (v in 0 until 3) out[Ob.PLANTER][v] = ArtBuild.planter(v).toBitmap(s)
        for (v in 0 until 4) {
            out[Ob.STALL][v] = ArtBuild.stall(v).toBitmap(s)
            out[Ob.SIGN][v] = ArtBuild.sign(v).toBitmap(s)
        }
        out[Ob.WELL][0] = ArtBuild.well().toBitmap(s)
        for (m in 0 until 16) out[Ob.FENCE][m] = ArtBuild.fence(m).toBitmap(s)
        out[Ob.BED][0] = ArtFurn.bed().toBitmap(s)
        for (v in 0 until 3) {
            out[Ob.TABLE][v] = ArtFurn.table(v).toBitmap(s)
            out[Ob.SHELF][v] = ArtFurn.shelf(v).toBitmap(s)
            out[Ob.SHELFP][v] = ArtFurn.shelfPotions(v).toBitmap(s)
        }
        out[Ob.CHAIR][0] = ArtFurn.chair().toBitmap(s)
        for (v in 0 until 2) {
            out[Ob.BARREL][v] = ArtFurn.barrel(v).toBitmap(s)
            out[Ob.CRATE][v] = ArtFurn.crate(v).toBitmap(s)
            out[Ob.FIRE][v] = ArtFurn.fireplace(v).toBitmap(s)
            out[Ob.CAULDRON][v] = ArtFurn.cauldron(v).toBitmap(s)
            out[Ob.COUNTER][v] = ArtFurn.counter(v).toBitmap(s)
        }
        out[Ob.CHEST][0] = ArtFurn.chest().toBitmap(s)
        return out
    }

    /** Decorações do chão: [decoração][variante 0..3]. */
    fun decals(size: Int): Array<Array<Bitmap?>> {
        val s = size / 32
        val out = Array(Dc.COUNT) { arrayOfNulls<Bitmap>(4) }
        val petals = intArrayOf(h(0xFFE8505B), h(0xFFF5D547), h(0xFF6A8CFF), h(0xFFFFFFFF))
        val centers = intArrayOf(h(0xFFF5D547), h(0xFFE08A1E), h(0xFFFFFFFF), h(0xFFF5D547))
        for (v in 0 until 4) {
            for (k in 0 until 4) out[Dc.FLOWER_R + k][v] = ArtNature.flowers(petals[k], centers[k], v).toBitmap(s)
            out[Dc.TALL][v] = ArtNature.tallGrass(v).toBitmap(s)
            out[Dc.PEBBLE][v] = ArtNature.pebbles(v).toBitmap(s)
            out[Dc.FERN][v] = ArtNature.fern(v).toBitmap(s)
            out[Dc.REED][v] = ArtNature.reed(v).toBitmap(s)
        }
        for (v in 0 until 3) {
            out[Dc.MUSH][v] = ArtNature.mushroom(false, v).toBitmap(s)
            out[Dc.MUSH2][v] = ArtNature.mushroom(true, v).toBitmap(s)
            out[Dc.LILY][v] = ArtNature.lily(false, v).toBitmap(s)
            out[Dc.LILYF][v] = ArtNature.lily(true, v).toBitmap(s)
        }
        return out
    }

    /** [direção 0 cima, 1 direita, 2 baixo, 3 esquerda][quadro 0..3] */
    fun mage(size: Int): Array<Array<Bitmap>> {
        val s = size / 32
        val up = Array(4) { f -> ArtSprites.mage(0, f).toBitmap(s) }
        val rightPix = Array(4) { f -> ArtSprites.mage(1, f) }
        val down = Array(4) { f -> ArtSprites.mage(2, f).toBitmap(s) }
        val right = Array(4) { f -> rightPix[f].toBitmap(s) }
        val left = Array(4) { f -> ArtNature.mirror(rightPix[f]).toBitmap(s) }
        return arrayOf(up, right, down, left)
    }

    fun rat(size: Int): Array<Array<Bitmap>> {
        val s = size / 32
        val up = Array(4) { f -> ArtSprites.rat(0, f).toBitmap(s) }
        val rightPix = Array(4) { f -> ArtSprites.rat(1, f) }
        val down = Array(4) { f -> ArtSprites.rat(2, f).toBitmap(s) }
        val right = Array(4) { f -> rightPix[f].toBitmap(s) }
        val left = Array(4) { f -> ArtNature.mirror(rightPix[f]).toBitmap(s) }
        return arrayOf(up, right, down, left)
    }

    fun ratDead(size: Int): Bitmap = ArtSprites.ratDead().toBitmap(size / 32)
}
