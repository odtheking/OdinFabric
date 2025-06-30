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
import me.odinmod.odin.utils.ui.animations.EaseInOutAnimation
import me.odinmod.odin.utils.ui.rendering.NVGRenderer
import meteordevelopment.orbit.EventHandler
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import kotlin.math.floor
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

    private var desc = Description("", 0f, 0f, HoverHandler(100))
    private var openAnim = EaseInOutAnimation(400)

    val gray38 = Color(38, 38, 38)
    val gray26 = Color(26, 26, 26)

    init {
        for (category in Category.entries) panels.add(Panel(category))
        OdinMod.EVENT_BUS.subscribe(this)
    }

    @EventHandler
    fun render(event: GuiEvent.Render) {
        if (mc.currentScreen != this) return
        NVGRenderer.beginFrame(1920f, 1080f)
        if (openAnim.isAnimating()) {
            NVGRenderer.translate(0f, floor(openAnim.get(-10f, 0f)))
            NVGRenderer.globalAlpha(openAnim.get(0f, 1f))
        }

        for (i in 0 until panels.size) { panels[i].draw(mc.mouse.x, mc.mouse.y) }
        val virtualX = 1920f / 2f - 175f
        val virtualY = 1080f - 110f

        SearchBar.draw(virtualX, virtualY, mc.mouse.x, mc.mouse.y)
        desc.render()
        NVGRenderer.endFrame()
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        val actualAmount = verticalAmount.sign * 16
        for (i in panels.size - 1 downTo 0) {
            if (panels[i].handleScroll(actualAmount.toInt())) return true
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        SearchBar.mouseClicked(mouseX, mouseY, button)
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

        for (panel in panels) {
            panel.x = ClickGUIModule.panelX[panel.category]!!.value
            panel.y = ClickGUIModule.panelY[panel.category]!!.value
            panel.extended = ClickGUIModule.panelExtended[panel.category]!!.enabled
        }
        super.init()
    }

    override fun close()  {
        for (panel in panels.filter { it.extended }.reversed()) {
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