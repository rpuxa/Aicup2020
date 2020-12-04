package my.strategies

import my.units.MovableUnit
import my.actions.NullAction
import my.actions.UnitAction

abstract class DependencyStrategy(priority: Int = 0) : UnitStrategy(priority) {
    var strategy: UnitStrategy? = null

    abstract fun init(unit: MovableUnit): UnitStrategy?

    override fun isFinished(unit: MovableUnit): Boolean {
        if (strategy == null) {
            strategy = init(unit)
        }
        return strategy?.isFinished(unit) ?: false
    }

    override fun perform(unit: MovableUnit): UnitAction? {
        return strategy?.perform(unit) ?: NullAction
    }
}