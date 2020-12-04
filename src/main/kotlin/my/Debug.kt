package my

import DebugInterface
import model.*
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

const val SHOW_DEBUG = false


class MyDebug(val debug: DebugInterface) {

    fun fill(x: Int, y: Int) {
        debug.send(DebugCommand.Add(DebugData.Primitives().apply {
            vertices = arrayOf(
                Vec2Int(x, y),
                Vec2Int(x + 1, y),
                Vec2Int(x + 1, y + 1),
                Vec2Int(x + 1, y + 1),
                Vec2Int(x, y),
                Vec2Int(x, y + 1),
            ).map {
                ColoredVertex(Vec2Float(it.x.toFloat(), it.y.toFloat()), Vec2Float(0f, 0f), Color(1f, 1f, 0f, 1f))
            }.toTypedArray()
            primitiveType = PrimitiveType.TRIANGLES
        }))
    }

    fun path(path: List<Int>) {
        path.zipWithNext { a, b ->
            line(a.toVec(), b.toVec())
        }
    }

    fun text(string: String) {
        debug.send(DebugCommand.Add(DebugData.Log(string)))
    }

    fun field(field: IntArray) {
        field.indices.forEach {
            textOnScreen(field[it].toString(), it.toVec())
        }
    }

    fun textOnScreen(text: String, point: Vec2Int) {
        debug.send(
            DebugCommand.Add(
                DebugData.PlacedText(
                    ColoredVertex(
                        Vec2Float(point.x.toFloat() + .5f, point.y.toFloat() + .5f),
                        Vec2Float(0f, 0f),
                        Color(1f, 1f, 0f, 1f)
                    ),
                    text,
                    0f,
                    10f
                )
            )
        )
    }

    fun line(from: Vec2Int, to: Vec2Int) {
        debug.send(DebugCommand.Add(DebugData.Primitives().apply {
            vertices = arrayOf(
                ColoredVertex(
                    Vec2Float(from.x.toFloat() + .5f, from.y.toFloat() + .5f),
                    Vec2Float(0f, 0f),
                    Color(0f, 1f, 0f, 1f)
                ),
                ColoredVertex(
                    Vec2Float(to.x.toFloat() + .5f, to.y.toFloat() + .5f),
                    Vec2Float(0f, 0f),
                    Color(0f, 1f, 0f, 1f)
                ),
            )
            primitiveType = PrimitiveType.LINES
        }))
    }
}

inline fun debug(block: MyDebug.() -> Unit) {
    if (SHOW_DEBUG) {
        debug?.let {
            MyDebug(it).block()
        }
    }
}

inline fun debugTime(text: String, block: () -> Unit): Double {
    val measureTime = System.nanoTime()
    block()
    if (debug != null) {
        val d = (System.nanoTime() - measureTime) / 10_000_000.0
        println("$text: $d")
        return d
    }
    return 0.0
}