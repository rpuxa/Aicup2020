package my.strategies

import my.*
import my.actions.MyMoveAction
import my.actions.NullAction
import my.actions.UnitAction
import my.units.MovableUnit

class ChargeMoveStrategy(val field: IntArray) : UnitStrategy() {

    override fun isFinished(unit: MovableUnit): Boolean {
        return false
    }

    override fun perform(unit: MovableUnit): UnitAction? {
        val vector = chargeMove(field, unit.pos.toI()).toVec()
        return if (vector == unit.pos) NullAction else MyMoveAction(vector, isResource[vector.toI()])
    }

    companion object {
        fun chargeMove(field: IntArray, pos: Int, add: Int = 0, reversed: Boolean = false): Int {

            fun get(i: Int) = if (reversed) -field[i] else field[i]

            var place = pos
            var max = get(place) + add
            val x = pos % side
            val y = pos / side
            for (dx in (x - 2)..(x + 2)) {
                if (dx in 0 until side) {
                    for (dy in (y - 2)..(y + 2)) {
                        if (dy in 0 until side) {
                            val i = toI(dx, dy)
                            val get = get(i)
                            if (get > max) {
                                place = i
                                max = get
                            }
                        }
                    }
                }
            }
            return place
        }
    }
}