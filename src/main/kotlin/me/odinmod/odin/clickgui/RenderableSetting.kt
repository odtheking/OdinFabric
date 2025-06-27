package me.odinmod.odin.clickgui

import me.odinmod.odin.clickgui.settings.Setting
import me.odinmod.odin.utils.ui.HoverHandler
import me.odinmod.odin.utils.ui.MouseUtils.isAreaHovered

abstract class RenderableSetting<T>(
    name: String,
    hidden: Boolean = false,
    description: String = "",
) : Setting<T>(name, hidden, description) {

    private val hoverHandler = HoverHandler(750, 200)
    protected val width = Panel.WIDTH
    protected var lastX = 0f
    protected var lastY = 0f
    var listening = false

    open fun render(x: Float, y: Float, mouseX: Double, mouseY: Double): Float {
        lastX = x
        lastY = y

        hoverHandler.handle(x, y, width, getHeight())
        if (hoverHandler.percent() > 0)
            ClickGUI.setDescription(description, x + width + 10f, y, hoverHandler)

        return getHeight()
    }

    open fun mouseClicked(mouseX: Double, mouseY: Double, mouseButton: Int): Boolean = false
    open fun mouseReleased(state: Int) {}
    open fun keyTyped(typedChar: Char, modifier: Int): Boolean = false
    open fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean = false
    open fun getHeight(): Float = Panel.HEIGHT

    open val isHovered get() = isAreaHovered(lastX, lastY, width, getHeight())
}