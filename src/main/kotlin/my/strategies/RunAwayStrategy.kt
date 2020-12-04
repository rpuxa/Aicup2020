package my.strategies

import my.*
import my.actions.MyMoveAction
import my.actions.NullAction
import my.actions.UnitAction
import my.units.MovableUnit

class RunAwayStrategy : UnitStrategy(2) {

    override fun isFinished(unit: MovableUnit): Boolean {
        return !afraidWorkersField[unit.pos.toI()]
    }

    override fun perform(unit: MovableUnit): UnitAction? {
        if (isFinished(unit)) return null
        val place = ChargeMoveStrategy.chargeMove(allEnemiesField, unit.pos.toI(), reversed = true)
        return if (place == unit.pos.toI()) return NullAction else MyMoveAction(place.toVec(), false)
    }
}