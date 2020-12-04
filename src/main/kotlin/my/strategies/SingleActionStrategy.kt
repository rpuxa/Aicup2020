package my.strategies

import my.units.MovableUnit
import my.actions.NullAction
import my.actions.UnitAction

class SingleActionStrategy(val action: UnitAction, val condition: () -> Boolean = { true }) : UnitStrategy() {

    private var first = true

    override fun isFinished(unit: MovableUnit): Boolean {
        return !first
    }

    override fun perform(unit: MovableUnit): UnitAction? {
        if (!first) return null
        if (!condition()) return NullAction
        first = false
        return action
    }
}