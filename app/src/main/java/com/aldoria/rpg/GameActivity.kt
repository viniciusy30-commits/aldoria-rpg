package com.aldoria.rpg

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class GameActivity : AppCompatActivity(), GameView.Listener {
    private lateinit var game: Game
    private lateinit var view: GameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT >= 28) {
            val lp = window.attributes
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            window.attributes = lp
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        game = Game(this)
        view = GameView(this, game, this)
        setContentView(view)
        hideBars()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                synchronized(game) {
                    game.ui = if (game.ui == 2) 0 else 2
                }
            }
        })
    }

    private fun hideBars() {
        val ctl = WindowInsetsControllerCompat(window, window.decorView)
        ctl.hide(WindowInsetsCompat.Type.systemBars())
        ctl.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideBars()
    }

    override fun onPause() {
        synchronized(game) {
            if (!game.dead) game.save()
        }
        super.onPause()
    }

    override fun onExit() {
        runOnUiThread { finish() }
    }
}
