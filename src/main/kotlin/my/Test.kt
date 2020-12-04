package my

import model.EntityType

fun afraid() {
    queue.clear()
    tmpField.clear()
    val range = EntityType.RANGED_UNIT.properties.attack!!.attackRange
    enemies.forEach {
        if (it.range == range && it.strength > 0) {
            val i = it.pos.toI()
            queue.push(i)
            tmpField[i] = range + 3
        }
    }
    bfs(wholeGraph, queue, tmpField)
    repeat(size) {
        afraidWorkersField[it] = tmpField[it] > 0
        tmpField[it] = 0
    }

    queue.clear()

    for (it in enemies) {
        if (it.range == 1 && it.strength > 0) {
            val i = it.pos.toI()
            queue.push(i)
            tmpField[i] = 4
        }
    }
    bfs(wholeGraph, queue, tmpField)
    repeat(size) {
        afraidWorkersField[it] = afraidWorkersField[it] || tmpField[it] > 0
    }
}