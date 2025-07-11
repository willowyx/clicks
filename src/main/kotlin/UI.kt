import imgui.ImGui
import imgui.flag.ImGuiCol
import imgui.type.ImBoolean
import imgui.type.ImString
import java.util.Properties

object UI : GameLogger {

    private val logBuffer = mutableListOf<String>()
    private val maxLogLines = 500

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
        val stream = Main::class.java.classLoader.getResourceAsStream("version.properties")
        return if (stream != null) {
            props.load(stream)
            props.getProperty("version") ?: "unknown"
        } else {
            "unknown"
        }
    }

    private val nameInput = ImString(128)
    private val isEnabled = ImBoolean(true)
    private val volume = floatArrayOf(0.5f)

    fun render() {
        ImGui.begin("Game control")

        ImGui.text("clicks")

        if (ImGui.button("New game")) {
            gl.genStart()
        }
        ImGui.sameLine()
        ImGui.text("start fresh")

        ImGui.inputText("Name", nameInput)
        if (ImGui.button("Save game")) {
            gl.stop()

            log("[INFO] would save game state as ${nameInput.get()}")
        }

        if (ImGui.button("Load game")) {
            log("[INFO] would load a saved game state")
        }

        ImGui.checkbox("Enable Feature", isEnabled)
        ImGui.sliderFloat("Volume", volume, 0F, 1F)

        ImGui.text("clicks v${getAppVersion()} by willow")
        ImGui.text("willowyx.dev/projects/clicks")
        ImGui.end()

        // Render game logs window
        ImGui.begin("Game Logs")

        ImGui.inputText("Filter", logFilter)

        if (ImGui.button("Clear")) {
            synchronized(logBuffer) {
                logBuffer.clear()
            }
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
                        line.contains("[WARN]")  -> ImGui.pushStyleColor(ImGuiCol.Text, 1f, 0.8f, 0.3f, 1f)   // Orange
                        line.contains("[INFO]")  -> ImGui.pushStyleColor(ImGuiCol.Text, 0.7f, 0.8f, 1f, 1f)   // Light blue
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
}
