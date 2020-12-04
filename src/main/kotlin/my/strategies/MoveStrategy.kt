/*
package my.strategies

import model.Vec2Int
import my.*
import my.actions.MoveAction
import my.actions.NullAction
import my.actions.UnitAction
import kotlin.math.abs


class MoveStrategy(
    private val location: Vec2Int,
    private val breakLast: Boolean = false,
    private val canStepOnBuildingPlace: Boolean = false
) : UnitStrategy() {

    var path: ArrayList<Int>? = null
    var index = 0
    val prohibitedList = ArrayList<Int>()

    fun clearPath() {
        path = null
        index = 0
    }

    override fun isFinished(unit: MovableUnit): Boolean {
        return location.x == unit.pos.x && location.y == unit.pos.y
    }

    override fun perform(unit: MovableUnit): UnitAction? {
        if (isFinished(unit)) return null

        path?.let { p ->
            if (p[index] == unit.pos.toI()) {
                index++
            }
            when {
                occupied[p[index]] -> {
                    prohibitedList.add(p[index])
                    clearPath()
                }
                p.any { cannotBuild[it] } -> {
                    clearPath()
                }
             */
/*   abs(unit.pos.x - location.x) + abs(unit.pos.y - location.y) > p.size + p.size / 2 -> {
                    clearPath()
                    prohibitedList.clear()
                }*//*

            }
        }
        if (path == null) {
            set.clear()
            used.clear()
            distance.let { d ->
                d.indices.forEach {
                    d[it] = Int.MAX_VALUE / 2
                }
            }
            repeat(size) {
                heuristic[it] = abs(location.x - unit.pos.x) + abs(location.y - unit.pos.y)
            }
            val prohibited = tmpBooleanArray
            prohibited.clear()
            prohibitedList.forEach {
                prohibited[it] = true
            }
            if (!canStepOnBuildingPlace) {
                buildingPlans.forEach {
                    it.cells.forEach {
                        prohibited[it] = true
                    }
                }
            }
            val farmingPlan = (unit as? WorkerUnit)?.farmingPlan
            farmingPlans.forEach {
                if (farmingPlan == null || farmingPlan.pos != it.pos) {
                    prohibited[it.farmingPos.toI()] = true
                    prohibited[it.pos.toI()] = true
                }
            }
            path = aStar(
                graph,
                weights,
                unit.pos.toI(),
                location.toI(),
                set,
                heuristic,
                distance,
                previous,
                used,
                prohibited
            )
        }
        val path = path ?: run {
            prohibitedList.clear()
            return NullAction
        }
        debug {
            path(path)
        }
        var breakTrough = false
        if (breakLast && index == path.lastIndex) {
            breakTrough = true
        }
        return MoveAction(path[index].toVec(), breakTrough)
    }
}
*/
