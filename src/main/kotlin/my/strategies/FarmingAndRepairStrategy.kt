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
        var place = unit.pos.toI()
        var max = farmersAndRepairField[place]
        neighbours(place) {
            if (farmersAndRepairField[it] > max) {
                place = it
                max = farmersAndRepairField[it]
            }
        }
        if (isResource[place]) {
            return MyAttackAction(resources[place]!!.id)
        }
        val id = notHealedEntitiesIds[place]
        if (id != -1) {
            return MyRepairAction(id)
        }
        val vector = place.toVec()
        return MyMoveAction(vector, false)
    }

}