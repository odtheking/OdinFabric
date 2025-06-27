package me.odinmod.odin.clickgui.settings.impl

import me.odinmod.odin.clickgui.Panel
import me.odinmod.odin.clickgui.RenderableSetting
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

    override val isHovered: Boolean get() = isAreaHovered(lastX + width - 28f, lastY + Panel.HEIGHT / 2f - 15f, 24f, 24f)

    override fun render(x: Float, y: Float, mouseX: Double, mouseY: Double): Float {
        super.render(x, y, mouseX, mouseY)

        NVGRenderer.text(name, x + 6f, y + Panel.HEIGHT / 2f - 8f, 16f, Colors.WHITE.rgba, NVGRenderer.defaultFont)

        NVGRenderer.push()
        NVGRenderer.translate(x + width - 28f + 12f, y + Panel.HEIGHT / 2f - 15f + 12f)
        NVGRenderer.rotate(toggleAnimation.get(0f, Math.PI.toFloat() / 2f, enabled))
        NVGRenderer.translate(-12f, -12f)
        NVGRenderer.renderImage("/assets/odin/chevron.svg", 0f, 0f, 24f, 24f)
        NVGRenderer.pop()

        return Panel.HEIGHT
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, mouseButton: Int): Boolean {
        if (mouseButton != 0 || !isHovered) return false
        enabled = !enabled
        toggleAnimation.start()
        return true
    }
}