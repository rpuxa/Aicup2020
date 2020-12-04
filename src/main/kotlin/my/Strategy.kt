@file:JvmName("StrategyKt")

package my

import DebugInterface
import model.*
import my.strategies.*
import my.units.FighterUnit
import my.units.MovableUnit
import my.units.WorkerUnit
import kotlin.math.*
import kotlin.time.ExperimentalTime


var init = false
lateinit var view: PlayerView
var debug: DebugInterface? = null
lateinit var me: Player
var size: Int = -1
var side: Int = -1
var defence = false

val entities = HashMap<Int, Entity>()
val movableUnits = HashMap<Int, MovableUnit>()
val workers = HashMap<Int, WorkerUnit>()
val fighters = HashMap<Int, FighterUnit>()
val fightersByPlayer = HashMap<Int, ArrayList<Entity>>()
val enemyByPlayer = HashMap<Int, ArrayList<Enemy>>()
val workerBases = HashSet<Entity>()
val rangeBases = HashSet<Entity>()
val meleeBases = HashSet<Entity>()
val resources = HashMap<Int, Entity>()
val enemies = ArrayList<Enemy>()
val actions = HashMap<Int, EntityAction>()
var population = -1
var maxPopulation = -1

var houseCost = 0
var builderCost = 0
var rangedCost = 0
var meleeCost = 0

lateinit var tmpDoubleField: DoubleArray
lateinit var tmpField: IntArray
lateinit var resourceField: IntArray
lateinit var buildPlanField: IntArray
lateinit var farmersAndRepairField: IntArray
lateinit var fighterField: IntArray
lateinit var enemyInfluenceField: IntArray
lateinit var fighterAttractiveField: IntArray
lateinit var defenceField: IntArray
lateinit var allEnemiesField: IntArray
lateinit var myUnitsField: IntArray
lateinit var buildingIds: IntArray
lateinit var repairWorkersField: IntArray
lateinit var repairUnitsField: IntArray
lateinit var notHealedEntitiesIds: IntArray
lateinit var damagedFighterField: IntArray


lateinit var used: BooleanArray
lateinit var unreachable: BooleanArray
lateinit var occupied: BooleanArray
lateinit var farmers: BooleanArray
lateinit var inappropriateForBuilding: BooleanArray
lateinit var isResource: BooleanArray
lateinit var fightersBoolArray: BooleanArray
lateinit var buildingsArea: BooleanArray
lateinit var afraidWorkersField: BooleanArray
lateinit var finishedbuilding: BooleanArray


lateinit var queue: MyQueue
lateinit var notOccupiedGraph: Array<Neighbours>
lateinit var reachableGraph: Array<Neighbours>
lateinit var wholeGraph: Array<Neighbours>
lateinit var sortedBuildPoints: IntArray
lateinit var properties: Map<EntityType, EntityProperties>

private fun strategy() {
    debug {
        text("Workers: ${workers.size}")
        text("Fighters: ${fighters.size}")
        text("Builders: ${workers.values.count { it.buildingPlan != null }}")
        text("Builders projects: ${buildingPlans.size}")
        //field(afraidField)
    }
    val plannedResources = me.resource - buildingPlans.sumBy {
        if (it.type == EntityType.HOUSE) {
            houseCost
        } else {
            error("Не тот тип")
        }
    }
    var plannedPopulation = population
    val plannedMaxPopulation = maxPopulation + buildingPlans.sumBy { it.type.properties.populationProvide }
    debug {
        text("Planned capacity: $plannedMaxPopulation")
    }

    val buildTypes = buildTypes()

    fun get(type: EntityType) = buildTypes[type] ?: 0

    fun base(bases: Set<Entity>, type: EntityType, cost: Int) {
        bases.forEach { base ->
            if (plannedPopulation < maxPopulation) {
                if (plannedResources >= cost && get(type) > 0 && base.buildUnit(type)) {
                    plannedPopulation++
                    buildTypes[type] = get(type) - 1
                } else {
                    base.cancelAction()
                }
            }
        }
    }
    base(workerBases, EntityType.BUILDER_UNIT, builderCost)
    base(rangeBases, EntityType.RANGED_UNIT, rangedCost)
    base(meleeBases, EntityType.MELEE_UNIT, meleeCost)

    if (plannedPopulation + 5 >= plannedMaxPopulation && plannedResources >= houseCost) {
        build(EntityType.HOUSE)
    }

    workers.values.forEach {
        if (afraidWorkersField[it.pos.toI()]) {
            it.strategyIfMorePriorityAndOtherType(RunAwayStrategy())
        }
    }

    workers.values.forEach {
        val repair = RepairStrategy.repair(it.pos.toI())
        if (repair != -1) {
            it.strategyIfMorePriorityAndOtherType(RepairStrategy(repair))
        }
    }

    workers.values.forEach {
        it.strategyIfMorePriorityAndOtherType(FarmingAndRepairStrategy())
    }

    fighters.values.forEach { fighter ->
        fighter.strategyIfMorePriorityAndOtherType(FighterMoveStrategy())
    }

    turretAiming()
    val start = System.nanoTime()
    processAttack()
    println("Attack time: ${(System.nanoTime() - start) / 10_000_000.0}")
    processBuilding()
}

fun buildTypes(): MutableMap<EntityType, Int> {
    val result = HashMap<EntityType, Int>()

    fun add(type: EntityType) {
        result[type] = (result[type] ?: 0) + 1
    }

    fun addFighterUnit() {
        if (rangedCost / 2.0 > meleeCost) {
            add(EntityType.MELEE_UNIT)
        } else {
            add(EntityType.RANGED_UNIT)
        }
    }

    fun addWorker() = add(EntityType.BUILDER_UNIT)

    run r@{
        if (defence) {
            addFighterUnit()
            return@r
        }
        if (workers.size < 10) {
            addWorker()
            return@r
        }
        if (fighters.size < 5) {
            addFighterUnit()
            return@r
        }
        val max = fightersByPlayer.maxOf { it.value.size }
        if (fighters.size < .7 * max) {
            addWorker()
            return@r
        }
        if (workers.size < 15) {
            addWorker()
            return@r
        }
        if (fighters.size < .8 * max) {
            addFighterUnit()
            return@r
        }
        if (workers.size < 20) {
            addWorker()
            return@r
        }
        if (fighters.size < max) {
            addFighterUnit()
            return@r
        }
        if (workers.size < 25) {
            addWorker()
            return@r
        }
        if (fighters.size < 1.2 * max) {
            addFighterUnit()
            return@r
        }
        if (workers.size < 30) {
            addWorker()
            return@r
        }
        if (fighters.size < 1.4 * max) {
            addFighterUnit()
            return@r
        }
        if (workers.size < 40) {
            addWorker()
            return@r
        }
        if (fighters.size < 1.7 * max) {
            addFighterUnit()
            return@r
        }
    }
    return result
}

private fun Entity.buildUnit(type: EntityType): Boolean {
    var empty = -1
    baseNeighbours(position, properties.size) {
        if (!occupied[it]) {
            empty = it
        }
    }
    if (empty == -1) return false
    actions[id] = EntityAction().apply {
        buildAction = BuildAction().also {
            it.entityType = type
            it.position = empty.toVec()
        }
    }
    return true
}

private fun Entity.cancelAction() {
    actions[id] = EntityAction()
}

@OptIn(ExperimentalTime::class)
fun init(v: PlayerView, d: DebugInterface?): Action {
    view = v
    debug = d
    me = v.players.first { it.id == v.myId }
    if (!init) {
        init = true
        size = view.mapSize * view.mapSize
        side = view.mapSize

        tmpDoubleField = DoubleArray(size)
        tmpField = IntArray(size)
        resourceField = IntArray(size)
        buildPlanField = IntArray(size)
        farmersAndRepairField = IntArray(size)
        fighterField = IntArray(size)
        enemyInfluenceField = IntArray(size)
        fighterAttractiveField = IntArray(size)
        defenceField = IntArray(size)
        allEnemiesField = IntArray(size)
        myUnitsField = IntArray(size)
        buildingIds = IntArray(size)
        repairWorkersField = IntArray(size)
        repairUnitsField = IntArray(size)
        notHealedEntitiesIds = IntArray(size)
        damagedFighterField = IntArray(size)


        used = BooleanArray(size)
        unreachable = BooleanArray(size)
        occupied = BooleanArray(size)
        farmers = BooleanArray(size)
        inappropriateForBuilding = BooleanArray(size)
        isResource = BooleanArray(size)
        fightersBoolArray = BooleanArray(size)
        buildingsArea = BooleanArray(size)
        afraidWorkersField = BooleanArray(size)
        finishedbuilding = BooleanArray(size)


        queue = MyQueue(size)
        notOccupiedGraph = Array(size) { Neighbours(0, 0, 0, 0, 0) }
        reachableGraph = Array(size) { Neighbours(0, 0, 0, 0, 0) }
        wholeGraph = Array(size) { Neighbours(0, 0, 0, 0, 0) }
        sortedBuildPoints = Array(size) { it }.apply {
            sortBy { max(it / side, it % side) }
        }.toIntArray()

        properties = v.entityProperties
    }

    enemyByPlayer.clear()
    enemies.clear()
    fightersByPlayer.clear()
    fighters.clear()
    entities.clear()
    movableUnits.clear()
    resources.clear()
    workerBases.clear()
    rangeBases.clear()
    meleeBases.clear()
    population = 0
    maxPopulation = 0
    view.players.forEach {
        fightersByPlayer[it.id] = ArrayList()
        enemyByPlayer[it.id] = ArrayList()
    }

    view.entities.forEach { entity ->
        //<editor-fold desc="Entities">
        entities[entity.id] = entity
        //</editor-fold>
        val prop = entity.properties
        if (entity.playerId == view.myId) {
            //<editor-fold desc="Workers">
            if (entity.entityType == EntityType.BUILDER_UNIT) {
                if (entity.id !in workers) {
                    workers[entity.id] = WorkerUnit(
                            entity.id,
                            entity.position,
                            entity.health,
                            entity.properties.maxHealth
                    )
                } else {
                    val workerUnit = workers[entity.id]!!
                    workerUnit.pos = entity.position
                    workerUnit.health = entity.health
                }
            }
            //</editor-fold>
            //<editor-fold desc="Fighters">
            if (entity.entityType == EntityType.MELEE_UNIT || entity.entityType == EntityType.RANGED_UNIT) {
                if (entity.id !in fighters) {
                    fighters[entity.id] = FighterUnit(
                            entity.id,
                            entity.position,
                            entity.health,
                            entity.properties.maxHealth,
                            entity.properties.attack!!.damage,
                            entity.properties.attack!!.attackRange,
                    )
                } else {
                    val fighterUnit = fighters[entity.id]!!
                    fighterUnit.pos = entity.position
                    fighterUnit.health = entity.health
                }
            }
            //</editor-fold>
            movableUnits += workers
            movableUnits += fighters
            //<editor-fold desc="Worker bases">
            if (entity.entityType == EntityType.BUILDER_BASE) {
                workerBases.add(entity)
            }
            //</editor-fold>
            //<editor-fold desc="Range bases">
            if (entity.entityType == EntityType.RANGED_BASE) {
                rangeBases.add(entity)
            }
            //</editor-fold>
            //<editor-fold desc="Melee bases">
            if (entity.entityType == EntityType.MELEE_BASE) {
                meleeBases.add(entity)
            }
            //</editor-fold>
            //<editor-fold desc="Population">
            population += prop.populationUse
            maxPopulation += prop.populationProvide
            //</editor-fold>
        } else {
            //<editor-fold desc="Enemy init">
            val attack = entity.properties.attack
            if (entity.playerId != null) {
                val enemy = Enemy(
                        entity.id,
                        entity.position,
                        entity.properties.size,
                        entity.health,
                        entity.properties.maxHealth,
                        attack?.damage ?: 0,
                        attack?.attackRange ?: 0
                )
                enemies += enemy
                enemyByPlayer[entity.playerId!!]!!.add(enemy)
            }
            //</editor-fold>
        }
        //<editor-fold desc="Fighters by player">
        if (entity.entityType == EntityType.MELEE_UNIT || entity.entityType == EntityType.RANGED_UNIT) {
            fightersByPlayer[entity.playerId]!!.add(entity)
        }
        //</editor-fold>
        //<editor-fold desc="Resources">
        if (entity.entityType == EntityType.RESOURCE) {
            resources[entity.position.toI()] = entity
        }
        //</editor-fold>

    }
    houseCost = EntityType.HOUSE.properties.initialCost
    builderCost = EntityType.BUILDER_UNIT.properties.initialCost + workers.size
    rangedCost = EntityType.RANGED_UNIT.properties.initialCost + fighters.count { it.value.range > 1 }
    meleeCost = EntityType.MELEE_UNIT.properties.initialCost + fighters.count { it.value.range == 1 }

    actions.clear()
    debugTime("Main") {
        //<editor-fold desc="Remove killed units">
        fighters.keys.mapNotNull {
            if (it !in entities) {
                it
            } else {
                null
            }
        }.forEach {
            fighters.remove(it)
        }
        workers.keys.mapNotNull {
            if (it !in entities) {
                it
            } else {
                null
            }
        }.forEach {
            workers.remove(it)
        }
        //</editor-fold>

        //<editor-fold desc="Fighters bool array">
        fightersBoolArray.clear()
        fighters.values.forEach {
            fightersBoolArray[it.pos.toI()] = true
        }
        //</editor-fold>
        //<editor-fold desc="Occupied">
        occupied.clear()
        entities.values.forEach {
            baseCells(it.position, it.properties.size) { x, y ->
                occupied[toI(x, y)] = true
            }
        }
        //</editor-fold>
        //<editor-fold desc="Unreachable">
        unreachable.clear()
        entities.values.forEach {
            if (!it.properties.canMove) {
                baseCells(it.position, it.properties.size) { x, y ->
                    unreachable[toI(x, y)] = true
                }
            }
        }
        //</editor-fold>
        //<editor-fold desc="Graph">
        val notOccupiedGraph1 = notOccupiedGraph
        val reachableGraph1 = reachableGraph
        val wholeGraph1 = wholeGraph
        val occupied = occupied
        val unreachable = unreachable
        val side = side
        repeat(size) { node ->
            val notOccupiedNeighbours = notOccupiedGraph1[node]
            val reachableNeighbours = reachableGraph1[node]
            val wholeNeighbours = wholeGraph1[node]
            notOccupiedNeighbours.clear()
            reachableNeighbours.clear()
            wholeNeighbours.clear()
            neighbours(node, side) {
                if (!occupied[it]) {
                    notOccupiedNeighbours.add(it)
                }
                if (!unreachable[it]) {
                    reachableNeighbours.add(it)
                }
                wholeNeighbours.add(it)
            }
        }

        //</editor-fold>
        //<editor-fold desc="Resource field">
        // if (view.currentTick % 2 == 0) {
        resourceField.clear()
        queue.clear()
        resources.values.forEach { entity ->
            val i = entity.position.toI()
            resourceField[i] = 100
            queue.push(i)
        }
        bfs(notOccupiedGraph, queue, resourceField)
        // }
        //</editor-fold>
        //<editor-fold desc="BuildPlan field">
        buildPlanField.clear()
        buildingPlans.forEach {
            buildPlanField.min(it.escapeField)
        }
        //</editor-fold>
        //<editor-fold desc="Finished building">
        finishedbuilding.clear()
        buildingIds.clearM1()
        entities.values.forEach { entity ->
            if (entity.entityType == EntityType.BUILDER_BASE ||
                    entity.entityType == EntityType.MELEE_BASE ||
                    entity.entityType == EntityType.RANGED_BASE ||
                    entity.entityType == EntityType.TURRET ||
                    entity.entityType == EntityType.HOUSE
            ) {
                baseCells(entity.position, entity.properties.size) { x, y ->
                    val i = toI(x, y)
                    buildingIds[i] = entity.id
                    if (entity.health == entity.properties.maxHealth) {
                        finishedbuilding[i] = true
                    }
                }
            }
        }
        //</editor-fold>
        //<editor-fold desc="Farmers">
        farmers.clear()
        workers.values.forEach {
            if (it.currentStrategy is FarmingAndRepairStrategy) {
                farmers[it.pos.toI()] = true
            }
        }
        //</editor-fold>
        //<editor-fold desc="Inappropriate for buildings">
        repeat(inappropriateForBuilding.size) {
            inappropriateForBuilding[it] = unreachable[it] || buildPlanField[it] != 0
        }
        inappropriateForBuilding[toI(11, 11)] = true
        inappropriateForBuilding[toI(12, 12)] = true
        inappropriateForBuilding[toI(13, 13)] = true
        inappropriateForBuilding[toI(14, 14)] = true
        inappropriateForBuilding[toI(15, 15)] = true
        //</editor-fold>
        //<editor-fold desc="Is resource">
        isResource.clear()
        resources.values.forEach {
            isResource[it.position.toI()] = true
        }
        //</editor-fold>

    }

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
    tmpField.clear()
    enemies.forEach {
        val i = it.pos.toI()
        if (buildingsArea[i]) {
            queue.push(i)
            tmpField[i] = 160
            defence = true
        }
    }
    debug {
        if (defence)
            text("DEFENCE!")
    }
    bfs(reachableGraph, queue, tmpField)
    repeat(size) {
        val value = tmpField[it].toDouble()
        defenceField[it] = -(ln(161 - value) * 30_000 * 10).toInt()
    }
    //</editor-fold>
    //<editor-fold desc="Fighter field">
    repeat(size) {
        fighterField[it] =
                (if (occupied[it]) -100_000_000 else 0) + enemyInfluenceField[it] + fighterAttractiveField[it] + defenceField[it] + buildPlanField[it]
    }
    //</editor-fold>
    //<editor-fold desc="Afraid field">
    afraid()
    //</editor-fold>

    notHealedEntitiesIds.clearM1()
    queue.clear()
    repairWorkersField.clear()
    entities.values.forEach { entity ->
        if (entity.isDamaged && entity.entityType != EntityType.BUILDER_UNIT) {
            baseNeighbours(entity.position, entity.properties.size) {
                queue.push(it)
                repairWorkersField[it] = 3
                notHealedEntitiesIds[it] = entity.id
            }
        }
    }
    bfs(notOccupiedGraph, queue, repairWorkersField)
    repeat(size) {
        repairWorkersField[it] *= 20
    }

    repairUnitsField.clear()
    queue.clear()
    repeat(size) {
        if (farmers[it]) {
            queue.push(it)
            repairUnitsField[it] = 160
        }
    }
    bfs(notOccupiedGraph, queue, repairUnitsField)

    repeat(size) {
        damagedFighterField[it] = (if (occupied[it]) -100_000_000 else 0) + defenceField[it] + buildPlanField[it] + repairUnitsField[it]
    }

    //<editor-fold desc="Farmers field">
    repeat(size) {
        farmersAndRepairField[it] = resourceField[it] + buildPlanField[it] + repairWorkersField[it]
    }
    //</editor-fold>

    allEnemiesField.clear()
    queue.clear()
    enemies.forEach {
        val i = it.pos.toI()
        allEnemiesField[i] = 200
        queue.push(i)
    }
    bfs(reachableGraph, queue, allEnemiesField)
    repeat(size) {
        if (isResource[it]) {
            allEnemiesField[it] = 1_000_000
        }
    }

    myUnitsField.clear()
    queue.clear()
    fighters.values.forEach {
        val i = it.pos.toI()
        myUnitsField[i] = 200
        queue.push(i)
    }
    bfs(reachableGraph, queue, myUnitsField)

    //<editor-fold desc="Return result">
    movableUnits.values.forEach {
        it.clearStrategy()
    }
    strategy()
    debugTime("Prediction time") {
        movePrediction()
    }
    val result = HashMap<Int, EntityAction>()
    movableUnits.values.forEach {
        result[it.id] = it.runStrategy()
    }
    result += actions
    return Action(result)
    //</editor-fold>
}

fun strength(fighters: List<Entity>, fighter: Entity): Int {
    val melee = fighter.entityType == EntityType.MELEE_UNIT
    var strength = 50
    fighters.forEach {
        if (it.id != fighter.id) {
            val dist = abs(fighter.position.x - it.position.x) + abs(fighter.position.y - it.position.y)
            if (dist < 10) {
                strength += dist
            }
        }
    }
    return strength
}

fun movePrediction() {
    class MyPredictUnit(val fighter: FighterUnit, override val pos: Vec2Int) : MyUnit by fighter
    class EnemyPredictUnit(val enemy: Enemy, override val pos: Vec2Int) : MyUnit by enemy

    val enemyUnits = ArrayList<EnemyPredictUnit>()
    val myUnits = ArrayList<MyPredictUnit>()
    val places = HashMap<Int, Vec2Int>()
    val strategies = HashMap<Int, FighterMoveStrategy>()

    fighters.values.forEach { unit ->
        val strategy = unit.currentStrategy
        if (strategy !is FighterMoveStrategy) return@forEach
        val expected = strategy.wannaMove(unit)
        val actual = actualMovePlace(unit.pos, expected)
        myUnits += MyPredictUnit(unit, actual)
        places[unit.id] = actual
        strategies[unit.id] = strategy
    }

    enemies.forEach { unit ->
        val place = ChargeMoveStrategy.chargeMove(myUnitsField, unit.pos.toI())
        enemyUnits += EnemyPredictUnit(
                unit,
                actualMovePlace(unit.pos, place)
        )
    }

    /*   debug {
           enemyUnits.forEach {
               fill(it.pos.x, it.pos.y)
           }
       }
       debug {
           myUnits.forEach {
               fill(it.pos.x, it.pos.y)
           }
       }*/
    val badMoves = predictBadMoves(
            myUnits,
            enemyUnits
    )

    myUnits.forEach { unit ->
        val moveResult: Int = if (unit.id in badMoves) {
            debug {
                fill(unit.pos.x, unit.pos.y)
            }
            ChargeMoveStrategy.chargeMove(allEnemiesField, unit.fighter.pos.toI(), reversed = true)
        } else {
            places[unit.id]!!.toI()
        }
        strategies[unit.id]!!.moveResult = moveResult
    }
}

fun actualMovePlace(current: Vec2Int, place: Int): Vec2Int {
    if (current.toI() == place) return current
    if (occupied[place]) return current
    return place.toVec()
}

val Entity.isDamaged get() = isDamaged(health, properties.maxHealth)
val Entity.isHealed get() = isHealed(health, properties.maxHealth, active)

fun isDamaged(health: Int, maxHealth: Int) = health <= maxHealth

fun isHealed(health: Int, maxHealth: Int, active: Boolean) =
        if (active) health >= maxHealth - 4 else health == maxHealth