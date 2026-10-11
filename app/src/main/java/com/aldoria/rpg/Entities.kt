package com.aldoria.rpg

open class Creature(var name: String) {
    var x = 0
    var y = 0
    var fx = 0f
    var fy = 0f
    var dir = 2
    var hp = 1
    var maxHp = 1
    var moving = false
    var sx = 0f
    var sy = 0f
    var prog = 0f
    var carry = 0f
    var idleT = 0f
    var speed = 3f
    var flash = 0f
    var walk = 0f

    fun place(nx: Int, ny: Int) {
        x = nx
        y = ny
        fx = nx.toFloat()
        fy = ny.toFloat()
        moving = false
        prog = 0f
        carry = 0f
    }

    fun startMove(nx: Int, ny: Int) {
        sx = fx
        sy = fy
        if (nx > x) dir = 1 else if (nx < x) dir = 3 else if (ny > y) dir = 2 else if (ny < y) dir = 0
        x = nx
        y = ny
        moving = true
        prog = if (carry > 0.4f) 0.4f else carry
        carry = 0f
        idleT = 0f
    }

    fun updateMove(dt: Float) {
        if (flash > 0f) flash -= dt
        if (!moving) {
            idleT += dt
            if (idleT > 0.06f) carry = 0f
            return
        }
        prog += dt * speed
        walk += dt * speed
        if (prog >= 1f) {
            carry = prog - 1f
            prog = 1f
            fx = x.toFloat()
            fy = y.toFloat()
            moving = false
            idleT = 0f
        } else {
            fx = sx + (x - sx) * prog
            fy = sy + (y - sy) * prog
        }
    }
}

class Player : Creature("Mago") {
    var level = 1
    var exp = 0L
    var mana = 35
    var maxMana = 35
    var ml = 0
    var manaSpent = 0L
    var hasteT = 0f
    var shieldT = 0f
    var regenHpT = 0f
    var regenManaT = 0f
}

class MonsterType(
    val id: String,
    val name: String,
    val hp: Int,
    val exp: Int,
    val speed: Float,
    val dmgMin: Int,
    val dmgMax: Int,
    val atkInterval: Float,
    val goldMax: Int,
    val cheeseChance: Float
)

object Monsters {
    val RAT = MonsterType("rat", "Rato", 20, 5, 2.4f, 1, 6, 1.8f, 4, 0.30f)
}

class Monster(val type: MonsterType, val spawnX: Int, val spawnY: Int) : Creature(type.name) {
    var alive = true
    var aggro = false
    var attackCd = 0f
    var thinkCd = 0f
    var wanderCd = 1f
    var deadT = 0f
    var burnT = 0f
    var shockT = 0f

    init {
        hp = type.hp
        maxHp = type.hp
        speed = type.speed
    }
}

class Corpse(val x: Int, val y: Int, val typeName: String, var life: Float, val loot: LinkedHashMap<String, Int>) {
    var looted = false
}

class FloatText(var x: Float, var y: Float, val text: String, val color: Int, var life: Float, val size: Float) {
    val maxLife = life
}

/**
 * Partícula: posição e tamanho em "tiles". kind 0 = círculo, 1 = quadrado, 2 = brilho em cruz,
 * 3 = fumaça, 4 = brasa (risco), 5 = brilho cintilante, 7 = runa, 8 = rastro de vento, 9 = caco hexagonal.
 */
class Particle(
    var x: Float, var y: Float, var vx: Float, var vy: Float,
    var life: Float, val color: Int, val size: Float, val kind: Int
) {
    val maxLife = life
    var gy = 0f
    var delay = 0f
    /** Crescimento do tamanho por segundo (fumaça). */
    var grow = 0f
    /** Rotação (rad) e velocidade de rotação (rad/s). */
    var rot = 0f
    var vr = 0f
    /** Atrito: fração da velocidade perdida por segundo. */
    var drag = 0f
}

class Projectile(val fromX: Float, val fromY: Float, val target: Monster, val dmg: Int, val color: Int, val dur: Float) {
    var t = 0f
}

class Spell(
    val name: String,
    val words: String,
    val level: Int,
    val mana: Int,
    val cd: Float,
    val kind: Int,
    val color: Int,
    val desc: String
)

object Spells {
    const val HEAL = 0
    const val FIRE = 1
    const val ENERGY = 2
    const val WAVE = 3
    const val HASTE = 4
    const val SHIELD = 5

    val list: Array<Spell> = arrayOf(
        Spell("Cura Leve", "Exura", 1, 20, 1.0f, HEAL, 0xFF7CFF8A.toInt(), "Recupera vida."),
        Spell("Golpe de Chama", "Exori flam", 1, 20, 2.0f, FIRE, 0xFFFF8A3D.toInt(), "Dano de fogo no alvo (até 3 quadrados)."),
        Spell("Golpe Elétrico", "Exori vis", 4, 20, 2.0f, ENERGY, 0xFFB388FF.toInt(), "Dano de energia no alvo (até 3 quadrados)."),
        Spell("Onda de Fogo", "Exevo flam hur", 6, 45, 4.0f, WAVE, 0xFFFF5A2D.toInt(), "Onda de fogo em cone à sua frente."),
        Spell("Velocidade", "Utani hur", 3, 40, 8.0f, HASTE, 0xFF7CFF8A.toInt(), "Você corre mais rápido por 30 segundos."),
        Spell("Escudo Mágico", "Utamo vita", 8, 50, 14.0f, SHIELD, 0xFF6FB6FF.toInt(), "Dano é tirado da mana por 20 segundos.")
    )
}

object Items {
    fun name(id: String, n: Int): String = when (id) {
        "gold" -> if (n == 1) "moeda de ouro" else "moedas de ouro"
        "cheese" -> if (n == 1) "queijo" else "queijos"
        else -> id
    }

    fun title(id: String): String = when (id) {
        "gold" -> "Moedas de ouro"
        "cheese" -> "Queijo"
        else -> id
    }

    fun desc(id: String): String = when (id) {
        "gold" -> "A moeda de Aldoria. Um dia vai servir para comprar coisas."
        "cheese" -> "Um queijo meio fedido. Ao comer, recupera um pouco de vida."
        else -> ""
    }

    fun usable(id: String): Boolean = id == "cheese"
}
