import model.*
import my.debug
import my.debugTime
import my.init

class MyStrategy {

    val all = ArrayList<Double>()

    fun getAction(playerView: PlayerView, debugInterface: DebugInterface?): Action {
        debugInterface?.send(DebugCommand.Clear())
        var result: Action? = null
        all += debugTime("Whole time") {
            result = init(playerView, debugInterface)
        }
        println("Average time: ${all.average()}")
        return result!!
    }

    fun debugUpdate(playerView: PlayerView, debugInterface: DebugInterface) {
    }
}
