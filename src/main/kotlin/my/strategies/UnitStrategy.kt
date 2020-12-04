package my.strategies

import my.units.MovableUnit
import my.actions.UnitAction

abstract class UnitStrategy(var priority: Int = 0) {
    abstract fun isFinished(unit: MovableUnit): Boolean
    abstract fun perform(unit: MovableUnit): UnitAction?
}