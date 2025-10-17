package com.odtheking.odin.clickgui

import com.odtheking.odin.OdinMod
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.settings.impl.ColorSetting
import com.odtheking.odin.config.Config
import com.odtheking.odin.events.GuiEvent
import com.odtheking.odin.features.Category
import com.odtheking.odin.features.impl.render.ClickGUIModule
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.ui.HoverHandler
import com.odtheking.odin.utils.ui.animations.LinearAnimation
import com.odtheking.odin.utils.ui.rendering.NVGRenderer
import meteordevelopment.orbit.EventHandler
import net.minecraft.client.gui.Click
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.input.CharInput
import net.minecraft.client.input.KeyInput
import net.minecraft.text.Text
import kotlin.math.sign
import com.odtheking.odin.utils.ui.mouseX as odinMouseX
import com.odtheking.odin.utils.ui.mouseY as odinMouseY

/**
 * Renders all the modules.
 *
 * Backend made by Aton, with some changes
 * Design mostly made by Stivais
 *
 * @author Stivais, Aton
 * @see [Panel]
 */
object ClickGUI : Screen(Text.of("Click GUI")) {

    private val panels: ArrayList<Panel> = arrayListOf<Panel>().apply {
        if (Category.entries.any { ClickGUIModule.panelSetting[it] == null }) ClickGUIModule.resetPositions()
        for (category in Category.entries) add(Panel(category))
    }

    private var openAnim = LinearAnimation<Float>(400)
    val gray38 = Color(38, 38, 38)
    val gray26 = Color(26, 26, 26)

    init {
        OdinMod.EVENT_BUS.subscribe(this)
    }

    @EventHandler
    fun render(event: GuiEvent.NVGRender) {
        if (mc.currentScreen != this) return
        NVGRenderer.beginFrame(mc.window.width.toFloat(), mc.window.height.toFloat())
        if (openAnim.isAnimating()) {
            NVGRenderer.translate(0f, openAnim.get(-10f, 0f))
            NVGRenderer.globalAlpha(openAnim.get(0f, 1f))
        }

        for (element in panels) {
            element.draw(odinMouseX, odinMouseY)
        }
        SearchBar.draw(mc.window.width / 2f - 175f, mc.window.height - 110f, odinMouseX, odinMouseY)
        desc.render()

        NVGRenderer.endFrame()
    }

    override fun mouseScrolled(
        mouseX: Double,
        mouseY: Double,
        horizontalAmount: Double,
        verticalAmount: Double
    ): Boolean {
        val actualAmount = (verticalAmount.sign * 16).toInt()
        for (i in panels.size - 1 downTo 0) {
            if (panels[i].handleScroll(actualAmount)) return true
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
    }

    override fun mouseClicked(click: Click, doubled: Boolean): Boolean {
        SearchBar.mouseClicked(odinMouseX, odinMouseY, click)
        for (i in panels.size - 1 downTo 0) {
            if (panels[i].mouseClicked(odinMouseX, odinMouseY, click)) return true
        }
        return super.mouseClicked(click, doubled)
    }

    override fun mouseReleased(click: Click): Boolean {
        SearchBar.mouseReleased()
        for (i in panels.size - 1 downTo 0) {
            panels[i].mouseReleased(click)
        }
        return super.mouseReleased(click)
    }

    override fun charTyped(input: CharInput): Boolean {
        SearchBar.keyTyped(input)
        for (i in panels.size - 1 downTo 0) {
            if (panels[i].keyTyped(input)) return true
        }
        return super.charTyped(input)
    }

    override fun keyPressed(input: KeyInput): Boolean {
        SearchBar.keyPressed(input)
        for (i in panels.size - 1 downTo 0) {
            if (panels[i].keyPressed(input)) return true
        }
        return super.keyPressed(input)
    }

    override fun init() {
        openAnim.start()
        super.init()
    }

    override fun close() {
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

    private var desc = Description("", 0f, 0f, HoverHandler(150))

    /** Sets the description without creating a new data class which isn't optimal */
    fun setDescription(text: String, x: Float, y: Float, hoverHandler: HoverHandler) {
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
            NVGRenderer.hollowRect(
                x,
                y,
                area[2] - area[0] + 16f,
                area[3] - area[1] + 16f,
                1.5f,
                ClickGUIModule.clickGUIColor.rgba,
                5f
            )
            NVGRenderer.drawWrappedString(text, x + 8f, y + 8f, 300f, 16f, Colors.WHITE.rgba, NVGRenderer.defaultFont)
        }
    }

    val movementImage = NVGRenderer.createImage("/assets/odin/MovementIcon.svg")
    val hueImage = NVGRenderer.createImage("/assets/odin/HueGradient.png")
    val chevronImage = NVGRenderer.createImage("/assets/odin/chevron.svg")
}