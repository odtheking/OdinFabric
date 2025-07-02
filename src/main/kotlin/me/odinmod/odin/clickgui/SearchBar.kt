package me.odinmod.odin.clickgui

import me.odinmod.odin.clickgui.ClickGUI.gray38
import me.odinmod.odin.features.impl.render.ClickGUIModule
import me.odinmod.odin.utils.Colors
import me.odinmod.odin.utils.ui.TextInputHandler
import me.odinmod.odin.utils.ui.rendering.NVGRenderer

object SearchBar {

    var currentSearch = ""
        private set (value) {
            if (value == field || value.length > 16) return
            field = value
            searchWidth = NVGRenderer.textWidth(value, 20f, NVGRenderer.defaultFont)
        }

    private var placeHolderWidth = NVGRenderer.textWidth("Search here...", 20f, NVGRenderer.defaultFont)
    private var searchWidth = NVGRenderer.textWidth(currentSearch, 20f, NVGRenderer.defaultFont)

    private val textInputHandler = TextInputHandler(
        textProvider = { currentSearch },
        textSetter = { currentSearch = it }
    )

    private var previousMousePos = 0.0 to 0.0

    fun draw(x: Float, y: Float, mouseX: Double, mouseY: Double) {
        if (previousMousePos != mouseX to mouseY) textInputHandler.mouseDragged(mouseX)
        previousMousePos = mouseX to mouseY

        NVGRenderer.dropShadow(x, y, 350f, 40f, 10f, 0.75f, 9f)
        NVGRenderer.rect(x, y, 350f, 40f, gray38.rgba, 9f)
        NVGRenderer.hollowRect(x, y, 350f, 40f, 3f, ClickGUIModule.clickGUIColor.rgba, 9f)

        val textY = y + 10f

        if (currentSearch.isEmpty()) NVGRenderer.text("Search here...", x + 175f - placeHolderWidth / 2, textY, 20f, Colors.WHITE.rgba, NVGRenderer.defaultFont)
        textInputHandler.x = (x + 175f - searchWidth / 2 - if (currentSearch.isEmpty()) placeHolderWidth / 2 + 2f else 0f).coerceAtLeast(x)
        textInputHandler.y = textY
        textInputHandler.width = 250f
        textInputHandler.height = 22f
        textInputHandler.draw()
    }

    fun mouseClicked(mouseX: Double, mouseY: Double, mouseButton: Int): Boolean {
        return textInputHandler.mouseClicked(mouseX, mouseY, mouseButton)
    }

    fun mouseReleased() {
        textInputHandler.mouseReleased()
    }

    fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        return textInputHandler.keyPressed(keyCode, scanCode, modifiers)
    }

    fun keyTyped(typedChar: Char, modifier: Int): Boolean {
        return textInputHandler.keyTyped(typedChar)
    }
}