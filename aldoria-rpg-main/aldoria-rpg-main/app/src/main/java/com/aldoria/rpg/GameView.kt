package com.aldoria.rpg

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.os.Build
import android.os.SystemClock
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlin.math.floor

class GameView(ctx: Context, private val game: Game, private val listener: Listener) :
    SurfaceView(ctx), SurfaceHolder.Callback, Runnable {

    interface Listener {
        fun onExit()
    }

    private val rd = Renderer(ctx, game)
    private var thread: Thread? = null
    @Volatile
    private var running = false
    private var joyId = -1

    init {
        holder.addCallback(this)
        isFocusable = true
        keepScreenOn = true
    }

    override fun surfaceCreated(h: SurfaceHolder) {
        running = true
        val th = Thread(this)
        thread = th
        th.start()
    }

    override fun surfaceChanged(h: SurfaceHolder, format: Int, w: Int, hh: Int) {
        synchronized(game) { rd.onSize(w, hh) }
    }

    override fun surfaceDestroyed(h: SurfaceHolder) {
        running = false
        try {
            thread?.join(1000)
        } catch (e: InterruptedException) {
        }
        thread = null
    }

    override fun run() {
        var last = System.nanoTime()
        while (running) {
            val now = System.nanoTime()
            val dt = (now - last) / 1_000_000_000f
            last = now
            val canvas: Canvas? = try {
                if (Build.VERSION.SDK_INT >= 26) holder.lockHardwareCanvas() else holder.lockCanvas()
            } catch (e: Exception) {
                null
            }
            if (canvas == null) {
                SystemClock.sleep(10)
                continue
            }
            try {
                synchronized(game) {
                    game.update(dt)
                    rd.draw(canvas)
                }
            } catch (e: Exception) {
                // um quadro com erro não derruba o jogo
            } finally {
                try {
                    holder.unlockCanvasAndPost(canvas)
                } catch (e: Exception) {
                }
            }
            val spent = (System.nanoTime() - now) / 1_000_000L
            if (spent < 14L) SystemClock.sleep(14L - spent)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(e: MotionEvent): Boolean {
        synchronized(game) {
            val act = e.actionMasked
            if (act == MotionEvent.ACTION_DOWN || act == MotionEvent.ACTION_POINTER_DOWN) {
                val i = e.actionIndex
                down(e.getPointerId(i), e.getX(i), e.getY(i))
            } else if (act == MotionEvent.ACTION_MOVE) {
                if (joyId >= 0) {
                    val i = e.findPointerIndex(joyId)
                    if (i >= 0) joyUpdate(e.getX(i), e.getY(i))
                }
            } else if (act == MotionEvent.ACTION_UP || act == MotionEvent.ACTION_POINTER_UP || act == MotionEvent.ACTION_CANCEL) {
                val id = e.getPointerId(e.actionIndex)
                if (id == joyId || act == MotionEvent.ACTION_CANCEL) {
                    joyId = -1
                    game.joyDir = -1
                    game.joyX = 0f
                    game.joyY = 0f
                }
            }
        }
        return true
    }

    private fun down(id: Int, x: Float, y: Float) {
        val k = rd.hit(x, y)
        if (k != Ui.NONE) {
            handle(k)
            return
        }
        if (game.ui != 0 || game.dead) return
        val dx = x - rd.joyCx
        val dy = y - rd.joyCy
        val lim = rd.joyR * 1.8f
        if (joyId < 0 && dx * dx + dy * dy <= lim * lim) {
            joyId = id
            joyUpdate(x, y)
            return
        }
        mapTap(x, y)
    }

    private fun joyUpdate(x: Float, y: Float) {
        var dx = x - rd.joyCx
        var dy = y - rd.joyCy
        val dist = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
        val max = rd.joyR
        if (dist > max && dist > 0f) {
            dx = dx / dist * max
            dy = dy / dist * max
        }
        game.joyX = if (max > 0f) dx / max else 0f
        game.joyY = if (max > 0f) dy / max else 0f
        if (dist < rd.joyR * 0.3f) {
            game.joyDir = -1
        } else {
            game.joyDir = if (Math.abs(dx) > Math.abs(dy)) (if (dx > 0f) 1 else 3) else (if (dy > 0f) 2 else 0)
        }
    }

    private fun mapTap(x: Float, y: Float) {
        val t = rd.tile.toFloat()
        val wx = (x - rd.w / 2f) / t + rd.camX
        val wy = (y - rd.h / 2f) / t + rd.camY
        // monstro perto do toque
        var best: Monster? = null
        var bd = 0.85f
        for (m in game.monsters) {
            if (!m.alive) continue
            val ddx = m.fx + 0.5f - wx
            val ddy = m.fy + 0.5f - wy
            val d = Math.sqrt((ddx * ddx + ddy * ddy).toDouble()).toFloat()
            if (d < bd) {
                bd = d
                best = m
            }
        }
        if (best != null) {
            game.toggleTarget(best)
            return
        }
        val tx = floor(wx).toInt()
        val ty = floor(wy).toInt()
        val cp = game.corpseAt(tx, ty)
        if (cp != null) {
            game.requestLoot(cp)
            return
        }
        game.walkTo(tx, ty)
    }

    private fun handle(k: Int) {
        if (k >= Ui.SPELL0 && k < Ui.SPELL0 + Spells.list.size) {
            game.castSpell(k - Ui.SPELL0)
            return
        }
        if (k >= Ui.BATTLE0 && k < Ui.BATTLE0 + 10) {
            val i = k - Ui.BATTLE0
            if (i < rd.battleList.size) game.toggleTarget(rd.battleList[i])
            return
        }
        if (k >= Ui.ITEM0) {
            val i = k - Ui.ITEM0
            if (i < rd.itemKeys.size) game.selItem = rd.itemKeys[i]
            return
        }
        when (k) {
            Ui.MENU -> game.ui = 2
            Ui.BAG -> game.ui = if (game.ui == 1) 0 else 1
            Ui.TARGET -> game.toggleNearestTarget()
            Ui.CHEAT -> game.toggleCheat()
            Ui.MENU_CHEAT -> {
                game.toggleCheat()
                game.ui = 0
            }
            Ui.MENU_CONT -> game.ui = 0
            Ui.MENU_SAVE -> {
                game.save()
                game.say("Jogo salvo.", COL_GREEN)
                game.ui = 0
            }
            Ui.MENU_EXIT -> {
                game.save()
                listener.onExit()
            }
            Ui.BAG_CLOSE -> game.ui = 0
            Ui.TAB_BAG -> game.bagTab = 0
            Ui.TAB_CHAR -> game.bagTab = 1
            Ui.USE -> game.useItem(game.selItem)
            Ui.RESPAWN -> game.respawn()
        }
    }
}
