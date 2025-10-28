import Constants.prettyFormat
import Constants.toRoman
import imgui.ImGui
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiCond
import imgui.type.ImBoolean
import imgui.type.ImString
import java.util.Properties
import Constants as constants
import Upgrades as upgrades

object UI : GameLogger {

    private val logBuffer = mutableListOf<String>()
    private val maxLogLines = 300

    private val autoScroll = ImBoolean(true)
    private val logFilter = ImString()

    override fun log(message: String) {
        synchronized(logBuffer) {
            logBuffer.add(message)
            if (logBuffer.size > maxLogLines) {
                logBuffer.removeFirst()
            }
        }
    }

    val gl = GameLogic(this)

    fun getAppVersion(): String {
        val props = Properties()
        Main::class.java.classLoader.getResourceAsStream("version.properties")?.use { stream ->
            props.load(stream)
            return props.getProperty("version") ?: "[unknown]"
        }
        return "[unknown]"
    }

    private fun renderControlsWindow(controlsWidth: Float, displayHeight: Float) {
        ImGui.setNextWindowPos(0f, 0f, ImGuiCond.Once)
        ImGui.setNextWindowSize(controlsWidth, displayHeight, ImGuiCond.Once)
        ImGui.begin("Controls")
        ImGui.text("clicks")

        if (ImGui.button("New game")) {
            gl.genStart()
        }
        ImGui.sameLine()
        ImGui.text("start fresh")

        if (ImGui.button("Save game")) {
            gl.stop()
            log("[INFO] would save game state")
        }

        if (ImGui.button("Load game...")) {
            log("[INFO] would load a saved game state")
        }

        ImGui.newLine()
        if (ImGui.button("How to play")) {
            log("[INFO] would open game guide")
        }

        ImGui.separator()
        // START ACTIONS
        ImGui.text("Game actions")
        if (gl.isAwaitingInput()) {
            if (ImGui.button("Package!")) {
                gl.confirmInput()
            }
        }
        if (upgrades.hedgeFund) {
            if (ImGui.button("BUY BUY BUY")) {
                upgrades.buybuybuy()
            }
            ImGui.sameLine()
            ImGui.text("invest ($${(constants.currentMoney * 0.5).toLong().coerceAtLeast(2000).prettyFormat()})")
        }

        ImGui.separator()
        // START UPGRADES
        ImGui.textWrapped("Upgrades")

        ImGui.text("clicks per pack")
        ImGui.sameLine()
        if (ImGui.button("+###clicksPerPack")) {
            log(constants.clicksPerPackAdd())
        }
        ImGui.sameLine()
        ImGui.text("($${constants.clicksPerPackPrice().prettyFormat()})")

        ImGui.text("base clicks per tick")
        ImGui.sameLine()
        if (ImGui.button("+###clicksPerTick")) {
            log(constants.clicksPerTickAdd())
        }
        ImGui.sameLine()
        ImGui.text("($${constants.clicksPerTickPrice().prettyFormat()})")

        ImGui.text("subticks per tick")
        ImGui.sameLine()
        if (ImGui.button("+###ticksPerSecond")) {
            log(constants.ticksPerSecondAdd())
        }
        ImGui.sameLine()
        ImGui.text("($${constants.ticksPerSecondPrice().prettyFormat()})")

        ImGui.text("base pack reward")
        ImGui.sameLine()
        if (ImGui.button("+###packRewardAmount")) {
            log(constants.packRewardAmountAdd())
        }
        ImGui.sameLine()
        ImGui.text("($${constants.packRewardAmountPrice().prettyFormat()})")

        if (ImGui.button("-###bonusPayInterval")) {
            log(constants.bonusPayIntervalAdd())
        }
        ImGui.sameLine()
        ImGui.text("bonus interval")
        ImGui.sameLine()
        ImGui.text("($${constants.bonusPayIntervalPrice().prettyFormat()})")

        ImGui.text("bonus pay multiplier")
        ImGui.sameLine()
        if (ImGui.button("+###bonusPayScale")) {
            log(constants.bonusPayScaleAdd())
        }
        ImGui.sameLine()
        ImGui.text("($${constants.bonusPayScalePrice().prettyFormat()})")

        ImGui.text("uncertainty floor")
        ImGui.sameLine()
        if (ImGui.button("+###uncertaintyFloor")) {
            log(constants.uncertaintyFloorAdd())
        }
        ImGui.sameLine()
        ImGui.text("($${constants.uncertaintyFloorPrice().prettyFormat()})")

        if (ImGui.button("-###uncertaintyLimitSub")) {
            log(constants.uncertaintyLimitSub())
        }
        ImGui.sameLine()
        ImGui.text("uncertainty limit")
        ImGui.sameLine()
        if (ImGui.button("+###uncertaintyLimitAdd")) {
            log(constants.uncertaintyLimitAdd())
        }
        ImGui.sameLine()
        ImGui.text("($${constants.uncertaintyLimitPrice().prettyFormat()})")

        if (ImGui.button("-###fuzzySelectRangeSub")) {
            log(constants.fuzzySelectRangeSub())
        }
        ImGui.sameLine()
        ImGui.text("pack penalty range")
        ImGui.sameLine()
        if (ImGui.button("+###fuzzySelectRangeAdd")) {
            log(constants.fuzzySelectRangeAdd())
        }
        ImGui.sameLine()
        ImGui.text("($${constants.fuzzySelectRangePrice().prettyFormat()})")

        ImGui.text("pack penalty interval")
        ImGui.sameLine()
        if (ImGui.button("+###fuzzySelectPenaltyUnit")) {
            log(constants.fuzzySelectPenaltyUnitAdd())
        }
        ImGui.sameLine()
        ImGui.text("($${constants.fuzzySelectPenaltyUnitPrice().prettyFormat()})")

        if (ImGui.button("-###maxPenalty")) {
            log(constants.maxPenaltyAdd())
        }
        ImGui.sameLine()
        ImGui.text("max penalty")
        ImGui.sameLine()
        ImGui.text("($${constants.maxPenaltyPrice().prettyFormat()})")

        ImGui.text("min reward")
        ImGui.sameLine()
        if (ImGui.button("+###minReward")) {
            log(constants.minRewardAdd())
        }
        ImGui.sameLine()
        ImGui.text("($${constants.minRewardPrice().prettyFormat()})")

        ImGui.separator()

        // START MODS
        ImGui.textWrapped("Mods")

        if (ImGui.button("Autopack")) {
            upgrades.autoPackToggle()
        }
        ImGui.sameLine()
        ImGui.text("(${if (upgrades.autoPack) "ON" else "$${upgrades.autoPackSp}"})")

        if (ImGui.button("Hedge fund")) {
            upgrades.startHedgeFund()
        }
        ImGui.sameLine()
        ImGui.text("(${if (upgrades.hedgeFund) "give up?" else "$${upgrades.hedgeFundSp.toLong().prettyFormat()}"})")

        if (ImGui.button("sell everything")) {
            if (constants.getRefundEligibility()) {
                upgrades.modResetClick()
            } else {
                log("[WARN] You must upgrade every attribute at least once to use this mod.")
            }
        }
        ImGui.sameLine()
        ImGui.text("(+$${constants.getRefundPrice().prettyFormat()})")

        if (ImGui.button("QA MONEY")) {
            constants.currentMoney += 100000
        }

        ImGui.end()
    }

    private fun clearLogBuffer() {
        synchronized(logBuffer) {
            logBuffer.clear()
        }
    }

    private fun renderPrestigeWindow(x: Float, y: Float, width: Float, height: Float) {
        ImGui.setNextWindowPos(x, y, ImGuiCond.Once)
        ImGui.setNextWindowSize(width, height, ImGuiCond.Once)

        if (Constants.currentPrestige < 100) {
            ImGui.begin("Prestige available")
            ImGui.textWrapped(
                "Ready to prestige? This will increase your Eminence to "
                        + (Constants.currentPrestige + 1).toString() + " and reset all other attributes."
            )
            ImGui.newLine()
            if (ImGui.button("Square yourself")) {
                gl.stop()
                clearLogBuffer()
                Upgrades.prestigeResetAuto()
                log("A wave of calm washes over you.")
                log("Eminence " + Constants.currentPrestige.toRoman() + " achieved.")
            }
        } else {
            ImGui.begin("Eminence C")
            ImGui.text("Mastery achieved.")
        }

        ImGui.end()
    }

    private fun renderEventLogWindow(x: Float, width: Float, height: Float) {
        ImGui.setNextWindowPos(x, 0f, ImGuiCond.Once)
        ImGui.setNextWindowSize(width, height, ImGuiCond.Once)
        ImGui.begin("Event Log")

        ImGui.inputText("Filter", logFilter)

        if (ImGui.button("Clear")) {
            clearLogBuffer()
        }
        ImGui.sameLine()
        ImGui.checkbox("Auto-scroll", autoScroll)

        ImGui.separator()
        (ImGui.beginChild("LogRegion", 0f, 0f, true))
        synchronized(logBuffer) {
            for (line in logBuffer) {
                if (logFilter.get().isBlank() || line.contains(logFilter.get(), ignoreCase = true)) {
                    when {
                        line.contains("[ERROR]") -> ImGui.pushStyleColor(ImGuiCol.Text, 1f, 0.4f, 0.4f, 1f)   // Red
                        line.contains("[WARN]")  -> ImGui.pushStyleColor(ImGuiCol.Text, 1f, 0.8f, 0.4f, 1f)   // Yellow
                        line.contains("[INFO]")  -> ImGui.pushStyleColor(ImGuiCol.Text, 0.7f, 0.8f, 1f, 1f)   // Light blue
                        line.contains("[READY]") || line.contains("[OK]") -> ImGui.pushStyleColor(ImGuiCol.Text, 0.4f, 1f, 0.4f, 1f) // Green
                        line.contains("Eminence") || line.contains("Prestige") -> ImGui.pushStyleColor(ImGuiCol.Text, 1.0f, 0.84f, 0.0f, 1f) // Gold
                        else                     -> ImGui.pushStyleColor(ImGuiCol.Text, 0.9f, 0.9f, 0.9f, 1f) // Default gray
                    }

                    ImGui.textWrapped(line)
                    ImGui.popStyleColor()
                }
            }

            if (autoScroll.get()) {
                ImGui.setScrollHereY(1.0f) // scroll
            }
        }
        ImGui.endChild()
        ImGui.end()
    }

    private fun renderInfoWindow(x: Float, width: Float, height: Float) {
        ImGui.setNextWindowPos(x, 0f, ImGuiCond.Once)
        ImGui.setNextWindowSize(width, height, ImGuiCond.Once)
        ImGui.begin("Info")
        if (ImGui.beginTabBar("InfoTabs")) {
            if (ImGui.beginTabItem("Stats")) {
                ImGui.text("Game statistics")
                ImGui.beginChild("StatsRegion", 0f, 0f, true)
                ImGui.text(gl.statDump())
                ImGui.endChild()
                ImGui.endTabItem()
            }
            if (ImGui.beginTabItem("About")) {
                ImGui.text("clicks v${getAppVersion()} by willow")
                ImGui.text("willowyx.dev/projects/clicks")
                ImGui.separator()
                ImGui.textWrapped("special thanks to gab for testing and moral support ^-^")
                ImGui.beginChild("AboutCredits", 0f, 0f, true)
                ImGui.textWrapped("""
                    This project was made possible by the following libraries and software:
        
                    -Dear ImGui by ocornut
                    -imgui-java by SpaiR
                    -Kotlin Coroutines
                    -Shadow (for jar creation)
                    -LWJGL3
                    -GLFW3
                    -Launch4j (for Windows executable)
                    -Packages by Stéphane Sudre (for macOS installer)
                """.trimIndent())
                ImGui.endChild()
                ImGui.endTabItem()
            }
            ImGui.endTabBar()
        }
        ImGui.end()
    }

    fun render() {
        upgrades.logger = this
        constants.logger = this

        val io = ImGui.getIO()
        val displayWidth = io.displaySize.x
        val displayHeight = io.displaySize.y

        // Column widths
        val controlsWidth = displayWidth * 0.30f
        val middleWidth = displayWidth * 0.40f
        val rightWidth = displayWidth * 0.30f

        // Window heights for right column
        val topHeight = displayHeight * 0.6f
        val bottomHeight = displayHeight * 0.4f

        renderControlsWindow(controlsWidth, displayHeight)
        renderEventLogWindow(controlsWidth, middleWidth, displayHeight)

        val rightColumnX = controlsWidth + middleWidth
        if (Constants.canPrestigeCheck()) {
            renderInfoWindow(rightColumnX, rightWidth, topHeight)
            renderPrestigeWindow(rightColumnX, topHeight, rightWidth, bottomHeight)
        } else {
            renderInfoWindow(rightColumnX, rightWidth, displayHeight)
        }
    }
}
