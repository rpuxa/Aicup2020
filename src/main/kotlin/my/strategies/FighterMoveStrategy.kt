package my.strategies

import my.*
import my.actions.MyMoveAction
import my.actions.NullAction
import my.actions.UnitAction
import my.units.MovableUnit

class FighterMoveStrategy : UnitStrategy() {

    var moveResult = -1

    override fun isFinished(unit: MovableUnit): Boolean {
        return false
    }

    fun wannaMove(unit: MovableUnit): Int {
        val i = unit.pos.toI()
        val field = when {
            unit.isDamaged -> damagedFighterField
            !unit.isHealed && notHealedFighterField[i] > -100_000_000 -> notHealedFighterField
            else -> fighterField
        }
        return path(i, ChargeMoveStrategy.chargeMove(field, i, 100_000_000))
    }

    override fun perform(unit: MovableUnit): UnitAction? {
        val vector = moveResult.toVec()
        return if (vector == unit.pos) NullAction else MyMoveAction(vector, false)
    }
}