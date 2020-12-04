package my.strategies

import my.*
import my.actions.MyRepairAction
import my.actions.UnitAction
import my.units.MovableUnit

class RepairStrategy(val id: Int) : UnitStrategy(1) {

    override fun isFinished(unit: MovableUnit): Boolean {
        val entity = entities[id]
        return entity == null || entity.health == entity.properties.maxHealth
    }

    override fun perform(unit: MovableUnit): UnitAction? {
        if (isFinished(unit)) return null
        return MyRepairAction(id)
    }

    companion object {
        fun repair(pos: Int): Int {
            neighbours(pos) {
                val id = buildingIds[it]
                if (id != -1) {
                    val entity = entities[id]!!
                    if (entity.health < entity.properties.maxHealth) {
                        return id
                    }
                }
            }
            return -1
        }
    }
}