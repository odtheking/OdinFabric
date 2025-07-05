package me.odinmod.odin.clickgui

import me.odinmod.odin.clickgui.ClickGUI.gray26
import me.odinmod.odin.clickgui.ClickGUI.gray38
import me.odinmod.odin.clickgui.settings.ModuleButton
import me.odinmod.odin.features.Category
import me.odinmod.odin.features.ModuleManager
import me.odinmod.odin.features.impl.render.ClickGUIModule
import me.odinmod.odin.features.impl.render.PlayerSize
import me.odinmod.odin.utils.Colors
import me.odinmod.odin.utils.ui.MouseUtils.isAreaHovered
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
class Panel(private val category: Category) {

    val moduleButtons: ArrayList<ModuleButton> = ArrayList<ModuleButton>().apply {
        ModuleManager.modules
            .filter { it.category == category && (!it.isDevModule || PlayerSize.isRandom) }
            .sortedByDescending { NVGRenderer.textWidth(it.name, 16f, NVGRenderer.defaultFont) }
            .forEach { add(ModuleButton(it, this@Panel)) }
    }
    private val lastModuleButton: ModuleButton? = moduleButtons.lastOrNull()
    private val isModuleButtonEmpty: Boolean = moduleButtons.isEmpty()

    val panelSetting = ClickGUIModule.panelSetting[category] ?: ClickGUIModule.PanelData()

    private var dragging = false

    private var length = 0f
    private var x2 = 0f
    private var y2 = 0f

    private val textWidth = NVGRenderer.textWidth(category.displayName, 22f, NVGRenderer.defaultFont)
    private var previousHeight = 0f
    private var scrollOffset = 0f

    fun draw(mouseX: Double, mouseY: Double) {
        if (dragging) {
            panelSetting.x = floor(x2 + mouseX).toFloat()
            panelSetting.y = floor(y2 + mouseY).toFloat()
        }

        NVGRenderer.dropShadow(panelSetting.x, panelSetting.y, WIDTH, (previousHeight + 10f).coerceAtLeast(HEIGHT), 10f, 3f, 5f)

        NVGRenderer.drawHalfRoundedRect(panelSetting.x, panelSetting.y, WIDTH, HEIGHT, gray26.rgba, 5f, true)
        NVGRenderer.text(category.displayName, panelSetting.x + WIDTH / 2f - textWidth / 2, panelSetting.y + HEIGHT / 2f - 11, 22f, Colors.WHITE.rgba, NVGRenderer.defaultFont)

        var startY = scrollOffset + HEIGHT

        if (scrollOffset != 0f) NVGRenderer.pushScissor(panelSetting.x, panelSetting.y + HEIGHT, WIDTH, previousHeight - HEIGHT + 10f)

        if (panelSetting.extended && !isModuleButtonEmpty) {
            for (button in moduleButtons) {
                if (!button.module.name.contains(SearchBar.currentSearch, true)) continue
                button.y = startY + panelSetting.y
                startY += button.draw(mouseX, mouseY)
            }
            length = startY + 5f
        }
        previousHeight = startY

        if (!isModuleButtonEmpty) NVGRenderer.drawHalfRoundedRect(panelSetting.x, panelSetting.y + startY, WIDTH, 10f, lastModuleButton?.color?.rgba ?: gray38.rgba, 5f, false)
        if (scrollOffset != 0f) NVGRenderer.popScissor()
    }

    fun handleScroll(amount: Int): Boolean {
        if (!isMouseOverExtended) return false
        scrollOffset = (scrollOffset + amount).coerceIn(-length + scrollOffset + 72f, 0f)
        return true
    }

    fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (isHovered) {
            if (button == 0) {
                x2 = (panelSetting.x - mouseX).toFloat()
                y2 = (panelSetting.y - mouseY).toFloat()
                dragging = true
                return true
            } else if (button == 1) {
                panelSetting.extended = !panelSetting.extended
                return true
            }
        } else if (isMouseOverExtended) {
            return moduleButtons.reversed().any {
                if (!it.module.name.contains(SearchBar.currentSearch, true)) return@any false
                it.mouseClicked(mouseX, mouseY, button)
            }
        }
        return false
    }

    fun mouseReleased(state: Int) {
        if (state == 0) dragging = false

        if (panelSetting.extended)
            moduleButtons.reversed().forEach {
                if (!it.module.name.contains(SearchBar.currentSearch, true)) return@forEach
                it.mouseReleased(state)
            }
    }

    fun keyTyped(typedChar: Char, modifier: Int): Boolean {
        if (!panelSetting.extended) return false

        return moduleButtons.reversed().any {
            if (!it.module.name.contains(SearchBar.currentSearch, true)) return@any false
            it.keyTyped(typedChar, modifier)
        }
    }

    fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (!panelSetting.extended) return false

        return moduleButtons.reversed().any {
            if (!it.module.name.contains(SearchBar.currentSearch, true)) return@any false
            it.keyPressed(keyCode, scanCode, modifiers)
        }
    }

    private inline val isHovered get() = isAreaHovered(panelSetting.x, panelSetting.y, WIDTH, HEIGHT)

    private inline val isMouseOverExtended get() = panelSetting.extended && isAreaHovered(panelSetting.x, panelSetting.y, WIDTH, length.coerceAtLeast(HEIGHT))

    companion object {
        const val WIDTH = 240f
        const val HEIGHT = 32f
    }
}