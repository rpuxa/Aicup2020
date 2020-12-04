package my.units

import model.Vec2Int
import my.BuildingPlan
import my.strategies.BuildStrategy


class WorkerUnit(id: Int, pos: Vec2Int, health: Int, maxHealth: Int) : MovableUnit(id, pos,health, maxHealth) {
    val buildingPlan: BuildingPlan?
        get() {
            val strategy = currentStrategy
            return if (strategy is BuildStrategy) strategy.plan else null
        }
}