package me.odinmod.odin.clickgui

import me.odinmod.odin.OdinMod
import me.odinmod.odin.OdinMod.mc
import me.odinmod.odin.clickgui.settings.impl.ColorSetting
import me.odinmod.odin.config.Config
import me.odinmod.odin.events.GuiEvent
import me.odinmod.odin.features.Category
import me.odinmod.odin.features.impl.render.ClickGUIModule
import me.odinmod.odin.utils.Color
import me.odinmod.odin.utils.Colors
import me.odinmod.odin.utils.ui.HoverHandler
import me.odinmod.odin.utils.ui.animations.LinearAnimation
import me.odinmod.odin.utils.ui.rendering.NVGRenderer
import meteordevelopment.orbit.EventHandler
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import kotlin.math.sign

/**
 * Renders all the modules.
 *
 * Backend made by Aton, with some changes
 * Design mostly made by Stivais
 *
 * @author Stivais, Aton
 * @see [Panel]
 */
object ClickGUI : Screen(Text.literal("Click GUI")) {

    private val panels: ArrayList<Panel> = arrayListOf()

    val movementImage = NVGRenderer.createImage("/assets/odin/MovementIcon.svg")
    val hueImage = NVGRenderer.createImage("/assets/odin/HueGradient.png")
    val chevronImage = NVGRenderer.createImage("/assets/odin/chevron.svg")

    private var desc = Description("", 0f, 0f, HoverHandler(100))
    private var openAnim = LinearAnimation<Float>(400)

    val gray38 = Color(38, 38, 38)
    val gray26 = Color(26, 26, 26)

    init {
        for (category in Category.entries) panels.add(Panel(category))
        OdinMod.EVENT_BUS.subscribe(this)
    }

    private var lastResetTime = System.nanoTime()
    private var avgFrameTimeMs = 0.0
    private var frameTimeSum = 0.0
    private var frameCount = 0

    @EventHandler
    fun render(event: GuiEvent.NVGRender) {
        if (mc.currentScreen != this) return
        val startTime = System.nanoTime()

        NVGRenderer.beginFrame(1920f, 1080f)
        if (openAnim.isAnimating()) {
            NVGRenderer.translate(0f, openAnim.get(-10f, 0f))
            NVGRenderer.globalAlpha(openAnim.get(0f, 1f))
        }

        for (i in 0 until panels.size) { panels[i].draw(mc.mouse.x, mc.mouse.y) }
        SearchBar.draw(1920f / 2f - 175f, 1080f - 110f, mc.mouse.x, mc.mouse.y)
        desc.render()

        NVGRenderer.endFrame()

        val frameTimeMs = (System.nanoTime() - startTime) / 1_000_000.0
        val now = System.nanoTime()

        frameTimeSum += frameTimeMs
        frameCount++

        if ((now - lastResetTime) > 500_000_000L) {
            lastResetTime = now
            avgFrameTimeMs = if (frameCount > 0) frameTimeSum / frameCount else 0.0
            frameTimeSum = 0.0
            frameCount = 0
        }

        NVGRenderer.text(
            text = "Avg frame time: %.2f ms".format(avgFrameTimeMs),
            x = 1920f - 220f,
            y = 1080f - 28f,
            size = 18f,
            color = 0xFFFFFFFF.toInt(),
            font = NVGRenderer.defaultFont
        )
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        val actualAmount = verticalAmount.sign * 16
        for (i in panels.size - 1 downTo 0) {
            if (panels[i].handleScroll(actualAmount.toInt())) return true
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        SearchBar.mouseClicked(mc.mouse.x, mc.mouse.y, button)
        for (i in panels.size - 1 downTo 0) {
            if (panels[i].mouseClicked(mc.mouse.x, mc.mouse.y, button)) return true
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, state: Int): Boolean {
        SearchBar.mouseReleased()
        for (i in panels.size - 1 downTo 0) {
            panels[i].mouseReleased(state)
        }
        return super.mouseReleased(mouseX, mouseY, state)
    }

    override fun charTyped(chr: Char, modifiers: Int): Boolean {
        SearchBar.keyTyped(chr, modifiers)
        for (i in panels.size - 1 downTo 0) {
            if (panels[i].keyTyped(chr, modifiers)) return true
        }
        return super.charTyped(chr, modifiers)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        SearchBar.keyPressed(keyCode, scanCode, modifiers)
        for (i in panels.size - 1 downTo 0) {
            if (panels[i].keyPressed(keyCode, scanCode, modifiers)) return true
        }
        if (keyCode == ClickGUIModule.settings.last().value && !openAnim.isAnimating()) {
            mc.setScreen(null)
            if (mc.currentScreen == null) mc.onWindowFocusChanged(true)
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun init() {
        openAnim.start()
        super.init()
    }

    override fun close()  {
        for (panel in panels.filter { it.panelSetting.extended }.reversed()) {
            for (moduleButton in panel.moduleButtons.filter { it.extended }) {
                for (setting in moduleButton.representableSettings) {
                    if (setting is ColorSetting) setting.section = null
                    setting.listening = false
                }
            }
        }
        Config.save()

        super.close()
    }

    override fun shouldPause(): Boolean = false

    /** Sets the description without creating a new data class which isn't optimal */
    fun setDescription(text: String, x: Float,  y: Float, hoverHandler: HoverHandler) {
        desc.text = text
        desc.x = x
        desc.y = y
        desc.hoverHandler = hoverHandler
    }

    data class Description(var text: String, var x: Float, var y: Float, var hoverHandler: HoverHandler) {

        fun render() {
            if (text.isEmpty() || hoverHandler.percent() <= 0) return
            val area = NVGRenderer.wrappedTextBounds(text, 300f, 16f, NVGRenderer.defaultFont)
            NVGRenderer.rect(x, y, area[2] - area[0] + 16f, area[3] - area[1] + 16f, gray38.rgba, 5f)
            NVGRenderer.hollowRect(x, y, area[2] - area[0] + 16f, area[3] - area[1] + 16f, 1.5f, ClickGUIModule.clickGUIColor.rgba, 5f)
            NVGRenderer.drawWrappedString(text, x + 8f, y + 8f, 300f, 16f, Colors.WHITE.rgba, NVGRenderer.defaultFont)
        }
    }
}