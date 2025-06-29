package me.odinmod.odin.clickgui.settings.impl

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import me.odinmod.odin.clickgui.ClickGUI.gray38
import me.odinmod.odin.clickgui.Panel
import me.odinmod.odin.clickgui.settings.RenderableSetting
import me.odinmod.odin.clickgui.settings.Saving
import me.odinmod.odin.features.impl.render.ClickGUIModule
import me.odinmod.odin.utils.Colors
import me.odinmod.odin.utils.ui.TextInputHandler
import me.odinmod.odin.utils.ui.rendering.NVGRenderer

class StringSetting(
    name: String,
    override val default: String = "",
    private var length: Int = 32,
    desc: String,
    hidden: Boolean = false
) : RenderableSetting<String>(name, hidden, desc), Saving {

    override var value: String = default
        set(value) {
            field = if (value.length <= length) value else return
        }

    private val textInputHandler = TextInputHandler(
        textProvider = { value },
        textSetter = { value = it }
    )

    private var previousMousePos = 0.0 to 0.0

    override fun render(x: Float, y: Float, mouseX: Double, mouseY: Double): Float {
        super.render(x, y, mouseX, mouseY)

        if (previousMousePos != mouseX to mouseY) textInputHandler.mouseDragged(mouseX)
        previousMousePos = mouseX to mouseY

        val rectStartX = x + 6f

        NVGRenderer.text(name, rectStartX, y + 5f, 16f, Colors.WHITE.rgba, NVGRenderer.defaultFont)

        NVGRenderer.rect(rectStartX, y + getHeight() - 35f, width - 12f, 30f, gray38.rgba, 4f)
        NVGRenderer.hollowRect(rectStartX, y + getHeight() - 35f, width - 12f, 30f, 2f, ClickGUIModule.clickGUIColor.rgba, 4f)

        textInputHandler.x = rectStartX
        textInputHandler.y = y + getHeight() - 30f
        textInputHandler.width = width - 16f
        textInputHandler.draw()

        return getHeight()
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, mouseButton: Int): Boolean {
        return if (mouseButton == 0) textInputHandler.mouseClicked(mouseX, mouseY, mouseButton)
        else false
    }

    override fun mouseReleased(state: Int) {
        if (state == 0) textInputHandler.mouseReleased()
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        return textInputHandler.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun keyTyped(typedChar: Char, modifier: Int): Boolean {
        return textInputHandler.keyTyped(typedChar)
    }

    override fun getHeight(): Float = Panel.HEIGHT + 28f

    override fun write(): JsonElement {
        return JsonPrimitive(value)
    }

    override fun read(element: JsonElement?) {
        element?.asString?.let {
            value = it
        }
    }
}