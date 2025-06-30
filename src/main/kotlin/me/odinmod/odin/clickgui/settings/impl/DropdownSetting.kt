package me.odinmod.odin.clickgui.settings.impl

import me.odinmod.odin.clickgui.settings.RenderableSetting
import me.odinmod.odin.utils.Colors
import me.odinmod.odin.utils.ui.MouseUtils.isAreaHovered
import me.odinmod.odin.utils.ui.animations.LinearAnimation
import me.odinmod.odin.utils.ui.rendering.NVGRenderer

/**
 * A setting intended to show or hide other settings in the GUI.
 *
 * @author Bonsai
 */
class DropdownSetting(
    name: String,
    override val default: Boolean = false,
    desc: String = ""
) : RenderableSetting<Boolean>(name, false, desc) {

    override var value: Boolean = default
    private var enabled: Boolean by this::value

    private val toggleAnimation = LinearAnimation<Float>(200)

    override fun render(x: Float, y: Float, mouseX: Double, mouseY: Double): Float {
        super.render(x, y, mouseX, mouseY)
        val height = getHeight()

        NVGRenderer.text(name, x + 6f, y + height / 2f - 8f, 16f, Colors.WHITE.rgba, NVGRenderer.defaultFont)

        NVGRenderer.push()
        NVGRenderer.translate(x + width - 18f, y + height / 2f - 4f)
        NVGRenderer.rotate(toggleAnimation.get(0f, Math.PI.toFloat() / 2f, enabled))
        NVGRenderer.translate(-12f, -12f)
        NVGRenderer.image("/assets/odin/chevron.svg", 0f, 0f, 24f, 24f, 0f)
        NVGRenderer.pop()

        return height
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, mouseButton: Int): Boolean {
        if (mouseButton != 0 || !isHovered) return false
        enabled = !enabled
        toggleAnimation.start()
        return true
    }

    override val isHovered: Boolean get() = isAreaHovered(lastX + width - 30f, lastY + getHeight() / 2f - 16f, 24f, 24f)
}