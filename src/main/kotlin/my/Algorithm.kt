@file:Suppress("ArrayInDataClass")

package my

/*

fun aStar(
    graph: Array<ArrayList<Int>>,
    weights: Array<HashMap<Int, Int>>,
    start: Int,
    end: Int,
    queue: MutableSet<Int>,
    heuristics: IntArray,
    distance: IntArray,
    previous: IntArray,
    used: BooleanArray,
    unreachable: BooleanArray
): ArrayList<Int>? {
    queue.add(start)
    distance[start] = 0
    heuristics[start] = 0
    previous[end] = -1
    while (true) {
        val node = queue.minByOrNull { distance[it] + heuristics[it] } ?: break
        if (node == end) break
        queue.remove(node)
        if (used[node]) continue
        val arrayList = graph[node]
        arrayList.forEach { child ->
            if (!unreachable[child]) {
                val value = distance[node] + (weights[node][child] ?: error("$node $child"))
                if (value < distance[child]) {
                    previous[child] = node
                    distance[child] = value
                    queue.add(child)
                }
            }
        }
        used[node] = true
    }
    if (previous[end] == -1) return null
    val path = ArrayList<Int>()
    var prev = end
    while (prev != start) {
        path.add(prev)
        prev = previous[prev]
    }
    repeat(path.size / 2) {
        val tmp = path[it]
        path[it] = path[path.lastIndex - it]
        path[path.lastIndex - it] = tmp
    }
    return path
}



fun nearestAndPrevious(
    graph: Array<ArrayList<Int>>,
    start: Int,
    appropriate: BooleanArray,
    queue: ArrayDeque<Int>,
    used: BooleanArray,
    unreachable: BooleanArray
): Pair<Int, Int>? {
    if (appropriate[start]) return start to start
    if (appropriate.all { !it }) return null
    queue.add(start)

    while (queue.isNotEmpty()) {
        val node = queue.removeFirst()
        if (used[node]) continue
        graph[node].forEach {
            if (!unreachable[it]) {
                if (appropriate[it]) return it to node
                queue.addLast(it)
            }
        }
        used[node] = true
    }
    return null
}
*/

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

    /*fun bfs(
        graph: Array<Neighbours>,
        queue: MyQueue,
        start: IntArray,
        field: IntArray,
        depth: Int
    ) {
        start.forEach {
            queue.push(it)
            field[it] = depth
        }
        bfs(graph, queue, field)
    }*/

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
    if (end == start - 1 || end == start + 1 || end == start + side || end == start - side) {
        return if (!occupied[end]) end else start
    }
    if (end == start + side - 1) {
        if (!occupied[start - 1]) {
            return start - 1
        }
        if (!occupied[start + side]) {
            return start + side
        }
        return start
    }
    if (end == start + side + 1) {
        if (!occupied[start + 1]) {
            return start + 1
        }
        if (!occupied[start + side]) {
            return start + side
        }
        return start
    }
    if (end == start - side - 1) {
        if (!occupied[start - 1]) {
            return start - 1
        }
        if (!occupied[start - side]) {
            return start - side
        }
        return start
    }
    if (end == start - side + 1) {
        if (!occupied[start + 1]) {
            return start + 1
        }
        if (!occupied[start - side]) {
            return start - side
        }
    }
    return start
}

/*

fun main() {
    val direction = arrayOf(0 to 1, 1 to 0, 0 to -1, -1 to 0)
    val graph = Array(60 * 60) {
        val x = it % 60
        val y = it / 60
        Neighbours(direction.mapNotNull { d ->
            val i = (x + d.first) + (y + d.second) * 60
            if (i in 0 until 60 * 60) i else null
        })
    }
    val my.getQueue = MyQueue(60 * 60)
    val field = IntArray(60 * 60)

    repeat(100) {
        my.getQueue.clear()
        repeat(field.size) {
            field[it] = -1
        }
        bfs(graph, my.getQueue, 300, field)
    }

    var time = 0L
    repeat(1_000_000) {
        my.getQueue.clear()
        repeat(field.size) {
            field[it] = -1
        }
        time += measureNanoTime { bfs(graph, my.getQueue, 300, field) }
    }
    println(time)
}*/
