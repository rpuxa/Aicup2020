package my

import model.Vec2Int

class Enemy(
        override val id: Int,
        override val pos: Vec2Int,
        override val size: Int,
        override val health: Int,
        override val maxHealth: Int,
        override val damage: Int,
        override val range: Int
) : MyUnit {
    var strength = 0
    override val our: Boolean get() = false
}