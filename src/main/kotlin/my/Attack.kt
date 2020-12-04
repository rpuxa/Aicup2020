package my

import model.AttackAction
import model.EntityAction
import model.EntityType
import model.Vec2Int
import my.actions.MyAttackAction
import my.strategies.SingleActionStrategy
import my.units.FighterUnit
import kotlin.math.abs

class AttackUnit(
        val id: Int,
        val targetId: Int,
        val pos: Vec2Int,
        val damage: Int,
        val health: Int,
        val maxHealth: Int,
        val unit: FighterUnit?,
        val isMe: Boolean,
        val points: Int
) {


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AttackUnit

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id
    }

    fun pointsPerDamage(health: Int, damage: Int): Float {
        if (health <= 0) return 0f
        if (damage >= health) return deltaPoints(health, 0, maxHealth) * points
        return deltaPoints(health, health - damage, maxHealth) * points
    }

    companion object {
        fun pointByHealth(health: Int, maxHealth: Int): Float {
            if (health == 0) return 12f
            val fl = health.toFloat() / maxHealth
            return (1 - fl * fl) + 10f
        }

        fun deltaPoints(from: Int, to: Int, maxHealth: Int) = pointByHealth(to, maxHealth) - pointByHealth(from, maxHealth)
    }
}

private fun onEachRegion(
        myUnits: List<MyUnit>,
        enemies: List<MyUnit>,
        withFriendlyFire: Boolean,
        block: (graph: List<ArrayList<Int>>, units: List<AttackUnit>, unitsById: List<AttackUnit>) -> Unit
) {

    var currentId = 0
    val graph = ArrayList<ArrayList<Int>>(256)
    val units = ArrayList<AttackUnit>(256)
    val targetToId = HashMap<Int, Int>()

    fun nextId(): Int {
        graph.add(ArrayList())
        return currentId++
    }

    fun addUnit(fighter: MyUnit, points: Int): Int {
        targetToId[fighter.id]?.let { return it }
        val id = nextId()
        units.add(AttackUnit(
                id,
                fighter.id,
                fighter.pos,
                fighter.damage,
                fighter.health,
                fighter.maxHealth,
                fighter as? FighterUnit,
                fighter.our,
                points,
        ))
        targetToId[fighter.id] = id
        return id
    }

    myUnits.forEach { myUnit ->
        if (myUnit.range == 0) return@forEach
        loop@ for (enemy in enemies) {
            for (dx in 0 until enemy.size)
                for (dy in 0 until enemy.size) {
                    val x = enemy.pos.x + dx
                    val y = enemy.pos.y + dy
                    val dist = abs(myUnit.pos.x - x) + abs(myUnit.pos.y - y)
                    var fighterId = -1
                    var enemyId = -1
                    when {
                        dist <= myUnit.range && dist <= enemy.range -> {
                            fighterId = addUnit(myUnit, -2)
                            enemyId = addUnit(enemy, 2)
                        }
                        dist <= myUnit.range -> {
                            fighterId = addUnit(myUnit, -2)
                            enemyId = addUnit(enemy, 1)
                        }
                        dist <= enemy.range -> {
                            fighterId = addUnit(myUnit, -1)
                            enemyId = addUnit(enemy, 2)
                        }
                    }
                    if (dist <= myUnit.range) {
                        graph[fighterId].add(enemyId)
                    }
                    if (dist <= enemy.range) {
                        graph[enemyId].add(fighterId)


                        if (withFriendlyFire) {
                            myUnits.forEach { otherFighter ->
                                if (myUnit.id != otherFighter.id) {
                                    if (abs(myUnit.pos.x - otherFighter.pos.x) + abs(myUnit.pos.y - otherFighter.pos.y) <= otherFighter.range) {
                                        graph[addUnit(otherFighter, -1)].add(fighterId)
                                    }
                                }
                            }
                        }
                    }
                    if (fighterId != -1) {
                        continue@loop
                    }
                }
        }
    }

    val doubleGraph = Array(graph.size) {
        ArrayList<Int>()
    }
    graph.forEachIndexed { from, children ->
        children.forEach { to ->
            doubleGraph[from].add(to)
            doubleGraph[to].add(from)
        }
    }
    val set = HashSet<AttackUnit>()
    val used = BooleanArray(graph.size)
    while (true) {
        val start = used.indexOfFirst { !it }
        if (start == -1) break
        set.clear()
        queue.clear()

        queue.push(start)
        used[start] = true
        set.add(units[start])
        while (!queue.isEmpty()) {
            val node = queue.pull()
            doubleGraph[node].forEach {
                if (!used[it]) {
                    used[it] = true
                    set.add(units[it])
                    queue.push(it)
                }
            }
        }
        block(graph, set.sortedBy { -allEnemiesField[it.pos.toI()] }, units)
    }
}

fun processAttack() {
    onEachRegion(
            fighters.values.toList(),
            enemies,
            true
    ) { graph, units, unitsById ->
        attack(units, graph, unitsById)
    }
}

fun predictBadMoves(
        fighters: List<MyUnit>,
        enemies: List<MyUnit>
): Set<Int> {
    val result = HashSet<AttackUnit>()
    onEachRegion(
            fighters,
            enemies,
            false
    ) { graph, units, unitsById ->
        val enemy = enemyAttack(filterEnemyAttack(units, graph, unitsById)).first
        val myAttack = filterMyAttack(units, graph, unitsById)
        val my = myAttack(myAttack).first
        if (enemy + my < 0) {
            result += myAttack.keys
        }
    }
    return result.mapTo(HashSet()) { it.targetId }
}

fun turretAiming() {
    val range = EntityType.TURRET.properties.attack!!.attackRange
    view.entities.forEach { turret ->
        if (turret.playerId == me.id && turret.entityType == EntityType.TURRET) {
            for (enemy in enemies) {
                baseCells(turret.position, turret.properties.size) { x, y ->
                    if (abs(x - enemy.pos.x) + abs(y - enemy.pos.y) <= range) {
                        actions[turret.id] = EntityAction().apply {
                            attackAction = AttackAction(enemy.id, null)
                        }
                        return
                    }
                }
            }
        }
    }
}

private fun attack(fighter: FighterUnit, enemy: Int) {
    fighter.strategyIfMorePriorityAndOtherType(SingleActionStrategy(MyAttackAction(enemy)))
}

private fun attack(
        units: List<AttackUnit>,
        graph: List<ArrayList<Int>>,
        unitsById: List<AttackUnit>
) {
    val killed = whoWasKilledByEnemy(units, graph, unitsById)
    val myAttack = filterMyAttack(units, graph, unitsById)
    val myAttackResult = myAttack(myAttack).second

    val friendlyAttack = LinkedHashMap<AttackUnit, ArrayList<AttackUnit>>()
    units.forEach { from ->
        if (from in myAttackResult || from in killed) return@forEach

        val arrayList = graph[from.id]
        if (arrayList.isNotEmpty()) {
            val to = arrayList.mapTo(ArrayList()) { unitsById[it] }
            if (from.isMe) {
                to.filterTo(ArrayList()) { it in killed }.let {
                    if (it.isNotEmpty()) {
                        friendlyAttack[from] = it
                    }
                }
            }
        }
    }

    val friendlyAttackResult = friendlyFire(friendlyAttack, killed)

    (myAttackResult + friendlyAttackResult).forEach { (from, to) ->
        attack(from.unit ?: error("Где юнит?"), to.targetId)
    }
}

private fun filterMyAttack(units: List<AttackUnit>, graph: List<ArrayList<Int>>, unitsById: List<AttackUnit>): LinkedHashMap<AttackUnit, ArrayList<AttackUnit>> {
    val myAttack = LinkedHashMap<AttackUnit, ArrayList<AttackUnit>>()
    units.forEach { from ->
        val arrayList = graph[from.id]
        if (arrayList.isNotEmpty()) {
            val to = arrayList.mapTo(ArrayList()) { unitsById[it] }
            if (from.isMe) {
                to.filterTo(ArrayList()) { !it.isMe }.let {
                    if (it.isNotEmpty()) {
                        myAttack[from] = it
                    }
                }
            }
        }
    }
    return myAttack
}

private fun filterEnemyAttack(units: List<AttackUnit>, graph: List<ArrayList<Int>>, unitsById: List<AttackUnit>): Map<AttackUnit, ArrayList<AttackUnit>> {
    val enemyAttack = HashMap<AttackUnit, ArrayList<AttackUnit>>()
    units.forEach { from ->
        val arrayList = graph[from.id]
        if (arrayList.isNotEmpty()) {
            val to = arrayList.mapTo(ArrayList()) { unitsById[it] }
            if (!from.isMe) {
                enemyAttack[from] = to
            }
        }
    }
    return enemyAttack
}

private fun whoWasKilledByEnemy(units: List<AttackUnit>, graph: List<ArrayList<Int>>, unitsById: List<AttackUnit>): Set<AttackUnit> {
    return enemyAttack(filterEnemyAttack(units, graph, unitsById)).second
}

private fun chooseWithNullMove(attack: Map<AttackUnit, ArrayList<AttackUnit>>, mask: Int): Map<AttackUnit, AttackUnit?> {
    var mask = mask
    val chosen = HashMap<AttackUnit, AttackUnit?>()
    attack.forEach { (fighter, enemies) ->
        val i = mask % (enemies.size + 1)
        chosen[fighter] = if (i == enemies.size) null else enemies[i]
        mask /= enemies.size + 1
    }
    return chosen
}

private fun chooseWithoutNullMove(attack: Map<AttackUnit, ArrayList<AttackUnit>>, mask: Int): Map<AttackUnit, AttackUnit> {
    var mask = mask
    val chosen = HashMap<AttackUnit, AttackUnit>()
    attack.forEach { (fighter, enemies) ->
        chosen[fighter] = enemies[mask % enemies.size]
        mask /= enemies.size
    }
    return chosen
}

private fun enemyAttack(enemyAttack: Map<AttackUnit, ArrayList<AttackUnit>>): Pair<Float, Set<AttackUnit>> {
    if (enemyAttack.isEmpty()) return 0f to emptySet()
    var enemyScore = 0f
    var killed: Set<AttackUnit>? = null
    var enemySize = 1
    enemyAttack.values.forEach {
        enemySize *= it.size
    }
    println("Killed size: $enemySize")
    if (enemySize > 70_000) return 0f to emptySet()

    repeat(enemySize) { mask ->
        val chosen = chooseWithoutNullMove(enemyAttack, mask)
        val health = HashMap<AttackUnit, Int>()
        chosen.values.forEach {
            health[it] = it.health
        }
        var s = 0f
        chosen.forEach { (from, to) ->
            val h = health[to]!!
            health[to] = h - from.damage
            s += to.pointsPerDamage(h, to.damage)
        }
        if (s < enemyScore) {
            killed = null
            enemyScore = s
        }
        if (s == enemyScore) {
            if (killed == null || killed!!.isNotEmpty()) {
                val new = HashSet<AttackUnit>()
                health.forEach { (unit, health) ->
                    if (health <= 0) {
                        if (killed == null || unit in killed!!) {
                            new.add(unit)
                        }
                    }
                }
                killed = new
            }
        }
    }
    return killed?.let {
        enemyScore to it
    } ?: 0f to emptySet()
}

private fun myAttack(myAttack: HashMap<AttackUnit, ArrayList<AttackUnit>>): Pair<Float, Map<AttackUnit, AttackUnit>> {
    if (myAttack.isEmpty()) return 0f to emptyMap()

    var score = -1f
    var movesCount = 0
    var result: Map<AttackUnit, AttackUnit?> = emptyMap()
    var size = 1
    var withNull = true
    var limit = 33_000
    myAttack.values.forEach {
        size *= it.size + 1
    }
    if (size > 33_000) {
        withNull = false
        myAttack.values.forEach {
            size *= it.size
        }
    }
    println("My attack size: $size")

    fun evaluate(chosen: Map<AttackUnit, AttackUnit?>): Float {
        val health = HashMap<AttackUnit, Int>()
        chosen.values.forEach {
            if (it != null) {
                health[it] = it.health
            }
        }
        var s = 0f
        chosen.forEach { (from, to) ->
            if (to == null) return@forEach
            val h = health[to]!!
            health[to] = h - from.damage
            s += to.pointsPerDamage(h, from.damage)
        }
        return s
    }

    for (mask in 0 until size) {
        val chosen = if (withNull) chooseWithNullMove(myAttack, mask) else chooseWithoutNullMove(myAttack, mask)
        val s = evaluate(chosen)
        val moves = chosen.values.count { it != null }
        if (s > score || s == score && moves < movesCount) {
            score = s
            movesCount = moves
            result = chosen
        }
        if (limit-- == 0) break
    }

    if (limit == 0) {
        val optimised = optimisedAttack(myAttack)
        val evaluate = evaluate(optimised)
        if (evaluate > score) {
            return evaluate to optimised
        }
    }

    val filtered = HashMap<AttackUnit, AttackUnit>()
    result.forEach { (k, v) ->
        if (v != null) {
            filtered[k] = v
        }
    }
    return score to filtered
}

private fun friendlyFire(
        friendlyAttack: HashMap<AttackUnit, ArrayList<AttackUnit>>,
        killed: Set<AttackUnit>
): Map<AttackUnit, AttackUnit> {
    if (friendlyAttack.isEmpty() || killed.isEmpty()) return emptyMap()

    var score = 0
    var movesCount = 0
    var result: Map<AttackUnit, AttackUnit?> = emptyMap()
    var size = 1
    var limit = 33_000

    friendlyAttack.values.forEach {
        size *= it.size + 1
    }

    println("Friendly size: $size")

    fun evaluate(chosen: Map<AttackUnit, AttackUnit?>): Int {
        val health = HashMap<AttackUnit, Int?>()
        killed.forEach {
            health[it] = it.health
        }
        var s = 0
        chosen.entries.forEach { (from, to) ->
            if (to == null) return@forEach

            val toHealth = health[to]!!
            health[to] = toHealth - from.damage
            if (toHealth > 0 && from.damage >= toHealth) {
                s++
            }
        }
        return s
    }

    for (mask in (0 until size).reversed()) {
        val chosen = chooseWithNullMove(friendlyAttack, mask)
        val s = evaluate(chosen)
        val moves = chosen.values.count { it != null }
        if (s > score || s == score && moves < movesCount) {
            score = s
            movesCount = moves
            result = chosen
        }
        if (limit-- == 0 || score >= killed.size) break
    }

    if (limit == 0) {
        val optimised = optimisedAttack(friendlyAttack)
        if (evaluate(optimised) > score) {
            return optimised
        }
    }

    val filtered = HashMap<AttackUnit, AttackUnit>()
    result.forEach { (k, v) ->
        if (v != null) {
            filtered[k] = v
        }
    }
    return filtered
}

private fun optimisedAttack(
        attack: HashMap<AttackUnit, ArrayList<AttackUnit>>
): Map<AttackUnit, AttackUnit> {
    println("OPTIMISED")
    val result = HashMap<AttackUnit, AttackUnit>()
    val healths = HashMap<AttackUnit, Int>()
    attack.values.forEach {
        it.forEach {
            healths[it] = it.health
        }
    }
    attack.entries.sortedBy {
        it.value.size
    }.forEach { (unit, enemies) ->
        val health = healths[enemies.first()]
        val target = if (enemies.all { healths[it] == health }) {
            if (health == 0) return@forEach
            enemies.minByOrNull { abs(it.pos.x - unit.pos.x) + abs(it.pos.y - unit.pos.y) }!!
        } else {
            enemies.minByOrNull { if (healths[it]!! == 0) 1_000_000 else healths[it]!! }!!
        }
        result[unit] = target
        healths[target] = (healths[target]!! - unit.damage).coerceAtLeast(0)
    }
    return result
}