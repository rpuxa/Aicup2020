package my

import model.EntityType
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.roundToInt

fun afraid() {
    queue.clear()
    tmpField.clear()
    val range = EntityType.RANGED_UNIT.properties.attack!!.attackRange
    enemies.forEach {
        if (it.range == range && it.strength > 0) {
            val i = it.pos.toI()
            queue.push(i)
            tmpField[i] = range + 3
        }
    }
    bfs(wholeGraph, queue, tmpField)
    repeat(size) {
        afraidWorkersField[it] = tmpField[it] > 0
        tmpField[it] = 0
    }

    queue.clear()

    for (it in enemies) {
        if (it.range == 1 && it.strength > 0) {
            val i = it.pos.toI()
            queue.push(i)
            tmpField[i] = 4
        }
    }
    bfs(wholeGraph, queue, tmpField)
    repeat(size) {
        afraidWorkersField[it] = afraidWorkersField[it] || tmpField[it] > 0
    }
}

fun attraction() {
    //<editor-fold desc="workersToNotHealedUnitsAttractionField AND notHealedEntitiesIds">
    notHealedEntitiesIds.clearM1()
    queue.clear()
    workersToNotHealedUnitsAttractionField.clear()
    entities.values.forEach { entity ->
        if (entity.playerId == me.id) {
            if (entity.isDamaged && entity.entityType != EntityType.BUILDER_UNIT) {
                baseCells(entity.position, entity.properties.size) { x, y ->
                    val i = toI(x, y)
                    queue.push(i)
                    workersToNotHealedUnitsAttractionField[i] = 3
                }
            }
            if (!entity.isHealed) {
                baseCells(entity.position, entity.properties.size) { x, y ->
                    notHealedEntitiesIds[toI(x, y)] = entity.id
                }
            }
        }
    }
    bfs(reachableGraph, queue, workersToNotHealedUnitsAttractionField)
    repeat(size) {
        workersToNotHealedUnitsAttractionField[it] *= 20
    }
    //</editor-fold>
    //<editor-fold desc="damagedUnitsToWorkersAttractionField">
    damagedUnitsToWorkersAttractionField.clear()
    queue.clear()
    repeat(size) {
        if (farmers[it]) {
            queue.push(it)
            damagedUnitsToWorkersAttractionField[it] = 160
        }
    }
    bfs(reachableGraph, queue, damagedUnitsToWorkersAttractionField)
    //</editor-fold>
    //<editor-fold desc="notHealedUnitsToWorkersAttractionField">
    notHealedUnitsToWorkersAttractionField.clear()
    queue.clear()
    repeat(size) {
        if (farmers[it]) {
            queue.push(it)
            notHealedUnitsToWorkersAttractionField[it] = 5
        }
    }
    bfs(reachableGraph, queue, notHealedUnitsToWorkersAttractionField)
    //</editor-fold>
    //<editor-fold desc="damagedFighterField AND notHealedFighterField">
    repeat(size) {
        val i = (if (occupied[it]) -100_000_000 else 0) + defenceField[it] + buildPlanField[it]
        damagedFighterField[it] = i + damagedUnitsToWorkersAttractionField[it]
        notHealedFighterField[it] = i + notHealedUnitsToWorkersAttractionField[it]
    }
    //</editor-fold>
}

fun mainCalculations() {
    //<editor-fold desc="Coeff">
    val playerStats = fightersByPlayer.map { (id, fighters) ->
        var totalStrength = 0
        fighters.forEach { fighter ->
            val strength = strength(fighters, fighter)
            if (id == me.id) {
                my.fighters[fighter.id]!!.strength = strength
            }
            for (enemy in enemies) {
                if (fighter.id == enemy.id) {
                    enemy.strength = strength
                    break
                }
            }
            totalStrength += strength
        }
        id to totalStrength
    }.toMap()

    val mine = playerStats[me.id]!!
    val coeff = playerStats.mapNotNull { (key, stat) ->
        if (me.id == key) null else {
            key to mine - stat
        }
    }.toMap().toMutableMap()
    /* run {
     if (coeff.values.all { it > 10000 }) {
         val player = enemyByPlayer.entries.find { it.value.isNotEmpty() }?.key ?: return@run
         coeff.entries.forEach {
             if (it.key != player)
                 coeff[it.key] = coeff[it.key]!! / 10
         }
     }
 }*/
    debug {
        coeff.map { (id, c) ->
            text("Коеффициент$id: $c")
        }
    }
    //</editor-fold>
    //<editor-fold desc="Enemy influence field">
    enemyInfluenceField.clear()
    coeff.forEach { (id, value) ->
        queue.clear()
        tmpField.clear()
        enemyByPlayer[id]!!.forEach {
            val i = it.pos.toI()
            queue.push(i)
            tmpField[i] = 160
        }
        bfs(reachableGraph, queue, tmpField)
        repeat(size) {
            val i = tmpField[it].toDouble()
            enemyInfluenceField[it] -= (ln(161 - i) * value * 10).toInt()
        }
    }
    repeat(size) {
        val i = max(it % side, it / side)
        if (i <= 20) {
            enemyInfluenceField[it] = enemyInfluenceField[it].coerceAtMost(0)
        }
    }
    //</editor-fold>
    //<editor-fold desc="Attractive field">
    if (view.currentTick % 2 == 0) {
        fighterAttractiveField.clear()
        val depth = when {
            fighters.size < 30 -> 22
            fighters.size < 40 -> 19
            fighters.size < 50 -> 17
            fighters.size < 70 -> 13
            fighters.size < 90 -> 11
            fighters.size < 100 -> 6
            fighters.size < 110 -> 5
            else -> 0
        }
        if (depth > 2) {
            tmpDoubleField.clear()
            fighters.values.forEach { fighter ->
                queue.clear()
                tmpField.clear()

                bfs(reachableGraph, queue, fighter.pos.toI(), tmpField, depth)
                repeat(size) {
                    var force = tmpField[it].coerceAtMost(depth - 2).toDouble()
                    force = -ln(depth - 1 - force) * 200
                    tmpDoubleField[it] += force
                    if (tmpDoubleField[it] > 10000)
                        tmpDoubleField[it] = 10000.0
                }
            }
            repeat(size) {
                val x = tmpDoubleField[it]
                fighterAttractiveField[it] = x.roundToInt()
            }
        }
    }
    //</editor-fold>
    //<editor-fold desc="Building area">
    queue.clear()
    tmpField.clear()
    view.entities.forEach {
        if (it.playerId == me.id && !it.properties.canMove) {
            baseCells(it.position, it.properties.size) { x, y ->
                val i = toI(x, y)
                tmpField[i] = 20
                queue.push(i)
            }
        }
    }
    bfs(reachableGraph, queue, tmpField)
    repeat(size) {
        buildingsArea[it] = tmpField[it] > 0
    }
    //</editor-fold>
    //<editor-fold desc="Defence field">
    defence = false
    queue.clear()
    defenceField.clear()
    enemies.forEach {
        val i = it.pos.toI()
        if (buildingsArea[i]) {
            queue.push(i)
            defenceField[i] = 160
            defence = true
        }
    }
    if (defence) {
        debug {
            text("DEFENCE!")
        }
        bfs(reachableGraph, queue, defenceField)
        repeat(size) {
            val value = defenceField[it].toDouble()
            defenceField[it] = -(ln(161 - value) * 30_000 * 10).toInt()
        }
    }
    //</editor-fold>
    //<editor-fold desc="Fighter field">
    repeat(size) {
        fighterField[it] =
                (if (occupied[it]) -100_000_000 else 0) + enemyInfluenceField[it] + fighterAttractiveField[it] + defenceField[it] + buildPlanField[it]
    }
    //</editor-fold>
}