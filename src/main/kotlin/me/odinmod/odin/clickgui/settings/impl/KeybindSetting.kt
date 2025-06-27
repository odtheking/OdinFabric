package me.odinmod.odin.clickgui.settings.impl

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import me.odinmod.odin.OdinMod.mc
import me.odinmod.odin.clickgui.ClickGUI.gray38
import me.odinmod.odin.clickgui.Panel
import me.odinmod.odin.clickgui.RenderableSetting
import me.odinmod.odin.clickgui.settings.Saving
import me.odinmod.odin.features.impl.render.ClickGUIModule
import me.odinmod.odin.utils.Colors
import me.odinmod.odin.utils.ui.MouseUtils.isAreaHovered
import me.odinmod.odin.utils.ui.rendering.NVGRenderer
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW

class KeybindSetting(
    name: String,
    override val default: Int,
    desc: String,
    hidden: Boolean = false
) : RenderableSetting<Int>(name, hidden, desc), Saving {

    override var value: Int = default

    var onPress: (() -> Unit)? = null

    private var key: Int
        get() = value
        set(value) {
            if (value == this.value) return
            this.value = value
            keyName = getKeyName(value)
            keyNameWidth = NVGRenderer.textWidth(keyName, 16f, NVGRenderer.defaultFont)
        }

    private var keyName = ""
    private var keyNameWidth = -1f

    override fun render(x: Float, y: Float, mouseX: Double, mouseY: Double): Float {
        super.render(x, y, mouseX, mouseY)
        if (keyName.isEmpty()) keyName = getKeyName(value)
        if (keyNameWidth < 0) keyNameWidth = NVGRenderer.textWidth(keyName, 16f, NVGRenderer.defaultFont)

        val rectX = x + width - 20 - keyNameWidth
        val rectY = y + Panel.HEIGHT / 2f - 10f
        val rectWidth = keyNameWidth + 12f
        val rectHeight = 20f

        NVGRenderer.dropShadow(rectX, rectY, rectWidth, rectHeight, 10f, 0.75f, 5f)
        NVGRenderer.rect(rectX, rectY, rectWidth, rectHeight, gray38.rgba, 5f)
        NVGRenderer.hollowRect(rectX - 1, rectY - 1, rectWidth + 2f, rectHeight + 2f, 1.5f, ClickGUIModule.clickGUIColor.rgba, 4f)

        NVGRenderer.text(name, x + 6f, y + Panel.HEIGHT / 2f - 8f, 16f, Colors.WHITE.rgba, NVGRenderer.defaultFont)
        NVGRenderer.text(keyName, rectX + (rectWidth - keyNameWidth) / 2, rectY + rectHeight / 2 - 8f, 16f, if (listening) Colors.MINECRAFT_YELLOW.rgba else Colors.WHITE.rgba, NVGRenderer.defaultFont)

        return Panel.HEIGHT
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, mouseButton: Int): Boolean {
        if (listening) {
            key = -100 + mouseButton
            listening = false
            return true
        } else if (mouseButton == 0 && isHovered) {
            listening = !listening
            return true
        }
        return false
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (listening) {
            when (keyCode) {
                GLFW.GLFW_KEY_ESCAPE, GLFW.GLFW_KEY_BACKSPACE -> key = 0
                GLFW.GLFW_KEY_ENTER -> listening = false
                else -> key = keyCode
            }
            listening = false
            return true
        }
        return false
    }

    private fun getKeyName(key: Int): String {
        return when {
            key > 0 -> InputUtil.fromKeyCode(key, 0)?.localizedText?.string
            key < 0 -> InputUtil.Type.MOUSE.createFromCode(key + 100)?.localizedText?.string
            else -> "None"
        } ?: "Error"
    }

    fun onPress(block: () -> Unit): KeybindSetting {
        onPress = block
        return this
    }

    fun isDown(): Boolean {
        return if (value == 0) false
        else InputUtil.isKeyPressed(mc.window.handle, value + if (value < 0) 100 else 0)
    }

    override val isHovered: Boolean get() =
        isAreaHovered(lastX + width - 20 - keyNameWidth, lastY + Panel.HEIGHT / 2f - 10f, keyNameWidth + 12f, 22f)

    override fun write(): JsonElement = JsonPrimitive(value)

    override fun read(element: JsonElement?) {
        element?.asInt?.let { value = it }
    }

    override fun reset() {
        value = default
    }
}