package com.aldoria.rpg

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.Shader
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/** Duração do Escudo Mágico em segundos. */
const val SHIELD_DUR = 20f

object FxK {
    const val HEAL = 0
    const val FIREBALL = 1
    const val BLAST = 2
    const val BOLT = 3
    const val WAVE = 4
    const val HASTE = 5
    const val SHIELD = 6
    const val BREAK = 7
    const val RIPPLE = 8
    const val FLARE = 9
    const val CAST = 10
    const val ARCANE = 11
    const val SCORCH = 12
}

/** Um efeito visual de magia. Tudo é desenhado a partir do tempo [t]. */
class Fx(val kind: Int, val x: Float, val y: Float, val dur: Float) {
    var t = 0f
    var tx = 0f
    var ty = 0f
    var dir = 0
    var seed = 0
    var color = 0
    var scale = 1f
    var ang = 0f
    var emit = 0f
    var follow = false
    var target: Monster? = null

    fun fbU(): Float {
        val u = if (dur > 0f) t / dur else 1f
        return if (u > 1f) 1f else u
    }

    private fun fbE(u: Float): Float = u * (0.6f + 0.4f * u)
    fun fbGX(u: Float): Float = x + (tx - x) * fbE(u)
    fun fbGY(u: Float): Float = y + (ty - y) * fbE(u)
    fun fbX(u: Float): Float = fbGX(u)
    fun fbY(u: Float): Float = fbGY(u) - sin(u * PI_F) * 0.22f
}

class Delayed(var t: Float, val act: () -> Unit)

/**
 * Desenha os efeitos das magias com brilho aditivo, círculos rúnicos, raios,
 * chamas, domo de escudo hexagonal etc. Tudo gerado em código, sem imagens.
 */
class SpellFx(private val g: Game) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val addMode = PorterDuffXfermode(PorterDuff.Mode.ADD)
    private val filters = HashMap<Int, PorterDuffColorFilter>()
    private val glow: Bitmap = makeGlow()
    private val colGrad = LinearGradient(
        0f, 0f, 0f, 1f,
        intArrayOf(0x00FFFFFF, 0x99FFFFFF.toInt(), 0xDDFFFFFF.toInt()),
        floatArrayOf(0f, 0.6f, 1f), Shader.TileMode.CLAMP
    )
    private val path = Path()
    private val rect = RectF()
    private val jx = FloatArray(140)
    private val jy = FloatArray(140)
    private val mx = FloatArray(140)
    private val my = FloatArray(140)
    private val rpA = FloatArray(4)
    private val rpT = FloatArray(4)
    private val rpD = FloatArray(4)

    private var tl = 64f
    private var camX = 0f
    private var camY = 0f
    private var sw = 0
    private var sh = 0
    private var rs = 1

    private val WHITE = 0xFFFFFFFF.toInt()

    init {
        p.strokeJoin = Paint.Join.ROUND
        p.strokeCap = Paint.Cap.ROUND
        p.isFilterBitmap = true
    }

    private fun makeGlow(): Bitmap {
        val n = 64
        val b = Bitmap.createBitmap(n, n, Bitmap.Config.ARGB_8888)
        val half = n / 2f
        for (y in 0 until n) {
            for (x in 0 until n) {
                val dx = (x + 0.5f - half) / half
                val dy = (y + 0.5f - half) / half
                val d = sqrt(dx * dx + dy * dy)
                var a = 1f - d
                if (a < 0f) a = 0f
                a = a * a
                val ai = (a * 255f).toInt()
                b.setPixel(x, y, (ai shl 24) or 0x00FFFFFF)
            }
        }
        return b
    }

    fun begin(tile: Int, camX: Float, camY: Float, w: Int, h: Int) {
        tl = tile.toFloat()
        this.camX = camX
        this.camY = camY
        sw = w
        sh = h
    }

    // ------------------------------------------------------------------ utilidades

    private fun sx(wx: Float): Float = (wx - camX) * tl + sw / 2f
    private fun sy(wy: Float): Float = (wy - camY) * tl + sh / 2f

    private fun cl(x: Float): Float = if (x < 0f) 0f else if (x > 1f) 1f else x
    private fun eo(x: Float): Float {
        val u = cl(x)
        return 1f - (1f - u) * (1f - u) * (1f - u)
    }
    private fun env(t: Float, a: Float, dur: Float, b: Float): Float = cl(t / a) * cl((dur - t) / b)

    private fun seedR(s: Int) {
        rs = s * 747796405 + 1013904223
    }

    private fun rnd(): Float {
        rs = rs * 1664525 + 1013904223
        return ((rs ushr 8) and 0xFFFF) / 65535f
    }

    private fun filter(col: Int): PorterDuffColorFilter {
        val k = col or -16777216
        val f0 = filters[k]
        if (f0 != null) return f0
        val f1 = PorterDuffColorFilter(k, PorterDuff.Mode.SRC_IN)
        filters[k] = f1
        return f1
    }

    private fun prep(col: Int, a: Float, add: Boolean) {
        p.shader = null
        p.colorFilter = null
        p.xfermode = if (add) addMode else null
        p.color = withAlpha(col or -16777216, a)
    }

    private fun fxX(f: Fx): Float = if (f.follow) g.player.fx + 0.5f else f.x
    private fun fxY(f: Fx): Float = if (f.follow) g.player.fy + 0.88f else f.y

    private fun glowAt(c: Canvas, x: Float, y: Float, r: Float, col: Int, a: Float, add: Boolean = true) {
        if (a <= 0.01f || r < 1f) return
        p.shader = null
        p.style = Paint.Style.FILL
        p.xfermode = if (add) addMode else null
        p.colorFilter = filter(col)
        var ai = (a * 255f).toInt()
        if (ai > 255) ai = 255
        p.alpha = ai
        rect.set(x - r, y - r, x + r, y + r)
        c.drawBitmap(glow, null, rect, p)
        p.colorFilter = null
    }

    private fun glowFlat(c: Canvas, x: Float, y: Float, r: Float, col: Int, a: Float) {
        c.save()
        c.translate(x, y)
        c.scale(1f, 0.5f)
        glowAt(c, 0f, 0f, r, col, a)
        c.restore()
    }

    private fun ringGlow(c: Canvas, x: Float, y: Float, rx: Float, ry: Float, sw0: Float, col: Int, a: Float) {
        if (a <= 0.01f || rx < 1f) return
        rect.set(x - rx, y - ry, x + rx, y + ry)
        p.style = Paint.Style.STROKE
        prep(col, a * 0.22f, true)
        p.strokeWidth = sw0 * 3.2f
        c.drawOval(rect, p)
        prep(col, a * 0.5f, true)
        p.strokeWidth = sw0 * 1.7f
        c.drawOval(rect, p)
        prep(mixCol(col, WHITE, 0.55f), a, true)
        p.strokeWidth = sw0
        c.drawOval(rect, p)
    }

    private fun lineAdd(c: Canvas, x1: Float, y1: Float, x2: Float, y2: Float, sw0: Float, col: Int, a: Float) {
        if (a <= 0.01f) return
        prep(col, a, true)
        p.style = Paint.Style.STROKE
        p.strokeWidth = sw0
        c.drawLine(x1, y1, x2, y2, p)
    }

    private fun star(c: Canvas, x: Float, y: Float, lx: Float, ly: Float, col: Int, a: Float, rot: Float) {
        if (a <= 0.01f) return
        prep(col, a, true)
        p.style = Paint.Style.FILL
        c.save()
        c.translate(x, y)
        c.rotate(rot)
        val w1 = lx * 0.07f
        val w2 = ly * 0.07f
        path.reset()
        path.moveTo(-lx, 0f)
        path.lineTo(0f, -w1)
        path.lineTo(lx, 0f)
        path.lineTo(0f, w1)
        path.close()
        c.drawPath(path, p)
        path.reset()
        path.moveTo(0f, -ly)
        path.lineTo(w2, 0f)
        path.lineTo(0f, ly)
        path.lineTo(-w2, 0f)
        path.close()
        c.drawPath(path, p)
        c.restore()
    }

    private fun plus(c: Canvas, x: Float, y: Float, s: Float, col: Int, a: Float) {
        if (a <= 0.01f) return
        prep(col, a, true)
        p.style = Paint.Style.FILL
        rect.set(x - s * 0.3f, y - s, x + s * 0.3f, y + s)
        c.drawRect(rect, p)
        rect.set(x - s, y - s * 0.3f, x + s, y + s * 0.3f)
        c.drawRect(rect, p)
    }

    private fun column(c: Canvas, cx: Float, baseY: Float, wd: Float, h: Float, col: Int, a: Float) {
        if (a <= 0.01f || h < 2f) return
        p.style = Paint.Style.FILL
        p.xfermode = addMode
        p.shader = colGrad
        p.colorFilter = filter(col)
        var ai = (a * 255f).toInt()
        if (ai > 255) ai = 255
        p.alpha = ai
        c.save()
        c.translate(cx, baseY - h)
        c.scale(wd, h)
        rect.set(-0.5f, 0f, 0.5f, 1f)
        c.drawRect(rect, p)
        c.restore()
        p.shader = null
        p.colorFilter = null
    }

    private fun softColumn(c: Canvas, cx: Float, baseY: Float, wd: Float, h: Float, col: Int, a: Float) {
        column(c, cx, baseY, wd, h, col, a * 0.38f)
        column(c, cx, baseY, wd * 0.78f, h, col, a * 0.38f)
        column(c, cx, baseY, wd * 0.56f, h, col, a * 0.38f)
        column(c, cx, baseY, wd * 0.34f, h, col, a * 0.38f)
    }

    // ------------------------------------------------------------------ chamas

    private fun flame(c: Canvas, bx: Float, byy: Float, fw: Float, fh: Float, lean: Float, col: Int, a: Float) {
        if (a <= 0.01f || fh < 2f) return
        path.reset()
        path.moveTo(bx - fw * 0.5f, byy)
        path.cubicTo(bx - fw * 0.65f, byy - fh * 0.35f, bx + lean * 0.5f - fw * 0.2f, byy - fh * 0.62f, bx + lean, byy - fh)
        path.cubicTo(bx + lean * 0.5f + fw * 0.2f, byy - fh * 0.62f, bx + fw * 0.65f, byy - fh * 0.35f, bx + fw * 0.5f, byy)
        path.quadTo(bx, byy + fw * 0.25f, bx - fw * 0.5f, byy)
        path.close()
        prep(col, a, true)
        p.style = Paint.Style.FILL
        c.drawPath(path, p)
    }

    private fun flameStack(c: Canvas, bx: Float, byy: Float, fw: Float, fh: Float, lean: Float, a: Float) {
        flame(c, bx, byy, fw, fh, lean, 0xFFFF2A0A.toInt(), a * 0.5f)
        flame(c, bx, byy, fw * 0.74f, fh * 0.82f, lean * 0.9f, 0xFFFF7A1E.toInt(), a * 0.7f)
        flame(c, bx, byy, fw * 0.5f, fh * 0.6f, lean * 0.8f, 0xFFFFD25A.toInt(), a * 0.85f)
        flame(c, bx, byy, fw * 0.26f, fh * 0.36f, lean * 0.6f, WHITE, a * 0.8f)
    }

    // ------------------------------------------------------------------ raios

    private fun makeBolt(x1: Float, y1: Float, x2: Float, y2: Float, levels: Int, disp0: Float): Int {
        jx[0] = x1
        jy[0] = y1
        jx[1] = x2
        jy[1] = y2
        var n = 2
        var disp = disp0
        val dx = x2 - x1
        val dy = y2 - y1
        var len = sqrt(dx * dx + dy * dy)
        if (len < 1f) len = 1f
        val nx = -dy / len
        val ny = dx / len
        for (lv in 0 until levels) {
            var j = n - 1
            while (j >= 0) {
                jx[j * 2] = jx[j]
                jy[j * 2] = jy[j]
                j--
            }
            j = 0
            while (j < n - 1) {
                val k = j * 2 + 1
                val mxx = (jx[j * 2] + jx[j * 2 + 2]) * 0.5f
                val myy = (jy[j * 2] + jy[j * 2 + 2]) * 0.5f
                val o = (rnd() - 0.5f) * 2f * disp
                jx[k] = mxx + nx * o
                jy[k] = myy + ny * o
                j++
            }
            n = n * 2 - 1
            disp *= 0.55f
        }
        return n
    }

    private fun strokePoly(n: Int, wd: Float, col: Int, a: Float, c: Canvas) {
        path.reset()
        path.moveTo(jx[0], jy[0])
        for (i in 1 until n) path.lineTo(jx[i], jy[i])
        p.style = Paint.Style.STROKE
        prep(col, a * 0.14f, true)
        p.strokeWidth = wd * 4.6f
        c.drawPath(path, p)
        prep(col, a * 0.35f, true)
        p.strokeWidth = wd * 2.3f
        c.drawPath(path, p)
        prep(mixCol(col, WHITE, 0.6f), a * 0.9f, true)
        p.strokeWidth = wd
        c.drawPath(path, p)
        prep(WHITE, a, true)
        p.strokeWidth = wd * 0.4f
        c.drawPath(path, p)
    }

    private fun drawBolt(c: Canvas, x1: Float, y1: Float, x2: Float, y2: Float, seed: Int, col: Int, a: Float, wd: Float, branches: Int) {
        if (a <= 0.01f) return
        seedR(seed)
        val dx = x2 - x1
        val dy = y2 - y1
        val len = sqrt(dx * dx + dy * dy)
        val n = makeBolt(x1, y1, x2, y2, 5, len * 0.16f)
        strokePoly(n, wd, col, a, c)
        if (branches <= 0 || n < 20) return
        System.arraycopy(jx, 0, mx, 0, n)
        System.arraycopy(jy, 0, my, 0, n)
        val base = atan2(dy, dx)
        for (b in 0 until branches) {
            val idx = 3 + (rnd() * (n - 8)).toInt()
            val bx0 = mx[idx]
            val by0 = my[idx]
            val ang = base + (rnd() - 0.5f) * 1.7f
            val bl = len * (0.16f + 0.24f * rnd())
            val ex = bx0 + cos(ang) * bl
            val ey = by0 + sin(ang) * bl
            val n2 = makeBolt(bx0, by0, ex, ey, 3, bl * 0.22f)
            strokePoly(n2, wd * 0.55f, col, a * 0.8f, c)
        }
    }

    // ------------------------------------------------------------------ círculo rúnico

    private fun runeCircle(c: Canvas, x: Float, y: Float, r: Float, rot: Float, col: Int, a: Float, seed: Int) {
        if (a <= 0.01f || r < 2f) return
        c.save()
        c.translate(x, y)
        c.scale(1f, 0.5f)
        c.rotate(rot)
        val w0 = max(2f, r * 0.028f)
        glowAt(c, 0f, 0f, r * 1.08f, col, a * 0.25f)
        p.style = Paint.Style.STROKE
        prep(col, a * 0.35f, true)
        p.strokeWidth = w0 * 3f
        c.drawCircle(0f, 0f, r, p)
        prep(col, a, true)
        p.strokeWidth = w0
        c.drawCircle(0f, 0f, r, p)
        prep(col, a * 0.8f, true)
        p.strokeWidth = w0 * 0.7f
        c.drawCircle(0f, 0f, r * 0.78f, p)
        prep(mixCol(col, WHITE, 0.6f), a * 0.7f, true)
        p.strokeWidth = w0 * 0.5f
        c.drawCircle(0f, 0f, r * 1.04f, p)
        // marcas na borda
        prep(col, a * 0.9f, true)
        p.strokeWidth = w0 * 0.6f
        for (i in 0 until 36) {
            val an = i * TAU / 36f
            val l2 = if (i % 3 == 0) 0.11f else 0.055f
            val co = cos(an)
            val si = sin(an)
            c.drawLine(co * r * 0.78f, si * r * 0.78f, co * r * (0.78f + l2), si * r * (0.78f + l2), p)
        }
        // glifos entre os dois anéis
        prep(mixCol(col, WHITE, 0.35f), a * 0.95f, true)
        p.strokeWidth = w0 * 0.7f
        val gs = r * 0.06f
        for (i in 0 until 12) {
            c.save()
            c.rotate(i * 30f)
            c.translate(r * 0.9f, 0f)
            seedR(seed + i * 31)
            for (k in 0 until 3) {
                val ax = (rnd() - 0.5f) * 2f * gs
                val ay = (rnd() - 0.5f) * 2f * gs
                val bx = (rnd() - 0.5f) * 2f * gs
                val byy = (rnd() - 0.5f) * 2f * gs
                c.drawLine(ax, ay, bx, byy, p)
            }
            c.restore()
        }
        // hexagrama no centro
        prep(col, a * 0.85f, true)
        p.strokeWidth = w0 * 0.8f
        val rs2 = r * 0.62f
        for (tri in 0 until 2) {
            path.reset()
            for (v in 0 until 3) {
                val an = v * TAU / 3f + tri * PI_F / 3f - PI_F / 2f
                val px = cos(an) * rs2
                val py = sin(an) * rs2
                if (v == 0) path.moveTo(px, py) else path.lineTo(px, py)
            }
            path.close()
            c.drawPath(path, p)
        }
        prep(mixCol(col, WHITE, 0.5f), a * 0.8f, true)
        p.strokeWidth = w0 * 0.6f
        c.drawCircle(0f, 0f, r * 0.3f, p)
        c.restore()
    }

    // ------------------------------------------------------------------ infinito (usado também na interface)

    fun infinity(c: Canvas, cx: Float, cy: Float, size: Float, col: Int, a: Float, add: Boolean, sw0: Float) {
        path.reset()
        val n = 48
        for (i in 0..n) {
            val th = i * TAU / n
            val s = sin(th)
            val co = cos(th)
            val dn = 1f + s * s
            val x = cx + size * co / dn
            val y = cy + size * s * co / dn
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        p.style = Paint.Style.STROKE
        prep(col, a, add)
        p.strokeWidth = sw0
        c.drawPath(path, p)
    }

    // ------------------------------------------------------------------ camada do chão

    fun drawGround(c: Canvas) {
        val pl = g.player
        val tm = g.time
        if (!g.dead) {
            val fx0 = sx(pl.fx + 0.5f)
            val fy0 = sy(pl.fy + 0.88f)
            if (pl.hasteT > 0f) {
                val fl = if (pl.hasteT < 3f) 0.6f + 0.4f * sin(pl.hasteT * 16f) else 1f
                glowFlat(c, fx0, fy0, tl * 0.7f, 0xFF7CFF8A.toInt(), 0.3f * fl)
                for (k in 0 until 3) {
                    rect.set(fx0 - tl * 0.46f, fy0 - tl * 0.23f, fx0 + tl * 0.46f, fy0 + tl * 0.23f)
                    prep(0xFF9CFFA8.toInt(), 0.6f * fl, true)
                    p.style = Paint.Style.STROKE
                    p.strokeWidth = tl * 0.035f
                    c.drawArc(rect, tm * 420f + k * 120f, 62f, false, p)
                }
            }
            if (pl.shieldT > 0f) {
                val age = SHIELD_DUR - pl.shieldT
                val fl = if (pl.shieldT < 3f) 0.55f + 0.45f * sin(pl.shieldT * 14f) else 1f
                val a = cl(age / 0.5f) * fl
                glowFlat(c, fx0, fy0, tl * 1.0f, 0xFF4FA3FF.toInt(), 0.28f * a)
                runeCircle(c, fx0, fy0, tl * 0.82f, tm * 28f, 0xFF6FB6FF.toInt(), 0.55f * a, 77)
            }
            if (g.cheat) {
                glowFlat(c, fx0, fy0, tl * 1.1f, 0xFFB388FF.toInt(), 0.22f)
                runeCircle(c, fx0, fy0, tl * 0.72f, -tm * 22f, 0xFFFFD27A.toInt(), 0.35f, 191)
            }
        }
        for (f in g.fx) {
            when (f.kind) {
                FxK.CAST -> {
                    val x = sx(fxX(f))
                    val y = sy(fxY(f))
                    val e = env(f.t, 0.08f, f.dur, 0.35f)
                    val grow = 0.6f + 0.4f * eo(f.t / 0.2f)
                    runeCircle(c, x, y, tl * f.scale * grow, f.t * 260f, f.color, e * 0.9f, f.seed)
                }
                FxK.HEAL -> {
                    val x = sx(fxX(f))
                    val y = sy(fxY(f))
                    val e = env(f.t, 0.2f, f.dur, 0.6f)
                    val grow = 0.55f + 0.45f * eo(f.t / 0.35f)
                    glowFlat(c, x, y, tl * 1.25f * grow, f.color, 0.35f * e)
                    runeCircle(c, x, y, tl * 0.85f * grow, f.t * 80f, f.color, e * 0.95f, f.seed)
                    runeCircle(c, x, y, tl * 0.5f * grow, -f.t * 130f, 0xFFD8FFE0.toInt(), e * 0.7f, f.seed + 7)
                }
                FxK.SHIELD -> {
                    val x = sx(fxX(f))
                    val y = sy(fxY(f))
                    val e = env(f.t, 0.15f, f.dur, 0.7f)
                    val grow = 0.5f + 0.5f * eo(f.t / 0.3f)
                    glowFlat(c, x, y, tl * 1.5f * grow, f.color, 0.4f * e)
                    runeCircle(c, x, y, tl * 1.05f * grow, -f.t * 70f, f.color, e, f.seed)
                    runeCircle(c, x, y, tl * 0.72f * grow, f.t * 150f, 0xFFD8EEFF.toInt(), e * 0.8f, f.seed + 5)
                }
                FxK.ARCANE -> {
                    val x = sx(fxX(f))
                    val y = sy(fxY(f))
                    val e = env(f.t, 0.25f, f.dur, 0.9f)
                    val grow = 0.4f + 0.6f * eo(f.t / 0.5f)
                    glowFlat(c, x, y, tl * 2.4f * grow, 0xFFFFD27A.toInt(), 0.4f * e)
                    glowFlat(c, x, y, tl * 1.6f * grow, 0xFFB388FF.toInt(), 0.4f * e)
                    runeCircle(c, x, y, tl * 1.5f * grow, f.t * 60f, 0xFFFFD27A.toInt(), e, f.seed)
                    runeCircle(c, x, y, tl * 1.1f * grow, -f.t * 110f, 0xFFB388FF.toInt(), e * 0.95f, f.seed + 3)
                    runeCircle(c, x, y, tl * 0.68f * grow, f.t * 190f, 0xFFFFFFFF.toInt(), e * 0.85f, f.seed + 9)
                }
                FxK.HASTE -> {
                    val x = sx(fxX(f))
                    val y = sy(fxY(f))
                    val e = env(f.t, 0.08f, f.dur, 0.5f)
                    val s = eo(f.t / 0.5f)
                    glowFlat(c, x, y, tl * (0.5f + 1.1f * s), f.color, 0.45f * e)
                    for (k in 0 until 3) {
                        val rr = tl * (0.35f + 0.85f * s) * (1f - k * 0.12f)
                        rect.set(x - rr, y - rr * 0.5f, x + rr, y + rr * 0.5f)
                        prep(f.color, e * 0.9f, true)
                        p.style = Paint.Style.STROKE
                        p.strokeWidth = tl * 0.045f
                        c.drawArc(rect, f.t * 600f + k * 120f, 78f, false, p)
                        prep(WHITE, e * 0.6f, true)
                        p.strokeWidth = tl * 0.016f
                        c.drawArc(rect, f.t * 600f + k * 120f + 6f, 60f, false, p)
                    }
                }
                FxK.BLAST -> {
                    val x = sx(f.x)
                    val y = sy(f.y) + tl * 0.18f
                    val s = cl(f.t / f.dur)
                    glowFlat(c, x, y, tl * 1.5f, 0xFFFF6A1E.toInt(), 0.6f * (1f - s))
                    glowFlat(c, x, y, tl * 0.8f, 0xFFFFD27A.toInt(), 0.5f * (1f - cl(f.t / 0.4f)))
                }
                FxK.SCORCH -> {
                    val x = sx(f.x)
                    val y = sy(f.y)
                    val a = cl(f.t / 0.2f) * cl((f.dur - f.t) / 2f)
                    seedR(f.seed)
                    p.style = Paint.Style.FILL
                    for (i in 0 until 7) {
                        val ox = (rnd() - 0.5f) * tl * 0.7f
                        val oy = (rnd() - 0.5f) * tl * 0.38f
                        val rx = tl * (0.14f + 0.18f * rnd())
                        val ry = rx * 0.55f
                        prep(0xFF140A06.toInt(), a * 0.42f, false)
                        rect.set(x + ox - rx, y + oy - ry, x + ox + rx, y + oy + ry)
                        c.drawOval(rect, p)
                    }
                    val emb = cl(1f - f.t / 1.6f)
                    if (emb > 0f) {
                        for (i in 0 until 9) {
                            val ox = (rnd() - 0.5f) * tl * 0.75f
                            val oy = (rnd() - 0.5f) * tl * 0.4f
                            val tw = 0.5f + 0.5f * sin(f.t * 14f + i * 2f)
                            glowAt(c, x + ox, y + oy, tl * 0.07f, 0xFFFF7A2D.toInt(), emb * tw)
                        }
                    }
                }
                FxK.WAVE -> waveGround(c, f)
            }
        }
    }

    private fun waveGround(c: Canvas, f: Fx) {
        val fxv = DX[f.dir]
        val fyv = DY[f.dir]
        val sxv = -fyv
        val syv = fxv
        for (d in 1..4) {
            val half = d / 2
            for (k in -half..half) {
                val dl = d * 0.07f + (hash2(d, k, f.seed) and 7) * 0.012f
                val local = f.t - dl
                if (local <= 0f) continue
                val fade = cl(local / 0.2f) * (1f - cl((f.t - 1.4f) / 1.6f))
                val gx = sx(f.x + fxv * d + sxv * k)
                val gy = sy(f.y + fyv * d + syv * k) + tl * 0.18f
                seedR(hash2(d, k, f.seed))
                p.style = Paint.Style.FILL
                for (i in 0 until 4) {
                    val ox = (rnd() - 0.5f) * tl * 0.5f
                    val oy = (rnd() - 0.5f) * tl * 0.3f
                    val rx = tl * (0.16f + 0.16f * rnd())
                    prep(0xFF140A06.toInt(), fade * 0.45f, false)
                    rect.set(gx + ox - rx, gy + oy - rx * 0.55f, gx + ox + rx, gy + oy + rx * 0.55f)
                    c.drawOval(rect, p)
                }
                glowFlat(c, gx, gy, tl * 0.75f, 0xFFFF5A1E.toInt(), 0.55f * fade * (1f - cl(local / 1.5f)))
            }
        }
    }

    // ------------------------------------------------------------------ camada aérea

    fun drawAir(c: Canvas) {
        val pl = g.player
        if (!g.dead) {
            if (g.cheat) drawCheatAura(c)
            if (pl.hasteT > 0f) drawHasteAir(c)
            if (pl.shieldT > 0f) {
                val age = SHIELD_DUR - pl.shieldT
                val fl = if (pl.shieldT < 3f) 0.55f + 0.45f * sin(pl.shieldT * 14f) else 1f
                drawDome(c, age, fl)
            }
        }
        for (m in g.monsters) {
            if (!m.alive) continue
            if (m.burnT > 0f) drawBurn(c, m)
            if (m.shockT > 0f) drawShock(c, m)
        }
        for (f in g.fx) {
            when (f.kind) {
                FxK.FLARE -> drawFlare(c, f)
                FxK.HEAL -> drawHeal(c, f)
                FxK.FIREBALL -> drawFireball(c, f)
                FxK.BLAST -> drawBlast(c, f)
                FxK.BOLT -> drawBoltFx(c, f)
                FxK.WAVE -> drawWave(c, f)
                FxK.HASTE -> drawHasteBurst(c, f)
                FxK.SHIELD -> drawShieldBurst(c, f)
                FxK.BREAK -> drawBreak(c, f)
                FxK.ARCANE -> drawArcane(c, f)
            }
        }
    }

    private fun flashScreen(c: Canvas, col: Int, a: Float) {
        if (a <= 0.01f) return
        prep(col, a, true)
        p.style = Paint.Style.FILL
        c.drawRect(0f, 0f, sw.toFloat(), sh.toFloat(), p)
    }

    private fun drawFlare(c: Canvas, f: Fx) {
        val s = cl(f.t / f.dur)
        val a = 1f - s
        val x = sx(f.x)
        val y = sy(f.y)
        val sz = tl * (0.35f + 0.55f * sin(s * PI_F))
        glowAt(c, x, y, sz * 1.5f, f.color, 0.7f * a)
        glowAt(c, x, y, sz * 0.7f, mixCol(f.color, WHITE, 0.6f), 0.9f * a)
        glowAt(c, x, y, sz * 0.3f, WHITE, a)
        star(c, x, y, tl * (0.7f - 0.3f * s), tl * (0.45f - 0.2f * s), mixCol(f.color, WHITE, 0.5f), a, f.t * 90f)
        ringGlow(c, x, y, tl * 0.75f * eo(s), tl * 0.75f * eo(s), tl * 0.025f, f.color, a * 0.8f)
    }

    private fun drawHeal(c: Canvas, f: Fx) {
        val x = sx(fxX(f))
        val y = sy(fxY(f))
        val e = env(f.t, 0.2f, f.dur, 0.6f)
        val rise = eo(f.t / 0.4f)
        softColumn(c, x, y, tl * 1.2f * (0.9f + 0.1f * sin(f.t * 9f)), tl * 2.3f * rise, f.color, 0.5f * e)
        softColumn(c, x, y, tl * 0.5f, tl * 2.6f * rise, WHITE, 0.35f * e)
        for (s in 0 until 2) {
            for (i in 0 until 16) {
                val u0 = i / 16f
                val uu = (u0 + f.t * 0.5f) % 1f
                val ang = uu * 10f + f.t * 3.2f + s * PI_F
                val rr = tl * 0.52f * (1f - 0.35f * uu)
                val hx = x + cos(ang) * rr
                val hy = y - uu * tl * 2.1f + sin(ang) * rr * 0.5f - tl * 0.05f
                val depth = 0.65f + 0.35f * sin(ang)
                val al = e * sin(uu * PI_F) * depth
                glowAt(c, hx, hy, tl * 0.1f * (0.7f + 0.5f * depth), f.color, al)
                glowAt(c, hx, hy, tl * 0.04f, WHITE, al)
                if (i % 4 == 0) star(c, hx, hy, tl * 0.12f, tl * 0.12f, WHITE, al * 0.8f, 0f)
            }
        }
        for (j in 0 until 6) {
            val ph = (f.t * 0.65f + j / 6f) % 1f
            val h1 = hash2(j, 3, f.seed)
            val ox = (((h1 and 255) / 255f) - 0.5f) * tl * 1.1f
            val al = e * sin(ph * PI_F)
            plus(c, x + ox, y - (0.15f + ph * 1.9f) * tl, tl * 0.075f, mixCol(f.color, WHITE, 0.5f), al)
        }
        if (f.t < 0.5f) {
            val q = f.t / 0.5f
            glowAt(c, x, y - tl * 0.35f, tl * (0.5f + 1.1f * q), f.color, 0.7f * (1f - q))
            glowAt(c, x, y - tl * 0.35f, tl * (0.3f + 0.5f * q), WHITE, 0.5f * (1f - q))
        }
    }

    private fun drawFireball(c: Canvas, f: Fx) {
        val u = f.fbU()
        val hx = sx(f.fbX(u))
        val hy = sy(f.fbY(u))
        val gx = sx(f.fbGX(u))
        val gy = sy(f.fbGY(u))
        glowFlat(c, gx, gy + tl * 0.1f, tl * 0.6f, 0xFFFF7A2D.toInt(), 0.5f)
        for (k in 0..11) {
            val uk = max(0f, u - k * 0.03f)
            val xk = sx(f.fbX(uk))
            val yk = sy(f.fbY(uk))
            val fall = 1f - k / 12f
            val rr = tl * (0.2f - k * 0.011f)
            glowAt(c, xk, yk, rr, 0xFFFF6A1E.toInt(), fall * 0.75f)
            if (k % 2 == 0) glowAt(c, xk, yk, rr * 1.9f, 0xFFFF3A12.toInt(), fall * 0.25f)
        }
        val dx = f.tx - f.x
        val dy = f.ty - f.y
        var dl = sqrt(dx * dx + dy * dy)
        if (dl < 0.001f) dl = 0.001f
        val phi = atan2(-dx / dl, dy / dl) * 57.29578f
        c.save()
        c.translate(hx, hy)
        c.rotate(phi)
        val fl = sin(g.time * 40f)
        flameStack(c, 0f, tl * 0.05f, tl * 0.36f, tl * (0.8f + 0.14f * fl), tl * 0.05f * fl, 1f)
        c.restore()
        glowAt(c, hx, hy, tl * 0.5f, 0xFFFF8A3D.toInt(), 0.9f)
        glowAt(c, hx, hy, tl * 0.28f, 0xFFFFD27A.toInt(), 1f)
        glowAt(c, hx, hy, tl * 0.13f, WHITE, 1f)
        star(c, hx, hy, tl * 0.32f, tl * 0.32f, 0xFFFFE0A0.toInt(), 0.8f, g.time * 300f)
    }

    private fun drawBlast(c: Canvas, f: Fx) {
        val x = sx(f.x)
        val y = sy(f.y)
        val t0 = f.t
        if (t0 < 0.12f) {
            val q = 1f - t0 / 0.12f
            glowAt(c, x, y, tl * 1.7f, WHITE, 0.9f * q)
            flashScreen(c, 0xFFFFB060.toInt(), 0.12f * q)
        }
        val rad = tl * (0.25f + 0.75f * eo(t0 / 0.3f))
        val fade = 1f - cl((t0 - 0.2f) / 0.6f)
        glowAt(c, x, y, rad * 2.0f, 0xFFFF3A12.toInt(), 0.55f * fade)
        glowAt(c, x, y, rad * 1.35f, 0xFFFF8A2D.toInt(), 0.8f * fade)
        glowAt(c, x, y, rad * 0.85f, 0xFFFFD25A.toInt(), 0.9f * fade)
        glowAt(c, x, y, rad * 0.42f, WHITE, fade * fade)
        val tf = 1f - cl((t0 - 0.1f) / 0.6f)
        for (i in 0 until 9) {
            val h1 = hash2(i, 5, f.seed)
            val an = i * TAU / 9f + (h1 and 255) / 255f * 0.6f
            val len = rad * (0.9f + 0.9f * (((h1 ushr 8) and 255) / 255f)) * eo(t0 / 0.25f)
            c.save()
            c.translate(x, y)
            c.rotate(an * 57.29578f + 90f)
            val fl = 0.85f + 0.15f * sin(g.time * 25f + i)
            flameStack(c, 0f, -rad * 0.25f, rad * 0.5f, len * fl, 0f, tf * 0.9f)
            c.restore()
        }
        val s1 = cl(t0 / 0.45f)
        val r1 = tl * (0.2f + 1.7f * eo(s1))
        ringGlow(c, x, y, r1, r1 * 0.58f, tl * 0.06f * (1f - s1) + 1f, 0xFFFFC27A.toInt(), 1f - s1)
        val s2 = cl((t0 - 0.07f) / 0.45f)
        if (t0 > 0.07f) {
            val r2 = tl * (0.1f + 1.15f * eo(s2))
            ringGlow(c, x, y, r2, r2 * 0.58f, tl * 0.04f * (1f - s2) + 1f, 0xFFFF8A3D.toInt(), (1f - s2) * 0.8f)
        }
        if (t0 > 0.05f) {
            val pq = cl((t0 - 0.05f) / 0.7f)
            val hh = tl * (0.4f + 1.15f * sin(pq * PI_F * 0.85f))
            val fl = 0.9f + 0.1f * sin(g.time * 22f)
            flameStack(c, x, y + tl * 0.12f, tl * 0.62f * (1f - pq * 0.4f), hh * fl, sin(g.time * 9f) * tl * 0.05f, (1f - pq) * 0.9f)
        }
    }

    private fun drawBoltFx(c: Canvas, f: Fx) {
        val x1 = sx(f.x)
        val y1 = sy(f.y)
        val x2 = sx(f.tx)
        val y2 = sy(f.ty)
        val frame = (f.t / 0.045f).toInt()
        val sd = f.seed + frame * 7919
        var a = if (f.t < 0.04f) f.t / 0.04f else 1f - (f.t - 0.04f) / (f.dur - 0.04f)
        if (a < 0f) a = 0f
        a = a * a * 0.4f + a * 0.6f
        val wd = tl * 0.05f
        flashScreen(c, f.color, 0.11f * cl(1f - f.t / 0.2f))
        drawBolt(c, x1, y1, x2, y2, sd, f.color, a, wd, 3)
        drawBolt(c, x1, y1 + tl * 0.02f, x2, y2, sd + 17, f.color, a * 0.55f, wd * 0.6f, 1)
        drawBolt(c, x1, y1, x2 + tl * 0.03f, y2, sd + 41, 0xFFE9DDFF.toInt(), a * 0.4f, wd * 0.45f, 0)
        // origem
        glowAt(c, x1, y1, tl * 0.5f * a, f.color, 0.8f)
        glowAt(c, x1, y1, tl * 0.22f * a, WHITE, 0.9f)
        for (j in 0 until 3) {
            seedR(sd + j * 211)
            val an = rnd() * TAU
            val l2 = tl * (0.22f + 0.2f * rnd())
            drawBolt(c, x1, y1, x1 + cos(an) * l2, y1 + sin(an) * l2, sd + j * 13, f.color, a * 0.8f, tl * 0.022f, 0)
        }
        // impacto
        glowAt(c, x2, y2, tl * 1.0f * a, f.color, 0.9f)
        glowAt(c, x2, y2, tl * 0.4f * a, WHITE, 1f)
        star(c, x2, y2, tl * 0.9f * a, tl * 0.6f * a, mixCol(f.color, WHITE, 0.6f), a, f.t * 200f)
        for (j in 0 until 5) {
            seedR(sd + j * 97 + 5)
            val an = rnd() * TAU
            val l2 = tl * (0.45f + 0.45f * rnd())
            drawBolt(c, x2, y2 + tl * 0.1f, x2 + cos(an) * l2, y2 + tl * 0.1f + sin(an) * l2 * 0.5f, sd + j * 29, f.color, a * 0.85f, tl * 0.02f, 0)
        }
        val s = cl(f.t / 0.3f)
        val rr = tl * (0.2f + 0.95f * eo(s))
        ringGlow(c, x2, y2 + tl * 0.1f, rr, rr * 0.55f, tl * 0.04f * (1f - s) + 1f, f.color, 1f - s)
    }

    private fun drawWave(c: Canvas, f: Fx) {
        val fxv = DX[f.dir]
        val fyv = DY[f.dir]
        val sxv = -fyv
        val syv = fxv
        for (d in 1..4) {
            val half = d / 2
            for (k in -half..half) {
                val dl = d * 0.07f + (hash2(d, k, f.seed) and 7) * 0.012f
                val local = f.t - dl
                if (local <= 0f || local >= 1.5f) continue
                val e = cl(local / 0.14f) * (1f - cl((local - 0.65f) / 0.85f))
                val bxw = sx(f.x + fxv * d + sxv * k)
                val byw = sy(f.y + fyv * d + syv * k) + tl * 0.3f
                for (j in 0 until 3) {
                    val h1 = hash2(d * 5 + k + 20, j, f.seed)
                    val r1 = (h1 and 255) / 255f
                    val r2 = ((h1 ushr 8) and 255) / 255f
                    val r3 = ((h1 ushr 16) and 255) / 255f
                    val ox = (r1 - 0.5f) * tl * 0.6f
                    val oy = (r2 - 0.5f) * tl * 0.28f
                    val hgt = tl * (0.65f + 0.6f * r3) * e * (0.85f + 0.15f * sin(f.t * 17f + j * 2.1f + d))
                    val wid = tl * (0.3f + 0.12f * r1) * (0.6f + 0.4f * e)
                    val lean = sin(f.t * 9f + j * 1.7f + k) * tl * 0.06f + fxv * tl * 0.1f * e
                    flameStack(c, bxw + ox, byw + oy, wid, hgt, lean, e)
                }
                glowAt(c, bxw, byw - tl * 0.25f, tl * 0.85f * e, 0xFFFF6A1E.toInt(), 0.55f * e)
            }
        }
        if (f.t < 0.55f) {
            val front = min(4.4f, f.t / 0.3f * 4.4f)
            val fa = 1f - cl((f.t - 0.2f) / 0.35f)
            val hx = sx(f.x + fxv * front)
            val hy = sy(f.y + fyv * front) + tl * 0.1f
            glowAt(c, hx, hy, tl * 1.1f, 0xFFFF7A2D.toInt(), 0.7f * fa)
            glowAt(c, hx, hy, tl * 0.6f, 0xFFFFE066.toInt(), 0.8f * fa)
            glowAt(c, hx, hy, tl * 0.28f, WHITE, fa)
            val aDeg = atan2(fyv.toFloat(), fxv.toFloat()) * 57.29578f
            val cxm = sx(f.x)
            val cym = sy(f.y) + tl * 0.1f
            for (j in 0 until 3) {
                val rr = (front - j * 0.45f) * tl
                if (rr < tl * 0.4f) continue
                val aj = fa * (1f - j * 0.3f)
                rect.set(cxm - rr, cym - rr, cxm + rr, cym + rr)
                p.style = Paint.Style.STROKE
                prep(0xFFFFB04D.toInt(), aj * 0.35f, true)
                p.strokeWidth = tl * 0.18f
                c.drawArc(rect, aDeg - 34f, 68f, false, p)
                prep(0xFFFFE9A0.toInt(), aj * 0.9f, true)
                p.strokeWidth = tl * 0.05f
                c.drawArc(rect, aDeg - 30f, 60f, false, p)
            }
        }
    }

    private fun drawHasteBurst(c: Canvas, f: Fx) {
        val x = sx(fxX(f))
        val y = sy(fxY(f))
        val e = env(f.t, 0.08f, f.dur, 0.5f)
        val bodyY = y - tl * 0.35f
        val s = cl(f.t / 0.55f)
        for (i in 0 until 14) {
            val h1 = hash2(i, 9, f.seed)
            val an = i * TAU / 14f + (h1 and 255) / 255f * 0.3f
            val r0 = tl * (0.3f + 0.9f * eo(s))
            val ln = tl * 0.55f * (1f - s)
            val co = cos(an)
            val si = sin(an)
            lineAdd(c, x + co * r0, bodyY + si * r0 * 0.7f, x + co * (r0 + ln), bodyY + si * (r0 + ln) * 0.7f, tl * 0.025f, f.color, (1f - s) * 0.9f)
        }
        val head = eo(f.t / 0.7f)
        for (k in 0 until 3) {
            val tail = max(0f, head - 0.4f)
            var u = tail
            val steps = 12
            val du = (head - tail) / steps
            for (i in 0 until steps) {
                val u2 = u + du
                val an1 = u * 7f + k * 2.09f + f.t * 6f
                val an2 = u2 * 7f + k * 2.09f + f.t * 6f
                val r1 = tl * 0.5f * (1f - 0.3f * u)
                val r2 = tl * 0.5f * (1f - 0.3f * u2)
                val al = (i + 1f) / steps * e
                lineAdd(
                    c,
                    x + cos(an1) * r1, y - u * tl * 1.8f + sin(an1) * r1 * 0.5f,
                    x + cos(an2) * r2, y - u2 * tl * 1.8f + sin(an2) * r2 * 0.5f,
                    tl * 0.045f, f.color, al
                )
                u = u2
            }
        }
        val pulse = 0.6f + 0.4f * sin(f.t * 30f)
        glowAt(c, x - tl * 0.18f, y, tl * 0.3f, f.color, 0.8f * e * pulse)
        glowAt(c, x + tl * 0.18f, y, tl * 0.3f, f.color, 0.8f * e * pulse)
        if (f.t < 0.4f) {
            val q = f.t / 0.4f
            glowAt(c, x, bodyY, tl * (0.5f + 0.9f * q), f.color, 0.6f * (1f - q))
        }
    }

    private fun drawHasteAir(c: Canvas) {
        val pl = g.player
        val tm = g.time
        val fl = if (pl.hasteT < 3f) 0.6f + 0.4f * sin(pl.hasteT * 16f) else 1f
        val cx = sx(pl.fx + 0.5f)
        val cy = sy(pl.fy + 0.6f)
        for (k in 0 until 2) {
            val yy = cy + (k - 0.5f) * tl * 0.38f
            rect.set(cx - tl * 0.4f, yy - tl * 0.14f, cx + tl * 0.4f, yy + tl * 0.14f)
            p.style = Paint.Style.STROKE
            prep(0xFF9CFFA8.toInt(), 0.5f * fl, true)
            p.strokeWidth = tl * 0.03f
            c.drawArc(rect, tm * 380f * (if (k == 0) 1f else -1f) + k * 90f, 105f, false, p)
        }
    }

    private fun drawShieldBurst(c: Canvas, f: Fx) {
        val x = sx(fxX(f))
        val y = sy(fxY(f))
        val e = env(f.t, 0.1f, f.dur, 0.6f)
        val hDome = tl * 1.35f
        for (k in 0 until 2) {
            val t2 = f.t - k * 0.18f
            if (t2 <= 0f || t2 > 0.7f) continue
            val q = t2 / 0.7f
            val h = q * hDome
            val ratio = h / hDome
            val rr = tl * 0.66f * sqrt(max(0f, 1f - ratio * ratio))
            ringGlow(c, x, y - h, rr, rr * 0.3f, tl * 0.04f, f.color, (1f - q) * 0.95f)
        }
        if (f.t < 0.45f) {
            val q = f.t / 0.45f
            glowAt(c, x, y - tl * 0.4f, tl * (0.6f + 1.0f * q), f.color, 0.7f * (1f - q))
            star(c, x, y - tl * 0.4f, tl * 1.0f * (1f - q), tl * 0.7f * (1f - q), WHITE, 0.9f * (1f - q), 0f)
        }
        if (e > 0f) {
            for (i in 0 until 6) {
                val an = i * TAU / 6f + f.t * 2.4f
                val rr = tl * 0.8f
                val hx = x + cos(an) * rr
                val hy = y - tl * 0.1f + sin(an) * rr * 0.45f
                glowAt(c, hx, hy, tl * 0.1f, f.color, 0.8f * e)
                glowAt(c, hx, hy, tl * 0.04f, WHITE, e)
            }
        }
    }

    private fun drawBreak(c: Canvas, f: Fx) {
        val x = sx(fxX(f))
        val y = sy(g.player.fy + 0.56f)
        val s = cl(f.t / f.dur)
        val col = 0xFF8CC8FF.toInt()
        if (f.t < 0.15f) {
            val q = 1f - f.t / 0.15f
            glowAt(c, x, y, tl * 1.5f, col, 0.9f * q)
            glowAt(c, x, y, tl * 0.8f, WHITE, 0.9f * q)
        }
        val rr = tl * (0.6f + 1.1f * eo(s / 0.6f))
        ringGlow(c, x, y, rr, rr * 1.1f, tl * 0.05f * (1f - s) + 1f, col, 1f - s)
        for (i in 0 until 8) {
            val h1 = hash2(i, 11, f.seed)
            val an = i * TAU / 8f + (h1 and 255) / 255f * 0.5f
            val l0 = tl * 0.3f * eo(s / 0.4f)
            val l1 = tl * (0.5f + 0.5f * eo(s / 0.4f))
            drawBolt(c, x + cos(an) * l0, y + sin(an) * l0, x + cos(an) * l1, y + sin(an) * l1, f.seed + i * 31 + (f.t / 0.05f).toInt(), col, (1f - s) * 0.9f, tl * 0.022f, 0)
        }
    }

    private fun drawArcane(c: Canvas, f: Fx) {
        val x = sx(fxX(f))
        val y = sy(fxY(f))
        val e = env(f.t, 0.25f, f.dur, 0.9f)
        val rise = eo(f.t / 0.5f)
        val gold = 0xFFFFD27A.toInt()
        val violet = 0xFFB388FF.toInt()
        softColumn(c, x, y, tl * 0.95f, tl * 3.3f * rise, gold, 0.5f * e)
        softColumn(c, x, y, tl * 0.4f, tl * 3.6f * rise, WHITE, 0.4f * e)
        for (i in 0 until 8) {
            val an = i * TAU / 8f + f.t * 0.6f
            val bx = x + cos(an) * tl * 1.15f
            val byy = y + sin(an) * tl * 0.58f
            val col = if (i % 2 == 0) gold else violet
            softColumn(c, bx, byy, tl * 0.24f, tl * 2.6f * rise * (0.8f + 0.2f * sin(f.t * 7f + i)), col, 0.45f * e)
        }
        for (k in 0 until 3) {
            val t2 = f.t - k * 0.25f
            if (t2 <= 0f) continue
            val s = cl(t2 / 0.9f)
            val rr = tl * (0.3f + 2.2f * eo(s))
            ringGlow(c, x, y, rr, rr * 0.5f, tl * 0.05f * (1f - s) + 1f, if (k == 1) violet else gold, (1f - s) * 0.9f)
        }
        for (i in 0 until 26) {
            val h1 = hash2(i, 21, f.seed)
            val ph = (f.t * (0.35f + ((h1 and 255) / 255f) * 0.35f) + ((h1 ushr 8) and 255) / 255f) % 1f
            val ang = ((h1 ushr 16) and 255) / 255f * TAU + f.t * 1.2f
            val rr = tl * (0.3f + 0.9f * (((h1 ushr 4) and 255) / 255f))
            val sxp = x + cos(ang) * rr
            val syp = y + sin(ang) * rr * 0.5f - ph * tl * 2.6f
            val al = e * sin(ph * PI_F)
            glowAt(c, sxp, syp, tl * 0.07f, if (i % 2 == 0) gold else violet, al)
            if (i % 5 == 0) star(c, sxp, syp, tl * 0.14f, tl * 0.14f, WHITE, al, f.t * 120f)
        }
        val cy = y - tl * 0.45f
        if (f.t < 0.8f) {
            val q = f.t / 0.8f
            glowAt(c, x, cy, tl * (0.8f + 1.8f * q), gold, 0.8f * (1f - q))
            glowAt(c, x, cy, tl * (0.4f + 0.8f * q), WHITE, 0.8f * (1f - q))
            flashScreen(c, 0xFFE0C0FF.toInt(), 0.14f * (1f - q))
        }
        for (i in 0 until 8) {
            val an = i * TAU / 8f + f.t * 0.9f
            val ln = tl * (1.1f + 0.3f * sin(f.t * 6f + i)) * e
            val co = cos(an)
            val si = sin(an)
            lineAdd(c, x + co * tl * 0.25f, cy + si * tl * 0.25f, x + co * ln, cy + si * ln, tl * 0.03f, gold, 0.55f * e)
        }
        val hov = cl(f.t / 0.5f) * e
        val iy = y - tl * 2.0f - 0.12f * tl * sin(f.t * 3f)
        glowAt(c, x, iy, tl * 0.9f, gold, 0.35f * hov)
        infinity(c, x, iy, tl * 0.5f * (0.6f + 0.4f * eo(f.t / 0.6f)), gold, 0.95f * hov, true, tl * 0.07f)
        infinity(c, x, iy, tl * 0.5f * (0.6f + 0.4f * eo(f.t / 0.6f)), WHITE, 0.9f * hov, true, tl * 0.025f)
    }

    // ------------------------------------------------------------------ sustentados

    private fun drawCheatAura(c: Canvas) {
        val pl = g.player
        val tm = g.time
        val cx = sx(pl.fx + 0.5f)
        val cy = sy(pl.fy + 0.55f)
        for (i in 0 until 4) {
            val ang = tm * 1.8f + i * TAU / 4f
            val col = if (i % 2 == 0) 0xFFFFE066.toInt() else 0xFFC9A0FF.toInt()
            for (k in 0 until 6) {
                val aa = ang - k * 0.17f
                val x = cx + cos(aa) * tl * 0.52f
                val y = cy + sin(aa) * tl * 0.24f - tl * 0.12f + sin(tm * 2f + i) * tl * 0.05f
                val fall = 1f - k / 6f
                glowAt(c, x, y, tl * 0.085f * (0.4f + fall), col, 0.85f * fall)
            }
        }
    }

    private fun drawDome(c: Canvas, age: Float, fade: Float) {
        val pl = g.player
        val cx = sx(pl.fx + 0.5f)
        val cy = sy(pl.fy + 0.56f)
        val grow = 0.25f + 0.75f * eo(age / 0.45f)
        val al = cl(age / 0.25f) * fade
        val rx = tl * 0.64f * grow
        val ry = tl * 0.76f * grow
        val tm = g.time
        // ondas de impacto recentes
        var rn = 0
        for (f in g.fx) {
            if (f.kind == FxK.RIPPLE && rn < 4) {
                rpA[rn] = f.ang
                rpT[rn] = f.t
                rpD[rn] = f.dur
                rn++
            }
        }
        // brilho interno
        glowAt(c, cx, cy, ry * 1.3f, 0xFF4FA3FF.toInt(), 0.3f * al)
        prep(0xFF7FC0FF.toInt(), 0.1f * al, false)
        p.style = Paint.Style.FILL
        rect.set(cx - rx, cy - ry, cx + rx, cy + ry)
        c.drawOval(rect, p)
        // hexágonos com projeção esférica
        val hs = 0.36f
        val hr = hs * 0.5774f * 0.9f
        p.style = Paint.Style.STROKE
        p.strokeWidth = max(1.5f, tl * 0.018f)
        for (row in -3..3) {
            val v = row * hs * 0.866f
            for (q in -4..4) {
                val u = (q + (row and 1) * 0.5f - 0.25f) * hs
                val r2 = u * u + v * v
                if (r2 > 0.9f) continue
                val z = sqrt(max(0f, 1f - r2))
                val rr = sqrt(r2)
                var ur = 0f
                var vr = 0f
                if (rr > 0.001f) {
                    ur = u / rr
                    vr = v / rr
                }
                val wave = sin(age * 2.6f - (u * 0.9f + v * 1.6f) * 3.2f)
                var swp = if (wave > 0f) wave else 0f
                swp = swp * swp
                swp = swp * swp * swp
                var boost = 0f
                for (j in 0 until rn) {
                    val hu = cos(rpA[j]) * 0.95f
                    val hv = sin(rpA[j]) * 0.95f
                    val dd = sqrt((u - hu) * (u - hu) + (v - hv) * (v - hv))
                    val front = rpT[j] * 3.2f
                    val bb = 1f - abs(dd - front) * 2.8f
                    if (bb > 0f) boost += bb * (1f - rpT[j] / rpD[j]) * 0.9f
                }
                var a = (0.1f + 0.25f * r2 + swp * 0.55f + boost) * al
                if (a > 1f) a = 1f
                if (a <= 0.02f) continue
                path.reset()
                for (k in 0 until 6) {
                    val an = (k * 60f + 30f) * 0.017453292f
                    val ox = cos(an) * hr
                    val oy = sin(an) * hr
                    var nx = ox
                    var ny = oy
                    if (rr > 0.001f) {
                        val par = ox * ur + oy * vr
                        val perp = -ox * vr + oy * ur
                        nx = par * z * ur - perp * vr
                        ny = par * z * vr + perp * ur
                    }
                    val px = cx + (u + nx) * rx
                    val py = cy + (v + ny) * ry
                    if (k == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }
                path.close()
                if (a > 0.3f) {
                    prep(0xFF9CD0FF.toInt(), a * 0.3f, true)
                    p.style = Paint.Style.FILL
                    c.drawPath(path, p)
                    p.style = Paint.Style.STROKE
                }
                prep(mixCol(0xFF7FC0FF.toInt(), WHITE, min(1f, boost + swp * 0.6f)), a, true)
                p.style = Paint.Style.STROKE
                c.drawPath(path, p)
            }
        }
        // borda do domo
        val rim = (0.55f + 0.2f * sin(tm * 4f)) * al
        ringGlow(c, cx, cy, rx, ry, tl * 0.032f, 0xFF8CC8FF.toInt(), rim)
        rect.set(cx - rx * 0.86f, cy - ry * 0.86f, cx + rx * 0.86f, cy + ry * 0.86f)
        p.style = Paint.Style.STROKE
        prep(WHITE, 0.7f * al, true)
        p.strokeWidth = tl * 0.035f
        c.drawArc(rect, 200f, 60f, false, p)
        prep(WHITE, 0.35f * al, true)
        p.strokeWidth = tl * 0.02f
        c.drawArc(rect, 20f, 34f, false, p)
        star(c, cx - rx * 0.45f, cy - ry * 0.55f, tl * 0.18f, tl * 0.18f, WHITE, 0.5f * al, tm * 20f)
        // orbes
        for (i in 0 until 3) {
            val ang = tm * 2.2f + i * TAU / 3f
            for (k in 0 until 7) {
                val aa = ang - k * 0.13f
                val ox = cx + cos(aa) * rx * 1.1f
                val oy = cy + sin(aa) * ry * 0.34f - ry * 0.1f
                val fall = 1f - k / 7f
                glowAt(c, ox, oy, tl * 0.1f * (0.4f + fall * 0.6f), 0xFF9CD0FF.toInt(), 0.85f * fall * al)
                if (k == 0) glowAt(c, ox, oy, tl * 0.04f, WHITE, al)
            }
        }
        // pontos de impacto
        for (j in 0 until rn) {
            val q = 1f - rpT[j] / rpD[j]
            val hx = cx + cos(rpA[j]) * rx
            val hy = cy + sin(rpA[j]) * ry
            glowAt(c, hx, hy, tl * 0.6f * q, 0xFF8CC8FF.toInt(), 0.9f * q)
            glowAt(c, hx, hy, tl * 0.25f * q, WHITE, q)
            star(c, hx, hy, tl * 0.45f * q, tl * 0.45f * q, WHITE, q, 45f)
        }
    }

    private fun drawBurn(c: Canvas, m: Monster) {
        val bxm = sx(m.fx + 0.5f)
        val bym = sy(m.fy + 0.85f)
        val k = min(1f, m.burnT / 0.6f)
        for (j in 0 until 3) {
            val ox = (j - 1) * tl * 0.17f
            val fl = 0.7f + 0.3f * sin(g.time * 19f + j * 2.3f + m.x)
            flameStack(c, bxm + ox, bym, tl * 0.22f, tl * (0.4f + 0.12f * j) * fl * k, sin(g.time * 8f + j) * tl * 0.04f, k)
        }
        glowAt(c, bxm, bym - tl * 0.25f, tl * 0.7f, 0xFFFF7A2D.toInt(), 0.35f * k)
    }

    private fun drawShock(c: Canvas, m: Monster) {
        val cxm = sx(m.fx + 0.5f)
        val cym = sy(m.fy + 0.55f)
        val k = min(1f, m.shockT / 0.3f)
        val frame = (g.time / 0.05f).toInt()
        for (j in 0 until 3) {
            seedR(frame * 13 + j * 101 + m.x * 7 + m.y)
            val a0 = rnd() * TAU
            val co = cos(a0)
            val si = sin(a0)
            drawBolt(c, cxm + co * tl * 0.1f, cym + si * tl * 0.1f, cxm + co * tl * 0.45f, cym + si * tl * 0.45f, frame * 131 + j, 0xFFB388FF.toInt(), k, tl * 0.025f, 0)
        }
        glowAt(c, cxm, cym, tl * 0.6f, 0xFFB388FF.toInt(), 0.3f * k)
    }

    // ------------------------------------------------------------------ partículas novas (tipos 3 a 9)

    fun particle(c: Canvas, pt: Particle) {
        val f = pt.life / pt.maxLife
        val age = pt.maxLife - pt.life
        val x = sx(pt.x)
        val y = sy(pt.y)
        if (x < -tl || y < -tl || x > sw + tl || y > sh + tl) return
        when (pt.kind) {
            3 -> {
                val r = (pt.size + pt.grow * age) * tl
                val a = f * 0.5f * cl(age / 0.12f)
                glowAt(c, x, y, r * 1.6f, pt.color, a, false)
            }
            4 -> {
                val l2 = 0.06f
                lineAdd(c, x, y, x - pt.vx * l2 * tl, y - pt.vy * l2 * tl, max(1.5f, pt.size * tl * 1.4f), pt.color, cl(f * 1.5f))
                glowAt(c, x, y, pt.size * tl * 2.6f, pt.color, cl(f * 1.5f) * 0.6f)
            }
            5 -> {
                val tw = 0.65f + 0.35f * sin(age * 22f + pt.rot)
                val r = pt.size * tl * (0.5f + 0.5f * f)
                glowAt(c, x, y, r * 3f, pt.color, cl(f * 1.4f) * 0.6f * tw)
                glowAt(c, x, y, r * 1.1f, WHITE, cl(f * 1.4f) * tw)
            }
            7 -> {
                val s = pt.size * tl
                val a = cl(f * 1.6f) * cl(age / 0.15f)
                c.save()
                c.translate(x, y)
                c.rotate(pt.rot * 57.29578f)
                prep(pt.color, a, true)
                p.style = Paint.Style.STROKE
                p.strokeWidth = max(1.5f, s * 0.22f)
                val h1 = hash2((pt.size * 1000f).toInt(), (pt.rot * 100f).toInt(), 3)
                for (k in 0 until 3) {
                    val q = (h1 ushr (k * 5)) and 31
                    val a1 = (q / 31f - 0.5f) * 2f * s
                    val a2 = (((h1 ushr (k * 5 + 3)) and 31) / 31f - 0.5f) * 2f * s
                    c.drawLine(a1, -s, a2, s, p)
                }
                c.restore()
                glowAt(c, x, y, s * 2.2f, pt.color, a * 0.4f)
            }
            8 -> {
                val a = cl(f * 1.6f)
                val l2 = pt.size * tl * 5f
                var vl = sqrt(pt.vx * pt.vx + pt.vy * pt.vy)
                if (vl < 0.001f) vl = 0.001f
                val dx = pt.vx / vl
                val dy = pt.vy / vl
                lineAdd(c, x, y, x - dx * l2, y - dy * l2, max(1.5f, tl * 0.03f), pt.color, a * 0.6f)
                lineAdd(c, x, y, x - dx * l2 * 0.6f, y - dy * l2 * 0.6f, max(1f, tl * 0.012f), WHITE, a * 0.8f)
            }
            9 -> {
                val s = pt.size * tl
                val a = cl(f * 1.4f)
                c.save()
                c.translate(x, y)
                c.rotate(pt.rot * 57.29578f)
                path.reset()
                for (k in 0 until 6) {
                    val an = k * 1.0471976f
                    val px = cos(an) * s
                    val py = sin(an) * s
                    if (k == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }
                path.close()
                p.style = Paint.Style.FILL
                prep(0xFF6FB6FF.toInt(), a * 0.45f, false)
                c.drawPath(path, p)
                p.style = Paint.Style.STROKE
                p.strokeWidth = max(1.5f, s * 0.18f)
                prep(0xFFD8EEFF.toInt(), a, true)
                c.drawPath(path, p)
                c.restore()
                glowAt(c, x, y, s * 2.2f, 0xFF8CC8FF.toInt(), a * 0.4f)
            }
        }
    }
}
