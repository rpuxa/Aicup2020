package my.actions

import model.*
import my.units.MovableUnit

class MyBuildAction(private val type: EntityType, private val location: Vec2Int) : UnitAction {

    override fun perform(unit: MovableUnit): EntityAction {
        return EntityAction().apply {
            buildAction = BuildAction(type, location)
        }
    }
}

