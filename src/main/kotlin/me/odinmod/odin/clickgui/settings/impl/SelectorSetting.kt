package me.odinmod.odin.clickgui.settings.impl

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import me.odinmod.odin.clickgui.ClickGUI.gray38
import me.odinmod.odin.clickgui.Panel
import me.odinmod.odin.clickgui.settings.RenderableSetting
import me.odinmod.odin.clickgui.settings.Saving
import me.odinmod.odin.features.impl.render.ClickGUIModule
import me.odinmod.odin.utils.Color
import me.odinmod.odin.utils.Color.Companion.brighter
import me.odinmod.odin.utils.Colors
import me.odinmod.odin.utils.ui.HoverHandler
import me.odinmod.odin.utils.ui.MouseUtils.isAreaHovered
import me.odinmod.odin.utils.ui.animations.EaseInOutAnimation
import me.odinmod.odin.utils.ui.rendering.NVGRenderer

class SelectorSetting(
    name: String,
    default: String,
    private var options: List<String>,
    desc: String,
    hidden: Boolean = false
) : RenderableSetting<Int>(name, hidden, desc), Saving {

    override val default: Int = optionIndex(default)

    override var value: Int
        get() = index
        set(value) {
            index = value
        }

    private var index: Int = optionIndex(default)
        set(value) {
            field = if (value > options.size - 1) 0 else if (value < 0) options.size - 1 else value
        }

    private var selected: String
        get() = options[index]
        set(value) {
            index = optionIndex(value)
        }

    private val elementWidths by lazy { options.map { NVGRenderer.textWidth(it, 16f, NVGRenderer.defaultFont) } }
    private val settingAnim = EaseInOutAnimation(200)
    private val hover = HoverHandler(0, 150)
    private val defaultHeight = Panel.HEIGHT
    private var extended = false

    private val color: Color get() = gray38.brighter(1 + hover.percent() / 500f)

    private fun isSettingHovered(index: Int): Boolean =
        isAreaHovered(lastX, lastY + 38f + 32f * index, width, 32f)

    override fun render(x: Float, y: Float, mouseX: Double, mouseY: Double): Float {
        super.render(x, y, mouseX, mouseY)

        val currentWidth = elementWidths[index]

        hover.handle(x + width - 20f - currentWidth, y + defaultHeight / 2f - 10f, currentWidth + 12f, 22f)
        NVGRenderer.rect(x + width - 20f - currentWidth, y + defaultHeight / 2f - 10f, currentWidth + 12f, 20f, color.rgba, 5f)
        NVGRenderer.hollowRect(x + width - 20f - currentWidth, y + defaultHeight / 2f - 10f, currentWidth + 12f, 20f, 1.5f, ClickGUIModule.clickGUIColor.rgba, 5f)

        NVGRenderer.text(name, x + 6f, y + defaultHeight / 2f - 8f, 16f, Colors.WHITE.rgba, NVGRenderer.defaultFont)
        NVGRenderer.text(selected, x + width - 14f - currentWidth, y + defaultHeight / 2f - 8f, 16f, Colors.WHITE.rgba, NVGRenderer.defaultFont)

        if (!extended && !settingAnim.isAnimating()) return defaultHeight

        val displayHeight = getHeight()
        if (settingAnim.isAnimating()) NVGRenderer.pushScissor(x, y, width, displayHeight)

        NVGRenderer.rect(x + 6, y + 37f, width - 12f, options.size * 32f, gray38.rgba, 5f)

        for (i in options.indices) {
            val optionY = y + 38 + 32 * i
            if (i != options.size - 1) NVGRenderer.line(x + 18f, optionY + 32, x + width - 12f, optionY + 32, 1.5f, Colors.MINECRAFT_DARK_GRAY.rgba)
            NVGRenderer.text(options[i], x + width / 2f - elementWidths[i] / 2, optionY + 8f, 16f, Colors.WHITE.rgba, NVGRenderer.defaultFont)
            if (isSettingHovered(i)) NVGRenderer.hollowRect(x + 6, optionY, width - 12f, 32f, 1.5f, ClickGUIModule.clickGUIColor.rgba, 4f)
        }
        if (settingAnim.isAnimating()) NVGRenderer.popScissor()

        return displayHeight
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, mouseButton: Int): Boolean {
        if (mouseButton == 0) {
            if (isHovered) {
                settingAnim.start()
                extended = !extended
                return true
            }

            if (!extended) return false

            for (index in options.indices) {
                if (isSettingHovered(index)) {
                    settingAnim.start()
                    selected = options[index]
                    extended = false
                    return true
                }
            }
        } else if (mouseButton == 1) {
            if (isHovered) {
                index++
                return true
            }
        }
        return false
    }

    private fun optionIndex(string: String): Int =
        options.map { it.lowercase() }.indexOf(string.lowercase()).coerceIn(0, options.size - 1)

    override val isHovered: Boolean get() = isAreaHovered(lastX, lastY, width, defaultHeight)

    override fun getHeight(): Float =
        settingAnim.get(defaultHeight, options.size * 32f + 44, !extended)

    override fun write(): JsonElement = JsonPrimitive(selected)

    override fun read(element: JsonElement?) {
        element?.asString?.let { selected = it }
    }
}