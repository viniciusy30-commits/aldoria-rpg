package com.aldoria.rpg

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/** Atualização pelo próprio app: consulta a última Release do GitHub, baixa o APK e abre o instalador. */
object Updater {
    private const val REPO = "viniciusy30-commits/aldoria-rpg"
    private const val APK_FILE = "Aldoria.apk"
    private const val CHECK_EVERY_MS = 6L * 60L * 60L * 1000L

    private val main = Handler(Looper.getMainLooper())
    @Volatile
    private var busy = false

    private class Info(val code: Int, val tag: String, val url: String, val notes: String)

    fun installedCode(ctx: Context): Int {
        val pi = ctx.packageManager.getPackageInfo(ctx.packageName, 0)
        return if (Build.VERSION.SDK_INT >= 28) pi.longVersionCode.toInt() else @Suppress("DEPRECATION") pi.versionCode
    }

    fun installedName(ctx: Context): String {
        return ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName ?: "?"
    }

    /** Verifica ao abrir o app, no máximo a cada 6 horas, e só avisa se tiver versão nova. */
    fun autoCheck(act: Activity) {
        val prefs = act.getSharedPreferences("updater", Context.MODE_PRIVATE)
        val last = prefs.getLong("last", 0L)
        val now = System.currentTimeMillis()
        if (now - last < CHECK_EVERY_MS) return
        prefs.edit().putLong("last", now).apply()
        check(act, false)
    }

    /** Verificação pelo botão: [manual] mostra mensagens também quando não há versão nova ou há erro. */
    fun check(act: Activity, manual: Boolean) {
        if (busy) return
        busy = true
        if (manual) toast(act, "Procurando atualização...")
        Thread {
            var info: Info? = null
            var error = false
            try {
                info = fetchLatest()
            } catch (e: Exception) {
                error = true
            }
            val got = info
            val failed = error
            main.post {
                busy = false
                if (act.isFinishing) return@post
                if (failed) {
                    if (manual) toast(act, "Não foi possível verificar. Confira a internet e tente de novo.")
                } else if (got == null) {
                    if (manual) toast(act, "Ainda não há versão publicada.")
                } else if (got.code > installedCode(act)) {
                    askUpdate(act, got)
                } else if (manual) {
                    toast(act, "Você já está na versão mais nova (" + installedName(act) + ").")
                }
            }
        }.start()
    }

    private fun fetchLatest(): Info? {
        val conn = URL("https://api.github.com/repos/$REPO/releases/latest").openConnection() as HttpURLConnection
        conn.connectTimeout = 12000
        conn.readTimeout = 12000
        conn.setRequestProperty("Accept", "application/vnd.github+json")
        conn.setRequestProperty("User-Agent", "Aldoria-App")
        try {
            if (conn.responseCode == 404) return null
            if (conn.responseCode != 200) throw RuntimeException("HTTP " + conn.responseCode)
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val j = JSONObject(body)
            val tag = j.optString("tag_name", "")
            val code = tag.trim().removePrefix("v").toIntOrNull() ?: return null
            val assets = j.optJSONArray("assets") ?: return null
            var url = ""
            for (i in 0 until assets.length()) {
                val a = assets.getJSONObject(i)
                if (a.optString("name", "").endsWith(".apk")) {
                    url = a.optString("browser_download_url", "")
                    break
                }
            }
            if (url.isEmpty()) return null
            return Info(code, tag, url, j.optString("body", ""))
        } finally {
            conn.disconnect()
        }
    }

    private fun askUpdate(act: Activity, info: Info) {
        val msg = "Versão " + info.tag + " pronta para instalar.\n\nSeu progresso fica salvo no celular."
        AlertDialog.Builder(act)
            .setTitle("Nova versão disponível")
            .setMessage(msg)
            .setPositiveButton("Atualizar") { _, _ -> download(act, info) }
            .setNegativeButton("Depois", null)
            .show()
    }

    private fun download(act: Activity, info: Info) {
        val pad = act.dp(20)
        val col = LinearLayout(act)
        col.orientation = LinearLayout.VERTICAL
        col.setPadding(pad, pad, pad, pad)
        val label = TextView(act)
        label.text = "Baixando..."
        label.gravity = Gravity.CENTER
        val bar = ProgressBar(act, null, android.R.attr.progressBarStyleHorizontal)
        bar.max = 100
        bar.isIndeterminate = true
        col.addView(label)
        col.addView(bar)
        val dlg = AlertDialog.Builder(act)
            .setTitle("Atualizando")
            .setView(col)
            .setCancelable(false)
            .create()
        dlg.show()
        Thread {
            var file: File? = null
            try {
                val dir = File(act.cacheDir, "updates")
                dir.mkdirs()
                val f = File(dir, APK_FILE)
                if (f.exists()) f.delete()
                val conn = URL(info.url).openConnection() as HttpURLConnection
                conn.connectTimeout = 15000
                conn.readTimeout = 30000
                conn.setRequestProperty("User-Agent", "Aldoria-App")
                val total = conn.contentLengthLong
                conn.inputStream.use { ins ->
                    f.outputStream().use { out ->
                        val buf = ByteArray(32 * 1024)
                        var done = 0L
                        var lastPct = -1
                        while (true) {
                            val n = ins.read(buf)
                            if (n < 0) break
                            out.write(buf, 0, n)
                            done += n
                            if (total > 0) {
                                val pct = (done * 100L / total).toInt()
                                if (pct != lastPct) {
                                    lastPct = pct
                                    main.post {
                                        bar.isIndeterminate = false
                                        bar.progress = pct
                                        label.text = "Baixando... $pct%"
                                    }
                                }
                            }
                        }
                    }
                }
                conn.disconnect()
                file = f
            } catch (e: Exception) {
                file = null
            }
            val result = file
            main.post {
                dlg.dismiss()
                if (act.isFinishing) return@post
                if (result == null) toast(act, "O download falhou. Tente de novo.") else install(act, result)
            }
        }.start()
    }

    private fun install(act: Activity, f: File) {
        if (Build.VERSION.SDK_INT >= 26 && !act.packageManager.canRequestPackageInstalls()) {
            AlertDialog.Builder(act)
                .setTitle("Permitir instalação")
                .setMessage("O Android precisa da sua permissão para o Aldoria instalar a atualização. Ative \"Permitir desta fonte\", volte ao app e toque em Verificar atualização de novo.")
                .setPositiveButton("Abrir configuração") { _, _ ->
                    val i = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + act.packageName))
                    act.startActivity(i)
                }
                .setNegativeButton("Agora não", null)
                .show()
            return
        }
        try {
            val uri = FileProvider.getUriForFile(act, act.packageName + ".fileprovider", f)
            val i = Intent(Intent.ACTION_VIEW)
            i.setDataAndType(uri, "application/vnd.android.package-archive")
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            act.startActivity(i)
        } catch (e: Exception) {
            toast(act, "Não consegui abrir o instalador.")
        }
    }

    private fun toast(act: Activity, s: String) {
        Toast.makeText(act, s, Toast.LENGTH_LONG).show()
    }
}
