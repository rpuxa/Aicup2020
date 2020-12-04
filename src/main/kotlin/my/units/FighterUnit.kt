package my.units

import model.Vec2Int
import my.MyUnit

class FighterUnit(
        id: Int,
        pos: Vec2Int,
        health: Int,
        maxHealth: Int,
        override val damage: Int,
        override val range: Int
) : MovableUnit(id, pos, health, maxHealth), MyUnit {
    var strength = 0
    override val our: Boolean get() = true
    override val size: Int get() = 1
}