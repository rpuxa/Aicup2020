package my.actions

import model.EntityAction
import my.units.MovableUnit

object NullAction : UnitAction {
    private val empty = EntityAction()

    override fun perform(unit: MovableUnit): EntityAction {
        return empty
    }
}
