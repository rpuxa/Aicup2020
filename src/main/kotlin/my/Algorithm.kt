@file:Suppress("ArrayInDataClass")

package my

import kotlin.math.sign


class Neighbours(
    @JvmField
    var n1: Int,
    @JvmField
    var n2: Int,
    @JvmField
    var n3: Int,
    @JvmField
    var n4: Int,
    @JvmField
    var size: Int
) {

   fun add(i: Int) {
        when (size++) {
            0 -> n1 = i
            1 -> n2 = i
            2 -> n3 = i
            3 -> n4 = i
            else -> error("Перебор")
        }
    }

    inline fun forEach(block: (Int) -> Unit) {
        if (size > 0) {
            block(n1)
        }
        if (size > 1) {
            block(n2)
        }
        if (size > 2) {
            block(n3)
        }
        if (size == 4) {
            block(n4)
        }
    }

   fun clear() {
        size = 0
    }
}


@Suppress("NOTHING_TO_INLINE")
class MyQueue(size: Int) {
    @JvmField
    val array = IntArray(size)

    @JvmField
    var first = 0

    @JvmField
    var last = -1

    inline fun push(i: Int) {
        array[++last] = i
    }

    inline fun pull(): Int {
        return array[first++]
    }

    inline fun isEmpty() = first > last

    fun clear() {
        first = 0
        last = -1
    }
}

fun bfs(
    graph: Array<Neighbours>,
    queue: MyQueue,
    start: Int,
    field: IntArray,
    depth: Int
) {
    queue.push(start)
    field[start] = depth
    bfs(graph, queue, field)
}

fun bfs(
    graph: Array<Neighbours>,
    queue: MyQueue,
    field: IntArray
) {
    while (!queue.isEmpty()) {
        val node = queue.pull()
        val value = field[node] - 1
        if (value == 0) continue
        graph[node].forEach {
            if (field[it] == 0) {
                field[it] = value
                queue.push(it)
            }
        }
    }
}

fun bfs(
    graph: Array<Neighbours>,
    queue: MyQueue,
    field: IntArray,
    stop: BooleanArray
) {
    while (!queue.isEmpty()) {
        val node = queue.pull()
        val value = field[node] - 1
        if (value == 0) continue
        graph[node].forEach {
            if (field[it] == 0) {
                field[it] = value
                if (!stop[it]) queue.push(it)
            }
        }
    }
}

fun nearest(
    graph: Array<Neighbours>,
    queue: MyQueue,
    start: Int,
    appropriate: BooleanArray,
    used: BooleanArray
): Int? {
    queue.push(start)
    used[start] = true
    while (!queue.isEmpty()) {
        val node = queue.pull()
        if (appropriate[node]) return node
        graph[node].forEach {
            if (!used[it]) {
                queue.push(it)
                used[it] = true
            }
        }
    }
    return null
}

fun path(
        start: Int,
        end: Int
): Int {
    if (start == end) return start
    val side = side
    val occupied = occupied

    val startX = start % side
    val startY = start / side
    val endX = end % side
    val endY = end / side

    val dx = (endX - startX).sign
    val dy = (endY - startY).sign

    if (dx == 0) {
        val i = toI(startX, startY + dy)
        if (!occupied[i]) {
            return i
        }
        return start
    }
    if (dy == 0) {
        val i = toI(startX + dx, startY)
        if (!occupied[i]) {
            return i
        }
        return start
    }
    var i = toI(startX, startY + dy)
    if (!occupied[i]) {
        return i
    }
    i = toI(startX + dx, startY)
    if (!occupied[i]) {
        return i
    }
    return start
}