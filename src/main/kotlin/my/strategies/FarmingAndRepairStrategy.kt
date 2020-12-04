package my.strategies

import my.*
import my.actions.MyAttackAction
import my.actions.MyMoveAction
import my.actions.MyRepairAction
import my.actions.UnitAction
import my.units.MovableUnit


class FarmingAndRepairStrategy : UnitStrategy(0) {

    override fun isFinished(unit: MovableUnit): Boolean {
        return false
    }

    override fun perform(unit: MovableUnit): UnitAction? {
        var fixId = -1
        var place = unit.pos.toI()
        var max = farmersAndRepairField[place]
        neighbours(place) {
            if (farmersAndRepairField[it] > max) {
                place = it
                max = farmersAndRepairField[it]
            }
            if (fixId == -1) {
                fixId = notHealedEntitiesIds[it]
            }
        }
        if (fixId != -1) {
            return MyRepairAction(fixId)
        }
        if (isResource[place]) {
            return MyAttackAction(resources[place]!!.id)
        }
        val vector = place.toVec()
        return MyMoveAction(vector, false)
    }

}