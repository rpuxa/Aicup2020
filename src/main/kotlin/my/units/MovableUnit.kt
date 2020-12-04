package my.units

import model.Vec2Int
import my.actions.NullAction
import my.isDamaged
import my.isHealed
import my.strategies.NullStrategy
import my.strategies.UnitStrategy

open class MovableUnit(
    val id: Int,
    var pos: Vec2Int,
    var health: Int,
    val maxHealth: Int
) {
    val isDamaged: Boolean get() = isDamaged(health, maxHealth)
    val isHealed: Boolean get() = isHealed(health, maxHealth, true)
    var currentStrategy: UnitStrategy = NullStrategy
        private set

    open fun strategy(strategy: UnitStrategy) {
        currentStrategy = strategy
    }

    fun strategyIfMorePriority(strategy: UnitStrategy): Boolean {
        if (currentStrategy.priority <= strategy.priority) {
            strategy(strategy)
            return true
        }
        return false
    }

    inline fun <reified T : UnitStrategy> strategyIfMorePriorityAndOtherType(strategy: T): Boolean {
        if (currentStrategy !is T && currentStrategy.priority <= strategy.priority) {
            strategy(strategy)
            return true
        }
        return false
    }

    fun runStrategy() = (
            currentStrategy.perform(this) ?: run {
                strategyDone()
                NullAction
            }
            ).perform(this)

    fun clearStrategy() {
        if (currentStrategy.isFinished(this)) {
            strategyDone()
        }
    }

    override fun equals(other: Any?) = other is MovableUnit && other.id == id
    override fun hashCode() = id
    fun strategyDone() {
        currentStrategy = NullStrategy
    }
}