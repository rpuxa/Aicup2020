package my.strategies

import my.units.MovableUnit
import my.actions.NullAction
import my.actions.UnitAction

object NullStrategy : UnitStrategy(-1_000_000) {

    override fun isFinished(unit: MovableUnit): Boolean {
        return false
    }

    override fun perform(unit: MovableUnit): UnitAction {
        return NullAction
    }
}
