package com.odtheking.odin.clickgui.settings.impl

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.odtheking.odin.clickgui.ClickGUI.gray38
import com.odtheking.odin.clickgui.Panel
import com.odtheking.odin.clickgui.settings.RenderableSetting
import com.odtheking.odin.clickgui.settings.Saving
import com.odtheking.odin.features.impl.render.ClickGUIModule
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.ui.TextInputHandler
import com.odtheking.odin.utils.ui.rendering.NVGRenderer

class StringSetting(
    name: String,
    override val default: String = "",
    private var length: Int = 32,
    desc: String
) : RenderableSetting<String>(name, desc), Saving {

    override var value: String = default
        set(value) {
            field = if (value.length <= length) value else return
        }

    private val textInputHandler = TextInputHandler(
        textProvider = { value },
        textSetter = { value = it }
    )

    override fun render(x: Float, y: Float, mouseX: Float, mouseY: Float): Float {
        super.render(x, y, mouseX, mouseY)

        val rectStartX = x + 6f

        NVGRenderer.text(name, rectStartX, y + 5f, 16f, Colors.WHITE.rgba, NVGRenderer.defaultFont)

        NVGRenderer.rect(rectStartX, y + getHeight() - 35f, width - 12f, 30f, gray38.rgba, 4f)
        NVGRenderer.hollowRect(
            rectStartX,
            y + getHeight() - 35f,
            width - 12f,
            30f,
            2f,
            ClickGUIModule.clickGUIColor.rgba,
            4f
        )

        textInputHandler.x = rectStartX
        textInputHandler.y = y + getHeight() - 30f
        textInputHandler.width = width - 16f
        textInputHandler.draw(mouseX, mouseY)

        return getHeight()
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, mouseButton: Int): Boolean {
        return if (mouseButton == 0) textInputHandler.mouseClicked(mouseX, mouseY, mouseButton)
        else false
    }

    override fun mouseReleased(state: Int) {
        if (state == 0) textInputHandler.mouseReleased()
    }

    override fun keyPressed(keyCode: Int, scanCode: Int): Boolean {
        return textInputHandler.keyPressed(keyCode)
    }

    override fun keyTyped(typedChar: Char): Boolean {
        return textInputHandler.keyTyped(typedChar)
    }

    override fun getHeight(): Float = Panel.HEIGHT + 28f

    override fun write(): JsonElement = JsonPrimitive(value)

    override fun read(element: JsonElement?) {
        element?.asString?.let { value = it }
    }
}