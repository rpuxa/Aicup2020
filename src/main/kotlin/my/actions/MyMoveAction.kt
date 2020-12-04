package my.actions

import model.EntityAction
import model.MoveAction
import model.Vec2Int
import my.units.MovableUnit

class MyMoveAction(private val vector: Vec2Int, private val breakTrough: Boolean) : UnitAction {

    override fun perform(unit: MovableUnit): EntityAction {
        val action = EntityAction()
        action.moveAction = MoveAction(
            target = vector,
            findClosestPosition = false,
            breakThrough = breakTrough
        )
        return action
    }
}