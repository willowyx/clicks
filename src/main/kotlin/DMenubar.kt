import org.lwjgl.glfw.GLFW.GLFW_COCOA_MENUBAR
import org.lwjgl.glfw.GLFW.GLFW_TRUE
import org.lwjgl.glfw.GLFW.glfwInitHint
import org.lwjgl.glfw.GLFWNativeCocoa.glfwGetCocoaWindow
import org.lwjgl.system.APIUtil.apiCreateCIF
import org.lwjgl.system.Callback
import org.lwjgl.system.CallbackI
import org.lwjgl.system.JNI.invokePPP
import org.lwjgl.system.JNI.invokePPPP
import org.lwjgl.system.JNI.invokePPPPPP
import org.lwjgl.system.JNI.invokePPPV
import org.lwjgl.system.JNI.invokePPV
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil.memAddress
import org.lwjgl.system.MemoryUtil.memGetAddress
import org.lwjgl.system.Pointer.POINTER_SIZE
import org.lwjgl.system.libffi.LibFFI.ffi_type_pointer
import org.lwjgl.system.libffi.LibFFI.ffi_type_void
import org.lwjgl.system.macosx.ObjCRuntime.class_addMethod
import org.lwjgl.system.macosx.ObjCRuntime.objc_allocateClassPair
import org.lwjgl.system.macosx.ObjCRuntime.objc_getClass
import org.lwjgl.system.macosx.ObjCRuntime.objc_registerClassPair
import org.lwjgl.system.macosx.ObjCRuntime.sel_registerName
import java.lang.invoke.MethodHandles

object DMenubar {
    private const val MENU_STATE_OFF = 0L
    private const val MENU_STATE_ON = 1L

    private const val MODIFIER_CONTROL = 1L shl 18
    private const val MODIFIER_OPTION = 1L shl 19
    private const val MODIFIER_COMMAND = 1L shl 20

    private val isMacOS = System.getProperty("os.name").orEmpty().contains("mac", ignoreCase = true)
    private var cocoaWindow = 0L
    private var messageSend = 0L
    private var menuTarget = 0L
    private var lastPauseTitle: String? = null
    private val callbacks = mutableListOf<ObjCAction>()
    private val menuItems = mutableMapOf<String, Long>()

    fun configureBeforeGlfwInit() {
        if (!isMacOS) return

        glfwInitHint(GLFW_COCOA_MENUBAR, GLFW_TRUE)
    }

    fun install(window: Long) {
        if (!isMacOS) return

        runCatching {
            cocoaWindow = glfwGetCocoaWindow(window)
            messageSend = org.lwjgl.system.macosx.ObjCRuntime.getLibrary()
                .getFunctionAddress("objc_msgSend")
            check(cocoaWindow != 0L && messageSend != 0L) { "Unable to access Cocoa window" }

            installNativeMenuBar()
        }.onFailure { error ->
            System.err.println("Menu bar integration failed: ${error.message ?: error.javaClass.simpleName}")
            error.printStackTrace(System.err)
        }
    }

    fun refreshMenuState() {
        if (!isMacOS || menuTarget == 0L) return

        setEnabled("new", UI.canStartNewGame())
        setEnabled("save", UI.canSaveGame())
        setEnabled("load", UI.canLoadGame())
        setEnabled("pause", UI.canPauseOrResume())
        setEnabled("package", UI.canPackage())
        val pauseTitle = if (UI.isGameRunning()) "Pause game" else "Resume game"
        if (pauseTitle != lastPauseTitle) {
            setTitle("pause", pauseTitle)
            lastPauseTitle = pauseTitle
        }
        setState("modern", UI.getLayoutMode() == 0)
        setState("columns", UI.getLayoutMode() == 1)
        setState("lockLayout", UI.isLayoutLocked())
    }

    fun shutdown() {
        callbacks.clear()
        menuItems.clear()
        menuTarget = 0L
        cocoaWindow = 0L
        lastPauseTitle = null
    }

    private fun installNativeMenuBar() {
        val nsApplication = objc_getClass("NSApplication")
        val nsMenu = objc_getClass("NSMenu")
        val nsMenuItem = objc_getClass("NSMenuItem")
        val nsObject = objc_getClass("NSObject")
        check(nsApplication != 0L && nsMenu != 0L && nsMenuItem != 0L && nsObject != 0L)

        val application = sendPointer(nsApplication, "sharedApplication")
        val targetClass = objc_allocateClassPair(nsObject, "ClicksMenuTarget", 0L)
        check(targetClass != 0L) { "Unable to create menu action target" }

        addAction(targetClass, "clicksNewGame:") { Main.enqueueAction(UI::requestNewGame) }
        addAction(targetClass, "clicksSave:") { Main.enqueueAction(UI::requestSaveGame) }
        addAction(targetClass, "clicksLoad:") { Main.enqueueAction(UI::requestLoadGame) }
        addAction(targetClass, "clicksPause:") { Main.enqueueAction(UI::requestPauseOrResume) }
        addAction(targetClass, "clicksPackage:") { Main.enqueueAction(UI::requestPackage) }
        addAction(targetClass, "clicksModernLayout:") { Main.enqueueAction { UI.setLayoutMode(0) } }
        addAction(targetClass, "clicksColumnsLayout:") { Main.enqueueAction { UI.setLayoutMode(1) } }
        addAction(targetClass, "clicksLockLayout:") { Main.enqueueAction(UI::toggleLayoutLock) }
        addAction(targetClass, "clicksPreferences:") { Main.enqueueAction(UI::requestPreferences) }
        addAction(targetClass, "clicksAbout:") { Main.enqueueAction(UI::requestAbout) }
        addAction(targetClass, "clicksHelp:") { Main.enqueueAction(UI::requestHowToPlay) }
        addAction(targetClass, "clicksQuit:") { Main.requestQuit() }

        objc_registerClassPair(targetClass)
        menuTarget = sendPointer(sendPointer(targetClass, "alloc"), "init")

        val mainMenu = createMenu("clicks")
        val appMenu = createMenu("clicks")
        val fileMenu = createMenu("File")
        val gameMenu = createMenu("Game")
        val viewMenu = createMenu("View")
        val windowMenu = createMenu("Window")
        val helpMenu = createMenu("Help")

        addSubmenu(mainMenu, "clicks", appMenu)
        addSubmenu(mainMenu, "File", fileMenu)
        addSubmenu(mainMenu, "Game", gameMenu)
        addSubmenu(mainMenu, "View", viewMenu)
        addSubmenu(mainMenu, "Help", helpMenu)

        addMenuItem(appMenu, "About clicks", "clicksAbout:", "", key = "about")
        addSeparator(appMenu, nsMenuItem)
        addMenuItem(appMenu, "Settings...", "clicksPreferences:", ",")
        addSeparator(appMenu, nsMenuItem)
        addSeparator(appMenu, nsMenuItem)
        addMenuItem(appMenu, "Hide clicks", "hide:", "h", target = application)
        addMenuItem(
            appMenu,
            "Hide Others",
            "hideOtherApplications:",
            "h",
            target = application,
            modifiers = MODIFIER_COMMAND or MODIFIER_OPTION,
        )
        addMenuItem(appMenu, "Show All", "unhideAllApplications:", "", target = application)
        addSeparator(appMenu, nsMenuItem)
        addMenuItem(appMenu, "Quit clicks", "clicksQuit:", "q")

        addMenuItem(fileMenu, "New game", "clicksNewGame:", "n", key = "new")
        addMenuItem(fileMenu, "Save game...", "clicksSave:", "s", key = "save")
        addMenuItem(fileMenu, "Load game...", "clicksLoad:", "o", key = "load")

        addMenuItem(gameMenu, "Pause game", "clicksPause:", "p", key = "pause")

        addMenuItem(gameMenu, "Package", "clicksPackage:", "\r", key = "package")

        addMenuItem(viewMenu, "Layout: Modern", "clicksModernLayout:", "1", key = "modern")
        addMenuItem(viewMenu, "Layout: Columns", "clicksColumnsLayout:", "2", key = "columns")
        addSeparator(viewMenu, nsMenuItem)
        addMenuItem(viewMenu, "Lock layout positions", "clicksLockLayout:", "l", key = "lockLayout")

        addMenuItem(windowMenu, "Close", "performClose:", "w", target = cocoaWindow)
        addSeparator(windowMenu, nsMenuItem)
        addMenuItem(windowMenu, "Minimize", "performMiniaturize:", "m", target = cocoaWindow)
        addMenuItem(windowMenu, "Zoom", "performZoom:", "", target = cocoaWindow)
        addSeparator(windowMenu, nsMenuItem)
        addMenuItem(
            windowMenu,
            "Toggle Full Screen",
            "toggleFullScreen:",
            "f",
            target = cocoaWindow,
            modifiers = MODIFIER_COMMAND or MODIFIER_CONTROL,
        )

        addMenuItem(helpMenu, "View clicks guide", "clicksHelp:", "?")

        sendPointerArgument(application, "setMainMenu:", mainMenu)
        sendPointerArgument(application, "setWindowsMenu:", windowMenu)
        refreshMenuState()
    }

    private fun addAction(targetClass: Long, selectorName: String, action: () -> Unit) {
        val callback = ObjCAction { _, _, _ -> action() }
        callbacks += callback
        check(class_addMethod(targetClass, selector(selectorName), callback.address(), "v@:@")) {
            "Unable to register $selectorName"
        }
    }

    private fun createMenu(title: String): Long {
        val menuClass = objc_getClass("NSMenu")
        val menu = sendPointerArgumentReturningPointer(sendPointer(menuClass, "alloc"), "initWithTitle:", nsString(title))
        sendBooleanArgument(menu, "setAutoenablesItems:", false)
        return menu
    }

    private fun addSubmenu(parent: Long, title: String, submenu: Long) {
        val item = addMenuItem(parent, title, null, "", target = 0L)
        sendPointerArgument(item, "setSubmenu:", submenu)
    }

    private fun addMenuItem(
        menu: Long,
        title: String,
        action: String?,
        equivalent: String,
        target: Long = menuTarget,
        modifiers: Long = MODIFIER_COMMAND,
        key: String? = null,
    ): Long {
        val itemClass = objc_getClass("NSMenuItem")
        val item = sendMenuItemInitializer(
            sendPointer(itemClass, "alloc"),
            nsString(title),
            action?.let(::selector) ?: 0L,
            nsString(equivalent),
        )
        if (target != 0L) sendPointerArgument(item, "setTarget:", target)
        if (equivalent.isNotEmpty()) sendLongArgument(item, "setKeyEquivalentModifierMask:", modifiers)
        sendPointerArgument(menu, "addItem:", item)
        if (key != null) menuItems[key] = item
        return item
    }

    private fun addSeparator(menu: Long, menuItemClass: Long) {
        sendPointerArgument(menu, "addItem:", sendPointer(menuItemClass, "separatorItem"))
    }

    private fun setEnabled(key: String, enabled: Boolean) {
        menuItems[key]?.let { sendBooleanArgument(it, "setEnabled:", enabled) }
    }

    private fun setState(key: String, enabled: Boolean) {
        menuItems[key]?.let { sendLongArgument(it, "setState:", if (enabled) MENU_STATE_ON else MENU_STATE_OFF) }
    }

    private fun setTitle(key: String, title: String) {
        menuItems[key]?.let { sendPointerArgument(it, "setTitle:", nsString(title)) }
    }

    private fun selector(name: String): Long = sel_registerName(name)

    private fun nsString(value: String): Long {
        val stringClass = objc_getClass("NSString")
        MemoryStack.stackPush().use { stack ->
            return invokePPPP(
                stringClass,
                selector("stringWithUTF8String:"),
                memAddress(stack.UTF8(value)),
                messageSend,
            )
        }
    }

    private fun sendPointer(receiver: Long, selectorName: String): Long =
        invokePPP(receiver, selector(selectorName), messageSend)

    private fun sendPointerArgument(receiver: Long, selectorName: String, argument: Long) {
        invokePPPV(receiver, selector(selectorName), argument, messageSend)
    }

    private fun sendPointerArgumentReturningPointer(receiver: Long, selectorName: String, argument: Long): Long =
        invokePPPP(receiver, selector(selectorName), argument, messageSend)

    private fun sendLongArgument(receiver: Long, selectorName: String, argument: Long) {
        invokePPPV(receiver, selector(selectorName), argument, messageSend)
    }

    private fun sendBooleanArgument(receiver: Long, selectorName: String, argument: Boolean) {
        invokePPV(receiver, selector(selectorName), argument, messageSend)
    }

    private fun sendMenuItemInitializer(
        receiver: Long,
        title: Long,
        action: Long,
        equivalent: Long,
    ): Long = invokePPPPPP(
        receiver,
        selector("initWithTitle:action:keyEquivalent:"),
        title,
        action,
        equivalent,
        messageSend,
    )

    private fun interface ObjCAction : CallbackI {
        companion object {
            val DESCRIPTOR = Callback.Descriptor(
                MethodHandles.lookup(),
                apiCreateCIF(ffi_type_void, ffi_type_pointer, ffi_type_pointer, ffi_type_pointer),
            )
        }

        override fun getDescriptor(): Callback.Descriptor = DESCRIPTOR

        override fun callback(ret: Long, args: Long) {
            invoke(
                memGetAddress(memGetAddress(args)),
                memGetAddress(memGetAddress(args + POINTER_SIZE)),
                memGetAddress(memGetAddress(args + 2L * POINTER_SIZE)),
            )
        }

        fun invoke(self: Long, command: Long, sender: Long)
    }
}
