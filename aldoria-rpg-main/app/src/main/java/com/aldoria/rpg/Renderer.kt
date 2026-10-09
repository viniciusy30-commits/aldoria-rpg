package com.aldoria.rpg

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.PorterDuffXfermode
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

object Ui {
    const val NONE = 0
    const val BLOCK = 1
    const val MENU = 2
    const val BAG = 3
    const val TARGET = 4
    const val CHEAT = 5
    const val SPELL0 = 10
    const val BATTLE0 = 20
    const val MENU_CONT = 30
    const val MENU_SAVE = 31
    const val MENU_EXIT = 32
    const val MENU_CHEAT = 33
    const val BAG_CLOSE = 40
    const val TAB_BAG = 41
    const val TAB_CHAR = 42
    const val USE = 43
    const val RESPAWN = 50
    const val ITEM0 = 100
}

class Hit(val id: Int, val l: Float, val t: Float, val r: Float, val b: Float)

class Renderer(@Suppress("UNUSED_PARAMETER") ctx: Context, private val g: Game) {
    var w = 0
    var h = 0
    var tile = 64
    var camX = 0f
    var camY = 0f
    var joyCx = 0f
    var joyCy = 0f
    var joyR = 0f

    val hits = ArrayList<Hit>()
    val battleList = ArrayList<Monster>()
    val itemKeys = ArrayList<String>()
    private var modalStart = -1

    private var groundB: Array<Array<Bitmap>>? = null
    private var edgeB: Array<Array<Array<Bitmap>>>? = null
    private var objB: Array<Array<Bitmap?>>? = null
    private var decoB: Array<Array<Bitmap?>>? = null
    private var mageB: Array<Array<Bitmap>>? = null
    private var ratB: Array<Array<Bitmap>>? = null
    private var ratDeadB: Bitmap? = null
    private var miniB: Bitmap? = null
    private val lights = FloatArray(96 * 4)
    private var nLights = 0
    private var night = 0f
    private var dusk = 0f
    private val glowP = Paint(Paint.ANTI_ALIAS_FLAG)
    private val addMode = PorterDuffXfermode(PorterDuff.Mode.ADD)
    private val PRIO = intArrayOf(0, 1, 2, 3, 4, 5, -1, -1, -1)
    private var vig: RadialGradient? = null

    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val tp = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bp = Paint()
    private val flashP = Paint()
    private val fxr = SpellFx(g)
    private val ghostP = Paint()
    private val GN = 6
    private val gX = FloatArray(GN)
    private val gY = FloatArray(GN)
    private val gD = IntArray(GN)
    private val gF = IntArray(GN)
    private val gT = FloatArray(GN) { -100f }
    private var gi = 0
    private var lastGhost = 0f
    private val rect = RectF()
    private val rect2 = RectF()
    private val path = Path()
    private val mono: Typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)

    init {
        tp.typeface = mono
        flashP.colorFilter = PorterDuffColorFilter(Color.argb(170, 255, 70, 70), PorterDuff.Mode.SRC_ATOP)
        ghostP.colorFilter = PorterDuffColorFilter(Color.argb(150, 90, 255, 140), PorterDuff.Mode.SRC_ATOP)
    }

    fun onSize(nw: Int, nh: Int) {
        w = nw
        h = nh
        var ts = ((h / 9f).toInt() / 32) * 32
        if (ts < 64) ts = 64
        if (ts != tile || groundB == null) {
            tile = ts
            groundB = Art.ground(tile)
            edgeB = Art.edges(tile)
            objB = Art.objects(tile)
            decoB = Art.decals(tile)
            mageB = Art.mage(tile)
            ratB = Art.rat(tile)
            ratDeadB = Art.ratDead(tile)
        }
        if (miniB == null) miniB = buildMini()
        vig = RadialGradient(
            w / 2f, h / 2f, max(w, h) * 0.75f,
            intArrayOf(0x00000000, 0x00000000, 0x88000000.toInt()),
            floatArrayOf(0f, 0.55f, 1f), Shader.TileMode.CLAMP
        )
    }

    private fun buildMini(): Bitmap {
        val wd = g.world
        val b = Bitmap.createBitmap(wd.w, wd.h, Bitmap.Config.ARGB_8888)
        for (y in 0 until wd.h) {
            for (x in 0 until wd.w) b.setPixel(x, y, wd.miniColor(x, y))
        }
        return b
    }

    // ------------------------------------------------------------------ toques

    fun hit(x: Float, y: Float): Int {
        val start = if (modalStart >= 0) modalStart else 0
        var i = hits.size - 1
        while (i >= start) {
            val k = hits[i]
            if (x >= k.l && x <= k.r && y >= k.t && y <= k.b) return k.id
            i--
        }
        return if (modalStart >= 0) Ui.BLOCK else Ui.NONE
    }

    private fun addHit(id: Int, l: Float, t: Float, r: Float, b: Float) {
        hits.add(Hit(id, l, t, r, b))
    }

    // ------------------------------------------------------------------ utilitários de desenho

    private fun sxOf(wx: Float): Float = (wx - camX) * tile + w / 2f
    private fun syOf(wy: Float): Float = (wy - camY) * tile + h / 2f

    private fun txt(c: Canvas, s: String, x: Float, y: Float, size: Float, col: Int, align: Paint.Align) {
        tp.textSize = size
        tp.textAlign = align
        tp.style = Paint.Style.STROKE
        tp.strokeWidth = max(2f, size * 0.2f)
        tp.color = 0xFF000000.toInt()
        c.drawText(s, x, y, tp)
        tp.style = Paint.Style.FILL
        tp.color = col
        c.drawText(s, x, y, tp)
    }

    private fun panel(c: Canvas, l: Float, t: Float, r: Float, b: Float) {
        rect.set(l, t, r, b)
        val rad = tile * 0.14f
        p.style = Paint.Style.FILL
        p.color = 0xD01B1410.toInt()
        c.drawRoundRect(rect, rad, rad, p)
        p.style = Paint.Style.STROKE
        p.strokeWidth = max(2f, tile * 0.035f)
        p.color = 0xFF6B5236.toInt()
        c.drawRoundRect(rect, rad, rad, p)
        p.style = Paint.Style.FILL
    }

    private fun bar(c: Canvas, x: Float, y: Float, bw: Float, bh: Float, frac: Float, c1: Int, c2: Int, label: String) {
        val f = if (frac < 0f) 0f else if (frac > 1f) 1f else frac
        rect.set(x, y, x + bw, y + bh)
        p.style = Paint.Style.FILL
        p.color = 0xFF0E0A08.toInt()
        c.drawRoundRect(rect, bh * 0.3f, bh * 0.3f, p)
        if (f > 0f) {
            rect2.set(x + 2f, y + 2f, x + 2f + (bw - 4f) * f, y + bh - 2f)
            p.color = c1
            c.drawRoundRect(rect2, bh * 0.25f, bh * 0.25f, p)
            p.color = c2
            rect2.set(x + 2f, y + 2f, x + 2f + (bw - 4f) * f, y + bh * 0.5f)
            c.drawRoundRect(rect2, bh * 0.25f, bh * 0.25f, p)
        }
        p.style = Paint.Style.STROKE
        p.strokeWidth = 2f
        p.color = 0xFF4A3A28.toInt()
        c.drawRoundRect(rect, bh * 0.3f, bh * 0.3f, p)
        p.style = Paint.Style.FILL
        if (label.isNotEmpty()) txt(c, label, x + bw / 2f, y + bh * 0.76f, bh * 0.72f, COL_WHITE, Paint.Align.CENTER)
    }

    private fun hpColor(f: Float): Int =
        if (f > 0.6f) 0xFF3CCB4A.toInt() else if (f > 0.3f) 0xFFE8C53A.toInt() else if (f > 0.1f) 0xFFE8802A.toInt() else 0xFFD93030.toInt()

    private fun button(c: Canvas, cx: Float, cy: Float, r: Float, border: Int) {
        p.style = Paint.Style.FILL
        p.color = 0xCC1B1410.toInt()
        c.drawCircle(cx, cy, r, p)
        p.style = Paint.Style.STROKE
        p.strokeWidth = max(2f, r * 0.09f)
        p.color = border
        c.drawCircle(cx, cy, r, p)
        p.style = Paint.Style.FILL
    }

    // ------------------------------------------------------------------ quadro principal

    private fun rowOf(fy: Float): Int = Math.ceil((fy - 0.001f).toDouble()).toInt()

    private fun isWallish(o: Int): Boolean = o == Ob.WALL || o == Ob.DOOR
    private fun isFenceish(o: Int): Boolean = o == Ob.FENCE || o == Ob.LAMP

    private fun addLight(x: Float, y: Float, r: Float, kind: Int) {
        if (nLights >= 96) return
        val i = nLights * 4
        lights[i] = x
        lights[i + 1] = y
        lights[i + 2] = r
        lights[i + 3] = kind.toFloat()
        nLights++
    }

    private fun glowAdd(c: Canvas, x: Float, y: Float, r: Float, col: Int, a: Float) {
        glowP.style = Paint.Style.FILL
        glowP.xfermode = addMode
        for (k in 0 until 5) {
            glowP.color = withAlpha(col, a * 0.11f)
            c.drawCircle(x, y, r * (1f - k * 0.2f), glowP)
        }
        glowP.xfermode = null
    }

    fun draw(c: Canvas) {
        if (w == 0 || groundB == null) return
        val pl = g.player
        camX = pl.fx + 0.5f
        camY = pl.fy + 0.5f
        if (g.shake > 0f) {
            val amp = 0.12f * g.shakeAmp * min(1f, g.shake * 5f)
            camX += (Math.random().toFloat() - 0.5f) * amp
            camY += (Math.random().toFloat() - 0.5f) * amp
        }
        hits.clear()
        battleList.clear()
        itemKeys.clear()
        modalStart = -1
        nLights = 0
        val phase = (g.time / 300f + 0.42f) % 1f
        var d = 0.5f + 0.65f * sin((phase - 0.25f) * TAU)
        d = if (d < 0f) 0f else if (d > 1f) 1f else d
        night = 1f - d
        dusk = 1f - abs(d - 0.5f) * 2f

        c.drawColor(0xFF000000.toInt())
        drawGround(c)
        fxr.begin(tile, camX, camY, w, h)
        fxr.drawGround(c)
        drawSorted(c)
        drawProjectiles(c)
        drawCritters(c)
        drawDaylight(c)
        fxr.drawAir(c)
        drawParticles(c)
        drawNames(c)
        drawTexts(c)

        val v = vig
        if (v != null) {
            p.style = Paint.Style.FILL
            p.shader = v
            c.drawRect(0f, 0f, w.toFloat(), h.toFloat(), p)
            p.shader = null
        }
        if (pl.flash > 0f) {
            p.color = withAlpha(0xFFFF2020.toInt(), min(0.35f, pl.flash * 1.6f))
            c.drawRect(0f, 0f, w.toFloat(), h.toFloat(), p)
        }

        drawHud(c)
        if (g.ui == 1) drawBag(c)
        if (g.ui == 2) drawMenu(c)
        if (g.dead) drawDead(c)
    }

    private fun drawGround(c: Canvas) {
        val gb = groundB ?: return
        val eb = edgeB ?: return
        val db = decoB ?: return
        val t = tile
        val halfW = w / 2f
        val halfH = h / 2f
        val x0 = floor(camX - halfW / t).toInt() - 1
        val x1 = ceil(camX + halfW / t).toInt() + 1
        val y0 = floor(camY - halfH / t).toInt() - 1
        val y1 = ceil(camY + halfH / t).toInt() + 1
        val wd = g.world
        val frame = (g.time * 2.2f).toInt()
        for (ty in y0..y1) {
            for (tx in x0..x1) {
                if (!wd.inside(tx, ty)) continue
                val i = ty * wd.w + tx
                val sx = Math.round((tx - camX) * t + halfW).toFloat()
                val sy = Math.round((ty - camY) * t + halfH).toFloat()
                val gid = wd.ground[i]
                var v = wd.vari[i] and 7
                if (gid == Gd.WATER || gid == Gd.SHALLOW) v = (frame + tx + ty * 2) and 3
                c.drawBitmap(gb[gid][v], sx, sy, bp)
                val pr = PRIO[gid]
                if (pr >= 0) {
                    for (side in 0 until 4) {
                        val ng = wd.groundAt(tx + DX[side], ty + DY[side])
                        if (ng >= 1 && ng <= 5 && PRIO[ng] > pr) {
                            val ev = if (ng >= Gd.SHALLOW) (frame + tx + ty * 2) and 3 else (wd.vari[i] and 3)
                            c.drawBitmap(eb[ng - 1][side][ev], sx, sy, bp)
                        }
                    }
                }
                val dc = wd.deco[i]
                if (dc != Dc.NONE) {
                    val row = db[dc]
                    var bm = row[(wd.vari[i] ushr 1) and 3]
                    if (bm == null) bm = row[0]
                    if (bm != null) c.drawBitmap(bm, sx, sy, bp)
                }
            }
        }
        drawCorpses(c)
    }

    private fun onScreen(x: Float, y: Float): Boolean {
        val t = tile.toFloat()
        return x > -t * 2 && x < w + t && y > -t * 3 && y < h + t
    }

    private fun drawCorpses(c: Canvas) {
        val db = ratDeadB ?: return
        val t = tile.toFloat()
        for (cp in g.corpses) {
            val sx = Math.round(sxOf(cp.x.toFloat())).toFloat()
            val sy = Math.round(syOf(cp.y.toFloat())).toFloat()
            if (!onScreen(sx, sy)) continue
            bp.alpha = if (cp.life < 3f) (cp.life / 3f * 255f).toInt().coerceIn(0, 255) else 255
            c.drawBitmap(db, sx, sy, bp)
            bp.alpha = 255
            if (!cp.looted && cp.loot.isNotEmpty()) {
                val a = 0.5f + 0.5f * sin(g.time * 5f + cp.x)
                p.style = Paint.Style.FILL
                p.color = withAlpha(0xFFFFE066.toInt(), a)
                c.drawCircle(sx + t * 0.5f, sy + t * 0.55f - a * t * 0.12f, t * 0.06f * (0.6f + a), p)
            }
        }
    }

    private fun drawSorted(c: Canvas) {
        val ob = objB ?: return
        val rb = ratB ?: return
        val mb = mageB ?: return
        val t = tile
        val halfW = w / 2f
        val halfH = h / 2f
        val x0 = floor(camX - halfW / t).toInt() - 1
        val x1 = ceil(camX + halfW / t).toInt() + 1
        val y0 = floor(camY - halfH / t).toInt() - 1
        val y1 = ceil(camY + halfH / t).toInt() + 2
        val wd = g.world
        val pl = g.player
        val plRow = rowOf(pl.fy)
        for (ty in y0..y1) {
            for (tx in x0..x1) {
                if (!wd.inside(tx, ty)) continue
                val i = ty * wd.w + tx
                val o = wd.obj[i]
                if (o != Ob.NONE) drawObject(c, ob, o, tx, ty, i)
            }
            for (m in g.monsters) {
                if (m.alive && rowOf(m.fy) == ty) drawCreature(c, m, rb, false)
            }
            if (plRow == ty) drawCreature(c, pl, mb, true)
        }
    }

    private fun drawObject(c: Canvas, ob: Array<Array<Bitmap?>>, o: Int, tx: Int, ty: Int, i: Int) {
        val wd = g.world
        val t = tile
        val s = t / 32f
        var v = wd.ovar[i]
        val sx0 = Math.round((tx - camX) * t + w / 2f).toFloat()
        val sy0 = Math.round((ty - camY) * t + h / 2f).toFloat()
        val hasN = isWallish(wd.objAt(tx, ty - 1))
        val hasS = isWallish(wd.objAt(tx, ty + 1))
        if (o == Ob.WALL) {
            val combo = (if (hasN) 1 else 0) + (if (hasS) 2 else 0)
            val kind = wd.ovar[i]
            if (kind == 1) {
                v = 4 + combo
                addLight(sx0 + 16f * s, sy0 + (if (hasN) 8f else 19f) * s, t * 1.1f, 2)
            } else if (kind == 2) {
                v = 8 + combo + (if (((g.time * 6f).toInt() + tx) % 2 == 1) 4 else 0)
                addLight(sx0 + 16f * s, sy0 + (if (hasN) 3f else 14f) * s, t * 2.0f, 0)
            } else {
                v = combo
            }
        } else if (o == Ob.DOOR) {
            v = if (hasN) 1 else 0
        } else if (o == Ob.FOUNTAIN) {
            v = (g.time * 5f).toInt() and 3
        } else if (o == Ob.FIRE) {
            v = ((g.time * 4f).toInt() + tx) and 1
        } else if (o == Ob.CAULDRON) {
            v = ((g.time * 3f).toInt() + tx) and 1
        } else if (o == Ob.FENCE) {
            v = (if (isFenceish(wd.objAt(tx, ty - 1))) 1 else 0) or
                (if (isFenceish(wd.objAt(tx + 1, ty))) 2 else 0) or
                (if (isFenceish(wd.objAt(tx, ty + 1))) 4 else 0) or
                (if (isFenceish(wd.objAt(tx - 1, ty))) 8 else 0)
        }
        if (v < 0 || v > 15) v = 0
        val arr = ob[o]
        var bm = arr[v]
        if (bm == null) bm = arr[0]
        if (bm == null) return
        val sy = sy0 - (bm.height - t)
        if (o == Ob.LAMP) addLight(sx0 + 16f * s, sy + 11f * s, t * 2.6f, 1)
        if (o == Ob.FIRE) addLight(sx0 + 16f * s, sy + 22f * s, t * 2.4f, 0)
        if (o == Ob.CAULDRON) addLight(sx0 + 16f * s, sy + 15f * s, t * 1.6f, 3)
        if (o == Ob.OAK || o == Ob.PINE || o == Ob.BUSH || o == Ob.BUSHF) {
            val k = sin(g.time * 1.3f + tx * 1.7f + ty * 0.9f) * 0.012f
            c.save()
            c.translate(sx0, sy + bm.height)
            c.skew(k, 0f)
            c.drawBitmap(bm, 0f, -bm.height.toFloat(), bp)
            c.restore()
        } else {
            c.drawBitmap(bm, sx0, sy, bp)
        }
    }

    private fun drawCreature(c: Canvas, cr: Creature, arr: Array<Array<Bitmap>>, mage: Boolean) {
        val t = tile.toFloat()
        val sx = Math.round(sxOf(cr.fx)).toFloat()
        val sy = Math.round(syOf(cr.fy)).toFloat()
        if (!onScreen(sx, sy)) return
        val frame = if (cr.moving) ((cr.walk * 2f).toInt() and 3) else 0
        p.style = Paint.Style.FILL
        p.color = 0x55000000
        rect.set(sx + t * 0.18f, sy + t * 0.82f, sx + t * 0.82f, sy + t * 0.98f)
        c.drawOval(rect, p)
        if (mage && g.player.hasteT > 0f) drawGhosts(c, cr, arr, frame)
        val bm = arr[cr.dir][frame]
        c.drawBitmap(bm, sx, sy, if (cr.flash > 0f) flashP else bp)
        if (mage) {
            val s = t / 64f
            val di = if (cr.dir == 3) 1 else cr.dir
            val ox = MageData.ORB[(di * 4 + frame) * 2]
            val hx = if (cr.dir == 3) 64f - ox else ox
            val hy = MageData.ORB[(di * 4 + frame) * 2 + 1]
            val pulse = 0.8f + 0.2f * sin(g.time * 6f)
            var ch = 0f
            for (v in g.pressT) if (v > ch) ch = v
            glowAdd(c, sx + hx * s, sy + hy * s, t * (0.3f + 0.45f * night) * pulse * (1f + ch * 3.5f), if (ch > 0f) 0xFFFFE0A0.toInt() else 0xFFFF9A3D.toInt(), 0.9f)
            if (night > 0.12f) addLight(sx + hx * s, sy + hy * s, t * 2.0f, 0)
        }
    }

    private fun drawGhosts(c: Canvas, cr: Creature, arr: Array<Array<Bitmap>>, frame: Int) {
        if (cr.moving && g.time - lastGhost > 0.045f) {
            lastGhost = g.time
            gi = (gi + 1) % GN
            gX[gi] = cr.fx
            gY[gi] = cr.fy
            gD[gi] = cr.dir
            gF[gi] = frame
            gT[gi] = g.time
        }
        for (k in 0 until GN) {
            val age = g.time - gT[k]
            if (age < 0f || age > 0.3f) continue
            ghostP.alpha = ((1f - age / 0.3f) * 150f).toInt()
            c.drawBitmap(arr[gD[k]][gF[k]], Math.round(sxOf(gX[k])).toFloat(), Math.round(syOf(gY[k])).toFloat(), ghostP)
        }
    }

    private fun drawCritters(c: Canvas) {
        val t = tile.toFloat()
        val wd = g.world
        val tm = g.time
        if (night < 0.6f) {
            val cols = intArrayOf(0xFFFFD84A.toInt(), 0xFFFF8FC0.toInt(), 0xFFFFFFFF.toInt(), 0xFF8FC8FF.toInt())
            for (i in 0 until 5) {
                val bx = camX + sin(tm * 0.35f + i * 1.9f) * 5.5f + sin(tm * 1.3f + i) * 0.6f
                val by = camY + cos(tm * 0.31f + i * 2.4f) * 3.2f + cos(tm * 1.7f + i * 2f) * 0.5f
                if (wd.groundAt(bx.toInt(), by.toInt()) != Gd.GRASS) continue
                val x = sxOf(bx)
                val y = syOf(by)
                val flap = abs(sin(tm * 13f + i * 3f))
                val wing = t * 0.075f
                p.style = Paint.Style.FILL
                p.color = cols[i % 4]
                rect.set(x - wing * 1.5f * flap - 1f, y - wing, x, y + wing * 0.8f)
                c.drawOval(rect, p)
                rect.set(x, y - wing, x + wing * 1.5f * flap + 1f, y + wing * 0.8f)
                c.drawOval(rect, p)
                p.color = 0xFF2A1A10.toInt()
                c.drawRect(x - 1f, y - wing * 0.7f, x + 1f, y + wing * 0.9f, p)
            }
        }
        if (night > 0.35f) {
            for (i in 0 until 14) {
                val fx = camX + sin(tm * 0.2f + i * 2.1f) * 7f + sin(tm * 0.9f + i) * 0.8f
                val fy = camY + cos(tm * 0.23f + i * 1.7f) * 4.2f + cos(tm * 1.1f + i * 1.3f) * 0.7f
                val a = max(0f, sin(tm * 2.3f + i * 1.3f)) * night
                if (a < 0.05f) continue
                val x = sxOf(fx)
                val y = syOf(fy)
                glowAdd(c, x, y, t * 0.22f, 0xFFC8FF7A.toInt(), a)
                p.style = Paint.Style.FILL
                p.color = withAlpha(0xFFF4FFC0.toInt(), a)
                c.drawCircle(x, y, max(1.5f, t * 0.018f), p)
            }
        }
    }

    private fun drawDaylight(c: Canvas) {
        p.style = Paint.Style.FILL
        if (night > 0.02f) {
            p.color = withAlpha(0xFF0A1236.toInt(), night * 0.55f)
            c.drawRect(0f, 0f, w.toFloat(), h.toFloat(), p)
        }
        if (dusk > 0.05f) {
            p.color = withAlpha(0xFFFF7A30.toInt(), dusk * 0.13f)
            c.drawRect(0f, 0f, w.toFloat(), h.toFloat(), p)
        }
        if (night > 0.12f) {
            val k = min(1f, night * 1.6f)
            for (i in 0 until nLights) {
                val x = lights[i * 4]
                val y = lights[i * 4 + 1]
                val r = lights[i * 4 + 2]
                val kind = lights[i * 4 + 3].toInt()
                val fl = 0.88f + 0.12f * sin(g.time * 11f + x * 0.1f)
                val col = when (kind) {
                    0 -> 0xFFFF9A4A.toInt()
                    1 -> 0xFFFFE6A0.toInt()
                    2 -> 0xFFFFD27A.toInt()
                    else -> 0xFF7CFF9A.toInt()
                }
                glowAdd(c, x, y, r * fl, col, k)
            }
        }
    }

    private fun drawProjectiles(c: Canvas) {
        val t = tile.toFloat()
        for (s in g.shots) {
            val tx = s.target.fx + 0.5f
            val ty = s.target.fy + 0.45f
            for (k in 0..3) {
                val tt = max(0f, s.t - k * 0.06f)
                val wx = s.fromX + (tx - s.fromX) * tt
                val wy = s.fromY + (ty - s.fromY) * tt
                val x = sxOf(wx)
                val y = syOf(wy)
                p.style = Paint.Style.FILL
                p.color = withAlpha(s.color, 0.8f - k * 0.2f)
                c.drawCircle(x, y, t * (0.09f - k * 0.015f), p)
                if (k == 0) {
                    p.color = withAlpha(s.color, 0.3f)
                    c.drawCircle(x, y, t * 0.17f, p)
                    p.color = 0xFFFFFFFF.toInt()
                    c.drawCircle(x, y, t * 0.04f, p)
                }
            }
        }
    }

    private fun drawParticles(c: Canvas) {
        val t = tile.toFloat()
        p.style = Paint.Style.FILL
        for (pt in g.parts) {
            if (pt.delay > 0f) continue
            if (pt.kind >= 3) {
                fxr.particle(c, pt)
                continue
            }
            val f = pt.life / pt.maxLife
            val x = sxOf(pt.x)
            val y = syOf(pt.y)
            if (!onScreen(x, y)) continue
            val r = pt.size * t * (0.4f + 0.6f * f)
            p.color = withAlpha(pt.color, min(1f, f * 1.6f))
            when (pt.kind) {
                1 -> c.drawRect(x - r, y - r, x + r, y + r, p)
                2 -> {
                    c.drawRect(x - r * 2f, y - r * 0.35f, x + r * 2f, y + r * 0.35f, p)
                    c.drawRect(x - r * 0.35f, y - r * 2f, x + r * 0.35f, y + r * 2f, p)
                }
                else -> {
                    c.drawCircle(x, y, r, p)
                    p.color = withAlpha(pt.color, min(1f, f) * 0.25f)
                    c.drawCircle(x, y, r * 2f, p)
                }
            }
        }
    }

    private fun drawNames(c: Canvas) {
        val t = tile.toFloat()
        for (m in g.monsters) {
            if (!m.alive) continue
            val x = sxOf(m.fx) + t * 0.5f
            val y = syOf(m.fy)
            if (!onScreen(x, y)) continue
            txt(c, m.name, x, y + t * 0.06f, t * 0.19f, COL_WHITE, Paint.Align.CENTER)
            bar(c, x - t * 0.4f, y + t * 0.1f, t * 0.8f, t * 0.1f, m.hp.toFloat() / m.maxHp, hpColor(m.hp.toFloat() / m.maxHp), 0x55FFFFFF, "")
        }
        val pl = g.player
        if (!g.dead) {
            val x = sxOf(pl.fx) + t * 0.5f
            val y = syOf(pl.fy)
            txt(c, pl.name, x, y + t * 0.06f, t * 0.19f, 0xFFB8F0B8.toInt(), Paint.Align.CENTER)
            bar(c, x - t * 0.4f, y + t * 0.1f, t * 0.8f, t * 0.1f, pl.hp.toFloat() / pl.maxHp, hpColor(pl.hp.toFloat() / pl.maxHp), 0x55FFFFFF, "")
        }
        val tg = g.target
        if (tg != null && tg.alive) {
            val x = sxOf(tg.fx)
            val y = syOf(tg.fy)
            val a = 0.65f + 0.35f * sin(g.time * 7f)
            p.style = Paint.Style.STROKE
            p.strokeWidth = max(3f, t * 0.05f)
            p.color = withAlpha(0xFFFF3030.toInt(), a)
            c.drawRect(x + 2f, y + 2f, x + t - 2f, y + t - 2f, p)
            p.style = Paint.Style.FILL
        }
    }

    private fun drawTexts(c: Canvas) {
        val t = tile.toFloat()
        for (ft in g.texts) {
            val x = sxOf(ft.x)
            val y = syOf(ft.y)
            if (!onScreen(x, y)) continue
            val f = ft.life / ft.maxLife
            val a = if (f > 0.3f) 1f else f / 0.3f
            txt(c, ft.text, x, y, t * 0.3f * ft.size, withAlpha(ft.color, a), Paint.Align.CENTER)
        }
    }

    // ------------------------------------------------------------------ interface

    private fun drawHud(c: Canvas) {
        val t = tile.toFloat()
        val mg = t * 0.22f
        drawStatus(c, mg)
        drawMiniAndButtons(c, mg)
        drawBattle(c, mg)
        drawJoystick(c, mg)
        drawHotbar(c, mg)
        drawConsole(c, mg)
        val pz = g.world.isPz(g.player.x, g.player.y)
        val zone = if (pz) "Aldoria - Zona de Proteção" else "Terras Selvagens"
        txt(c, zone, w / 2f, mg + t * 0.28f, t * 0.24f, if (pz) 0xFF9BE7FF.toInt() else 0xFFFFD0A0.toInt(), Paint.Align.CENTER)
    }

    private fun drawStatus(c: Canvas, mg: Float) {
        val t = tile.toFloat()
        val pl = g.player
        val x = mg
        val y = mg
        panel(c, x, y, x + t * 4.4f, y + t * 1.5f)
        txt(c, pl.name + "   Nível " + pl.level, x + t * 0.2f, y + t * 0.38f, t * 0.27f, COL_YELLOW, Paint.Align.LEFT)
        bar(c, x + t * 0.2f, y + t * 0.52f, t * 4.0f, t * 0.28f, pl.hp.toFloat() / pl.maxHp, 0xFFD23A3A.toInt(), 0x66FFFFFF, "" + pl.hp + " / " + pl.maxHp)
        if (g.cheat) {
            val ph = 0.5f + 0.5f * sin(g.time * 2.2f)
            bar(c, x + t * 0.2f, y + t * 0.86f, t * 4.0f, t * 0.28f, 1f, mixCol(0xFF3A6FD2.toInt(), 0xFFB35CFF.toInt(), ph), 0x66FFFFFF, "")
            manaShine(c, x + t * 0.2f, y + t * 0.86f, t * 4.0f, t * 0.28f)
            fxr.infinity(c, x + t * 2.2f, y + t * 1.0f, t * 0.19f, COL_WHITE, 1f, false, max(2f, t * 0.035f))
        } else {
            bar(c, x + t * 0.2f, y + t * 0.86f, t * 4.0f, t * 0.28f, pl.mana.toFloat() / pl.maxMana, 0xFF3A6FD2.toInt(), 0x66FFFFFF, "" + pl.mana + " / " + pl.maxMana)
        }
        val cur = expForLevel(pl.level)
        val nxt = expForLevel(pl.level + 1)
        val ef = if (nxt > cur) (pl.exp - cur).toFloat() / (nxt - cur).toFloat() else 0f
        bar(c, x + t * 0.2f, y + t * 1.2f, t * 4.0f, t * 0.14f, ef, 0xFFE0B030.toInt(), 0x66FFFFFF, "")
        // ícones de efeito
        var ix = x + t * 0.3f
        val iy = y + t * 1.5f + t * 0.35f
        if (pl.hasteT > 0f) {
            button(c, ix, iy, t * 0.28f, 0xFF7CFF8A.toInt())
            txt(c, "V", ix, iy + t * 0.09f, t * 0.28f, 0xFF7CFF8A.toInt(), Paint.Align.CENTER)
            txt(c, "" + pl.hasteT.toInt(), ix, iy + t * 0.55f, t * 0.18f, COL_WHITE, Paint.Align.CENTER)
            ix += t * 0.7f
        }
        if (pl.shieldT > 0f) {
            button(c, ix, iy, t * 0.28f, 0xFF6FB6FF.toInt())
            txt(c, "E", ix, iy + t * 0.09f, t * 0.28f, 0xFF6FB6FF.toInt(), Paint.Align.CENTER)
            txt(c, "" + pl.shieldT.toInt(), ix, iy + t * 0.55f, t * 0.18f, COL_WHITE, Paint.Align.CENTER)
        }
    }

    private fun manaShine(c: Canvas, x: Float, y: Float, bw: Float, bh: Float) {
        val k = (g.time * 0.5f) % 1.6f - 0.3f
        c.save()
        c.clipRect(x + 2f, y + 2f, x + bw - 2f, y + bh - 2f)
        p.style = Paint.Style.FILL
        p.color = withAlpha(0xFFFFFFFF.toInt(), 0.35f)
        val sxp = x + bw * k
        c.drawRect(sxp, y, sxp + bw * 0.1f, y + bh, p)
        c.drawRect(sxp + bw * 0.13f, y, sxp + bw * 0.16f, y + bh, p)
        c.restore()
    }

    private fun drawMiniAndButtons(c: Canvas, mg: Float) {
        val t = tile.toFloat()
        val ms = t * 2.3f
        val l = w - mg - ms
        val top = mg
        panel(c, l - 3f, top - 3f, l + ms + 3f, top + ms + 3f)
        val mb = miniB
        val wd = g.world
        if (mb != null) {
            rect.set(l, top, l + ms, top + ms)
            // recorte: mostra a região ao redor do jogador (40 x 40 quadrados)
            val half = 20f
            var cx = g.player.fx + 0.5f
            var cy = g.player.fy + 0.5f
            cx = if (cx < half) half else if (cx > wd.w - half) wd.w - half else cx
            cy = if (cy < half) half else if (cy > wd.h - half) wd.h - half else cy
            val srcL = (cx - half).toInt()
            val srcT = (cy - half).toInt()
            val src = android.graphics.Rect(srcL, srcT, min(wd.w, srcL + 40), min(wd.h, srcT + 40))
            c.drawBitmap(mb, src, rect, bp)
            val ux = ms / 40f
            // monstros
            p.style = Paint.Style.FILL
            p.color = 0xFFFF4040.toInt()
            for (m in g.monsters) {
                if (!m.alive) continue
                val px = (m.x - srcL + 0.5f) * ux
                val py = (m.y - srcT + 0.5f) * ux
                if (px < 0f || py < 0f || px > ms || py > ms) continue
                c.drawCircle(l + px, top + py, max(2f, ux * 0.5f), p)
            }
            val ppx = (g.player.x - srcL + 0.5f) * ux
            val ppy = (g.player.y - srcT + 0.5f) * ux
            p.color = if (((g.time * 3f).toInt() and 1) == 0) 0xFFFFFFFF.toInt() else 0xFFFFE066.toInt()
            c.drawCircle(l + ppx, top + ppy, max(3f, ux * 0.8f), p)
        }
        // botões de menu e mochila à esquerda do minimapa
        val br = t * 0.4f
        val bx1 = l - mg - br
        val by = top + br
        button(c, bx1, by, br, 0xFF8A6A45.toInt())
        for (k in -1..1) {
            p.color = 0xFFE8D6B0.toInt()
            c.drawRect(bx1 - br * 0.45f, by + k * br * 0.32f - br * 0.07f, bx1 + br * 0.45f, by + k * br * 0.32f + br * 0.07f, p)
        }
        addHit(Ui.MENU, bx1 - br, by - br, bx1 + br, by + br)
        val bx2 = bx1 - br * 2f - mg
        button(c, bx2, by, br, if (g.ui == 1) 0xFFFFE066.toInt() else 0xFF8A6A45.toInt())
        // ícone de mochila
        rect.set(bx2 - br * 0.45f, by - br * 0.4f, bx2 + br * 0.45f, by + br * 0.5f)
        p.color = 0xFFB07A3C.toInt()
        c.drawRoundRect(rect, br * 0.25f, br * 0.25f, p)
        p.color = 0xFF7A4F22.toInt()
        c.drawRect(bx2 - br * 0.45f, by - br * 0.05f, bx2 + br * 0.45f, by + br * 0.1f, p)
        p.color = 0xFFF2C94C.toInt()
        c.drawRect(bx2 - br * 0.08f, by - br * 0.1f, bx2 + br * 0.08f, by + br * 0.2f, p)
        addHit(Ui.BAG, bx2 - br, by - br, bx2 + br, by + br)
        // botão do modo mago supremo (mana infinita + todas as magias)
        val bx3 = bx2 - br * 2f - mg
        val on = g.cheat
        if (on) glowAdd(c, bx3, by, br * 1.7f, 0xFFB388FF.toInt(), 0.55f + 0.25f * sin(g.time * 4f))
        button(c, bx3, by, br, if (on) mixCol(0xFFFFD27A.toInt(), 0xFFB388FF.toInt(), 0.5f + 0.5f * sin(g.time * 3f)) else 0xFF8A6A45.toInt())
        fxr.infinity(c, bx3, by, br * 0.52f, if (on) 0xFFFFE9A0.toInt() else 0xFFE8D6B0.toInt(), 1f, false, max(2f, br * 0.14f))
        addHit(Ui.CHEAT, bx3 - br, by - br, bx3 + br, by + br)
    }

    private fun drawBattle(c: Canvas, mg: Float) {
        val t = tile.toFloat()
        val ms = t * 2.3f
        val l = w - mg - ms
        val top = mg + ms + t * 0.3f
        // lista os monstros mais próximos
        val list = ArrayList<Monster>()
        for (m in g.monsters) {
            if (!m.alive) continue
            if (cheb(g.player.x, g.player.y, m.x, m.y) <= 8) list.add(m)
        }
        list.sortBy { cheb(g.player.x, g.player.y, it.x, it.y) }
        val n = min(4, list.size)
        if (n == 0) return
        val rowH = t * 0.62f
        panel(c, l - 3f, top, l + ms + 3f, top + rowH * n + t * 0.12f)
        for (i in 0 until n) {
            val m = list[i]
            battleList.add(m)
            val ry = top + t * 0.06f + i * rowH
            if (g.target === m) {
                p.style = Paint.Style.STROKE
                p.strokeWidth = max(2f, t * 0.04f)
                p.color = 0xFFFF3030.toInt()
                rect.set(l + 2f, ry + 1f, l + ms - 2f, ry + rowH - 1f)
                c.drawRect(rect, p)
                p.style = Paint.Style.FILL
            }
            val rb = ratB
            if (rb != null) {
                rect.set(l + t * 0.04f, ry + t * 0.02f, l + t * 0.04f + rowH * 0.95f, ry + t * 0.02f + rowH * 0.95f)
                c.drawBitmap(rb[2][0], null, rect, bp)
            }
            txt(c, m.name, l + rowH + t * 0.06f, ry + t * 0.24f, t * 0.2f, COL_WHITE, Paint.Align.LEFT)
            val f = m.hp.toFloat() / m.maxHp
            bar(c, l + rowH + t * 0.06f, ry + t * 0.32f, ms - rowH - t * 0.18f, t * 0.16f, f, hpColor(f), 0x55FFFFFF, "")
            addHit(Ui.BATTLE0 + i, l, ry, l + ms, ry + rowH)
        }
    }

    private fun drawJoystick(c: Canvas, mg: Float) {
        val t = tile.toFloat()
        joyR = t * 1.15f
        joyCx = mg + joyR + t * 0.25f
        joyCy = h - mg - joyR - t * 0.1f
        p.style = Paint.Style.FILL
        p.color = 0x44FFFFFF
        c.drawCircle(joyCx, joyCy, joyR, p)
        p.style = Paint.Style.STROKE
        p.strokeWidth = max(2f, t * 0.04f)
        p.color = 0x88FFFFFF.toInt()
        c.drawCircle(joyCx, joyCy, joyR, p)
        p.style = Paint.Style.FILL
        p.color = 0x66FFFFFF
        for (d in 0 until 4) {
            val ax = joyCx + DX[d] * joyR * 0.72f
            val ay = joyCy + DY[d] * joyR * 0.72f
            path.reset()
            val s = joyR * 0.14f
            when (d) {
                0 -> { path.moveTo(ax, ay - s); path.lineTo(ax - s, ay + s); path.lineTo(ax + s, ay + s) }
                1 -> { path.moveTo(ax + s, ay); path.lineTo(ax - s, ay - s); path.lineTo(ax - s, ay + s) }
                2 -> { path.moveTo(ax, ay + s); path.lineTo(ax - s, ay - s); path.lineTo(ax + s, ay - s) }
                else -> { path.moveTo(ax - s, ay); path.lineTo(ax + s, ay - s); path.lineTo(ax + s, ay + s) }
            }
            path.close()
            c.drawPath(path, p)
        }
        val kx = joyCx + g.joyX * joyR * 0.55f
        val ky = joyCy + g.joyY * joyR * 0.55f
        p.color = if (g.joyDir >= 0) 0xCCFFE066.toInt() else 0x99FFFFFF.toInt()
        c.drawCircle(kx, ky, joyR * 0.38f, p)
        p.style = Paint.Style.STROKE
        p.color = 0xFF5A4630.toInt()
        c.drawCircle(kx, ky, joyR * 0.38f, p)
        p.style = Paint.Style.FILL
    }

    private fun drawHotbar(c: Canvas, mg: Float) {
        val t = tile.toFloat()
        val bs = t * 1.0f
        val gap = t * 0.14f
        for (i in 0 until Spells.list.size) {
            val col = i % 3
            val row = i / 3
            val cx = w - mg - bs / 2f - col * (bs + gap)
            val cy = h - mg - bs / 2f - row * (bs + gap)
            drawSpellButton(c, i, cx, cy, bs / 2f)
            addHit(Ui.SPELL0 + i, cx - bs / 2f, cy - bs / 2f, cx + bs / 2f, cy + bs / 2f)
        }
        // botão de mira, à esquerda da fileira de baixo
        val tcx = w - mg - bs / 2f - 3 * (bs + gap)
        val tcy = h - mg - bs / 2f
        val active = g.target != null
        button(c, tcx, tcy, bs / 2f, if (active) 0xFFFF3030.toInt() else 0xFF8A6A45.toInt())
        p.style = Paint.Style.STROKE
        p.strokeWidth = max(2f, bs * 0.06f)
        p.color = if (active) 0xFFFF6060.toInt() else 0xFFE8D6B0.toInt()
        c.drawCircle(tcx, tcy, bs * 0.22f, p)
        c.drawLine(tcx - bs * 0.36f, tcy, tcx - bs * 0.1f, tcy, p)
        c.drawLine(tcx + bs * 0.1f, tcy, tcx + bs * 0.36f, tcy, p)
        c.drawLine(tcx, tcy - bs * 0.36f, tcx, tcy - bs * 0.1f, p)
        c.drawLine(tcx, tcy + bs * 0.1f, tcx, tcy + bs * 0.36f, p)
        p.style = Paint.Style.FILL
        addHit(Ui.TARGET, tcx - bs / 2f, tcy - bs / 2f, tcx + bs / 2f, tcy + bs / 2f)
    }

    private fun drawSpellButton(c: Canvas, i: Int, cx: Float, cy: Float, r: Float) {
        val s = Spells.list[i]
        val pl = g.player
        val locked = pl.level < s.level && !g.cheat
        val noMana = !locked && !g.cheat && pl.mana < s.mana
        val ready = !locked && !noMana && g.cd[i] <= 0f
        if (ready) glowAdd(c, cx, cy, r * 1.4f, s.color, 0.3f + 0.12f * sin(g.time * 3f + i))
        val border = if (locked) 0xFF444444.toInt()
        else if (g.cheat) mixCol(s.color, 0xFFFFE9A0.toInt(), 0.35f + 0.35f * sin(g.time * 3f + i))
        else s.color
        button(c, cx, cy, r, border)
        val ic = if (locked) 0xFF666666.toInt() else if (noMana) mixCol(s.color, 0xFF444444.toInt(), 0.6f) else s.color
        spellIcon(c, s.kind, cx, cy - r * 0.08f, r * 0.7f, ic)
        if (locked) {
            txt(c, "Nv " + s.level, cx, cy + r * 0.72f, r * 0.34f, 0xFFAAAAAA.toInt(), Paint.Align.CENTER)
        } else if (g.cheat) {
            fxr.infinity(c, cx, cy + r * 0.62f, r * 0.24f, 0xFF9BC8FF.toInt(), 1f, false, max(2f, r * 0.07f))
        } else {
            txt(c, "" + s.mana, cx, cy + r * 0.74f, r * 0.34f, if (noMana) 0xFF7A9BD0.toInt() else 0xFF9BC8FF.toInt(), Paint.Align.CENTER)
        }
        val cdv = g.cd[i]
        if (cdv > 0f && !locked) {
            val f = min(1f, cdv / s.cd)
            p.style = Paint.Style.FILL
            p.color = 0xAA000000.toInt()
            rect.set(cx - r, cy - r, cx + r, cy + r)
            c.drawArc(rect, -90f, 360f * f, true, p)
            txt(c, String.format("%.1f", cdv), cx, cy + r * 0.12f, r * 0.45f, COL_WHITE, Paint.Align.CENTER)
        }
        val rt = g.readyT[i]
        if (rt > 0f && !locked) {
            val q = 1f - rt / 0.7f
            p.style = Paint.Style.FILL
            p.color = withAlpha(0xFFFFFFFF.toInt(), (1f - q) * 0.45f)
            c.drawCircle(cx, cy, r, p)
            p.style = Paint.Style.STROKE
            p.strokeWidth = max(2f, r * 0.08f)
            p.color = withAlpha(s.color, 1f - q)
            c.drawCircle(cx, cy, r * (1f + 0.5f * q), p)
            p.style = Paint.Style.FILL
        }
        val pr = g.pressT[i]
        if (pr > 0f) {
            val q = 1f - pr / 0.4f
            p.style = Paint.Style.STROKE
            p.strokeWidth = max(2f, r * 0.1f)
            p.color = withAlpha(0xFFFFFFFF.toInt(), 1f - q)
            c.drawCircle(cx, cy, r * (0.85f + 0.9f * q), p)
            p.color = withAlpha(s.color, (1f - q) * 0.8f)
            c.drawCircle(cx, cy, r * (0.7f + 1.3f * q), p)
            p.style = Paint.Style.FILL
        }
    }

    private fun spellIcon(c: Canvas, kind: Int, cx: Float, cy: Float, r: Float, col: Int) {
        p.style = Paint.Style.FILL
        p.color = col
        when (kind) {
            Spells.HEAL -> {
                rect.set(cx - r * 0.2f, cy - r * 0.6f, cx + r * 0.2f, cy + r * 0.6f)
                c.drawRoundRect(rect, r * 0.1f, r * 0.1f, p)
                rect.set(cx - r * 0.6f, cy - r * 0.2f, cx + r * 0.6f, cy + r * 0.2f)
                c.drawRoundRect(rect, r * 0.1f, r * 0.1f, p)
            }
            Spells.FIRE -> {
                path.reset()
                path.moveTo(cx, cy - r * 0.7f)
                path.cubicTo(cx + r * 0.6f, cy - r * 0.1f, cx + r * 0.55f, cy + r * 0.6f, cx, cy + r * 0.6f)
                path.cubicTo(cx - r * 0.55f, cy + r * 0.6f, cx - r * 0.6f, cy - r * 0.1f, cx, cy - r * 0.7f)
                path.close()
                c.drawPath(path, p)
                p.color = 0xFFFFE066.toInt()
                path.reset()
                path.moveTo(cx, cy - r * 0.1f)
                path.cubicTo(cx + r * 0.3f, cy + r * 0.2f, cx + r * 0.28f, cy + r * 0.55f, cx, cy + r * 0.55f)
                path.cubicTo(cx - r * 0.28f, cy + r * 0.55f, cx - r * 0.3f, cy + r * 0.2f, cx, cy - r * 0.1f)
                path.close()
                c.drawPath(path, p)
            }
            Spells.ENERGY -> {
                path.reset()
                path.moveTo(cx + r * 0.15f, cy - r * 0.75f)
                path.lineTo(cx - r * 0.45f, cy + r * 0.05f)
                path.lineTo(cx - r * 0.02f, cy + r * 0.05f)
                path.lineTo(cx - r * 0.18f, cy + r * 0.75f)
                path.lineTo(cx + r * 0.45f, cy - r * 0.15f)
                path.lineTo(cx + r * 0.02f, cy - r * 0.15f)
                path.close()
                c.drawPath(path, p)
            }
            Spells.WAVE -> {
                p.style = Paint.Style.STROKE
                p.strokeWidth = max(2f, r * 0.15f)
                for (k in 1..3) {
                    val rr = r * 0.28f * k
                    rect.set(cx - rr, cy + r * 0.45f - rr, cx + rr, cy + r * 0.45f + rr)
                    c.drawArc(rect, 230f, 80f, false, p)
                }
                p.style = Paint.Style.FILL
            }
            Spells.HASTE -> {
                p.style = Paint.Style.STROKE
                p.strokeWidth = max(2f, r * 0.17f)
                for (k in 0..1) {
                    val ox = cx - r * 0.35f + k * r * 0.5f
                    path.reset()
                    path.moveTo(ox - r * 0.2f, cy - r * 0.5f)
                    path.lineTo(ox + r * 0.2f, cy)
                    path.lineTo(ox - r * 0.2f, cy + r * 0.5f)
                    c.drawPath(path, p)
                }
                p.style = Paint.Style.FILL
            }
            else -> {
                path.reset()
                path.moveTo(cx - r * 0.5f, cy - r * 0.55f)
                path.lineTo(cx + r * 0.5f, cy - r * 0.55f)
                path.lineTo(cx + r * 0.5f, cy + r * 0.05f)
                path.quadTo(cx + r * 0.5f, cy + r * 0.55f, cx, cy + r * 0.75f)
                path.quadTo(cx - r * 0.5f, cy + r * 0.55f, cx - r * 0.5f, cy + r * 0.05f)
                path.close()
                c.drawPath(path, p)
                p.style = Paint.Style.STROKE
                p.strokeWidth = max(2f, r * 0.08f)
                p.color = 0xFFFFFFFF.toInt()
                c.drawPath(path, p)
                p.style = Paint.Style.FILL
            }
        }
    }

    private fun drawConsole(c: Canvas, mg: Float) {
        val t = tile.toFloat()
        val x = joyCx + joyR + t * 0.35f
        val maxX = w - mg - 4 * (t * 1.14f) - t * 0.2f
        val size = t * 0.21f
        var shown = 0
        var i = g.log.size - 1
        c.save()
        c.clipRect(x - 4f, 0f, maxX, h.toFloat())
        while (i >= 0 && shown < 6) {
            val ln = g.log[i]
            if (ln.life > 0f) {
                val a = if (ln.life > 2f) 1f else ln.life / 2f
                txt(c, ln.text, x, h - mg - shown * size * 1.25f - t * 0.05f, size, withAlpha(ln.color, a), Paint.Align.LEFT)
                shown++
            }
            i--
        }
        c.restore()
    }

    // ------------------------------------------------------------------ mochila e personagem

    private fun itemIcon(c: Canvas, id: String, cx: Float, cy: Float, r: Float) {
        p.style = Paint.Style.FILL
        when (id) {
            "gold" -> {
                p.color = 0xFFB8860B.toInt()
                c.drawCircle(cx, cy, r * 0.62f, p)
                p.color = 0xFFF2C94C.toInt()
                c.drawCircle(cx, cy, r * 0.5f, p)
                p.color = 0xFFFFE9A0.toInt()
                c.drawCircle(cx - r * 0.12f, cy - r * 0.12f, r * 0.18f, p)
                p.style = Paint.Style.STROKE
                p.strokeWidth = max(2f, r * 0.07f)
                p.color = 0xFFB8860B.toInt()
                c.drawCircle(cx, cy, r * 0.32f, p)
                p.style = Paint.Style.FILL
            }
            "cheese" -> {
                path.reset()
                path.moveTo(cx - r * 0.7f, cy + r * 0.4f)
                path.lineTo(cx + r * 0.7f, cy + r * 0.4f)
                path.lineTo(cx + r * 0.7f, cy - r * 0.05f)
                path.lineTo(cx - r * 0.7f, cy - r * 0.45f)
                path.close()
                p.color = 0xFFF2C94C.toInt()
                c.drawPath(path, p)
                p.color = 0xFFE0A91E.toInt()
                c.drawRect(cx - r * 0.7f, cy + r * 0.25f, cx + r * 0.7f, cy + r * 0.4f, p)
                p.color = 0xFFC88A12.toInt()
                c.drawCircle(cx - r * 0.1f, cy + r * 0.02f, r * 0.12f, p)
                c.drawCircle(cx + r * 0.38f, cy + r * 0.12f, r * 0.09f, p)
                c.drawCircle(cx - r * 0.45f, cy + r * 0.18f, r * 0.07f, p)
            }
        }
    }

    private fun drawBag(c: Canvas) {
        val t = tile.toFloat()
        val sw = w.toFloat()
        val sh = h.toFloat()
        modalStart = hits.size
        addHit(Ui.BAG_CLOSE, 0f, 0f, sw, sh)
        p.style = Paint.Style.FILL
        p.color = 0xAA000000.toInt()
        c.drawRect(0f, 0f, sw, sh, p)
        val pw = min(sw * 0.94f, t * 10.4f)
        val ph = min(sh * 0.92f, t * 6.6f)
        val l = (sw - pw) / 2f
        val top = (sh - ph) / 2f
        panel(c, l, top, l + pw, top + ph)
        addHit(Ui.BLOCK, l, top, l + pw, top + ph)
        // abas
        val tabW = t * 2.4f
        val tabH = t * 0.62f
        for (k in 0..1) {
            val tl = l + t * 0.3f + k * (tabW + t * 0.15f)
            val tt = top + t * 0.22f
            val sel = g.bagTab == k
            rect.set(tl, tt, tl + tabW, tt + tabH)
            p.style = Paint.Style.FILL
            p.color = if (sel) 0xFF6B5236.toInt() else 0xFF2A211A.toInt()
            c.drawRoundRect(rect, t * 0.1f, t * 0.1f, p)
            txt(c, if (k == 0) "Mochila" else "Personagem", tl + tabW / 2f, tt + tabH * 0.68f, t * 0.27f, if (sel) COL_YELLOW else COL_GRAY, Paint.Align.CENTER)
            addHit(if (k == 0) Ui.TAB_BAG else Ui.TAB_CHAR, tl, tt, tl + tabW, tt + tabH)
        }
        // fechar
        val cr = t * 0.3f
        val ccx = l + pw - t * 0.5f
        val ccy = top + t * 0.53f
        button(c, ccx, ccy, cr, 0xFFC84A4A.toInt())
        p.style = Paint.Style.STROKE
        p.strokeWidth = max(2f, cr * 0.18f)
        p.color = COL_WHITE
        c.drawLine(ccx - cr * 0.4f, ccy - cr * 0.4f, ccx + cr * 0.4f, ccy + cr * 0.4f, p)
        c.drawLine(ccx + cr * 0.4f, ccy - cr * 0.4f, ccx - cr * 0.4f, ccy + cr * 0.4f, p)
        p.style = Paint.Style.FILL
        addHit(Ui.BAG_CLOSE, ccx - cr, ccy - cr, ccx + cr, ccy + cr)

        if (g.bagTab == 0) drawBackpack(c, l, top, pw, ph) else drawCharacter(c, l, top, pw, ph)
    }

    private fun drawBackpack(c: Canvas, l: Float, top: Float, pw: Float, ph: Float) {
        val t = tile.toFloat()
        val slot = t * 0.95f
        val cols = 6
        val rows = 3
        val gx = l + t * 0.4f
        val gy = top + t * 1.1f
        itemKeys.clear()
        for ((id, n) in g.inv) if (n > 0) itemKeys.add(id)
        for (r in 0 until rows) {
            for (k in 0 until cols) {
                val idx = r * cols + k
                val sx = gx + k * (slot + t * 0.08f)
                val sy = gy + r * (slot + t * 0.08f)
                rect.set(sx, sy, sx + slot, sy + slot)
                p.style = Paint.Style.FILL
                p.color = 0xFF2A211A.toInt()
                c.drawRoundRect(rect, t * 0.08f, t * 0.08f, p)
                p.style = Paint.Style.STROKE
                p.strokeWidth = 2f
                p.color = 0xFF5A4630.toInt()
                c.drawRoundRect(rect, t * 0.08f, t * 0.08f, p)
                p.style = Paint.Style.FILL
                if (idx < itemKeys.size) {
                    val id = itemKeys[idx]
                    val n = g.inv[id] ?: 0
                    if (g.selItem == id) {
                        p.style = Paint.Style.STROKE
                        p.strokeWidth = max(3f, t * 0.05f)
                        p.color = COL_YELLOW
                        c.drawRoundRect(rect, t * 0.08f, t * 0.08f, p)
                        p.style = Paint.Style.FILL
                    }
                    itemIcon(c, id, sx + slot / 2f, sy + slot / 2f, slot * 0.5f)
                    if (n > 1 || id == "gold") txt(c, "" + n, sx + slot - t * 0.06f, sy + slot - t * 0.08f, t * 0.22f, COL_WHITE, Paint.Align.RIGHT)
                    addHit(Ui.ITEM0 + idx, sx, sy, sx + slot, sy + slot)
                }
            }
        }
        // descrição do item escolhido
        val dy = gy + rows * (slot + t * 0.08f) + t * 0.15f
        val sel = g.selItem
        if (sel.isNotEmpty() && (g.inv[sel] ?: 0) > 0) {
            txt(c, Items.title(sel), l + t * 0.4f, dy + t * 0.25f, t * 0.28f, COL_YELLOW, Paint.Align.LEFT)
            txt(c, Items.desc(sel), l + t * 0.4f, dy + t * 0.62f, t * 0.2f, COL_GRAY, Paint.Align.LEFT)
            if (Items.usable(sel)) {
                val bw = t * 1.6f
                val bh = t * 0.6f
                val bl = l + pw - t * 0.4f - bw
                rect.set(bl, dy, bl + bw, dy + bh)
                p.color = 0xFF3C7A3C.toInt()
                c.drawRoundRect(rect, t * 0.1f, t * 0.1f, p)
                txt(c, "Usar", bl + bw / 2f, dy + bh * 0.68f, t * 0.28f, COL_WHITE, Paint.Align.CENTER)
                addHit(Ui.USE, bl, dy, bl + bw, dy + bh)
            }
        } else if (itemKeys.isEmpty()) {
            txt(c, "A mochila está vazia. Derrote monstros e pegue o loot.", l + t * 0.4f, dy + t * 0.3f, t * 0.22f, COL_GRAY, Paint.Align.LEFT)
        } else {
            txt(c, "Toque em um item para ver os detalhes.", l + t * 0.4f, dy + t * 0.3f, t * 0.22f, COL_GRAY, Paint.Align.LEFT)
        }
    }

    private fun drawCharacter(c: Canvas, l: Float, top: Float, pw: Float, ph: Float) {
        val t = tile.toFloat()
        val pl = g.player
        val x = l + t * 0.4f
        var y = top + t * 1.3f
        val sz = t * 0.26f
        val lh = t * 0.44f
        val cur = expForLevel(pl.level)
        val nxt = expForLevel(pl.level + 1)
        val pct = if (nxt > cur) ((pl.exp - cur) * 100L / (nxt - cur)).toInt() else 0
        val mlNeed = manaNeeded(pl.ml)
        val mlPct = if (mlNeed > 0) (pl.manaSpent * 100L / mlNeed).toInt() else 0
        val lines = arrayOf(
            "Nível: " + pl.level,
            "Experiência: " + pl.exp + " (" + pct + "% para o nível " + (pl.level + 1) + ")",
            "Vida: " + pl.hp + " / " + pl.maxHp,
            "Mana: " + pl.mana + " / " + pl.maxMana,
            "Nível mágico: " + pl.ml + " (" + mlPct + "%)",
            "Velocidade: " + (if (pl.hasteT > 0f) "rápida" else "normal"),
            "Ouro: " + (g.inv["gold"] ?: 0)
        )
        for (s in lines) {
            txt(c, s, x, y, sz, COL_WHITE, Paint.Align.LEFT)
            y += lh
        }
        // equipamento
        val slot = t * 0.95f
        val ex = l + pw - t * 0.5f - 3 * (slot + t * 0.12f) + t * 0.12f
        val ey = top + t * 1.3f
        val names = arrayOf("Chapéu", "Amuleto", "Mochila", "Varinha", "Manto", "Anel")
        for (i in 0 until 6) {
            val col = i % 3
            val row = i / 3
            val sx = ex + col * (slot + t * 0.12f)
            val sy = ey + row * (slot + t * 0.5f)
            rect.set(sx, sy, sx + slot, sy + slot)
            p.style = Paint.Style.FILL
            p.color = 0xFF2A211A.toInt()
            c.drawRoundRect(rect, t * 0.08f, t * 0.08f, p)
            p.style = Paint.Style.STROKE
            p.strokeWidth = 2f
            p.color = 0xFF5A4630.toInt()
            c.drawRoundRect(rect, t * 0.08f, t * 0.08f, p)
            p.style = Paint.Style.FILL
            val cx = sx + slot / 2f
            val cy = sy + slot / 2f
            val r = slot * 0.5f
            when (i) {
                0 -> {
                    path.reset()
                    path.moveTo(cx + r * 0.1f, cy - r * 0.7f)
                    path.lineTo(cx - r * 0.5f, cy + r * 0.3f)
                    path.lineTo(cx + r * 0.5f, cy + r * 0.3f)
                    path.close()
                    p.color = 0xFF2F55C8.toInt()
                    c.drawPath(path, p)
                    p.color = 0xFFF2C94C.toInt()
                    c.drawRect(cx - r * 0.65f, cy + r * 0.25f, cx + r * 0.65f, cy + r * 0.45f, p)
                }
                2 -> {
                    rect.set(cx - r * 0.45f, cy - r * 0.4f, cx + r * 0.45f, cy + r * 0.5f)
                    p.color = 0xFFB07A3C.toInt()
                    c.drawRoundRect(rect, r * 0.2f, r * 0.2f, p)
                    p.color = 0xFF7A4F22.toInt()
                    c.drawRect(cx - r * 0.45f, cy - r * 0.05f, cx + r * 0.45f, cy + r * 0.1f, p)
                }
                3 -> {
                    p.style = Paint.Style.STROKE
                    p.strokeWidth = max(3f, r * 0.14f)
                    p.color = 0xFF8B5E3C.toInt()
                    c.drawLine(cx - r * 0.45f, cy + r * 0.6f, cx + r * 0.35f, cy - r * 0.4f, p)
                    p.style = Paint.Style.FILL
                    p.color = 0xFFC9A6FF.toInt()
                    c.drawCircle(cx + r * 0.42f, cy - r * 0.5f, r * 0.2f, p)
                }
                4 -> {
                    path.reset()
                    path.moveTo(cx - r * 0.3f, cy - r * 0.6f)
                    path.lineTo(cx + r * 0.3f, cy - r * 0.6f)
                    path.lineTo(cx + r * 0.6f, cy + r * 0.6f)
                    path.lineTo(cx - r * 0.6f, cy + r * 0.6f)
                    path.close()
                    p.color = 0xFF3E68DE.toInt()
                    c.drawPath(path, p)
                    p.color = 0xFF8A5A2B.toInt()
                    c.drawRect(cx - r * 0.4f, cy - r * 0.05f, cx + r * 0.4f, cy + r * 0.08f, p)
                }
            }
            txt(c, names[i], cx, sy + slot + t * 0.26f, t * 0.19f, COL_GRAY, Paint.Align.CENTER)
        }
        txt(c, "Chapéu de Mago, Varinha de Vórtice e Manto Azul", l + t * 0.4f, top + ph - t * 0.3f, t * 0.19f, 0xFF9A8A70.toInt(), Paint.Align.LEFT)
    }

    // ------------------------------------------------------------------ menu e morte

    private fun menuButton(c: Canvas, id: Int, label: String, cx: Float, y: Float, bw: Float, bh: Float, col: Int) {
        rect.set(cx - bw / 2f, y, cx + bw / 2f, y + bh)
        p.style = Paint.Style.FILL
        p.color = col
        c.drawRoundRect(rect, bh * 0.2f, bh * 0.2f, p)
        p.style = Paint.Style.STROKE
        p.strokeWidth = 3f
        p.color = 0xFFE8D6B0.toInt()
        c.drawRoundRect(rect, bh * 0.2f, bh * 0.2f, p)
        p.style = Paint.Style.FILL
        txt(c, label, cx, y + bh * 0.67f, bh * 0.42f, COL_WHITE, Paint.Align.CENTER)
        addHit(id, cx - bw / 2f, y, cx + bw / 2f, y + bh)
    }

    private fun drawMenu(c: Canvas) {
        val t = tile.toFloat()
        val sw = w.toFloat()
        val sh = h.toFloat()
        modalStart = hits.size
        addHit(Ui.BLOCK, 0f, 0f, sw, sh)
        p.style = Paint.Style.FILL
        p.color = 0xBB000000.toInt()
        c.drawRect(0f, 0f, sw, sh, p)
        val pw = t * 6.2f
        val ph = t * 5.7f
        val l = (sw - pw) / 2f
        val top = (sh - ph) / 2f
        panel(c, l, top, l + pw, top + ph)
        txt(c, "Menu", sw / 2f, top + t * 0.75f, t * 0.5f, COL_YELLOW, Paint.Align.CENTER)
        menuButton(c, Ui.MENU_CONT, "Continuar", sw / 2f, top + t * 1.05f, pw - t * 0.8f, t * 0.8f, 0xFF3C7A3C.toInt())
        menuButton(c, Ui.MENU_SAVE, "Salvar jogo", sw / 2f, top + t * 2.0f, pw - t * 0.8f, t * 0.8f, 0xFF3A5A9A.toInt())
        val on = g.cheat
        menuButton(
            c, Ui.MENU_CHEAT, if (on) "Mana infinita: LIGADA" else "Mana infinita: DESLIGADA", sw / 2f, top + t * 2.95f,
            pw - t * 0.8f, t * 0.8f, if (on) 0xFF7A3FC0.toInt() else 0xFF5A5A66.toInt()
        )
        menuButton(c, Ui.MENU_EXIT, "Sair para o início", sw / 2f, top + t * 3.9f, pw - t * 0.8f, t * 0.8f, 0xFF8A4A3A.toInt())
        txt(c, "Mana infinita também libera todas as magias.", sw / 2f, top + ph - t * 0.5f, t * 0.17f, 0xFFD0B8FF.toInt(), Paint.Align.CENTER)
        txt(c, "O jogo salva sozinho a cada poucos segundos.", sw / 2f, top + ph - t * 0.2f, t * 0.17f, COL_GRAY, Paint.Align.CENTER)
    }

    private fun drawDead(c: Canvas) {
        val t = tile.toFloat()
        val sw = w.toFloat()
        val sh = h.toFloat()
        modalStart = hits.size
        addHit(Ui.BLOCK, 0f, 0f, sw, sh)
        p.style = Paint.Style.FILL
        p.color = 0xCC3A0000.toInt()
        c.drawRect(0f, 0f, sw, sh, p)
        txt(c, "Você morreu!", sw / 2f, sh / 2f - t * 0.6f, t * 0.9f, 0xFFFF5A5A.toInt(), Paint.Align.CENTER)
        txt(c, "Você vai perder 10% da experiência e voltar ao templo.", sw / 2f, sh / 2f - t * 0.05f, t * 0.27f, COL_WHITE, Paint.Align.CENTER)
        menuButton(c, Ui.RESPAWN, "Renascer", sw / 2f, sh / 2f + t * 0.35f, t * 3.2f, t * 0.9f, 0xFF3C7A3C.toInt())
    }
}
