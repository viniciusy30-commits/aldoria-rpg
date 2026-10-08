package com.aldoria.rpg

import android.content.Context
import android.graphics.Color
import kotlin.math.floor

object Cfg {
    /** Multiplicador de experiência (modo de teste: sobe de nível rápido). */
    const val EXP_RATE = 10
    const val MAP_W = 64
    const val MAP_H = 48
}

const val PI_F = 3.1415927f
const val TAU = 6.2831855f

/** Direções: 0 cima, 1 direita, 2 baixo, 3 esquerda. */
val DX = intArrayOf(0, 1, 0, -1)
val DY = intArrayOf(-1, 0, 1, 0)

val COL_WHITE = 0xFFFFFFFF.toInt()
val COL_YELLOW = 0xFFFFE066.toInt()
val COL_RED = 0xFFFF5A5A.toInt()
val COL_GREEN = 0xFF7CFF8A.toInt()
val COL_LOOT = 0xFFFFA94D.toInt()
val COL_BLUE = 0xFF6FB6FF.toInt()
val COL_ORANGE = 0xFFFF8A3D.toInt()
val COL_PURPLE = 0xFFB388FF.toInt()
val COL_GRAY = 0xFFC8C8D0.toInt()

fun Context.dp(v: Int): Int = (v * resources.displayMetrics.density + 0.5f).toInt()

fun hash2(x: Int, y: Int, seed: Int): Int {
    var h = x * 374761393 + y * 668265263 + seed * 1274126177
    h = (h xor (h ushr 13)) * 1274126177
    h = h xor (h ushr 16)
    return h
}

fun rand01(x: Int, y: Int, seed: Int): Float = (hash2(x, y, seed) and 0xFFFF) / 65535f

fun smoothNoise(x: Float, y: Float, seed: Int): Float {
    val x0 = floor(x).toInt()
    val y0 = floor(y).toInt()
    val fx = x - x0
    val fy = y - y0
    val sx = fx * fx * (3f - 2f * fx)
    val sy = fy * fy * (3f - 2f * fy)
    val a = rand01(x0, y0, seed)
    val b = rand01(x0 + 1, y0, seed)
    val c = rand01(x0, y0 + 1, seed)
    val d = rand01(x0 + 1, y0 + 1, seed)
    val top = a + (b - a) * sx
    val bot = c + (d - c) * sx
    return top + (bot - top) * sy
}

fun withAlpha(col: Int, a: Float): Int {
    val aa = (Color.alpha(col) * a).toInt()
    val c2 = if (aa < 0) 0 else if (aa > 255) 255 else aa
    return (c2 shl 24) or (col and 0x00FFFFFF)
}

fun mixCol(a: Int, b: Int, f: Float): Int {
    val r = (Color.red(a) + (Color.red(b) - Color.red(a)) * f).toInt()
    val g = (Color.green(a) + (Color.green(b) - Color.green(a)) * f).toInt()
    val bl = (Color.blue(a) + (Color.blue(b) - Color.blue(a)) * f).toInt()
    return Color.rgb(r.coerceIn(0, 255), g.coerceIn(0, 255), bl.coerceIn(0, 255))
}

/** Distância em "quadrados" (diagonal conta como 1), a mesma do Tibia. */
fun cheb(ax: Int, ay: Int, bx: Int, by: Int): Int {
    val dx = if (ax > bx) ax - bx else bx - ax
    val dy = if (ay > by) ay - by else by - ay
    return if (dx > dy) dx else dy
}

/** Direção (0 a 3) de A olhando para B. */
fun dirTo(ax: Int, ay: Int, bx: Int, by: Int): Int {
    val dx = bx - ax
    val dy = by - ay
    val adx = if (dx < 0) -dx else dx
    val ady = if (dy < 0) -dy else dy
    return if (adx > ady) {
        if (dx > 0) 1 else 3
    } else {
        if (dy > 0) 2 else if (dy < 0) 0 else 2
    }
}

fun expForLevel(l: Int): Long {
    if (l <= 1) return 0L
    val x = l.toDouble()
    return ((50.0 / 3.0) * (x * x * x - 6.0 * x * x + 17.0 * x - 12.0)).toLong()
}

fun hpFor(l: Int): Int = 145 + 5 * l

fun manaFor(l: Int): Int = 5 + 30 * l

/** Mana que precisa gastar para subir do nível mágico [ml] para o próximo. */
fun manaNeeded(ml: Int): Long = (100.0 * Math.pow(1.35, ml.toDouble())).toLong()
