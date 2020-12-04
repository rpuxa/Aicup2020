package my

import model.Vec2Int

interface MyUnit {
    val id: Int
    val pos: Vec2Int
    val health: Int
    val maxHealth: Int
    val damage: Int
    val range: Int
    val size: Int
    val our: Boolean
}