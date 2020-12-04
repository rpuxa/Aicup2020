package my.actions

import model.EntityAction
import model.RepairAction
import my.units.MovableUnit

class MyRepairAction(val target: Int) : UnitAction {
    override fun perform(unit: MovableUnit): EntityAction {
        return EntityAction().apply {
            repairAction = RepairAction(target)
        }
    }
}