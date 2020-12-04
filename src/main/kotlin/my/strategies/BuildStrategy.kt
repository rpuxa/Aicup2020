package my.strategies

import model.BuildAction
import model.EntityAction
import model.MoveAction
import model.RepairAction
import my.*
import my.actions.SingleAction
import my.actions.UnitAction
import my.units.MovableUnit

class BuildStrategy(val plan: BuildingPlan) : UnitStrategy(PRIORITY) {

    override fun isFinished(unit: MovableUnit): Boolean {
        return finishedbuilding[plan.pos.toI()]
    }

    override fun perform(unit: MovableUnit): UnitAction? {
        if (isFinished(unit)) return null
        queue.clear()
        val moveField = IntArray(size)
        baseNeighbours(plan.pos, plan.type.properties.size) {
            queue.push(it)
            moveField[it] = 60
        }
        bfs(notOccupiedGraph, queue, moveField)
        val i = unit.pos.toI()
        repeat(size) {
            if (i != it && occupied[it]) {
                moveField[it] = -10000
            }
        }
        val place = ChargeMoveStrategy.chargeMove(moveField, unit.pos.toI())
        return SingleAction(EntityAction().apply {
            moveAction = MoveAction(
                    place.toVec(),
                    findClosestPosition = false,
                    breakThrough = false
            )
            buildAction = BuildAction(plan.type, plan.pos)
            repairAction = RepairAction(buildingIds[plan.pos.toI()].let { if (it == -1) 0 else it })
        })
    }

    companion object {
        const val PRIORITY = 3
    }

}