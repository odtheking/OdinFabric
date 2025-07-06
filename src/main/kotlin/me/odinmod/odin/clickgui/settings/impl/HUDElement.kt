package me.odinmod.odin.clickgui.settings.impl

import me.odinmod.odin.utils.Colors
import me.odinmod.odin.utils.render.hollowFill
import me.odinmod.odin.utils.ui.MouseUtils.isAreaHovered
import net.minecraft.client.gui.DrawContext

open class HudElement(
    var x: Float,
    var y: Float,
    var scale: Float,
    var enabled: Boolean = true,
    val render: DrawContext.(Boolean) -> Pair<Number, Number> = { _ -> 0f to 0f }
) {
    var width = 0f
        private set
    var height = 0f
        private set

    fun draw(context: DrawContext, example: Boolean) {
        context.matrices.push()
        context.matrices.translate(x.toDouble(), y.toDouble(), 1.0)
        context.matrices.scale(scale, scale, 1f)
        val (width, height) = context.render(example).let { (w, h) -> w.toFloat() to h.toFloat() }

        if (example) context.hollowFill(0f, 0f, width, height, 1 / scale + if (isHovered()) 0.5f else 0f, Colors.WHITE)

        context.matrices.pop()

        this.width = width
        this.height = height
    }

    fun isHovered(): Boolean = isAreaHovered(x, y, width * scale, height * scale)
}