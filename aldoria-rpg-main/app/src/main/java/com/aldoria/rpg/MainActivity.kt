package com.aldoria.rpg

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.sin

class MainActivity : AppCompatActivity() {
    private lateinit var playBtn: TextView
    private lateinit var newBtn: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = FrameLayout(this)
        root.addView(TitleView(this), FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

        val col = LinearLayout(this)
        col.orientation = LinearLayout.VERTICAL
        col.gravity = Gravity.CENTER_HORIZONTAL
        col.setPadding(dp(28), dp(24), dp(28), dp(20))

        // título
        val top = LinearLayout(this)
        top.orientation = LinearLayout.VERTICAL
        top.gravity = Gravity.CENTER
        val title = TextView(this)
        title.text = "ALDORIA"
        title.textSize = 48f
        title.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        title.setTextColor(0xFFFFD866.toInt())
        title.setShadowLayer(dp(8).toFloat(), 0f, dp(3).toFloat(), 0xFF000000.toInt())
        title.letterSpacing = 0.12f
        title.gravity = Gravity.CENTER
        val sub = TextView(this)
        sub.text = "Crônicas do Mago"
        sub.textSize = 17f
        sub.typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
        sub.setTextColor(0xFFD8C8F4.toInt())
        sub.setShadowLayer(dp(4).toFloat(), 0f, dp(2).toFloat(), 0xFF000000.toInt())
        sub.gravity = Gravity.CENTER
        top.addView(title)
        top.addView(sub)
        col.addView(top, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        // botões
        playBtn = fantasyButton("Jogar", 0xFF3C7A3C.toInt(), 0xFFB8F0B8.toInt()) { startActivity(Intent(this, GameActivity::class.java)) }
        newBtn = fantasyButton("Novo jogo", 0xFF7A3C3C.toInt(), 0xFFF0B8B8.toInt()) { confirmNew() }
        val upd = fantasyButton("Verificar atualização", 0xFF3A5A9A.toInt(), 0xFFB8D0F8.toInt()) { Updater.check(this, true) }
        val news = fantasyButton("O que mudou", 0xFF5A4630.toInt(), 0xFFE8D6B0.toInt()) { Novidades.showAll(this) }
        for (b in arrayOf(playBtn, newBtn, upd, news)) {
            val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(54))
            lp.setMargins(0, dp(6), 0, dp(6))
            col.addView(b, lp)
        }

        val ver = TextView(this)
        ver.text = "Versão " + Updater.installedName(this)
        ver.textSize = 12f
        ver.setTextColor(0xFFA898C8.toInt())
        ver.gravity = Gravity.CENTER
        val vlp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        vlp.topMargin = dp(8)
        col.addView(ver, vlp)

        root.addView(col, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        setContentView(root)

        Updater.autoCheck(this)
        Novidades.showIfNew(this)
    }

    override fun onResume() {
        super.onResume()
        val has = Game.hasSave(this)
        playBtn.text = if (has) "Continuar" else "Jogar"
        newBtn.visibility = if (has) View.VISIBLE else View.GONE
    }

    private fun confirmNew() {
        AlertDialog.Builder(this)
            .setTitle("Novo jogo")
            .setMessage("Isso apaga o seu personagem atual (nível, magias treinadas e itens). Quer mesmo começar de novo?")
            .setPositiveButton("Apagar e começar") { _, _ ->
                Game.wipe(this)
                startActivity(Intent(this, GameActivity::class.java))
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun fantasyButton(label: String, fill: Int, text: Int, onClick: () -> Unit): TextView {
        val b = TextView(this)
        b.text = label
        b.textSize = 18f
        b.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        b.setTextColor(text)
        b.gravity = Gravity.CENTER
        val bg = GradientDrawable()
        bg.cornerRadius = dp(14).toFloat()
        bg.setColor(fill)
        bg.setStroke(dp(2), 0xFFE8D6B0.toInt())
        b.background = bg
        b.isClickable = true
        b.setOnClickListener { onClick() }
        return b
    }
}

/** Fundo animado da tela inicial: céu estrelado, lua, montanhas, torre do mago e vaga-lumes. */
class TitleView(ctx: Context) : View(ctx) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val start = System.nanoTime()
    private val rnd = java.util.Random(9L)
    private val n = 70
    private val sx = FloatArray(n) { rnd.nextFloat() }
    private val sy = FloatArray(n) { rnd.nextFloat() * 0.62f }
    private val ss = FloatArray(n) { 0.6f + rnd.nextFloat() * 1.8f }
    private val sp = FloatArray(n) { rnd.nextFloat() * TAU }
    private val fx = FloatArray(14) { rnd.nextFloat() }
    private val fy = FloatArray(14) { 0.55f + rnd.nextFloat() * 0.4f }
    private val fp = FloatArray(14) { rnd.nextFloat() * TAU }
    private var sky: LinearGradient? = null
    private val path = Path()
    private val dens = ctx.resources.displayMetrics.density

    override fun onSizeChanged(w: Int, h: Int, ow: Int, oh: Int) {
        sky = LinearGradient(
            0f, 0f, 0f, h.toFloat(),
            intArrayOf(0xFF0B0830.toInt(), 0xFF2A1A5E.toInt(), 0xFF6A4496.toInt()),
            floatArrayOf(0f, 0.6f, 1f), Shader.TileMode.CLAMP
        )
    }

    private fun hills(c: Canvas, base: Float, amp: Float, freq: Float, off: Float, col: Int) {
        val w = width.toFloat()
        val h = height.toFloat()
        path.reset()
        path.moveTo(0f, h)
        var x = 0f
        while (x <= w + 12f) {
            val y = base + sin(x * freq + off) * amp + sin(x * freq * 2.3f + off * 1.7f) * amp * 0.4f
            path.lineTo(x, y)
            x += 12f
        }
        path.lineTo(w, h)
        path.close()
        p.style = Paint.Style.FILL
        p.color = col
        c.drawPath(path, p)
    }

    override fun onDraw(c: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val t = (System.nanoTime() - start) / 1_000_000_000f
        p.style = Paint.Style.FILL
        p.shader = sky
        c.drawRect(0f, 0f, w, h, p)
        p.shader = null
        for (i in 0 until n) {
            val a = 0.35f + 0.65f * (0.5f + 0.5f * sin(t * ss[i] + sp[i]))
            p.color = withAlpha(0xFFFFF4C8.toInt(), a)
            c.drawCircle(sx[i] * w, sy[i] * h, (0.8f + ss[i] * 0.7f) * dens, p)
        }
        // lua
        val mx = w * 0.78f
        val my = h * 0.17f
        val mr = w * 0.09f
        p.color = withAlpha(0xFFFFF4C8.toInt(), 0.10f)
        c.drawCircle(mx, my, mr * 2.4f, p)
        p.color = withAlpha(0xFFFFF4C8.toInt(), 0.16f)
        c.drawCircle(mx, my, mr * 1.6f, p)
        p.color = 0xFFFFF4D2.toInt()
        c.drawCircle(mx, my, mr, p)
        p.color = 0xFFEBDDB4.toInt()
        c.drawCircle(mx - mr * 0.3f, my - mr * 0.2f, mr * 0.2f, p)
        c.drawCircle(mx + mr * 0.35f, my + mr * 0.25f, mr * 0.28f, p)
        // montanhas
        hills(c, h * 0.62f, h * 0.05f, 0.012f, 1f, 0xFF2B2150.toInt())
        hills(c, h * 0.70f, h * 0.045f, 0.017f, 3f, 0xFF1D1738.toInt())
        // torre do mago
        val tx = w * 0.20f
        val tb = h * 0.76f
        val tw = w * 0.10f
        val th = h * 0.26f
        p.color = 0xFF17122E.toInt()
        c.drawRect(tx - tw / 2f, tb - th, tx + tw / 2f, tb + 4f, p)
        path.reset()
        path.moveTo(tx - tw * 0.7f, tb - th)
        path.lineTo(tx, tb - th - tw * 1.3f)
        path.lineTo(tx + tw * 0.7f, tb - th)
        path.close()
        c.drawPath(path, p)
        val wa = 0.7f + 0.3f * sin(t * 2.2f)
        p.color = withAlpha(0xFFFFC857.toInt(), 0.25f * wa)
        c.drawCircle(tx, tb - th * 0.62f, tw * 0.45f, p)
        p.color = withAlpha(0xFFFFC857.toInt(), wa)
        c.drawRect(tx - tw * 0.12f, tb - th * 0.7f, tx + tw * 0.12f, tb - th * 0.52f, p)
        // colina da frente
        hills(c, h * 0.80f, h * 0.03f, 0.02f, 5f, 0xFF120E26.toInt())
        // vaga-lumes
        for (i in fx.indices) {
            val x = (fx[i] + 0.04f * sin(t * 0.5f + fp[i])) * w
            val y = (fy[i] + 0.03f * sin(t * 0.7f + fp[i] * 2f)) * h
            val a = 0.4f + 0.6f * (0.5f + 0.5f * sin(t * 2f + fp[i]))
            p.color = withAlpha(0xFFC8FF7A.toInt(), 0.18f * a)
            c.drawCircle(x, y, 7f * dens, p)
            p.color = withAlpha(0xFFE8FFAA.toInt(), a)
            c.drawCircle(x, y, 1.8f * dens, p)
        }
        postInvalidateOnAnimation()
    }
}
