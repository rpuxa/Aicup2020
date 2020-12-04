package my

import model.EntityType
import model.Vec2Int
import my.strategies.BuildStrategy
import kotlin.math.abs

val buildingPlans = ArrayList<BuildingPlan>()

class BuildingPlan(val pos: Vec2Int, val type: EntityType, val cells: List<Int>, val escapeField: IntArray) {
    var canceled = false
}

fun processBuilding() {
    buildingPlans.removeAll {
        it.canceled || finishedbuilding[it.pos.toI()]
    }

    for (plan in buildingPlans) {
        val count = workers.values.count { it.buildingPlan === plan }
        if (count >= 3) continue
        queue.clear()
        tmpField.clear()
        baseNeighbours(plan.pos, plan.type.properties.size) {
            queue.push(it)
            tmpField[it] = 160
        }
        bfs(reachableGraph, queue, tmpField)
        workers.values
                .sortedBy {
                    if (it.currentStrategy is BuildStrategy || it.currentStrategy.priority > BuildStrategy.PRIORITY) {
                        1_000_000
                    } else {
                        -tmpField[it.pos.toI()]
                    }
                }
                .take(3 - count)
                .forEach {
                    it.strategy(BuildStrategy(plan))
                }
    }

    debug {
        buildingPlans.forEach {
            it.cells.forEach {
                fill(it % side, it / side)
            }
        }
    }
}

fun build(type: EntityType) {
    val pos = choseBuildingPos(type) ?: return
    val list = ArrayList<Int>()
    val field = IntArray(size)
    baseCells(pos, type.properties.size) { x, y ->
        val i = toI(x, y)
        list += i
        field[i] = -1000 + 100 * (abs(pos.x - x) + abs(pos.y - y))
    }
    val buildingPlan = BuildingPlan(pos, type, list, field)
    buildingPlans += buildingPlan
}

fun choseBuildingPos(type: EntityType): Vec2Int? {
    val buildSize = type.properties.size + 2
    loop@ for (it in sortedBuildPoints) {
        val x = it % side
        val y = it / side

        for (dx in 0 until buildSize)
            for (dy in 0 until buildSize) {
                val toI = toI(x + dx, y + dy)
                if (inappropriateForBuilding[toI]) {
                    continue@loop
                }
            }

        //  println("Место постройки: ${x + 1}, ${y + 1}")
        return Vec2Int(x + 1, y + 1)
    }
    return null
}