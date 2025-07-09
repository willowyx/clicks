import imgui.ImGui
import imgui.ImGuiIO
import imgui.flag.ImGuiConfigFlags
import imgui.gl3.ImGuiImplGl3
import imgui.glfw.ImGuiImplGlfw
import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryUtil.NULL

object Main {
    private var imGuiGl3: ImGuiImplGl3? = null
    private var imGuiGlfw: ImGuiImplGlfw? = null


    @JvmStatic
    fun main(args: Array<String>) {
        // create error callback
        GLFWErrorCallback.createPrint(System.err).set()
        if (!glfwInit()) error("Unable to initialize GLFW")

        glfwDefaultWindowHints()
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)

        val window = glfwCreateWindow(800, 600, "imgui::clicks v", NULL, NULL)
        if (window == NULL) error("Failed to create window")
        glfwMakeContextCurrent(window)
        glfwSwapInterval(1) // V-sync
        glfwShowWindow(window)

        GL.createCapabilities()
        ImGui.createContext()

        imGuiGlfw = ImGuiImplGlfw()
        imGuiGlfw!!.init(window, true)

        imGuiGl3 = ImGuiImplGl3()
        imGuiGl3!!.init("#version 330")

        val io: ImGuiIO = ImGui.getIO()
        io.iniFilename = null
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable)

        imGuiGl3 = ImGuiImplGl3().apply { init("#version 330") }

        // Main loop
        while (!glfwWindowShouldClose(window)) {
            glfwPollEvents()

            val fbWidth = IntArray(1)
            val fbHeight = IntArray(1)
            glfwGetFramebufferSize(window, fbWidth, fbHeight)

            val io = ImGui.getIO()
            io.displaySize.x = fbWidth[0].toFloat()
            io.displaySize.y = fbHeight[0].toFloat()

            if (fbWidth[0] > 0 && fbHeight[0] > 0) {
                // checking frame start requirements
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
                // Skip ImGui frame if display size is invalid
                glClearColor(0.1f, 0.1f, 0.1f, 1f)
                glClear(GL_COLOR_BUFFER_BIT)
            }
            glfwSwapBuffers(window)
        }

        // Cleanup
        imGuiGl3?.shutdown()
        ImGui.destroyContext()
        glfwFreeCallbacks(window)
        glfwDestroyWindow(window)
        glfwTerminate()
    }
}
