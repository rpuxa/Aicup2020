package my.strategies

import my.units.MovableUnit
import my.actions.UnitAction

class StrategyList(vararg val list: UnitStrategy) : UnitStrategy() {

    var index = 0

    override fun isFinished(unit: MovableUnit): Boolean {
        while (true) {
            if (index == list.size) return true
            val isFinished = list[index].isFinished(unit)
            if (!isFinished) return false
            index++
        }
    }

    override fun perform(unit: MovableUnit): UnitAction? {
        while (true) {
            if (index == list.size) return null
            val strategy = list[index]
            val preform = strategy.perform(unit)
            if (preform != null)
                return preform
            index++
        }
    }
}