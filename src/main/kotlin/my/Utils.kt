package my

import model.Entity
import model.EntityType
import model.Vec2Int
import java.util.*

fun IntArray.clear() {
    indices.forEach {
        set(it, 0)
    }
}

fun DoubleArray.clear() {
    indices.forEach {
        set(it, 0.0)
    }
}

fun IntArray.set(i: Int) {
    indices.forEach {
        set(it, i)
    }
}

fun IntArray.clearM1() {
    indices.forEach {
        set(it,-1)
    }
}

fun BooleanArray.clear() {
    indices.forEach {
        set(it, false)
    }
}
fun Vec2Int.toI() = toI(x, y)
fun Int.toVec() = Vec2Int(
    this % side,
    this / side
)

fun toI(x: Int, y: Int) = x + y * side

inline fun toIIfPossible(x: Int, y: Int, block: (Int) -> Unit) {
    val i = toI(x, y)
    if (i in 0 until size) {
        block(i)
    }
}

val EntityType.properties get() = my.properties[this]!!
val Entity.properties get() = this.entityType.properties

inline fun baseNeighbours(pos: Vec2Int, buildSize: Int, block: (Int) -> Unit) {
    for (x in pos.x until (pos.x + buildSize))
        toIIfPossible(x, pos.y - 1, block)
    for (y in pos.y until (pos.y + buildSize))
        toIIfPossible(pos.x - 1, y, block)
    for (x in pos.x until (pos.x + buildSize))
        toIIfPossible(x, pos.y + buildSize, block)
    for (y in pos.y until (pos.y + buildSize))
        toIIfPossible(pos.x + buildSize, y, block)
}

inline fun baseCells(pos: Vec2Int, buildSize: Int, block: (Int, Int) -> Unit) {
    for (dx in 0 until buildSize)
        for (dy in 0 until buildSize) {
            block(pos.x + dx, pos.y + dy)
        }
}

inline fun neighbours(i: Int, side: Int = my.side, block: (Int) -> Unit) {
    if (i % side != 0)
        block(i - 1)
    if ((i + 1) % side != 0)
        block(i + 1)
    if (i >= side)
        block(i - side)
    if (i / side < side - 1)
        block(i + side)
}

fun IntArray.sum(other: IntArray) {
    repeat(size) {
        this[it] += other[it]
    }
}

fun IntArray.max(other: IntArray) {
    repeat(size) {
        this[it] = kotlin.math.max(this[it], other[it])
    }
}

fun IntArray.times(m: Int) {
    repeat(size) {
        this[it] *= m
    }
}

fun IntArray.min(other: IntArray) {
    repeat(size) {
        this[it] = kotlin.math.min(this[it], other[it])
    }
}