import imgui.ImGui
import imgui.type.ImBoolean
import imgui.type.ImFloat
import imgui.type.ImString
import java.util.Properties

object UI {

    val gl = dev.willowyx.GameLogic()

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
        ImGui.begin("Main Panel")

        ImGui.text("clicks")

        if (ImGui.button("New game")) {
            gl.genStart()
        }

        ImGui.sameLine()
        ImGui.text("← Button label")

        ImGui.inputText("Name", nameInput)
        ImGui.checkbox("Enable Feature", isEnabled)
        ImGui.sliderFloat("Volume", volume, 0F, 1F)

        ImGui.text("clicks v${getAppVersion()} by willow")
        ImGui.text("willowyx.dev/projects/clicks")

        ImGui.end()
    }
}
