package my.actions

import model.EntityAction
import my.units.MovableUnit

interface UnitAction {
    fun perform(unit: MovableUnit): EntityAction
}