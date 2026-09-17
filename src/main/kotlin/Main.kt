import imgui.ImGui
import imgui.ImGuiIO
import imgui.ImFont
import imgui.ImFontConfig
import imgui.flag.ImGuiConfigFlags
import imgui.gl3.ImGuiImplGl3
import imgui.glfw.ImGuiImplGlfw
import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryUtil.NULL
import java.nio.file.Files
import java.nio.file.Path
import kotlin.system.exitProcess

object Main {
    private var imGuiGl3: ImGuiImplGl3? = null
    private var imGuiGlfw: ImGuiImplGlfw? = null
    private var glslVersion: String = "#version 150"
    private var wShouldQuit: Boolean = false
    private var currentFontName: String = "clicks UI default"
    private var monoFont: ImFont? = null
    private var monoFontName: String = "clicks UI mono"
    private const val UI_FONT_SIZE = 16.0f

    fun getCurrentFontName(): String {
        return currentFontName
    }

    fun getMonoDef(): ImFont? {
        return monoFont
    }

    fun getMonoDefName(): String {
        return monoFontName
    }

    @JvmStatic
    fun main(args: Array<String>) {
        GLFWErrorCallback.createPrint(System.err).set()
        if (!glfwInit()) error("Unable to initialize GLFW")

        fun decideGlGlslVersions() {
            if (System.getProperty("os.name").lowercase().contains("mac")) {
//                glslVersion = "#version 150"
                glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
                glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2)
                glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE)
                glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)
            } else {
                glslVersion = "#version 330"
                glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
                glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 0)
            }
        }

        glfwDefaultWindowHints()
        decideGlGlslVersions()

        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)

        val window = glfwCreateWindow(800, 600, "clicks", NULL, NULL)
        if (window == NULL) error("Failed to create window")
        glfwMakeContextCurrent(window)
        glfwSwapInterval(1) // vsync
        glfwShowWindow(window)

        GL.createCapabilities()
        ImGui.createContext()

        imGuiGlfw = ImGuiImplGlfw()
        imGuiGlfw!!.init(window, true)

        val io: ImGuiIO = ImGui.getIO()
        io.iniFilename = null
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable)
        configureFonts(io)

        imGuiGl3 = ImGuiImplGl3().apply { init(glslVersion) }

        while (!glfwWindowShouldClose(window) && !wShouldQuit) {
            glfwPollEvents()

            val fbWidth = IntArray(1)
            val fbHeight = IntArray(1)
            glfwGetFramebufferSize(window, fbWidth, fbHeight)

            val io = ImGui.getIO()
            io.displaySize.x = fbWidth[0].toFloat()
            io.displaySize.y = fbHeight[0].toFloat()

            if (fbWidth[0] > 0 && fbHeight[0] > 0) {
                // check frame start requirements
                imGuiGlfw!!.newFrame()
                imGuiGl3!!.newFrame()
                ImGui.newFrame()

                UI.render()

                ImGui.render()
                glViewport(0, 0, fbWidth[0], fbHeight[0])
                glClearColor(0.1f, 0.1f, 0.1f, 1f)
                glClear(GL_COLOR_BUFFER_BIT)
                imGuiGl3!!.renderDrawData(ImGui.getDrawData())
            } else {
                // skip imgui frame if invalid display size
                glClearColor(0.1f, 0.1f, 0.1f, 1f)
                glClear(GL_COLOR_BUFFER_BIT)
            }
            glfwSwapBuffers(window)
        }

        // cleanup
        UI.gl.stop()
        imGuiGlfw?.shutdown()
        imGuiGl3?.shutdown()
        ImGui.destroyContext()
        glfwFreeCallbacks(window)
        glfwDestroyWindow(window)
        glfwTerminate()
        glfwSetErrorCallback(null)?.free()
        exitProcess(0)
    }

    private fun configureFonts(io: ImGuiIO) {
        val fonts = io.fonts
        val config = ImFontConfig()

        try {
            fonts.setFreeTypeRenderer(true)
            config.oversampleH = 3
            config.oversampleV = 1
            config.pixelSnapH = false
            config.rasterizerMultiply = 1.05f

            val fontPath = findPreferredFontPath()
            if (fontPath != null) {
                currentFontName = fontPath.fileName.toString()
                config.setName("clicks UI: $currentFontName")
                fonts.addFontFromFileTTF(fontPath.toString(), UI_FONT_SIZE, config)
            } else {
                config.sizePixels = UI_FONT_SIZE
                currentFontName = "clicks UI default"
                config.setName("clicks UI default")
                fonts.addFontDefault(config)
            }

            val monoDefPath = findPreferredMonoDefPath()
            if (monoDefPath != null) {
                monoFontName = monoDefPath.fileName.toString()
                config.setName("clicks mono: $monoFontName")
                monoFont = fonts.addFontFromFileTTF(monoDefPath.toString(), UI_FONT_SIZE, config)
            } else {
                monoFont = null
                monoFontName = "clicks UI mono"
            }
        } catch (e: RuntimeException) {
            System.err.println("Unable to configure interface font: ${e.message}")
            fonts.clear()
            currentFontName = "clicks UI default"
            monoFont = null
            monoFontName = "clicks UI mono"
            fonts.addFontDefault()
        } finally {
            config.destroy()
        }
    }

    private fun findPreferredFontPath(): Path? {
        return fontCandidates().firstOrNull { Files.isRegularFile(it) && Files.isReadable(it) }
    }

    private fun findPreferredMonoDefPath(): Path? {
        return monoFontCandidates().firstOrNull { Files.isRegularFile(it) && Files.isReadable(it) }
    }

    private fun fontCandidates(): List<Path> {
        val userHome = System.getProperty("user.home").orEmpty()
        val explicitFontPaths = listOfNotNull(
            System.getProperty("clicks.font.path"),
            System.getenv("CLICKS_FONT_PATH")
        ).filter { it.isNotBlank() }

        val installedFontPaths = listOf(
            "C:\\Windows\\Fonts\\segoeui.ttf",
            "$userHome/Library/Fonts/Inter-Regular.ttf",
            "/Library/Fonts/Inter-Regular.ttf",
            "/System/Library/Fonts/SFNS.ttf",
            "$userHome/.local/share/fonts/Inter-Regular.ttf",
            "/usr/share/fonts/truetype/inter/Inter-Regular.ttf",
            "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
            "/usr/share/fonts/TTF/DejaVuSans.ttf"
        )

        return (explicitFontPaths + installedFontPaths)
            .map { Path.of(it).toAbsolutePath().normalize() }
            .distinct()
    }

    private fun monoFontCandidates(): List<Path> {
        val userHome = System.getProperty("user.home").orEmpty()
        val explicitFontPaths = listOfNotNull(
            System.getProperty("clicks.monoFont.path"),
            System.getenv("CLICKS_MONO_FONT_PATH")
        ).filter { it.isNotBlank() }

        val installedFontPaths = listOf(
            "C:\\Windows\\Fonts\\CascadiaMono.ttf",
            "C:\\Windows\\Fonts\\CascadiaCode.ttf",
            "C:\\Windows\\Fonts\\consola.ttf",
            "C:\\Windows\\Fonts\\cour.ttf",
            "$userHome/Library/Fonts/JetBrainsMono-Regular.ttf",
            "$userHome/.local/share/fonts/JetBrainsMono-Regular.ttf",
            "/usr/share/fonts/truetype/jetbrains-mono/JetBrainsMono-Regular.ttf",
            "/usr/share/fonts/truetype/dejavu/DejaVuSansMono.ttf",
            "/usr/share/fonts/TTF/DejaVuSansMono.ttf"
        )

        return (explicitFontPaths + installedFontPaths)
            .map { Path.of(it).toAbsolutePath().normalize() }
            .distinct()
    }
}
