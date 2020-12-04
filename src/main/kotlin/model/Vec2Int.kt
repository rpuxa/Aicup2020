package model

import util.StreamUtil

class Vec2Int {
    var x: Int = 0
    var y: Int = 0
    constructor() {}
    constructor(x: Int, y: Int) {
        this.x = x
        this.y = y
    }

    companion object {
        @Throws(java.io.IOException::class)
        fun readFrom(stream: java.io.InputStream): Vec2Int {
            val result = Vec2Int()
            result.x = StreamUtil.readInt(stream)
            result.y = StreamUtil.readInt(stream)
            return result
        }
    }
    @Throws(java.io.IOException::class)
    fun writeTo(stream: java.io.OutputStream) {
        StreamUtil.writeInt(stream, x)
        StreamUtil.writeInt(stream, y)
    }

    override fun equals(other: Any?)= other is Vec2Int && other.x == x && other.y == y

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        return result
    }
}
