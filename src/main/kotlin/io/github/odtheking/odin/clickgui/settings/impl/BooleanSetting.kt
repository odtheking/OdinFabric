package io.github.odtheking.odin.clickgui.settings.impl

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import io.github.odtheking.odin.clickgui.ClickGUI.gray38
import io.github.odtheking.odin.clickgui.settings.RenderableSetting
import io.github.odtheking.odin.clickgui.settings.Saving
import io.github.odtheking.odin.features.impl.render.ClickGUIModule
import io.github.odtheking.odin.utils.Colors
import io.github.odtheking.odin.utils.ui.animations.LinearAnimation
import io.github.odtheking.odin.utils.ui.isAreaHovered
import io.github.odtheking.odin.utils.ui.rendering.NVGRenderer

class BooleanSetting(
    name: String,
    override val default: Boolean = false,
    desc: String,
) : RenderableSetting<Boolean>(name, desc), Saving {

    override var value: Boolean = default
    var enabled: Boolean by this::value

    private val toggleAnimation = LinearAnimation<Float>(200)

    override fun render(x: Float, y: Float, mouseX: Float, mouseY: Float): Float {
        super.render(x, y, mouseX, mouseY)
        val height = getHeight()

        NVGRenderer.text(name, x + 6f, y + height / 2f - 8f, 16f, Colors.WHITE.rgba, NVGRenderer.defaultFont)

        NVGRenderer.rect(x + width - 40f, y + height / 2f - 10f, 34f, 20f, gray38.rgba, 9f)

        if (enabled || toggleAnimation.isAnimating())
            NVGRenderer.rect(
                x + width - 40f,
                y + height / 2f - 10f,
                toggleAnimation.get(34f, 9f, enabled),
                20f,
                ClickGUIModule.clickGUIColor.rgba,
                9f
            )

        NVGRenderer.hollowRect(
            x + width - 40f,
            y + height / 2f - 10f,
            34f,
            20f,
            2f,
            ClickGUIModule.clickGUIColor.rgba,
            9f
        )
        NVGRenderer.circle(x + width - toggleAnimation.get(30f, 14f, !enabled), y + height / 2f, 6f, Colors.WHITE.rgba)

        return height
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, mouseButton: Int): Boolean {
        return if (mouseButton != 0 || !isHovered) false
        else {
            toggleAnimation.start()
            enabled = !enabled
            true
        }
    }

    override val isHovered: Boolean get() = isAreaHovered(lastX + width - 43f, lastY + getHeight() / 2f - 10f, 34f, 20f)

    override fun write(): JsonElement = JsonPrimitive(enabled)

    override fun read(element: JsonElement?) {
        if (element?.asBoolean != enabled) enabled = !enabled
    }
}