package com.aldoria.rpg

import android.content.Context
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

class LogLine(val text: String, val color: Int, var life: Float)

class Game(ctx: Context) {
    val world = World()
    val player = Player()
    val monsters = ArrayList<Monster>()
    val corpses = ArrayList<Corpse>()
    val texts = ArrayList<FloatText>()
    val parts = ArrayList<Particle>()
    val shots = ArrayList<Projectile>()
    val log = ArrayList<LogLine>()
    val inv = LinkedHashMap<String, Int>()

    private val rnd = java.util.Random()
    private val prefs = ctx.getSharedPreferences("aldoria_save", Context.MODE_PRIVATE)

    var target: Monster? = null
    var lootTarget: Corpse? = null
    private var path: IntArray? = null
    private var pathI = 0

    var joyDir = -1
    var joyX = 0f
    var joyY = 0f
    var ui = 0          // 0 nada, 1 mochila, 2 menu
    var dead = false
    var time = 0f
    var shake = 0f
    var bagTab = 0
    var selItem = ""
    val cd = FloatArray(Spells.list.size)
    private var autoCd = 0f
    private var saveT = 0f
    val pressT = FloatArray(Spells.list.size)
    val readyT = FloatArray(Spells.list.size)
    val fx = ArrayList<Fx>()
    val delayed = ArrayList<Delayed>()
    /** Modo mago supremo: mana infinita e todas as magias liberadas. */
    var cheat = false
    var shakeAmp = 1f
    private var auraT = 0f
    private val orbPt = FloatArray(2)

    init {
        for (s in world.spawns) {
            val m = Monster(Monsters.RAT, s[0], s[1])
            m.place(s[0], s[1])
            m.dir = rnd.nextInt(4)
            m.wanderCd = rnd.nextFloat() * 3f
            monsters.add(m)
        }
        if (!load()) newChar()
        say("Bem-vindo a Aldoria, Mago!", COL_YELLOW)
        say("Toque em um rato para mirar e use as magias.", COL_GRAY)
    }

    // ------------------------------------------------------------------ personagem

    private fun newChar() {
        val p = player
        p.level = 1
        p.exp = 0L
        p.ml = 0
        p.manaSpent = 0L
        p.maxHp = hpFor(1)
        p.maxMana = manaFor(1)
        p.hp = p.maxHp
        p.mana = p.maxMana
        p.place(world.startX, world.startY)
        inv.clear()
    }

    fun save() {
        val p = player
        prefs.edit()
            .putBoolean("has", true)
            .putInt("lv", p.level)
            .putLong("exp", p.exp)
            .putInt("hp", p.hp)
            .putInt("mana", p.mana)
            .putInt("ml", p.ml)
            .putLong("ms", p.manaSpent)
            .putInt("x", p.x)
            .putInt("y", p.y)
            .putInt("gold", inv["gold"] ?: 0)
            .putInt("cheese", inv["cheese"] ?: 0)
            .putBoolean("cheat", cheat)
            .apply()
    }

    private fun load(): Boolean {
        if (!prefs.getBoolean("has", false)) return false
        val p = player
        p.level = prefs.getInt("lv", 1)
        p.exp = prefs.getLong("exp", 0L)
        p.ml = prefs.getInt("ml", 0)
        p.manaSpent = prefs.getLong("ms", 0L)
        cheat = prefs.getBoolean("cheat", false)
        p.maxHp = hpFor(p.level)
        p.maxMana = manaFor(p.level)
        p.hp = prefs.getInt("hp", p.maxHp).coerceIn(1, p.maxHp)
        p.mana = prefs.getInt("mana", p.maxMana).coerceIn(0, p.maxMana)
        var x = prefs.getInt("x", world.startX)
        var y = prefs.getInt("y", world.startY)
        if (!world.walkable(x, y)) {
            x = world.startX
            y = world.startY
        }
        p.place(x, y)
        inv.clear()
        val gold = prefs.getInt("gold", 0)
        val cheese = prefs.getInt("cheese", 0)
        if (gold > 0) inv["gold"] = gold
        if (cheese > 0) inv["cheese"] = cheese
        return true
    }

    companion object {
        fun hasSave(ctx: Context): Boolean =
            ctx.getSharedPreferences("aldoria_save", Context.MODE_PRIVATE).getBoolean("has", false)

        fun wipe(ctx: Context) {
            ctx.getSharedPreferences("aldoria_save", Context.MODE_PRIVATE).edit().clear().apply()
        }
    }

    // ------------------------------------------------------------------ mensagens e efeitos

    fun say(text: String, color: Int) {
        log.add(LogLine(text, color, 14f))
        while (log.size > 40) log.removeAt(0)
    }

    fun addText(x: Float, y: Float, text: String, color: Int, size: Float, life: Float) {
        texts.add(FloatText(x, y, text, color, life, size))
    }

    fun burst(
        x: Float, y: Float, n: Int, color: Int, speed: Float, life: Float,
        size: Float, gy: Float, kind: Int
    ) {
        for (i in 0 until n) {
            val a = rnd.nextFloat() * TAU
            val s = speed * (0.35f + rnd.nextFloat() * 0.65f)
            val pt = Particle(
                x, y, cos(a) * s, sin(a) * s,
                life * (0.6f + rnd.nextFloat() * 0.4f), color,
                size * (0.6f + rnd.nextFloat() * 0.6f), kind
            )
            pt.gy = gy
            parts.add(pt)
        }
    }

    private fun ring(x: Float, y: Float, n: Int, color: Int, radius: Float, rise: Float, life: Float, size: Float, kind: Int) {
        for (i in 0 until n) {
            val a = i * TAU / n
            val pt = Particle(
                x + cos(a) * radius, y + sin(a) * radius * 0.5f,
                0f, -rise, life * (0.7f + rnd.nextFloat() * 0.3f), color, size, kind
            )
            parts.add(pt)
        }
    }

    // ------------------------------------------------------------------ efeitos de magia

    private fun newFx(kind: Int, x: Float, y: Float, dur: Float, color: Int): Fx {
        val f = Fx(kind, x, y, dur)
        f.color = color
        f.seed = rnd.nextInt(100000)
        fx.add(f)
        while (fx.size > 60) fx.removeAt(0)
        return f
    }

    /** Cria uma partícula com todos os parâmetros. */
    private fun pt(
        kind: Int, x: Float, y: Float, vx: Float, vy: Float, life: Float, color: Int, size: Float,
        gy: Float, grow: Float, drag: Float, vr: Float, delay: Float
    ) {
        if (parts.size > 900) return
        val q = Particle(x, y, vx, vy, life, color, size, kind)
        q.gy = gy
        q.grow = grow
        q.drag = drag
        q.vr = vr
        q.delay = delay
        q.rot = rnd.nextFloat() * TAU
        parts.add(q)
    }

    /** Posição (em quadrados) da esfera do cajado, de onde saem as magias. */
    private fun orb(): FloatArray {
        val p = player
        val frame = if (p.moving) ((p.walk * 2f).toInt() and 3) else 0
        val di = if (p.dir == 3) 1 else p.dir
        val ox = MageData.ORB[(di * 4 + frame) * 2]
        val hx = if (p.dir == 3) 64f - ox else ox
        val hy = MageData.ORB[(di * 4 + frame) * 2 + 1]
        orbPt[0] = p.fx + hx / 64f
        orbPt[1] = p.fy + hy / 64f
        return orbPt
    }

    private fun rf(): Float = rnd.nextFloat()

    fun toggleCheat() {
        if (dead) return
        cheat = !cheat
        prefs.edit().putBoolean("cheat", cheat).apply()
        val p = player
        val gold = 0xFFFFD27A.toInt()
        val violet = 0xFFB388FF.toInt()
        if (cheat) {
            p.mana = p.maxMana
            say("MODO MAGO SUPREMO: mana infinita e todas as magias liberadas!", COL_YELLOW)
            val f = newFx(FxK.ARCANE, p.fx + 0.5f, p.fy + 0.88f, 2.6f, gold)
            f.follow = true
            shake = 0.4f
            shakeAmp = 1.5f
            for (i in 0 until 26) {
                val a = rf() * TAU
                val r = 0.3f + rf() * 1.0f
                pt(5, p.fx + 0.5f + cos(a) * r, p.fy + 0.88f + sin(a) * r * 0.5f, 0f, -0.6f - rf() * 1.2f,
                    1.2f + rf() * 0.8f, if (i % 2 == 0) gold else violet, 0.05f, 0f, 0f, 0f, 0f, rf() * 0.5f)
            }
            for (i in 0 until 10) {
                val a = i * TAU / 10f
                pt(7, p.fx + 0.5f + cos(a) * 1.0f, p.fy + 0.7f + sin(a) * 0.5f, 0f, -0.5f - rf() * 0.5f,
                    1.6f, gold, 0.09f, 0f, 0f, 0f, (rf() - 0.5f) * 2f, rf() * 0.4f)
            }
        } else {
            say("Modo mago supremo desligado.", COL_GRAY)
            ring(p.fx + 0.5f, p.fy + 0.9f, 18, violet, 0.6f, 0.8f, 0.8f, 0.06f, 2)
        }
    }

    // ------------------------------------------------------------------ consultas

    fun monsterAt(x: Int, y: Int): Monster? {
        for (m in monsters) if (m.alive && m.x == x && m.y == y) return m
        return null
    }

    fun corpseAt(x: Int, y: Int): Corpse? {
        for (c in corpses) if (!c.looted && c.x == x && c.y == y) return c
        return null
    }

    // ------------------------------------------------------------------ comandos do jogador

    fun toggleTarget(m: Monster) {
        if (dead || !m.alive) return
        if (target === m) {
            target = null
            say("Você parou de mirar.", COL_GRAY)
        } else {
            target = m
            say("Alvo: " + m.name + ".", COL_GRAY)
        }
    }

    fun targetNearest(range: Int): Boolean {
        var best: Monster? = null
        var bd = 999
        for (m in monsters) {
            if (!m.alive) continue
            val d = cheb(player.x, player.y, m.x, m.y)
            if (d <= range && d < bd) {
                bd = d
                best = m
            }
        }
        if (best == null) return false
        target = best
        return true
    }

    fun toggleNearestTarget() {
        if (dead) return
        if (target != null) {
            target = null
            say("Você parou de mirar.", COL_GRAY)
        } else if (!targetNearest(8)) {
            say("Nenhum monstro por perto.", COL_RED)
        } else {
            say("Alvo: " + (target?.name ?: "") + ".", COL_GRAY)
        }
    }

    fun walkTo(tx: Int, ty: Int) {
        if (dead) return
        lootTarget = null
        val p = player
        if (tx == p.x && ty == p.y) return
        if (!world.walkable(tx, ty)) return
        val pa = world.path(p.x, p.y, tx, ty, 2500, false) { nx, ny ->
            world.walkable(nx, ny) && monsterAt(nx, ny) == null
        }
        if (pa == null) {
            say("Não há caminho até lá.", COL_RED)
            return
        }
        path = pa
        pathI = 0
        burst(tx + 0.5f, ty + 0.6f, 6, 0xFF9BE7FF.toInt(), 0.8f, 0.35f, 0.05f, 0f, 0)
    }

    fun requestLoot(c: Corpse) {
        if (dead || c.looted) return
        val p = player
        if (cheb(p.x, p.y, c.x, c.y) <= 1) {
            doLoot(c)
            return
        }
        val pa = world.path(p.x, p.y, c.x, c.y, 2500, true) { nx, ny ->
            world.walkable(nx, ny) && monsterAt(nx, ny) == null
        }
        if (pa == null) {
            say("Não há caminho até lá.", COL_RED)
            return
        }
        path = pa
        pathI = 0
        lootTarget = c
    }

    private fun doLoot(c: Corpse) {
        if (c.looted) return
        c.looted = true
        c.life = 1.2f
        val sb = StringBuilder()
        var first = true
        for ((id, n) in c.loot) {
            if (n <= 0) continue
            if (!first) sb.append(", ")
            first = false
            sb.append(n).append(' ').append(Items.name(id, n))
            inv[id] = (inv[id] ?: 0) + n
        }
        val what = if (first) "nada" else sb.toString()
        say("Loot de um " + c.typeName.lowercase() + ": " + what + ".", COL_LOOT)
        burst(c.x + 0.5f, c.y + 0.5f, 10, 0xFFFFE066.toInt(), 1.2f, 0.5f, 0.06f, 0f, 2)
    }

    fun useItem(id: String) {
        if (dead) return
        val n = inv[id] ?: 0
        if (n <= 0) return
        if (id == "cheese") {
            inv[id] = n - 1
            if (n - 1 <= 0) {
                inv.remove(id)
                if (selItem == id) selItem = ""
            }
            say("Munch.", COL_YELLOW)
            heal(20)
        }
    }

    fun respawn() {
        if (!dead) return
        val p = player
        val loss = p.exp / 10L
        p.exp -= loss
        while (p.level > 1 && p.exp < expForLevel(p.level)) p.level--
        p.maxHp = hpFor(p.level)
        p.maxMana = manaFor(p.level)
        p.hp = p.maxHp
        p.mana = p.maxMana
        p.hasteT = 0f
        p.shieldT = 0f
        p.place(world.startX, world.startY)
        dead = false
        fx.clear()
        delayed.clear()
        for (m in monsters) m.aggro = false
        say("Você renasceu no templo de Aldoria.", COL_YELLOW)
        if (loss > 0L) say("Você perdeu $loss pontos de experiência.", COL_RED)
        save()
    }

    // ------------------------------------------------------------------ magias

    fun castSpell(i: Int) {
        if (dead || i < 0 || i >= Spells.list.size) return
        val s = Spells.list[i]
        val p = player
        if (!cheat && p.level < s.level) {
            say("Você precisa do nível " + s.level + " para usar " + s.words + ".", COL_RED)
            return
        }
        if (cd[i] > 0f) {
            say("Você está exausto.", COL_RED)
            return
        }
        if (!cheat && p.mana < s.mana) {
            say("Você não tem mana suficiente.", COL_RED)
            burst(p.fx + 0.5f, p.fy + 0.3f, 6, 0xFF6FB6FF.toInt(), 0.9f, 0.4f, 0.05f, 0f, 0)
            return
        }
        var tg: Monster? = null
        if (s.kind == Spells.FIRE || s.kind == Spells.ENERGY) {
            tg = target
            if (tg == null || !tg.alive) {
                if (!targetNearest(3)) {
                    say("Nenhum alvo ao alcance.", COL_RED)
                    return
                }
                tg = target
            }
            if (tg == null) return
            if (cheb(p.x, p.y, tg.x, tg.y) > 3) {
                say("O alvo está longe demais.", COL_RED)
                return
            }
        }
        if (!cheat) p.mana -= s.mana
        addManaSpent(s.mana)
        cd[i] = s.cd
        pressT[i] = 0.4f
        addText(p.fx + 0.5f, p.fy - 0.25f, s.words, COL_YELLOW, 0.9f, 1.3f)

        val base = p.level * 0.2f + p.ml * 1.4f
        val feetX = p.fx + 0.5f
        val feetY = p.fy + 0.88f
        when (s.kind) {
            Spells.HEAL -> {
                val amt = (base + 18f + rnd.nextInt(12)).toInt()
                heal(amt)
                val o = orb()
                newFx(FxK.FLARE, o[0], o[1], 0.5f, s.color)
                val f = newFx(FxK.HEAL, feetX, feetY, 1.9f, s.color)
                f.follow = true
            }
            Spells.FIRE -> {
                val m = tg ?: return
                p.dir = dirTo(p.x, p.y, m.x, m.y)
                val dmg = (base + 8f + rnd.nextInt(8)).toInt()
                val o = orb()
                val ox = o[0]
                val oy = o[1]
                val travel = 0.17f + 0.065f * cheb(p.x, p.y, m.x, m.y)
                newFx(FxK.FLARE, ox, oy, 0.5f, s.color)
                val cf = newFx(FxK.CAST, feetX, feetY, 0.7f, s.color)
                cf.follow = true
                cf.scale = 0.55f
                val fb = newFx(FxK.FIREBALL, ox, oy, travel, s.color)
                fb.target = m
                fb.tx = m.fx + 0.5f
                fb.ty = m.fy + 0.5f
                for (n in 0 until 10) {
                    val a = rf() * TAU
                    pt(5, ox + cos(a) * 0.35f, oy + sin(a) * 0.35f, -cos(a) * 1.6f, -sin(a) * 1.6f,
                        0.22f, if (n % 2 == 0) 0xFFFFA43D.toInt() else 0xFFFFE066.toInt(), 0.04f, 0f, 0f, 0f, 0f, 0f)
                }
                delayed.add(Delayed(travel) { fireImpact(m, dmg) })
            }
            Spells.ENERGY -> {
                val m = tg ?: return
                p.dir = dirTo(p.x, p.y, m.x, m.y)
                val dmg = (base + 6f + rnd.nextInt(9)).toInt()
                val o = orb()
                val ox = o[0]
                val oy = o[1]
                newFx(FxK.FLARE, ox, oy, 0.5f, s.color)
                val cf = newFx(FxK.CAST, feetX, feetY, 0.7f, s.color)
                cf.follow = true
                cf.scale = 0.55f
                val bf = newFx(FxK.BOLT, ox, oy, 0.6f, s.color)
                bf.target = m
                bf.tx = m.fx + 0.5f
                bf.ty = m.fy + 0.5f
                shake = max(shake, 0.12f)
                shakeAmp = 0.7f
                for (n in 0 until 12) {
                    val a = rf() * TAU
                    pt(4, ox, oy, cos(a) * 3.2f, sin(a) * 3.2f, 0.25f, if (n % 2 == 0) s.color else 0xFFE9DDFF.toInt(),
                        0.03f, 0f, 0f, 3f, 0f, 0f)
                }
                delayed.add(Delayed(0.07f) { energyImpact(m, dmg) })
            }
            Spells.WAVE -> castWave(base, s.color)
            Spells.HASTE -> {
                p.hasteT = 30f
                say("Você está mais rápido!", COL_GREEN)
                val o = orb()
                newFx(FxK.FLARE, o[0], o[1], 0.5f, s.color)
                val f = newFx(FxK.HASTE, feetX, feetY, 1.1f, s.color)
                f.follow = true
                for (n in 0 until 18) {
                    val a = n * TAU / 18f
                    pt(8, feetX + cos(a) * 0.45f, feetY - 0.1f + sin(a) * 0.2f, -sin(a) * 1.3f, -1.8f - rf() * 0.9f,
                        0.55f, s.color, 0.06f, 0f, 0f, 0f, 0f, rf() * 0.2f)
                }
                for (n in 0 until 10) {
                    val a = rf() * TAU
                    pt(3, feetX, feetY, cos(a) * 1.0f, sin(a) * 0.4f, 0.7f, 0xFFC8D8A8.toInt(), 0.09f, 0f, 0.28f, 2f, 0f, 0f)
                }
            }
            Spells.SHIELD -> {
                p.shieldT = SHIELD_DUR
                say("Escudo mágico ativado.", COL_BLUE)
                val o = orb()
                newFx(FxK.FLARE, o[0], o[1], 0.5f, s.color)
                val f = newFx(FxK.SHIELD, feetX, feetY, 1.4f, s.color)
                f.follow = true
                shake = max(shake, 0.12f)
                shakeAmp = 0.6f
                for (n in 0 until 20) {
                    val a = rf() * TAU
                    pt(5, feetX + cos(a) * 0.7f, feetY + sin(a) * 0.35f, 0f, -0.8f - rf() * 0.8f,
                        1.0f, s.color, 0.04f, 0f, 0f, 0f, 0f, rf() * 0.4f)
                }
                for (n in 0 until 8) {
                    val a = n * TAU / 8f
                    pt(7, feetX + cos(a) * 0.8f, feetY - 0.2f + sin(a) * 0.4f, 0f, -0.5f - rf() * 0.3f,
                        1.3f, s.color, 0.07f, 0f, 0f, 0f, (rf() - 0.5f) * 2f, rf() * 0.3f)
                }
            }
        }
    }

    private fun fireImpact(m: Monster, dmg: Int) {
        val cx = m.fx + 0.5f
        val cy = m.fy + 0.5f
        newFx(FxK.BLAST, cx, cy, 1.0f, 0xFFFF8A3D.toInt())
        newFx(FxK.SCORCH, cx, cy + 0.3f, 3.2f, 0)
        shake = max(shake, 0.2f)
        shakeAmp = 1.3f
        for (n in 0 until 24) {
            val a = rf() * TAU
            val sp = 1.2f + rf() * 2.6f
            pt(4, cx, cy, cos(a) * sp, sin(a) * sp - 1.6f, 0.7f + rf() * 0.7f,
                if (n % 3 == 0) 0xFFFFE066.toInt() else if (n % 3 == 1) 0xFFFF8A3D.toInt() else 0xFFFF5A2D.toInt(),
                0.035f, 4.2f, 0f, 0.6f, 0f, 0f)
        }
        for (n in 0 until 7) {
            pt(3, cx + (rf() - 0.5f) * 0.4f, cy, (rf() - 0.5f) * 0.6f, -0.7f - rf() * 0.7f, 1.3f,
                0xFF3A2A22.toInt(), 0.14f, 0f, 0.4f, 0.6f, 0f, 0.1f + rf() * 0.2f)
        }
        for (n in 0 until 10) {
            val a = rf() * TAU
            pt(5, cx, cy, cos(a) * 2.4f, sin(a) * 2.4f, 0.45f, 0xFFFFD27A.toInt(), 0.045f, 0f, 0f, 3f, 0f, 0f)
        }
        damageMonster(m, dmg, 1)
        m.burnT = if (m.alive) 2.4f else 0f
    }

    private fun energyImpact(m: Monster, dmg: Int) {
        val cx = m.fx + 0.5f
        val cy = m.fy + 0.5f
        shake = max(shake, 0.16f)
        shakeAmp = 1.0f
        for (n in 0 until 20) {
            val a = rf() * TAU
            val sp = 2.0f + rf() * 3.6f
            pt(4, cx, cy, cos(a) * sp, sin(a) * sp, 0.3f + rf() * 0.3f,
                if (n % 2 == 0) 0xFFB388FF.toInt() else 0xFFE9DDFF.toInt(), 0.03f, 0f, 0f, 3f, 0f, 0f)
        }
        for (n in 0 until 8) {
            val a = rf() * TAU
            pt(5, cx, cy, cos(a) * 1.4f, sin(a) * 1.4f - 0.4f, 0.6f, 0xFF7FE9FF.toInt(), 0.04f, 0f, 0f, 2f, 0f, 0f)
        }
        damageMonster(m, dmg, 2)
        m.shockT = if (m.alive) 0.8f else 0f
    }

    private fun waveHit(m: Monster, dmg: Int) {
        val cx = m.fx + 0.5f
        val cy = m.fy + 0.5f
        for (n in 0 until 8) {
            val a = rf() * TAU
            pt(4, cx, cy, cos(a) * 1.8f, sin(a) * 1.8f - 1.2f, 0.6f, 0xFFFFA43D.toInt(), 0.035f, 4f, 0f, 0.5f, 0f, 0f)
        }
        damageMonster(m, dmg, 1)
        m.burnT = if (m.alive) 2.6f else 0f
    }

    private fun castWave(base: Float, col: Int) {
        val p = player
        val fxv = DX[p.dir]
        val fyv = DY[p.dir]
        val sxv = -fyv
        val syv = fxv
        val o = orb()
        val ox = o[0]
        val oy = o[1]
        val wf = newFx(FxK.WAVE, p.x + 0.5f, p.y + 0.5f, 3.0f, col)
        wf.dir = p.dir
        newFx(FxK.FLARE, ox, oy, 0.55f, col)
        val cf = newFx(FxK.CAST, p.fx + 0.5f, p.fy + 0.88f, 0.8f, col)
        cf.follow = true
        cf.scale = 0.8f
        shake = 0.38f
        shakeAmp = 1.9f
        val a0 = atan2(fyv.toFloat(), fxv.toFloat())
        for (n in 0 until 34) {
            val a = a0 + (rf() - 0.5f) * 0.9f
            val sp = 4.5f + rf() * 3.5f
            pt(if (n % 3 == 0) 5 else 4, ox, oy, cos(a) * sp, sin(a) * sp, 0.35f + rf() * 0.25f,
                if (n % 2 == 0) 0xFFFFA43D.toInt() else 0xFFFF5A2D.toInt(), 0.05f, 0f, 0f, 2.5f, 0f, n * 0.006f)
        }
        for (d in 1..4) {
            val half = d / 2
            for (k in -half..half) {
                val tx = p.x + fxv * d + sxv * k
                val ty = p.y + fyv * d + syv * k
                for (n in 0 until 3) {
                    pt(4, tx + 0.5f + (rf() - 0.5f) * 0.6f, ty + 0.65f + (rf() - 0.5f) * 0.3f,
                        (rf() - 0.5f) * 0.8f, -1.2f - rf() * 1.8f, 0.6f + rf() * 0.5f,
                        if (n % 2 == 0) 0xFFFFA43D.toInt() else 0xFFFFE066.toInt(), 0.04f, 3.5f, 0f, 0.3f, 0f, d * 0.07f)
                }
                pt(3, tx + 0.5f, ty + 0.5f, (rf() - 0.5f) * 0.4f, -0.5f - rf() * 0.5f, 1.3f,
                    0xFF2E2420.toInt(), 0.16f, 0f, 0.3f, 0.8f, 0f, d * 0.07f + 0.35f)
            }
        }
        for (m in monsters) {
            if (!m.alive) continue
            val rx = m.x - p.x
            val ry = m.y - p.y
            val fd = rx * fxv + ry * fyv
            val sd = rx * sxv + ry * syv
            if (fd in 1..4 && abs(sd) <= fd / 2) {
                val dmg = (base + 10f + rnd.nextInt(10)).toInt()
                delayed.add(Delayed(fd * 0.07f + 0.06f) { waveHit(m, dmg) })
            }
        }
    }

    private fun shatterShield() {
        val p = player
        val f = newFx(FxK.BREAK, p.fx + 0.5f, p.fy + 0.88f, 0.9f, 0xFF8CC8FF.toInt())
        f.follow = true
        val cx = p.fx + 0.5f
        val cy = p.fy + 0.56f
        for (n in 0 until 16) {
            val a = rf() * TAU
            pt(9, cx + cos(a) * 0.55f, cy + sin(a) * 0.7f, cos(a) * (1.2f + rf() * 1.2f), sin(a) * (1.0f + rf()) - 0.8f,
                0.9f + rf() * 0.4f, 0xFF6FB6FF.toInt(), 0.06f + rf() * 0.06f, 4f, 0f, 0.4f, (rf() - 0.5f) * 10f, 0f)
        }
    }

    private fun heal(amt: Int) {
        val p = player
        val before = p.hp
        p.hp = min(p.maxHp, p.hp + amt)
        val got = p.hp - before
        if (got > 0) addText(p.fx + 0.5f, p.fy + 0.1f, "+" + got, COL_GREEN, 1f, 1.2f)
        for (i in 0 until 14) {
            val a = rnd.nextFloat() * TAU
            val pt = Particle(
                p.fx + 0.5f + cos(a) * 0.4f, p.fy + 0.9f + rnd.nextFloat() * 0.2f,
                0f, -0.9f - rnd.nextFloat() * 0.8f, 0.8f, if (i % 2 == 0) 0xFF7CFF8A.toInt() else 0xFFFFFFFF.toInt(),
                0.07f, 2
            )
            parts.add(pt)
        }
    }

    private fun addManaSpent(n: Int) {
        val p = player
        p.manaSpent += n.toLong()
        while (p.manaSpent >= manaNeeded(p.ml)) {
            p.manaSpent -= manaNeeded(p.ml)
            val old = p.ml
            p.ml++
            say("Você avançou do nível mágico $old para ${p.ml}.", COL_GREEN)
            addText(p.fx + 0.5f, p.fy - 0.6f, "Nível mágico " + p.ml, COL_BLUE, 1.1f, 2f)
            ring(p.fx + 0.5f, p.fy + 0.9f, 18, 0xFF6FB6FF.toInt(), 0.6f, 1.2f, 1f, 0.07f, 2)
        }
    }

    // ------------------------------------------------------------------ dano, morte, experiência

    fun damageMonster(m: Monster, dmg: Int, kind: Int) {
        if (!m.alive) return
        m.hp -= dmg
        m.flash = 0.18f
        m.aggro = true
        val col = if (kind == 1) COL_ORANGE else if (kind == 2) COL_PURPLE else COL_RED
        addText(m.fx + 0.5f, m.fy + 0.1f, dmg.toString(), col, 1f, 1.1f)
        burst(m.fx + 0.5f, m.fy + 0.55f, 8, 0xFFB01E1E.toInt(), 1.5f, 0.5f, 0.06f, 3f, 1)
        if (m.hp <= 0) killMonster(m)
    }

    private fun killMonster(m: Monster) {
        m.alive = false
        m.hp = 0
        m.moving = false
        m.deadT = 25f
        if (target === m) target = null
        val loot = LinkedHashMap<String, Int>()
        if (rnd.nextFloat() < 0.75f) loot["gold"] = 1 + rnd.nextInt(m.type.goldMax)
        if (rnd.nextFloat() < m.type.cheeseChance) loot["cheese"] = 1
        corpses.add(Corpse(m.x, m.y, m.type.name, 60f, loot))
        burst(m.fx + 0.5f, m.fy + 0.6f, 16, 0xFFB01E1E.toInt(), 2f, 0.6f, 0.07f, 3f, 1)
        val xp = m.type.exp * Cfg.EXP_RATE
        addText(m.fx + 0.5f, m.fy - 0.1f, xp.toString(), COL_WHITE, 1.1f, 1.5f)
        say("Você ganhou $xp pontos de experiência.", COL_GREEN)
        gainExp(xp.toLong())
    }

    private fun gainExp(n: Long) {
        val p = player
        p.exp += n
        while (p.exp >= expForLevel(p.level + 1)) {
            val oh = p.maxHp
            val om = p.maxMana
            val old = p.level
            p.level++
            p.maxHp = hpFor(p.level)
            p.maxMana = manaFor(p.level)
            p.hp += p.maxHp - oh
            p.mana += p.maxMana - om
            say("Você avançou do nível $old para ${p.level}.", COL_YELLOW)
            addText(p.fx + 0.5f, p.fy - 0.9f, "NÍVEL " + p.level + "!", COL_YELLOW, 1.5f, 2.4f)
            ring(p.fx + 0.5f, p.fy + 0.9f, 28, 0xFFFFE066.toInt(), 0.7f, 1.6f, 1.2f, 0.08f, 2)
            burst(p.fx + 0.5f, p.fy + 0.5f, 30, 0xFFFFE066.toInt(), 3f, 1f, 0.07f, 1.5f, 2)
            for (s in Spells.list) {
                if (s.level == p.level) say("Nova magia liberada: " + s.words + " (" + s.name + ").", COL_YELLOW)
            }
        }
    }

    fun damagePlayer(dmg: Int, ang: Float = 1.5708f) {
        if (dead) return
        val p = player
        var d = dmg
        if (p.shieldT > 0f && p.mana > 0) {
            val ab = min(p.mana, d)
            p.mana -= ab
            d -= ab
            if (ab > 0) {
                addText(p.fx + 0.5f, p.fy - 0.1f, ab.toString(), COL_BLUE, 1f, 1f)
                val rp = newFx(FxK.RIPPLE, 0f, 0f, 0.8f, 0xFF8CC8FF.toInt())
                rp.ang = ang
                val hx = p.fx + 0.5f + cos(ang) * 0.62f
                val hy = p.fy + 0.56f + sin(ang) * 0.74f
                for (n in 0 until 7) {
                    val a = ang + (rf() - 0.5f) * 2.4f
                    pt(5, hx, hy, cos(a) * 1.6f, sin(a) * 1.6f, 0.4f, 0xFF9CD0FF.toInt(), 0.035f, 0f, 0f, 2f, 0f, 0f)
                }
            }
        }
        if (d > 0) {
            p.hp -= d
            addText(p.fx + 0.5f, p.fy + 0.1f, d.toString(), COL_RED, 1f, 1.1f)
            burst(p.fx + 0.5f, p.fy + 0.55f, 8, 0xFFB01E1E.toInt(), 1.4f, 0.5f, 0.06f, 3f, 1)
        }
        p.flash = 0.2f
        shake = 0.15f
        shakeAmp = 1f
        if (p.hp <= 0) {
            p.hp = 0
            die()
        }
    }

    private fun die() {
        dead = true
        path = null
        target = null
        lootTarget = null
        ui = 0
        joyDir = -1
        say("Você morreu.", COL_RED)
    }

    // ------------------------------------------------------------------ atualização

    fun update(dt0: Float) {
        if (ui == 2) return
        val dt = if (dt0 > 0.05f) 0.05f else dt0
        time += dt
        for (i in cd.indices) {
            if (cd[i] > 0f) {
                cd[i] -= dt
                if (cd[i] <= 0f) readyT[i] = 0.7f
            }
            if (pressT[i] > 0f) pressT[i] -= dt
            if (readyT[i] > 0f) readyT[i] -= dt
        }
        if (shake > 0f) shake -= dt
        var di = delayed.size - 1
        while (di >= 0) {
            if (di < delayed.size) {
                val dl = delayed[di]
                dl.t -= dt
                if (dl.t <= 0f) {
                    delayed.removeAt(di)
                    dl.act()
                }
            }
            di--
        }
        if (!dead) updatePlayer(dt)
        updateMonsters(dt)
        updateShots(dt)
        updateFx(dt)
        updateEffects(dt)
        saveT += dt
        if (saveT > 15f) {
            saveT = 0f
            if (!dead) save()
        }
    }

    private fun updatePlayer(dt: Float) {
        val p = player
        p.speed = if (p.hasteT > 0f) 4.7f else 3.3f
        p.updateMove(dt)
        if (p.hasteT > 0f) p.hasteT -= dt
        if (p.shieldT > 0f) {
            p.shieldT -= dt
            if (p.shieldT <= 0f) {
                say("O escudo mágico acabou.", COL_GRAY)
                shatterShield()
            }
        }
        if (cheat) {
            p.mana = p.maxMana
            if (rf() < dt * 7f) {
                pt(5, p.fx + 0.5f + (rf() - 0.5f) * 0.6f, p.fy + 0.9f - rf() * 0.6f, 0f, -0.5f - rf() * 0.5f, 1.0f,
                    if (rf() < 0.5f) 0xFFFFD27A.toInt() else 0xFFB388FF.toInt(), 0.04f, 0f, 0f, 0f, 0f, 0f)
            }
        }
        if (p.hasteT > 0f && p.moving) {
            auraT -= dt
            if (auraT <= 0f) {
                auraT = 0.05f
                pt(8, p.fx + 0.5f + (rf() - 0.5f) * 0.4f, p.fy + 0.4f + rf() * 0.5f, -DX[p.dir] * 2.2f, -DY[p.dir] * 2.2f,
                    0.32f, 0xFF9CFFA8.toInt(), 0.07f, 0f, 0f, 0f, 0f, 0f)
            }
        }
        val inPz = world.isPz(p.x, p.y)
        p.regenHpT += dt
        if (p.regenHpT >= (if (inPz) 1.5f else 4f)) {
            p.regenHpT = 0f
            if (p.hp < p.maxHp) p.hp = min(p.maxHp, p.hp + 1 + p.level / 10)
        }
        p.regenManaT += dt
        if (p.regenManaT >= (if (inPz) 0.6f else 1.5f)) {
            p.regenManaT = 0f
            if (p.mana < p.maxMana) p.mana = min(p.maxMana, p.mana + 1 + p.level / 8)
        }

        if (!p.moving) {
            if (joyDir >= 0) {
                path = null
                lootTarget = null
                tryStep(joyDir)
            } else {
                followPath()
            }
        }

        val t = target
        if (t != null) {
            if (!t.alive || cheb(p.x, p.y, t.x, t.y) > 9) {
                target = null
            } else {
                autoAttack(t, dt)
            }
        }

        val lt = lootTarget
        if (lt != null && !p.moving) {
            if (lt.looted) {
                lootTarget = null
            } else if (cheb(p.x, p.y, lt.x, lt.y) <= 1) {
                doLoot(lt)
                lootTarget = null
            } else if (path == null) {
                lootTarget = null
            }
        }
    }

    private fun tryStep(d: Int) {
        val p = player
        val nx = p.x + DX[d]
        val ny = p.y + DY[d]
        p.dir = d
        if (!world.walkable(nx, ny)) return
        if (monsterAt(nx, ny) != null) return
        p.startMove(nx, ny)
    }

    private fun followPath() {
        val pa = path ?: return
        val p = player
        if (pathI >= pa.size) {
            path = null
            return
        }
        val idx = pa[pathI]
        val nx = idx % world.w
        val ny = idx / world.w
        val dx = nx - p.x
        val dy = ny - p.y
        val adjacent = (abs(dx) + abs(dy)) == 1
        if (!adjacent || !world.walkable(nx, ny) || monsterAt(nx, ny) != null) {
            path = null
            return
        }
        p.startMove(nx, ny)
        pathI++
        if (pathI >= pa.size) path = null
    }

    private fun autoAttack(t: Monster, dt: Float) {
        val p = player
        if (autoCd > 0f) autoCd -= dt
        if (!p.moving) p.dir = dirTo(p.x, p.y, t.x, t.y)
        val dist = cheb(p.x, p.y, t.x, t.y)
        if (dist <= 3 && autoCd <= 0f) {
            autoCd = 2f
            val dmg = 3 + rnd.nextInt(5) + p.ml / 2 + p.level / 10
            shots.add(Projectile(p.fx + 0.5f, p.fy + 0.35f, t, dmg, 0xFFC9A6FF.toInt(), 0.28f))
        }
    }

    private fun updateMonsters(dt: Float) {
        val p = player
        for (m in monsters) {
            if (!m.alive) {
                m.deadT -= dt
                if (m.deadT <= 0f && cheb(p.x, p.y, m.spawnX, m.spawnY) > 9 && monsterAt(m.spawnX, m.spawnY) == null) {
                    m.alive = true
                    m.hp = m.maxHp
                    m.aggro = false
                    m.place(m.spawnX, m.spawnY)
                }
                continue
            }
            m.updateMove(dt)
            val dist = cheb(p.x, p.y, m.x, m.y)
            if (dist > 26) continue
            m.attackCd -= dt
            m.thinkCd -= dt
            m.wanderCd -= dt
            val pInPz = world.isPz(p.x, p.y)
            if (!dead && !pInPz && dist <= 7) m.aggro = true
            if (dead || pInPz || dist > 14) m.aggro = false
            if (m.moving) continue
            if (m.aggro) {
                if (dist <= 1) {
                    m.dir = dirTo(m.x, m.y, p.x, p.y)
                    if (m.attackCd <= 0f) {
                        m.attackCd = m.type.atkInterval
                        val d = m.type.dmgMin + rnd.nextInt(m.type.dmgMax - m.type.dmgMin + 1)
                        damagePlayer(d, atan2((m.y - p.y).toFloat(), (m.x - p.x).toFloat()))
                    }
                } else if (m.thinkCd <= 0f) {
                    m.thinkCd = 0.3f
                    val pa = world.path(m.x, m.y, p.x, p.y, 400, true) { nx, ny ->
                        world.walkable(nx, ny) && !world.isPz(nx, ny) && monsterAt(nx, ny) == null &&
                            !(nx == p.x && ny == p.y)
                    }
                    if (pa != null && pa.isNotEmpty()) {
                        val idx = pa[0]
                        m.startMove(idx % world.w, idx / world.w)
                    }
                }
            } else if (m.wanderCd <= 0f) {
                m.wanderCd = 1.5f + rnd.nextFloat() * 3.5f
                val d = rnd.nextInt(4)
                val nx = m.x + DX[d]
                val ny = m.y + DY[d]
                if (world.walkable(nx, ny) && !world.isPz(nx, ny) && monsterAt(nx, ny) == null &&
                    !(nx == p.x && ny == p.y) && cheb(nx, ny, m.spawnX, m.spawnY) <= 4
                ) {
                    m.startMove(nx, ny)
                }
            }
        }
    }

    private fun updateShots(dt: Float) {
        var i = shots.size - 1
        while (i >= 0) {
            val s = shots[i]
            s.t += dt / s.dur
            if (s.t >= 1f) {
                if (s.target.alive) {
                    burst(s.target.fx + 0.5f, s.target.fy + 0.45f, 6, 0xFFC9A6FF.toInt(), 1.6f, 0.3f, 0.05f, 0f, 2)
                    damageMonster(s.target, s.dmg, 2)
                }
                shots.removeAt(i)
            }
            i--
        }
    }

    private fun updateFx(dt: Float) {
        for (i in fx.size - 1 downTo 0) {
            val f = fx[i]
            f.t += dt
            val tm = f.target
            if (tm != null) {
                f.tx = tm.fx + 0.5f
                f.ty = tm.fy + 0.5f
            }
            if (f.kind == FxK.FIREBALL) {
                f.emit -= dt
                var guard = 0
                while (f.emit <= 0f && guard < 4) {
                    f.emit += 0.018f
                    guard++
                    val u = f.fbU()
                    val wx = f.fbX(u)
                    val wy = f.fbY(u)
                    pt(4, wx, wy, -(f.tx - f.x) * 0.2f + (rf() - 0.5f) * 1.2f, (rf() - 0.5f) * 1.2f - 0.3f,
                        0.35f + rf() * 0.3f, if (rf() < 0.5f) 0xFFFFA43D.toInt() else 0xFFFF6A1E.toInt(),
                        0.035f, 1.5f, 0f, 1.5f, 0f, 0f)
                    if (rf() < 0.35f) {
                        pt(3, wx, wy, (rf() - 0.5f) * 0.4f, -0.3f - rf() * 0.3f, 0.7f, 0xFF3A2A22.toInt(),
                            0.09f, 0f, 0.25f, 1f, 0f, 0f)
                    }
                }
            } else if (f.kind == FxK.HEAL && f.t < f.dur - 0.5f) {
                f.emit -= dt
                if (f.emit <= 0f) {
                    f.emit = 0.06f
                    val a = rf() * TAU
                    val r = 0.3f + rf() * 0.5f
                    pt(5, player.fx + 0.5f + cos(a) * r, player.fy + 0.88f + sin(a) * r * 0.5f, (rf() - 0.5f) * 0.2f,
                        -0.7f - rf() * 0.7f, 1.0f, f.color, 0.045f, 0f, 0f, 0f, 0f, 0f)
                }
            } else if (f.kind == FxK.ARCANE && f.t < f.dur - 0.8f) {
                f.emit -= dt
                if (f.emit <= 0f) {
                    f.emit = 0.04f
                    val a = rf() * TAU
                    val r = 0.4f + rf() * 1.1f
                    pt(5, player.fx + 0.5f + cos(a) * r, player.fy + 0.88f + sin(a) * r * 0.5f, 0f,
                        -0.9f - rf() * 1.0f, 1.2f, if (rf() < 0.5f) 0xFFFFD27A.toInt() else 0xFFB388FF.toInt(),
                        0.045f, 0f, 0f, 0f, 0f, 0f)
                }
            }
            if (f.t >= f.dur) fx.removeAt(i)
        }
        for (m in monsters) {
            if (!m.alive) {
                m.burnT = 0f
                m.shockT = 0f
                continue
            }
            if (m.burnT > 0f) {
                m.burnT -= dt
                if (rf() < dt * 16f) {
                    pt(4, m.fx + 0.5f + (rf() - 0.5f) * 0.3f, m.fy + 0.7f, (rf() - 0.5f) * 0.5f, -0.9f - rf() * 0.8f,
                        0.6f, 0xFFFFA43D.toInt(), 0.03f, 0f, 0f, 0.5f, 0f, 0f)
                }
                if (rf() < dt * 4f) {
                    pt(3, m.fx + 0.5f, m.fy + 0.4f, (rf() - 0.5f) * 0.3f, -0.6f, 1.0f, 0xFF2E2420.toInt(),
                        0.08f, 0f, 0.25f, 0.5f, 0f, 0f)
                }
            }
            if (m.shockT > 0f) m.shockT -= dt
        }
    }

    private fun updateEffects(dt: Float) {
        for (i in parts.size - 1 downTo 0) {
            val pt = parts[i]
            if (pt.delay > 0f) {
                pt.delay -= dt
                continue
            }
            if (pt.drag > 0f) {
                val k = 1f - pt.drag * dt
                val kk = if (k < 0f) 0f else k
                pt.vx *= kk
                pt.vy *= kk
            }
            pt.rot += pt.vr * dt
            pt.x += pt.vx * dt
            pt.y += pt.vy * dt
            pt.vy += pt.gy * dt
            pt.life -= dt
            if (pt.life <= 0f) parts.removeAt(i)
        }
        for (i in texts.size - 1 downTo 0) {
            val t = texts[i]
            t.y -= dt * 0.8f
            t.life -= dt
            if (t.life <= 0f) texts.removeAt(i)
        }
        for (i in corpses.size - 1 downTo 0) {
            val c = corpses[i]
            c.life -= dt
            if (c.life <= 0f) corpses.removeAt(i)
        }
        for (i in log.size - 1 downTo 0) {
            log[i].life -= dt
        }
    }
}
