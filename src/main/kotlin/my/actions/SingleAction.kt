package my.actions

import model.EntityAction
import my.units.MovableUnit

class SingleAction(
        val action: EntityAction
) : UnitAction {
    override fun perform(unit: MovableUnit): EntityAction {
        return action
    }
}