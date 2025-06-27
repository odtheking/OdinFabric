package me.odinmod.odin.clickgui.settings

import me.odinmod.odin.clickgui.ClickGUI
import me.odinmod.odin.clickgui.ClickGUI.gray26
import me.odinmod.odin.clickgui.Panel
import me.odinmod.odin.clickgui.RenderableSetting
import me.odinmod.odin.features.Module
import me.odinmod.odin.features.impl.render.ClickGUIModule
import me.odinmod.odin.utils.Color
import me.odinmod.odin.utils.Color.Companion.brighter
import me.odinmod.odin.utils.Colors
import me.odinmod.odin.utils.ui.HoverHandler
import me.odinmod.odin.utils.ui.MouseUtils.isAreaHovered
import me.odinmod.odin.utils.ui.animations.ColorAnimation
import me.odinmod.odin.utils.ui.animations.EaseInOutAnimation
import me.odinmod.odin.utils.ui.rendering.NVGRenderer
import kotlin.math.floor

/**
 * Renders all the modules.
 *
 * @author Stivais, Aton
 *
 * see [RenderableSetting]
 */
class ModuleButton(val module: Module, val panel: Panel) {

    val representableSettings = module.settings.filterIsInstance<RenderableSetting<*>>()

    inline val x: Float get() = panel.x
    var y = 0f

    private val colorAnim = ColorAnimation(150)

    val color: Color get() =
        colorAnim.get(ClickGUIModule.clickGUIColor, gray26, module.enabled).brighter(1 + hover.percent() / 500f)

    val width = Panel.WIDTH
    val height = Panel.HEIGHT

    var extended = false

    private val nameWidth = NVGRenderer.textWidth(module.name, 18f, NVGRenderer.defaultFont)
    private val hoverHandler = HoverHandler(750, 200)
    private val extendAnim = EaseInOutAnimation(250)
    private val hover = HoverHandler(250)

    fun draw(mouseX: Double, mouseY: Double): Float {
        hoverHandler.handle(x, y, width, height - 1)
        hover.handle(x, y, width, height - 1)

        if (hoverHandler.percent() > 0)
            ClickGUI.setDescription(module.description, x + width + 10f, y, hoverHandler)

        NVGRenderer.rect(x, y, width, height, color.rgba)
        NVGRenderer.text(module.name, x + width / 2 - nameWidth / 2, y + height / 2 - 9f, 18f, Colors.WHITE.rgba, NVGRenderer.defaultFont)

        if (!extendAnim.isAnimating() && !extended || module.settings.isEmpty()) return height

        var drawY = height
        val totalHeight = height + floor(extendAnim.get(0f, getSettingHeight(), !extended))

        NVGRenderer.pushScissor(x, y, width, totalHeight)
        for (setting in module.settings) {
            if (setting is RenderableSetting<*> && setting.shouldBeVisible) drawY += setting.render(x, y + drawY, mouseX, mouseY)
        }
        NVGRenderer.popScissor()

        return totalHeight
    }

    fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (isButtonHovered) {
            if (button == 0) {
                colorAnim.start()
                module.toggle()
                return true
            } else if (button == 1) {
                if (module.settings.isNotEmpty()) {
                    extendAnim.start()
                    extended = !extended
                }
                return true
            }
        } else if (isMouseUnderButton) {
            for (setting in representableSettings) {
                if (setting.shouldBeVisible && setting.mouseClicked(mouseX, mouseY, button)) return true
            }
        }
        return false
    }

    fun mouseReleased(state: Int) {
        if (!extended) return
        for (setting in representableSettings) {
            if (setting.shouldBeVisible) setting.mouseReleased(state)
        }
    }

    fun keyTyped(typedChar: Char, modifier: Int): Boolean {
        if (!extended) return false
        for (setting in representableSettings) {
            if (setting.shouldBeVisible && setting.keyTyped(typedChar, modifier)) return true
        }
        return false
    }

    fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (!extended) return false
        for (setting in representableSettings) {
            if (setting.shouldBeVisible && setting.keyPressed(keyCode, scanCode, modifiers)) return true
        }
        return false
    }

    private inline val isButtonHovered: Boolean get() = isAreaHovered(x, y, width, height - 1)

    private inline val isMouseUnderButton: Boolean get() = extended && isAreaHovered(x, y + height, width)

    private fun getSettingHeight(): Float =
        representableSettings.filter { it.shouldBeVisible }.sumOf { it.getHeight().toDouble() }.toFloat()
}