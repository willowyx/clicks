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
import Mods as mods
import State as state

object UI : GameLogger {
    private val logBuffer = mutableListOf<String>()
    private var maxLogLines = 500
    fun setMaxLogLines(max: Int) {
        if(max in 1..<100_000) { maxLogLines = max }
    }
    fun getMaxLogLines(): Int { return maxLogLines }

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
    private val debugMode = ImBoolean(false)
    private val rewardGroup = ImInt(1)
    private val aboutInfoMOpen = ImBoolean(false)
    private val reviewReqMOpen = ImBoolean(false)
    private val saveGameMOpen = ImBoolean(false)
    private val loadGameMOpen = ImBoolean(false)
    
    private var layoutMode = 0
    // 0 = modern (default), 1 = columns, 2 = columns (classic)
    fun setLayoutMode(preset: Int) {
        resetLayout = true
        layoutMode = preset
    }
    fun getLayoutMode(): Int {
        return layoutMode
    }
    private var resetLayout = false

    override fun log(message: String) {
        synchronized(logBuffer) {
            logBuffer.add(message)
            while (logBuffer.size > maxLogLines) {
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

    private fun renderControlsWindow(x: Float, y: Float, width: Float, height: Float, cond: Int) {
        // start window style
        ImGui.pushStyleColor(ImGuiCol.TitleBgActive, 0f, 0.05f, 0.1f, 1.0f)
        ImGui.pushStyleColor(ImGuiCol.WindowBg, 0f, 0.22f, 0.31f, 1.0f)
        ImGui.pushStyleColor(ImGuiCol.FrameBg, 1.0f, 1.0f, 0.8f, 1.0f)
        ImGui.pushStyleColor(ImGuiCol.PopupBg, 0f, 0.22f, 0.31f, 1.0f)
        ImGui.pushStyleColor(ImGuiCol.ModalWindowDimBg, 0f, 0.06f, 0.11f, 0.7f)
        ImGui.pushStyleColor(ImGuiCol.Button, 0.22f, 0.01f, 0f, 1.0f)
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.41f, 0.14f, 0f, 1.0f)
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.54f, 0.01f, 0f, 1.0f)
        ImGui.pushStyleColor(ImGuiCol.Text, 1.0f, 0.78f, 0.69f, 1.0f)
        // end window style

        ImGui.setNextWindowPos(x, y, cond)
        ImGui.setNextWindowSize(width, height, cond)
        ImGui.begin("Controls", ImGuiWindowFlags.HorizontalScrollbar)
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
            ImGui.textWrapped("Save your game to a file:")
            val childWidth = 300f
            val childHeight = 100f
            if (ImGui.beginChild("saveGameModal", childWidth, childHeight, true)) {
                if(ImGui.button("Save game...")) {
                    State.initializeStateSave()
                    if(State.saveStateDialog()) {
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
        if (mods.hedgeFund) {
            if (ImGui.button("BUY BUY BUY")) {
                mods.buybuybuy()
            }
            ImGui.sameLine()
            ImGui.text("invest ($${(constants.currentMoney * 0.75).toLong().coerceAtLeast(5000).prettyFormat()})")
        }

        ImGui.separator()
        // START UPGRADES
        ImGui.textWrapped("Upgrades")

        ImGui.text("clicks per pack")
        if (ImGui.isItemHovered()) {
            ImGui.pushStyleColor(ImGuiCol.PopupBg, 0f, 0.05f, 0.1f, 1.0f)
            ImGui.setTooltip("the number of clicks required per package")
            ImGui.popStyleColor()
        }
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
        ImGui.text("base clicks")
        if (ImGui.isItemHovered()) {
            ImGui.pushStyleColor(ImGuiCol.PopupBg, 0f, 0.05f, 0.1f, 1.0f)
            ImGui.setTooltip("base number of clicks generated per tick")
            ImGui.popStyleColor()
        }
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
        ImGui.text("subticks")
        if (ImGui.isItemHovered()) {
            ImGui.pushStyleColor(ImGuiCol.PopupBg, 0f, 0.05f, 0.1f, 1.0f)
            ImGui.setTooltip("clicks are generated this many times per tick")
            ImGui.popStyleColor()
        }
        ImGui.sameLine()
        if (ImGui.button("+###ticksPerSecondAdd")) {
            log(constants.ticksPerSecondAdd())
        }
        ImGui.sameLine()
        ImGui.text("($${constants.ticksPerSecondPrice().prettyFormat()})")

        ImGui.text("base reward")
        if (ImGui.isItemHovered()) {
            ImGui.pushStyleColor(ImGuiCol.PopupBg, 0f, 0.05f, 0.1f, 1.0f)
            ImGui.setTooltip("base reward each time clicks are packaged")
            ImGui.popStyleColor()
        }
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
        if (ImGui.isItemHovered()) {
            ImGui.pushStyleColor(ImGuiCol.PopupBg, 0f, 0.05f, 0.1f, 1.0f)
            ImGui.setTooltip("bonus package reward interval (fewer is more frequent)")
            ImGui.popStyleColor()
        }
        ImGui.sameLine()
        ImGui.text("($${constants.bonusPayIntervalPrice().prettyFormat()})")

        ImGui.text("bonus multiplier")
        if (ImGui.isItemHovered()) {
            ImGui.pushStyleColor(ImGuiCol.PopupBg, 0f, 0.05f, 0.1f, 1.0f)
            ImGui.setTooltip("bonuses multiply base reward by this amount")
            ImGui.popStyleColor()
        }
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
        ImGui.text("package range")
        if (ImGui.isItemHovered()) {
            ImGui.pushStyleColor(ImGuiCol.PopupBg, 0f, 0.05f, 0.1f, 1.0f)
            ImGui.setTooltip("range below clicksPerPack in which clicks may be packaged")
            ImGui.popStyleColor()
        }
        ImGui.sameLine()
        if (ImGui.button("+###fuzzySelectRangeAdd")) {
            log(constants.fuzzySelectRangeAdd())
        }
        ImGui.sameLine()
        ImGui.text("($${constants.fuzzySelectRangePrice().prettyFormat()})")

        ImGui.text("penalty interval")
        if (ImGui.isItemHovered()) {
            ImGui.pushStyleColor(ImGuiCol.PopupBg, 0f, 0.05f, 0.1f, 1.0f)
            ImGui.setTooltip("penalty applied for every x clicks away from a perfect pack")
            ImGui.popStyleColor()
        }
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
        if (ImGui.isItemHovered()) {
            ImGui.pushStyleColor(ImGuiCol.PopupBg, 0f, 0.05f, 0.1f, 1.0f)
            ImGui.setTooltip("package penalty cannot exceed this percentage of reward")
            ImGui.popStyleColor()
        }
        ImGui.sameLine()
        ImGui.text("($${constants.maxPenaltyPrice().prettyFormat()})")

        ImGui.text("min reward")
        if (ImGui.isItemHovered()) {
            ImGui.pushStyleColor(ImGuiCol.PopupBg, 0f, 0.05f, 0.1f, 1.0f)
            ImGui.setTooltip("any reward may not be less than this amount")
            ImGui.popStyleColor()
        }
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
            mods.autoPackToggle()
        }
        ImGui.sameLine()
        ImGui.text("(${if (mods.autoPack) "ON" else "$${mods.autoPackSp}"})")

        if (ImGui.button("Hedge fund")) {
            mods.startHedgeFund()
        }
        ImGui.sameLine()
        ImGui.text("(${if (mods.hedgeFund) "give up?" else "$${mods.hedgeFundSp.toLong().prettyFormat()}"})")

        if(!mods.CRInternStatus) {
            if (ImGui.button("Coffee Run")) {
                mods.startCRIntern()
            }
            ImGui.sameLine()
            ImGui.text("($${mods.CRInternSp.prettyFormat()})")
        }

        if (ImGui.button("sell everything")) {
            if (constants.getRefundEligibility()) {
                mods.modResetClick()
            } else {
                log("[WARN] You must upgrade every attribute at least once to use this mod.")
            }
        }
        ImGui.sameLine()
        ImGui.text("(+$${constants.getRefundPrice().prettyFormat()})")

        ImGui.end()
        ImGui.popStyleColor(9)
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
        ImGui.pushStyleColor(ImGuiCol.WindowBg, 1.0f, 1.0f, 0.8f, 1.0f)
        ImGui.pushStyleColor(ImGuiCol.FrameBg, 1.0f, 1.0f, 0.8f, 1.0f)
        ImGui.pushStyleColor(ImGuiCol.Button, 1.0f, 1.0f, 1.0f, 1.0f)
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 1.0f, 1.0f, 1.0f, 1.0f)
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.53f, 0.81f, 0.92f, 1.0f)
        ImGui.pushStyleColor(ImGuiCol.Text, 0f, 0f, 0f, 1.0f)

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
                mods.prestigeResetAuto()
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
        ImGui.pushStyleColor(ImGuiCol.FrameBg, brown[0], brown[1], brown[2], 1.0f)
        ImGui.pushStyleColor(ImGuiCol.FrameBgHovered, lightBrown[0], lightBrown[1], lightBrown[2], 1.0f)
        ImGui.pushStyleColor(ImGuiCol.FrameBgActive, lighterBrown[0], lighterBrown[1], lighterBrown[2], 1.0f)
        ImGui.pushStyleColor(ImGuiCol.PopupBg, 0.1f, 0.05f, 0.01f, 0.95f)
        ImGui.pushStyleColor(ImGuiCol.Button, brown[0], brown[1], brown[2], 1.0f)
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, lightBrown[0], lightBrown[1], lightBrown[2], 1.0f)
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, lighterBrown[0], lighterBrown[1], lighterBrown[2], 1.0f)
        ImGui.pushStyleColor(ImGuiCol.Header, brown[0], brown[1], brown[2], 1.0f)
        ImGui.pushStyleColor(ImGuiCol.HeaderHovered, lightBrown[0], lightBrown[1], lightBrown[2], 1.0f)
        ImGui.pushStyleColor(ImGuiCol.HeaderActive, lighterBrown[0], lighterBrown[1], lighterBrown[2], 1.0f)
        ImGui.pushStyleColor(ImGuiCol.CheckMark, 1.0f, 1.0f, 1.0f, 1.0f)
        ImGui.pushStyleColor(ImGuiCol.TitleBg, 0.15f, 0.15f, 0.15f, 1.0f)
        ImGui.pushStyleColor(ImGuiCol.TitleBgActive, 0.15f, 0.15f, 0.15f, 1.0f)
        ImGui.pushStyleColor(ImGuiCol.TitleBgCollapsed, 0.1f, 0.35f, 0.15f, 1.0f)
        ImGui.pushStyleColor(ImGuiCol.Text, 1.0f, 1.0f, 1.0f, 1.0f)

        ImGui.begin("Coffee Run")

        if (ImGui.button("Check notepad")) {
            ImGui.openPopup("check order")
            reviewReqMOpen.set(true)
        }
        ImGui.newLine()

        ImGui.pushStyleColor(ImGuiCol.PopupBg, 0.6f, 0.5f, 0f, 1.0f)
        if (ImGui.beginPopupModal(
                "check order",
                reviewReqMOpen,
                ImGuiWindowFlags.NoMove + ImGuiWindowFlags.NoResize + ImGuiWindowFlags.NoCollapse
            )
        ) {
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
            "2 percent milk",
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

        ImGui.beginGroup()
        ImGui.text("Attribute to upgrade:")
        ImGui.newLine()
        ImGui.radioButton("baseReward", rewardGroup, 0)
        ImGui.sameLine()
        ImGui.radioButton("minReward", rewardGroup, 1)
        ImGui.endGroup()

        ImGui.newLine()
        ImGui.separator()
        ImGui.newLine()

        if (ImGui.button("Place order >>")) {
            val userOrder = CoffeeOrderFormData(
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
                reward = rewardGroup.get(),     // 0 = baseReward, 1 = minReward
                debug = debugMode.get()
            )
            gl.setUserOrder(userOrder)
            val cgenScoreVal = cgenlogic.scoreCoffeeGen(gl.getUserOrder())
            if (cgenlogic.getDebugEnabled()) {
                log("[INFO] User placed order: $userOrder")
                log("[INFO] Target order: ${cgenlogic.getValidatedOrder()}")
            }
            log("[INFO] SCORED $cgenScoreVal")

            if (!cgenlogic.getDebugEnabled()) {     // if debug mode is NOT enabled
                mods.calcApplyCRBonus(cgenScoreVal, cgenlogic.getRewardType())
                gl.regenCoffeeOrder()               // regen once order is placed
            } else {
                log("[WARN] Debug mode enabled, score not applied")
            }
        }
        ImGui.sameLine()
        ImGui.checkbox("debug mode", debugMode)
        if (ImGui.isItemHovered()) {
            ImGui.pushStyleColor(ImGuiCol.Text, 1.0f, 1.0f, 1.0f, 1.0f)
            ImGui.setTooltip("QA: prevent orders from refreshing after submitting")
            ImGui.popStyleColor()
        }
        if (ImGui.isItemDeactivated() && !debugMode.get()) {
            log("[INFO] debug mode deactivated") // could refresh order
        }

        ImGui.end()
        ImGui.popStyleColor(16)
        ImGui.popStyleVar()
    }

    private fun renderEventLogWindow(x: Float, y: Float, width: Float, height: Float, cond: Int) {
        // start window style
        ImGui.pushStyleColor(ImGuiCol.TitleBgActive, 0f, 0.05f, 0.1f, 1.0f)
        ImGui.pushStyleColor(ImGuiCol.WindowBg, 0f, 0.22f, 0.31f, 1.0f)
        ImGui.pushStyleColor(ImGuiCol.FrameBg, 0.22f, 0f, 0f, 1.0f)
        ImGui.pushStyleColor(ImGuiCol.ChildBg, 0f, 0.06f, 0.11f, 1.0f)
        ImGui.pushStyleColor(ImGuiCol.Button, 0.22f, 0f, 0f, 1.0f)
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.41f, 0.14f, 0f, 1.0f)
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.54f, 0.01f, 0f, 1.0f)
        ImGui.pushStyleColor(ImGuiCol.CheckMark, 1.0f, 0.78f, 0.69f, 1.0f)
        ImGui.pushStyleColor(ImGuiCol.Text, 1.0f, 0.78f, 0.69f, 1.0f)
        // end window style

        ImGui.setNextWindowPos(x, y, cond)
        ImGui.setNextWindowSize(width, height, cond)

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
        ImGui.popStyleColor(9)
    }

    private fun renderInfoWindow(x: Float, y: Float, width: Float, height: Float, cond: Int) {
        // start window style
        ImGui.pushStyleColor(ImGuiCol.TitleBgActive, 0f, 0.05f, 0.1f, 1.0f)
        ImGui.pushStyleColor(ImGuiCol.WindowBg, 0f, 0.22f, 0.31f, 1.0f)
        ImGui.pushStyleColor(ImGuiCol.FrameBg, 0.22f, 0f, 0f, 1.0f)
        ImGui.pushStyleColor(ImGuiCol.ChildBg, 0f, 0.06f, 0.11f, 1.0f)
        ImGui.pushStyleColor(ImGuiCol.PopupBg, 0f, 0.22f, 0.31f, 1.0f)
        ImGui.pushStyleColor(ImGuiCol.ModalWindowDimBg, 0f, 0.06f, 0.11f, 0.7f)
        ImGui.pushStyleColor(ImGuiCol.Tab, 0f, 0.05f, 0.1f, 1.0f)
        ImGui.pushStyleColor(ImGuiCol.Button, 0.22f, 0f, 0f, 1.0f)
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.42f, 0.14f, 0f, 1.0f)
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.54f, 0.01f, 0f, 1.0f)
        ImGui.pushStyleColor(ImGuiCol.CheckMark, 1.0f, 0.78f, 0.69f, 1.0f)
        ImGui.pushStyleColor(ImGuiCol.Text, 1.0f, 0.78f, 0.69f, 1.0f)
        // end window style

        ImGui.setNextWindowPos(x, y, cond)
        ImGui.setNextWindowSize(width, height, cond)
        ImGui.begin("Info", ImGuiWindowFlags.HorizontalScrollbar)
        if (ImGui.beginTabBar("InfoTabs")) {
            if (ImGui.beginTabItem("Stats")) {
                ImGui.text("Game statistics")
                ImGui.beginChild("StatsRegion", 0f, 0f, true)
                ImGui.text(gl.statDump())
                ImGui.endChild()
                ImGui.endTabItem()
            }
            if (ImGui.beginTabItem("Settings")) {
                ImGui.text("Prefs")
                ImGui.beginChild("PrefsList", 0f, 0f, true)

                ImGui.text("Layout preset")
                if(ImGui.button("Modern")) {
                    layoutMode = 0
                    resetLayout = true
                }
                ImGui.sameLine()
                if(ImGui.button("Columns")) {
                    layoutMode = 1
                    resetLayout = true
                }
                ImGui.sameLine()
                if(ImGui.button("Classic")) {
                    layoutMode = 2
                    resetLayout = true
                }

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

                        ImGui.textWrapped(gl.getValidatedCredits())
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
        ImGui.popStyleColor(12)
    }

    fun render() {
        mods.logger = this
        constants.logger = this
        cgenlogic.logger = this
        state.logger = this

        val io = ImGui.getIO()
        val displayWidth = io.displaySize.x
        val displayHeight = io.displaySize.y

        var cond = ImGuiCond.Once
        if (resetLayout) {
            cond = ImGuiCond.Always
            resetLayout = false
        }

        if (getLayoutMode() in listOf(1, 2)) {
            val colWidth = displayWidth / 3f
            renderControlsWindow(0f, 0f, colWidth, displayHeight, cond)
            renderEventLogWindow(colWidth, 0f, colWidth, displayHeight, cond)
            renderInfoWindow(colWidth * 2, 0f, colWidth, displayHeight, cond)
        } else {
            val rightWidth = displayWidth * 0.4f
            val leftWidth = displayWidth - rightWidth
            val topHeight = displayHeight * 0.5f
            val bottomHeight = displayHeight - topHeight

            renderControlsWindow(0f, 0f, leftWidth, topHeight, cond)
            renderEventLogWindow(0f, topHeight, leftWidth, bottomHeight, cond)
            renderInfoWindow(leftWidth, 0f, rightWidth, displayHeight, cond)
        }

        if (Constants.canPrestigeCheck()) {
            renderPrestigeWindow(50f, 50f, 200f, displayHeight / 2)
        }
        if (Mods.CRInternStatus) {
            renderCRWindow(50f, 50f, 300f, displayHeight / 2)
        }
    }
}
