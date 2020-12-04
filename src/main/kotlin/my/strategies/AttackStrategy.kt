package my.strategies

import my.units.MovableUnit
import my.actions.MyAttackAction
import my.actions.UnitAction

class AttackStrategy(val enemyId: Int) : UnitStrategy() {

    override fun isFinished(unit: MovableUnit): Boolean {
       return false
    }

    override fun perform(unit: MovableUnit): UnitAction? {
        if (isFinished(unit)) return null
        return MyAttackAction(enemyId!!)
    }
}