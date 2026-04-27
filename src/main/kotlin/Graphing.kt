import Constants.prettyFormat
import imgui.ImGui
import imgui.type.ImBoolean
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
    private val baseClicksDeviation = History("base clicks deviation")
    private val rewardPerPack = History("pack reward")
    private val penaltyPerPack = History("package penalty")
    private val deviationFromBase = History("base reward deviation")
    private val bonusPerPackage = History("bonus per package")
    private val investmentGainPct = History("investment gain")
    private val prestigeBonus = History("prestige bonus")

    private val histories: List<History> = listOf(
        actualClicks,
        baseClicksDeviation,
        rewardPerPack,
        penaltyPerPack,
        deviationFromBase,
        bonusPerPackage,
        investmentGainPct,
        prestigeBonus
    )
    private val chartVisibility = histories.associateWith { ImBoolean(true) }.toMutableMap()

    fun reset() {
        try {
            for (h in histories) {
                h.clear()
            }
        } catch (_: Exception) { }
    }

    fun recordPackage(rewardGiven: Long, packagePenalty: Int, deviation: Int, bonusGiven: Int, prestigeMultiplier: Double) {
        try {
            rewardPerPack.push(rewardGiven.toFloat())
            penaltyPerPack.push(packagePenalty.toFloat())
            deviationFromBase.push(deviation.toFloat())
            bonusPerPackage.push(bonusGiven.toFloat())
            val bonusAmount = (prestigeMultiplier - 1.0).toFloat()
            prestigeBonus.push(bonusAmount)
        } catch (_: Exception) { }
    }

    fun recordClicksPerTick(v: Int, baseClicks: Int) {
        try {
            actualClicks.push(v.toFloat())
            baseClicksDeviation.push((v - baseClicks).toFloat())
        } catch (_: Exception) { }
    }

    fun recordInvestmentGainPct(gainPct: Double) {
        try {
            investmentGainPct.push(gainPct.toFloat())
        } catch (_: Exception) { }
    }

    fun renderInInfo() {
        val plotHeight = 60f
        val availW = ImGui.getContentRegionAvailX()
        for (h in histories) {
            if (!chartVisibility.getValue(h).get()) {
                continue
            }
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
                actualClicks, baseClicksDeviation, rewardPerPack, penaltyPerPack, deviationFromBase, bonusPerPackage -> {
                    val finiteVals = arr.filter { it.isFinite() }
                    val avg = if (finiteVals.isEmpty()) 0f else finiteVals.sum() / finiteVals.size
                    val last = h.lastValue()
                    "${h.name}: ${last.prettyFormat()} (avg. ${avg.prettyFormat()})"
                }
                investmentGainPct, prestigeBonus -> {
                    val finiteVals = arr.filter { it.isFinite() }
                    val avg = if (finiteVals.isEmpty()) 0f else finiteVals.sum() / finiteVals.size
                    val last = h.lastValue()
                    "${h.name}: ${"%+.2f".format(last * 100)}% (avg. ${"%+.2f".format(avg * 100)}%)"
                }
                else -> "${h.name}: ${"%.2f".format(h.lastValue())}"
            }

            ImGui.text(overlay)

            ImGui.plotLines("###graph_${h.name}", arr, arr.size, 0, "", minV, maxV, availW, plotHeight)

            ImGui.separator()
        }

        if (histories.none { chartVisibility.getValue(it).get() }) {
            ImGui.textDisabled("No charts are enabled")
        }
    }

    fun renderChartPreferences() {
        ImGui.text("Visualizations")
        for ((index, h) in histories.withIndex()) {
            ImGui.checkbox("${h.name}###chart_toggle_$index", chartVisibility.getValue(h))
        }
    }
}
