package me.odinmod.odin.clickgui

import me.odinmod.odin.clickgui.ClickGUI.gray26
import me.odinmod.odin.clickgui.settings.ModuleButton
import me.odinmod.odin.features.Category
import me.odinmod.odin.features.ModuleManager
import me.odinmod.odin.features.impl.render.ClickGUIModule
import me.odinmod.odin.utils.Colors
import me.odinmod.odin.utils.ui.MouseUtils.isAreaHovered
import me.odinmod.odin.utils.ui.animations.LinearAnimation
import me.odinmod.odin.utils.ui.rendering.NVGRenderer
import kotlin.math.floor

/**
 * Renders all the panels.
 *
 * Backend made by Aton, with some changes
 * Design mostly made by Stivais
 *
 * @author Stivais, Aton
 * @see [ModuleButton]
 */
class Panel(val category: Category) {

    val moduleButtons: ArrayList<ModuleButton> = ArrayList()

    var extended: Boolean = ClickGUIModule.panelExtended[category]!!.enabled
    var x = ClickGUIModule.panelX[category]!!.value
    var y = ClickGUIModule.panelY[category]!!.value

    private var dragging = false

    private var length = 0f
    private var x2 = 0f
    private var y2 = 0f

    private val textWidth = NVGRenderer.textWidth(category.displayName, 22f, NVGRenderer.defaultFont)
    private val scrollAnimation = LinearAnimation<Float>(200)
    private var previousHeight = 0f
    private var scrollTarget = 0f
    private var scrollOffset = 0f

    init {
        for (module in ModuleManager.modules.sortedByDescending { NVGRenderer.textWidth(it.name, 16f, NVGRenderer.defaultFont) }) {
            if (module.category == this@Panel.category) moduleButtons.add(ModuleButton(module, this@Panel))
        }
    }

    fun draw(mouseX: Double, mouseY: Double) {
        if (dragging) {
            x = floor(x2 + mouseX).toFloat()
            y = floor(y2 + mouseY).toFloat()
        }

        NVGRenderer.dropShadow(x, y, WIDTH, (previousHeight + 10f).coerceAtLeast(HEIGHT), 12.5f, 6f, 5f)

        NVGRenderer.rect(x, y, WIDTH, HEIGHT, gray26.rgba, 5f, 0f, 0f, 5f)
        NVGRenderer.text(category.displayName, x + WIDTH / 2f - textWidth / 2, y + HEIGHT / 2f - 11, 22f, Colors.WHITE.rgba, NVGRenderer.defaultFont)

        scrollOffset = scrollAnimation.get(scrollOffset, scrollTarget)
        var startY = scrollOffset + HEIGHT

        NVGRenderer.pushScissor(x, y + HEIGHT, WIDTH, 5000f)
        if (extended && moduleButtons.isNotEmpty()) {
            for (button in moduleButtons.filter { it.module.name.contains(SearchBar.currentSearch, true) }) {
                button.y = startY + y
                startY += button.draw(mouseX, mouseY)
            }
            length = startY + 5f
        }
        previousHeight = startY

        if (moduleButtons.isNotEmpty()) NVGRenderer.rect(x, y + startY, WIDTH, 10f, moduleButtons.last().color.rgba, 0f, 5f, 5f, 0f)
        NVGRenderer.popScissor()
    }

    fun handleScroll(amount: Int): Boolean {
        if (!isMouseOverExtended) return false
        scrollTarget = (scrollTarget + amount).coerceIn(-length + scrollOffset + 72f, 0f)
        scrollAnimation.start()
        return true
    }

    fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (isHovered) {
            if (button == 0) {
                x2 = (x - mouseX).toFloat()
                y2 = (y - mouseY).toFloat()
                dragging = true
                return true
            } else if (button == 1) {
                extended = !extended
                return true
            }
        } else if (isMouseOverExtended) {
            for (i in moduleButtons.size - 1 downTo 0) {
                if (moduleButtons[i].mouseClicked(mouseX, mouseY, button)) return true
            }
        }
        return false
    }

    fun mouseReleased(state: Int) {
        if (state == 0) dragging = false

        ClickGUIModule.panelX[category]!!.value = x
        ClickGUIModule.panelY[category]!!.value = y
        ClickGUIModule.panelExtended[category]!!.enabled = extended

        if (extended) for (i in moduleButtons.size - 1 downTo 0) moduleButtons[i].mouseReleased(state)
    }

    fun keyTyped(typedChar: Char, modifier: Int): Boolean {
        if (!extended) return false
        for (i in moduleButtons.size - 1 downTo 0) {
            if (moduleButtons[i].keyTyped(typedChar, modifier)) return true
        }
        return false
    }

    fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (!extended) return false
        for (i in moduleButtons.size - 1 downTo 0) {
            if (moduleButtons[i].keyPressed(keyCode, scanCode, modifiers)) return true
        }
        return false
    }

    private inline val isHovered get() = isAreaHovered(x, y, WIDTH, HEIGHT)

    private inline val isMouseOverExtended get() = extended && isAreaHovered(x, y, WIDTH, length.coerceAtLeast(HEIGHT))

    companion object {
        const val WIDTH = 240f
        const val HEIGHT = 32f
    }
}