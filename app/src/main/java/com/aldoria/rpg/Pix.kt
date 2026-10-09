package com.aldoria.rpg

import android.graphics.Bitmap
import android.graphics.Color

/** Atalho para escrever cores 0xFFRRGGBB (que o Kotlin lê como Long). */
fun h(v: Long): Int = v.toInt()

/** Clareia (amt > 0) ou escurece (amt < 0) uma cor, com um leve tom azulado nas sombras. */
fun shadeCol(c: Int, amt: Float): Int {
    val a = c ushr 24
    val r = Color.red(c)
    val g = Color.green(c)
    val b = Color.blue(c)
    val nr: Int
    val ng: Int
    val nb: Int
    if (amt >= 0f) {
        nr = (r + (255 - r) * amt).toInt()
        ng = (g + (255 - g) * amt).toInt()
        nb = (b + (255 - b) * amt).toInt()
    } else {
        val k = 1f + amt
        nr = (r * k).toInt()
        ng = (g * k).toInt()
        nb = (b * (k + 0.07f)).toInt()
    }
    val cr = if (nr < 0) 0 else if (nr > 255) 255 else nr
    val cg = if (ng < 0) 0 else if (ng > 255) 255 else ng
    val cb = if (nb < 0) 0 else if (nb > 255) 255 else nb
    return (a shl 24) or (cr shl 16) or (cg shl 8) or cb
}

/** Imagem em pixel art: cada pixel é um "pixel grande" que depois é esticado para o tamanho do tile. */
class PixBuf(val w: Int, val h: Int) {
    val px = IntArray(w * h)

    fun get(x: Int, y: Int): Int = if (x < 0 || y < 0 || x >= w || y >= h) 0 else px[y * w + x]

    fun set(x: Int, y: Int, c: Int) {
        if (x >= 0 && y >= 0 && x < w && y < h) px[y * w + x] = c
    }

    fun fill(c: Int) {
        java.util.Arrays.fill(px, c)
    }

    fun rect(x: Int, y: Int, rw: Int, rh: Int, c: Int) {
        for (yy in y until y + rh) for (xx in x until x + rw) set(xx, yy, c)
    }

    fun line(x0: Int, y0: Int, x1: Int, y1: Int, c: Int) {
        var x = x0
        var y = y0
        val dx = Math.abs(x1 - x0)
        val dy = -Math.abs(y1 - y0)
        val sx = if (x0 < x1) 1 else -1
        val sy = if (y0 < y1) 1 else -1
        var err = dx + dy
        var guard = 0
        while (guard < 400) {
            guard++
            set(x, y, c)
            if (x == x1 && y == y1) break
            val e2 = 2 * err
            if (e2 >= dy) {
                err += dy
                x += sx
            }
            if (e2 <= dx) {
                err += dx
                y += sy
            }
        }
    }

    /** Elipse; com [shaded] a luz vem de cima à esquerda e a cor ganha 5 tons. */
    fun ellipse(cx: Float, cy: Float, rx: Float, ry: Float, c: Int, shaded: Boolean) {
        val x0 = (cx - rx - 1f).toInt()
        val x1 = (cx + rx + 1f).toInt()
        val y0 = (cy - ry - 1f).toInt()
        val y1 = (cy + ry + 1f).toInt()
        for (y in y0..y1) {
            for (x in x0..x1) {
                val nx = (x + 0.5f - cx) / rx
                val ny = (y + 0.5f - cy) / ry
                if (nx * nx + ny * ny > 1f) continue
                var col = c
                if (shaded) {
                    val l = -(nx * 0.5f + ny * 0.8f)
                    col = if (l > 0.5f) shadeCol(c, 0.30f)
                    else if (l > 0.1f) shadeCol(c, 0.14f)
                    else if (l > -0.35f) c
                    else if (l > -0.7f) shadeCol(c, -0.18f)
                    else shadeCol(c, -0.34f)
                }
                set(x, y, col)
            }
        }
    }

    /** Só o anel da elipse (1 pixel de espessura). */
    fun ring(cx: Float, cy: Float, rx: Float, ry: Float, c: Int) {
        val x0 = (cx - rx - 1f).toInt()
        val x1 = (cx + rx + 1f).toInt()
        val y0 = (cy - ry - 1f).toInt()
        val y1 = (cy + ry + 1f).toInt()
        for (y in y0..y1) {
            for (x in x0..x1) {
                val nx = (x + 0.5f - cx) / rx
                val ny = (y + 0.5f - cy) / ry
                val d = nx * nx + ny * ny
                if (d <= 1f && d > 0.62f) set(x, y, c)
            }
        }
    }

    /** Polígono preenchido (pares x,y nos arrays). */
    fun poly(xs: FloatArray, ys: FloatArray, c: Int) {
        val n = xs.size
        var minY = ys[0]
        var maxY = ys[0]
        for (i in 1 until n) {
            if (ys[i] < minY) minY = ys[i]
            if (ys[i] > maxY) maxY = ys[i]
        }
        val cr = FloatArray(n + 2)
        val ya = Math.max(0, minY.toInt() - 1)
        val yb = Math.min(h - 1, maxY.toInt() + 1)
        for (y in ya..yb) {
            val yc = y + 0.5f
            var k = 0
            for (i in 0 until n) {
                val j = (i + 1) % n
                val y1 = ys[i]
                val y2 = ys[j]
                if ((y1 <= yc && y2 > yc) || (y2 <= yc && y1 > yc)) {
                    cr[k] = xs[i] + (yc - y1) * (xs[j] - xs[i]) / (y2 - y1)
                    k++
                }
            }
            for (a in 1 until k) {
                val v = cr[a]
                var bi = a - 1
                while (bi >= 0 && cr[bi] > v) {
                    cr[bi + 1] = cr[bi]
                    bi--
                }
                cr[bi + 1] = v
            }
            var i = 0
            while (i + 1 < k) {
                val xa = Math.ceil((cr[i] - 0.5f).toDouble()).toInt()
                val xb = Math.ceil((cr[i + 1] - 0.5f).toDouble()).toInt() - 1
                for (x in xa..xb) set(x, y, c)
                i += 2
            }
        }
    }

    private fun solid(c: Int): Boolean = (c ushr 24) > 128

    /** Contorno de 1 pixel em volta de tudo que está pintado. */
    fun outline(c: Int) {
        val add = ArrayList<Int>()
        for (y in 0 until h) {
            for (x in 0 until w) {
                if (solid(px[y * w + x])) continue
                if (solid(get(x - 1, y)) || solid(get(x + 1, y)) || solid(get(x, y - 1)) || solid(get(x, y + 1))) {
                    add.add(y * w + x)
                }
            }
        }
        for (i in add) px[i] = c
    }

    /** Sombra suave no chão: só pinta onde ainda está vazio. */
    fun groundShadow(cx: Float, cy: Float, rx: Float, ry: Float) {
        val x0 = (cx - rx - 1f).toInt()
        val x1 = (cx + rx + 1f).toInt()
        val y0 = (cy - ry - 1f).toInt()
        val y1 = (cy + ry + 1f).toInt()
        for (y in y0..y1) {
            for (x in x0..x1) {
                val nx = (x + 0.5f - cx) / rx
                val ny = (y + 0.5f - cy) / ry
                if (nx * nx + ny * ny <= 1f && get(x, y) == 0) set(x, y, 0x55000000)
            }
        }
    }

    /** Cola outra imagem por cima (ignora pixels vazios). */
    fun blit(o: PixBuf, ox: Int, oy: Int) {
        for (y in 0 until o.h) {
            for (x in 0 until o.w) {
                val c = o.px[y * o.w + x]
                if (c != 0) set(x + ox, y + oy, c)
            }
        }
    }

    fun toBitmap(scale: Int): Bitmap {
        val small = Bitmap.createBitmap(px, w, h, Bitmap.Config.ARGB_8888)
        if (scale <= 1) return small
        return Bitmap.createScaledBitmap(small, w * scale, h * scale, false)
    }
}
