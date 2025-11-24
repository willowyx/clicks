import Constants
import Constants.prettyFormat
import Constants.toRoman
import imgui.ImGui
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiCond
import imgui.flag.ImGuiStyleVar
import imgui.flag.ImGuiWindowFlags
import imgui.type.ImBoolean
import imgui.type.ImInt
import imgui.type.ImString
import java.util.Properties
import CoffeeGen as cgenlogic
import Constants as constants
import Upgrades as upgrades
import State as state

object UI : GameLogger {
    private val logBuffer = mutableListOf<String>()
    private val maxLogLines = 500

    private val autoScroll = ImBoolean(true)
    private val logFilter = ImString()

    private val sizeGroup = ImInt(2)
    private val tempGroup = ImInt(0)
    private val syrupIndex = ImInt(0)
    private val drinkIndex = ImInt(0)
    private val dairyIndex = ImInt(0)
    private val sugarIndex = ImInt(2)
    private val iceAmountIndex = ImInt(2)
    private val steamedMilk = ImBoolean(false)
    private val makeDecaf = ImBoolean(false)
    private val addEspresso = ImBoolean(false)
    private val aboutInfoMOpen = ImBoolean(false)
    private val reviewReqMOpen = ImBoolean(false)
    private val saveGameMOpen = ImBoolean(false)
    private val loadGameMOpen = ImBoolean(false)
    private val saveGameInput = ImString("clicks-save")

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

        ImGui.newLine()

        if (!gl.getJobRunStateInd() && !gl.isAwaitingInput()) {
            if(gl.getIsGameStarted()) {
                if (ImGui.button("Resume")) {
                    gl.genStart()
                }
                ImGui.newLine()
            } else {
                if (ImGui.button("New game")) {
                    gl.genStart()
                }
                ImGui.sameLine()
                ImGui.text("start fresh")
            }

            if(gl.getIsGameStarted()) {
                if (ImGui.button("Save game")) {
                    saveGameMOpen.set(true)
                    ImGui.openPopup("Save game")
                }
            }
            if (ImGui.button("Load game")) {
                loadGameMOpen.set(true)
                ImGui.openPopup("Load save")
            }
        }

        if(gl.getJobRunStateInd() && !gl.isAwaitingInput()) {
            if (ImGui.button("Pause game")) {
                gl.stop()
            }
        } else if (gl.getJobRunStateInd() && gl.isAwaitingInput()) {
            ImGui.text("pending game actions")
        }

        if (ImGui.beginPopupModal("Save game", saveGameMOpen, ImGuiWindowFlags.NoMove + ImGuiWindowFlags.NoResize + ImGuiWindowFlags.NoCollapse)) {
            ImGui.textWrapped("Name your save file:")
            val childWidth = 300f
            val childHeight = 100f
            if (ImGui.beginChild("saveGameModal", childWidth, childHeight, true)) {
                ImGui.inputText("data", saveGameInput)
                ImGui.newLine()
                if(ImGui.button("Save game...")) {
                    State.initializeStateSave(saveGameInput.get())
                    if(State.saveStateDialog()) {
                        ImGui.closeCurrentPopup()
                    }
                }
                ImGui.endChild()
            }
            ImGui.newLine()
            if (ImGui.button("Close")) {
                ImGui.closeCurrentPopup()
            }
            ImGui.endPopup()
        }

        if (ImGui.beginPopupModal("Load save", loadGameMOpen, ImGuiWindowFlags.NoMove + ImGuiWindowFlags.NoResize + ImGuiWindowFlags.NoCollapse)) {
            ImGui.textWrapped("Select save data to load:")
            val childWidth = 300f
            val childHeight = 100f
            if (ImGui.beginChild("loadGameModal", childWidth, childHeight, true)) {
                if(ImGui.button("Choose file...")) {
                    if(State.loadStateDialog()) {
                        gl.genStart()
                        ImGui.closeCurrentPopup()
                    }
                }
                ImGui.endChild()
            }
            ImGui.newLine()
            if (ImGui.button("Close")) {
                ImGui.closeCurrentPopup()
            }
            ImGui.endPopup()
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
            ImGui.newLine()
        }
        if (upgrades.hedgeFund) {
            if (ImGui.button("BUY BUY BUY")) {
                upgrades.buybuybuy()
            }
            ImGui.sameLine()
            ImGui.text("invest ($${(constants.currentMoney * 0.75).toLong().coerceAtLeast(5000).prettyFormat()})")
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

        if (ImGui.button("-###clicksPerTickSub")) {
            log(constants.clicksPerTickSub())
        }
        ImGui.sameLine()
        ImGui.text("base clicks per tick")
        ImGui.sameLine()
        if (ImGui.button("+###clicksPerTickAdd")) {
            log(constants.clicksPerTickAdd())
        }
        ImGui.sameLine()
        ImGui.text("($${constants.clicksPerTickPrice().prettyFormat()})")

        if (ImGui.button("-###ticksPerSecondSub")) {
            log(constants.ticksPerSecondSub())
        }
        ImGui.sameLine()
        ImGui.text("subticks per tick")
        ImGui.sameLine()
        if (ImGui.button("+###ticksPerSecondAdd")) {
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


        ImGui.separator()
        // START TEMP QA
        ImGui.text("QA money")

        if (ImGui.button("to 0")) {
            constants.currentMoney = 0
        }
        ImGui.sameLine()
        if (ImGui.button("+100k")) {
            constants.currentMoney += 100_000
        }
        ImGui.sameLine()
        if (ImGui.button("+1m")) {
            constants.currentMoney += 1_000_000
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

        ImGui.pushStyleColor(ImGuiCol.TitleBgActive, 0.53f, 0.81f, 0.92f, 1.0f)
        ImGui.pushStyleColor(ImGuiCol.TitleBg, 0.53f, 0.81f, 0.92f, 1.0f)
        ImGui.pushStyleColor(ImGuiCol.TitleBgCollapsed, 1.0f, 1.0f, 0.8f, 1.0f)
        ImGui.pushStyleColor(ImGuiCol.FrameBg, 1.0f, 1.0f, 0.8f, 1.0f)
        ImGui.pushStyleColor(ImGuiCol.WindowBg, 1.0f, 1.0f, 0.8f, 1.0f)
        ImGui.pushStyleColor(ImGuiCol.Button, 1.0f, 1.0f, 1.0f, 1.0f)
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 1.0f, 1.0f, 1.0f, 1.0f)
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.53f, 0.81f, 0.92f, 1.0f)
        ImGui.pushStyleColor(ImGuiCol.Text, 0.0f, 0.0f, 0.0f, 1.0f)

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
                upgrades.prestigeResetAuto()
                log("A wave of calm washes over you.")
                log("Eminence " + Constants.currentPrestige.toRoman() + " achieved.")
            }
        } else {
            ImGui.begin("Eminence C")
            ImGui.text("Mastery achieved.")
        }
        ImGui.end()
        ImGui.popStyleColor(9)
    }

    // todo: popup modal after prestige?

    private fun renderCRWindow(x: Float, y: Float, width: Float, height: Float) {
        ImGui.setNextWindowPos(x, y, ImGuiCond.Once)
        ImGui.setNextWindowSize(width, height, ImGuiCond.Once)

        ImGui.pushStyleVar(ImGuiStyleVar.WindowMinSize, 200f, 300f)

        val brown = floatArrayOf(0.1f, 0.05f, 0.01f, 1.0f)
        val lightBrown = floatArrayOf(0.2f, 0.15f, 0.11f, 1.0f)
        val lighterBrown = floatArrayOf(0.3f, 0.25f, 0.21f, 1.0f)

        ImGui.pushStyleColor(ImGuiCol.WindowBg, 0.1f, 0.35f, 0.15f, 1.0f)
        ImGui.pushStyleColor(ImGuiCol.Text, 1.0f, 1.0f, 1.0f, 1.0f)
        ImGui.pushStyleColor(ImGuiCol.FrameBg, brown[0], brown[1], brown[2], 1.0f)
        ImGui.pushStyleColor(ImGuiCol.FrameBgHovered, lightBrown[0], lightBrown[1], lightBrown[2], 1.0f)
        ImGui.pushStyleColor(ImGuiCol.FrameBgActive, lighterBrown[0], lighterBrown[1], lighterBrown[2], 1.0f)
        ImGui.pushStyleColor(ImGuiCol.Button, brown[0], brown[1], brown[2], 1.0f)
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, lightBrown[0], lightBrown[1], lightBrown[2], 1.0f)
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, lighterBrown[0], lighterBrown[1], lighterBrown[2], 1.0f)
        ImGui.pushStyleColor(ImGuiCol.Header, brown[0], brown[1], brown[2], 1.0f)
        ImGui.pushStyleColor(ImGuiCol.HeaderHovered, lightBrown[0], lightBrown[1], lightBrown[2], 1.0f)
        ImGui.pushStyleColor(ImGuiCol.HeaderActive, lighterBrown[0], lighterBrown[1], lighterBrown[2], 1.0f)
        ImGui.pushStyleColor(ImGuiCol.CheckMark, 1.0f, 1.0f, 1.0f, 1.0f)
        ImGui.pushStyleColor(ImGuiCol.TitleBgActive, 0.15f, 0.15f, 0.15f, 1.0f)
        ImGui.pushStyleColor(ImGuiCol.TitleBg, 0.15f, 0.15f, 0.15f, 1.0f)
        ImGui.pushStyleColor(ImGuiCol.TitleBgCollapsed, 0.1f, 0.35f, 0.15f, 1.0f)
        ImGui.pushStyleColor(ImGuiCol.PopupBg, 0.1f, 0.05f, 0.01f, 0.95f)

        ImGui.begin("Coffee Run")

        if (ImGui.button("Check notepad")) {
            ImGui.openPopup("check order")
            reviewReqMOpen.set(true)
        }
        ImGui.newLine()

        ImGui.pushStyleColor(ImGuiCol.PopupBg, 0.6f, 0.5f, 0.0f, 1.0f)
        if (ImGui.beginPopupModal("check order", reviewReqMOpen, ImGuiWindowFlags.NoMove + ImGuiWindowFlags.NoResize + ImGuiWindowFlags.NoCollapse)) {
            ImGui.text("You need to place this order:")
            val childWidth = ImGui.getContentRegionAvailX()
            val childHeight = 200f
            if (ImGui.beginChild("orderOutput", childWidth, childHeight, true)) {
                val orderText = gl.getTargetOrder().let { cgenlogic.formatOrderData(it) }
                ImGui.textWrapped(orderText)
                ImGui.endChild()
            }
            ImGui.newLine()
            if (ImGui.button("Got it")) {
                ImGui.closeCurrentPopup()
            }
            ImGui.sameLine()
            if (ImGui.button("Regen order")) {
                gl.regenCoffeeOrder()
            }
            ImGui.endPopup()
        }
        ImGui.popStyleColor()

        ImGui.beginGroup()
        ImGui.radioButton("XS", sizeGroup, 0)
        ImGui.sameLine()
        ImGui.radioButton("Small", sizeGroup, 1)
        ImGui.sameLine()
        ImGui.radioButton("Medium", sizeGroup, 2)
        ImGui.sameLine()
        ImGui.radioButton("Large", sizeGroup, 3)
        ImGui.endGroup()
        ImGui.newLine()

        ImGui.beginGroup()
        ImGui.radioButton("Hot", tempGroup, 0)
        ImGui.sameLine()
        ImGui.radioButton("Iced", tempGroup, 1)
        ImGui.endGroup()
        ImGui.newLine()

        val syrup = arrayOf(
            "Caramel",
            "Dark chocolate",
            "Pecan",
            "Pumpkin spice",
            "Vanilla",
            "None"
        )
        ImGui.combo("Syrup", syrupIndex, syrup, syrup.size)
        if (ImGui.isItemHovered()) {
            ImGui.pushStyleColor(ImGuiCol.Text, 1.0f, 1.0f, 1.0f, 1.0f)
            ImGui.setTooltip("sweetened syrups")
            ImGui.popStyleColor()
        }
        ImGui.newLine()

        val drink = arrayOf(
            "Americano",
            "Black",
            "Breve",
            "Cappucino",
            "Cold Brew",
            "Espresso",
            "Hot Chocolate",
            "Latte",
            "Macchiato"
        )
        ImGui.combo("Drink", drinkIndex, drink, drink.size)
        ImGui.newLine()

        val dairy = arrayOf(
            "2% milk",
            "Almond milk",
            "Condensed milk",
            "Cream",
            "Half-and-half",
            "Oat milk",
            "Skim milk",
            "Soy milk",
            "Whole milk",
            "None"
        )
        ImGui.combo("Dairy", dairyIndex, dairy, dairy.size)
        ImGui.newLine()

        ImGui.separator()

        ImGui.beginGroup()
        ImGui.labelText("", "Additional options")
        ImGui.newLine()

        val sugar = arrayOf(
            "Add sugar syrup",
            "Add granulated sugar",
            "Regular sugar",
            "NO SUGAR"
        )
        ImGui.combo("Sugar", sugarIndex, sugar, sugar.size)

        val iceAmount = arrayOf(
            "No ice",
            "Light ice",
            "Regular ice",
            "Extra ice"
        )
        ImGui.combo("Ice amount", iceAmountIndex, iceAmount, iceAmount.size)
        ImGui.newLine()

        ImGui.checkbox("Steamed milk", steamedMilk)
        if (ImGui.isItemHovered()) {
            ImGui.pushStyleColor(ImGuiCol.Text, 1.0f, 1.0f, 1.0f, 1.0f)
            ImGui.setTooltip("steam the selected dairy option")
            ImGui.popStyleColor()
        }

        ImGui.checkbox("Make decaf", makeDecaf)

        ImGui.checkbox("Add espresso", addEspresso)

        ImGui.endGroup()

        ImGui.newLine()
        ImGui.separator()
        ImGui.newLine()

        if (ImGui.button("Place order >>")) {
            val userOrder = CoffeeOrderFormData (
                size = if (sizeGroup.get() == 0) "extra small" else if (sizeGroup.get() == 1) "small" else if (sizeGroup.get() == 2) "medium" else "large",
                temp = if (tempGroup.get() == 0) "hot" else "iced",
                syrup = syrup[syrupIndex.get()].lowercase(),
                type = drink[drinkIndex.get()].lowercase(),
                dairy = dairy[dairyIndex.get()].lowercase(),
                iceAmount = iceAmount[iceAmountIndex.get()].lowercase(),
                sugar = sugar[sugarIndex.get()].lowercase(),
                isDecaf = makeDecaf.get(),
                addEspresso = addEspresso.get(),
                steamed = steamedMilk.get(),
            )
            log("[INFO] User placed order: $userOrder")
            gl.setUserOrder(userOrder)
            log("[INFO] SCORED " + cgenlogic.scoreCoffeeGen(gl.getUserOrder()))

//            gl.regenCoffeeOrder() // regen once order is placed
        }
        ImGui.end()
        ImGui.popStyleColor(16)
        ImGui.popStyleVar()
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

        ImGui.beginChild("LogRegion", 0f, 0f, true)
        synchronized(logBuffer) {
            for (line in logBuffer) {
                if (logFilter.get().isBlank() || line.contains(logFilter.get(), ignoreCase = true)) {
                    when {
                        line.contains("[ERROR]") -> ImGui.pushStyleColor(ImGuiCol.Text, 1f, 0.4f, 0.4f, 1f)   // Red
                        line.contains("[WARN]") || line.contains("Eminence") || line.contains("Prestige")
                              -> ImGui.pushStyleColor(ImGuiCol.Text, 1f, 0.8f, 0.4f, 1f)   // Yellow
                        line.contains("[INFO]")  -> ImGui.pushStyleColor(ImGuiCol.Text, 0.7f, 0.8f, 1f, 1f)   // Light blue
                        line.contains("[READY]") || line.contains("[OK]") -> ImGui.pushStyleColor(ImGuiCol.Text, 0.4f, 1f, 0.4f, 1f) // Green
                        else                     -> ImGui.pushStyleColor(ImGuiCol.Text, 0.9f, 0.9f, 0.9f, 1f) // Default gray
                    }
                    ImGui.textWrapped(line)
                    ImGui.popStyleColor()
                }
            }
            if (autoScroll.get()) {
                ImGui.setScrollHereY(1.0f)
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
                ImGui.newLine()
                if (ImGui.button("Credits & thanks")) {
                    ImGui.openPopup("Credits & thanks")
                    aboutInfoMOpen.set(true)
                }

                if (ImGui.beginPopupModal("Credits & thanks", aboutInfoMOpen, ImGuiWindowFlags.NoMove + ImGuiWindowFlags.NoResize + ImGuiWindowFlags.NoCollapse)) {
                    ImGui.newLine()
                    if(ImGui.beginChild("AboutCredits", 500f, 300f, true)) {
                        ImGui.textWrapped(
                            """
                            This project was made possible by the following open-source libraries and software:
                            
                            -Dear ImGui by ocornut
                            -imgui-java by SpaiR
                            -Shadow (jar creation)
                            -LWJGL3
                            -GLFW3
                            -Launch4j (Windows executable)
                        """.trimIndent()
                        )
                        ImGui.newLine()

                        ImGui.separator()
                        ImGui.newLine()

                        ImGui.textWrapped("Special thanks to gab and westo for testing and moral support!! ^-^")
                        ImGui.newLine()

                        ImGui.separator()
                        ImGui.newLine()

                        ImGui.textWrapped("Finally: thank you for taking the time to try out my thing!")
                    }
                    ImGui.endChild()

                    ImGui.newLine()
                    if (ImGui.button("Close")) {
                        ImGui.closeCurrentPopup()
                    }
                    ImGui.endPopup()
                }
                ImGui.endTabItem()
            }
            ImGui.endTabBar()
        }
        ImGui.end()
    }

    fun render() {
        upgrades.logger = this
        constants.logger = this
        cgenlogic.logger = this
        state.logger = this

        val io = ImGui.getIO()
        val displayWidth = io.displaySize.x
        val displayHeight = io.displaySize.y

        val controlsWidth = displayWidth * 0.30f
        val middleWidth = displayWidth * 0.40f
        val rightWidth = displayWidth * 0.30f

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
        if (Upgrades.CRInternStatus) {
            renderCRWindow(50f, 50f, 300f, displayHeight / 2)
        }
    }
}
