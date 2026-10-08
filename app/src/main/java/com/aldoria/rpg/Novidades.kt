package com.aldoria.rpg

import android.app.Activity
import android.app.AlertDialog
import android.content.Context

/** Lê assets/novidades.txt (seção mais nova no topo) e mostra "O que mudou". */
object Novidades {
    class Entry(val id: String, var title: String, val items: ArrayList<String>)

    private fun load(ctx: Context): ArrayList<Entry> {
        val out = ArrayList<Entry>()
        try {
            val text = ctx.assets.open("novidades.txt").bufferedReader().use { it.readText() }
            var cur: Entry? = null
            for (raw in text.lines()) {
                val line = raw.trim()
                if (line.startsWith("#id ")) {
                    val ne = Entry(line.substring(4).trim(), "", ArrayList())
                    out.add(ne)
                    cur = ne
                } else if (line.startsWith("#titulo ")) {
                    val e = cur
                    if (e != null) e.title = line.substring(8).trim()
                } else if (line.startsWith("- ")) {
                    val e = cur
                    if (e != null) e.items.add(line.substring(2).trim())
                }
            }
        } catch (e: Exception) {
        }
        return out
    }

    private fun body(e: Entry): String {
        val sb = StringBuilder()
        for (s in e.items) sb.append("• ").append(s).append("\n\n")
        return sb.toString().trim()
    }

    fun showIfNew(act: Activity) {
        val list = load(act)
        if (list.isEmpty()) return
        val prefs = act.getSharedPreferences("novidades", Context.MODE_PRIVATE)
        val last = prefs.getString("seen", "")
        val top = list[0]
        if (top.id == last) return
        prefs.edit().putString("seen", top.id).apply()
        AlertDialog.Builder(act).setTitle("O que mudou: " + top.title).setMessage(body(top)).setPositiveButton("Entendi", null).show()
    }

    fun showAll(act: Activity) {
        val list = load(act)
        if (list.isEmpty()) return
        val sb = StringBuilder()
        var n = 0
        for (e in list) {
            if (n >= 3) break
            sb.append(e.title).append("\n\n").append(body(e)).append("\n\n\n")
            n++
        }
        AlertDialog.Builder(act).setTitle("O que mudou").setMessage(sb.toString().trim()).setPositiveButton("Fechar", null).show()
    }
}
