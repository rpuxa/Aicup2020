package my.actions

import model.EntityAction
import model.AttackAction
import my.units.MovableUnit

class MyAttackAction(val target: Int) : UnitAction {


    override fun perform(unit: MovableUnit): EntityAction {
        return EntityAction().apply {
            attackAction = AttackAction(target, null)
        }
    }
}