import Constants.prettyFormat
import imgui.ImGui
import kotlin.math.max
import kotlin.math.min

object Graphing {
    private const val HISTORY_SIZE = 200 // todo: make same as log max size?

    private class History(val name: String) {
        val buf = FloatArray(HISTORY_SIZE)
        var idx = 0
        var filled = false
        fun push(v: Float) {
            buf[idx] = v
            idx = (idx + 1) % buf.size
            if (idx == 0) filled = true
        }
        fun activeCount() = if (filled) buf.size else idx
        fun orderedArray(): FloatArray {
            val count = activeCount()
            val out = FloatArray(if (count == 0) 1 else count)
            if (count == 0) {
                out[0] = 0f
                return out
            }
            val start = if (filled) idx else 0
            for (i in 0 until count) {
                out[i] = buf[(start + i) % buf.size]
            }
            return out
        }
        fun lastValue(): Float = if (activeCount() == 0) 0f else buf[(idx - 1 + buf.size) % buf.size]

        fun clear() {
            for (i in buf.indices) buf[i] = 0f
            idx = 0
            filled = false
        }
    }

    private val actualClicks = History("click amount")
    private val rewardPerPack = History("pack reward")
    private val penaltyPerPack = History("package penalty")
    private val deviationFromBase = History("base reward deviation")
    private val prestigeBonus = History("prestige bonus")

    private val histories: List<History> = listOf(actualClicks, rewardPerPack, penaltyPerPack, deviationFromBase, prestigeBonus)

    fun reset() {
        try {
            for (h in histories) {
                h.clear()
            }
        } catch (_: Exception) { }
    }

    fun recordPackage(rewardGiven: Long, packagePenalty: Int, deviation: Int, prestigeMultiplier: Double) {
        try {
            rewardPerPack.push(rewardGiven.toFloat())
            penaltyPerPack.push(packagePenalty.toFloat())
            deviationFromBase.push(deviation.toFloat())
            val bonusAmount = (prestigeMultiplier - 1.0).toFloat()
            prestigeBonus.push(bonusAmount)
        } catch (_: Exception) { }
    }

    fun recordClicksPerTick(v: Int) {
        try {
            actualClicks.push(v.toFloat())
        } catch (_: Exception) { }
    }

    fun renderInInfo() {
        val plotHeight = 60f
        val availW = ImGui.getContentRegionAvailX()
        for (h in histories) {
            val arr = h.orderedArray()
            var minV = Float.MAX_VALUE
            var maxV = -Float.MAX_VALUE
            for (v in arr) {
                if (v.isFinite()) {
                    minV = min(minV, v)
                    maxV = max(maxV, v)
                }
            }
            if (minV == Float.MAX_VALUE || maxV == -Float.MAX_VALUE) { minV = 0f; maxV = 1f }
            if (minV == maxV) { maxV = minV + 1f } // avoid zero range
            val overlay = when (h) {
                actualClicks, rewardPerPack, penaltyPerPack, deviationFromBase -> {
                    val finiteVals = arr.filter { it.isFinite() }
                    val avg = if (finiteVals.isEmpty()) 0f else finiteVals.sum() / finiteVals.size
                    val last = h.lastValue()
                    "${h.name}: ${last.prettyFormat()} (avg. ${avg.prettyFormat()})"
                }
                prestigeBonus -> {
                    val finiteVals = arr.filter { it.isFinite() }
                    val avg = if (finiteVals.isEmpty()) 0f else finiteVals.sum() / finiteVals.size
                    val last = h.lastValue()
                    "${h.name}: +${"%.2f".format(last * 100)}% (avg. +${"%.2f".format(avg * 100)}%)"
                }
                else -> "${h.name}: ${"%.2f".format(h.lastValue())}"
            }

            ImGui.text(overlay)

            ImGui.plotLines("###graph_${h.name}", arr, arr.size, 0, "", minV, maxV, availW, plotHeight)

            ImGui.separator()
        }
    }
}
