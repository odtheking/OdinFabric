package me.odinmod.odin.clickgui.settings.impl

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import me.odinmod.odin.OdinMod.mc
import me.odinmod.odin.clickgui.ClickGUI.gray38
import me.odinmod.odin.clickgui.settings.RenderableSetting
import me.odinmod.odin.clickgui.settings.Saving
import me.odinmod.odin.features.impl.render.ClickGUIModule
import me.odinmod.odin.utils.Colors
import me.odinmod.odin.utils.ui.MouseUtils.isAreaHovered
import me.odinmod.odin.utils.ui.rendering.NVGRenderer
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW

class KeybindSetting(
    name: String,
    override val default: InputUtil.Key,
    desc: String,
    hidden: Boolean = false
) : RenderableSetting<InputUtil.Key>(name, hidden, desc), Saving {

    constructor(name: String, defaultKeyCode: Int, desc: String = "", hidden: Boolean = false) : this(name, InputUtil.Type.KEYSYM.createFromCode(defaultKeyCode), desc, hidden)

    override var value: InputUtil.Key = default
    var onPress: (() -> Unit)? = null
    private var keyNameWidth = -1f

    private var key: InputUtil.Key
        get() = value
        set(newKey) {
            if (newKey == value) return
            value = newKey
            keyNameWidth = NVGRenderer.textWidth(value.localizedText.string, 16f, NVGRenderer.defaultFont)
        }

    override fun render(x: Float, y: Float, mouseX: Double, mouseY: Double): Float {
        super.render(x, y, mouseX, mouseY)
        if (keyNameWidth < 0) keyNameWidth = NVGRenderer.textWidth(value.localizedText.string, 16f, NVGRenderer.defaultFont)
        val height = getHeight()

        val rectX = x + width - 20 - keyNameWidth
        val rectY = y + height / 2f - 10f
        val rectWidth = keyNameWidth + 12f
        val rectHeight = 20f

        NVGRenderer.dropShadow(rectX, rectY, rectWidth, rectHeight, 10f, 0.75f, 5f)
        NVGRenderer.rect(rectX, rectY, rectWidth, rectHeight, gray38.rgba, 5f)
        NVGRenderer.hollowRect(rectX - 1, rectY - 1, rectWidth + 2f, rectHeight + 2f, 1.5f, ClickGUIModule.clickGUIColor.rgba, 4f)

        NVGRenderer.text(name, x + 6f, y + height / 2f - 8f, 16f, Colors.WHITE.rgba, NVGRenderer.defaultFont)
        NVGRenderer.text(value.localizedText.string, rectX + (rectWidth - keyNameWidth) / 2, rectY + rectHeight / 2 - 8f, 16f, if (listening) Colors.MINECRAFT_YELLOW.rgba else Colors.WHITE.rgba, NVGRenderer.defaultFont)

        return height
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, mouseButton: Int): Boolean {
        if (listening) {
            key = InputUtil.Type.MOUSE.createFromCode(mouseButton)
            listening = false
            return true
        } else if (mouseButton == 0 && isHovered) {
            listening = true
            return true
        }
        return false
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (!listening) return false

        when (keyCode) {
            GLFW.GLFW_KEY_ESCAPE, GLFW.GLFW_KEY_BACKSPACE -> key = InputUtil.UNKNOWN_KEY
            GLFW.GLFW_KEY_ENTER -> listening = false
            else -> key = InputUtil.fromKeyCode(keyCode, scanCode)
        }

        listening = false
        return true
    }

    fun onPress(block: () -> Unit): KeybindSetting {
        onPress = block
        return this
    }

    fun isDown(): Boolean =
        value != InputUtil.UNKNOWN_KEY && InputUtil.isKeyPressed(mc.window.handle, value.code)

    override val isHovered: Boolean get() =
        isAreaHovered(lastX + width - 20 - keyNameWidth, lastY + getHeight() / 2f - 10f, keyNameWidth + 12f, 22f)

    override fun write(): JsonElement = JsonPrimitive(value.translationKey)

    override fun read(element: JsonElement?) {
        element?.asString?.let { value = InputUtil.fromTranslationKey(it) }
    }

    override fun reset() {
        value = default
    }
}
