package me.odinmod.odin.clickgui.settings.impl

import me.odinmod.odin.utils.Colors
import me.odinmod.odin.utils.render.hollowFill
import me.odinmod.odin.utils.ui.HoverHandler
import net.minecraft.client.gui.DrawContext

open class HudElement(
    var x: Float,
    var y: Float,
    var scale: Float,
    var enabled: Boolean = true,
    val render: DrawContext.(Boolean) -> Pair<Number, Number> = { _ -> 0f to 0f }
) {
    private val hoverHandler = HoverHandler(200)

    var width = 0f
        private set
    var height = 0f
        private set

    fun draw(context: DrawContext, example: Boolean) {
        context.matrices.push()
        context.matrices.translate(x.toDouble(), y.toDouble(), 0.0)
        context.matrices.scale(scale, scale, 1f)
        val (width, height) = context.render(example).let { (w, h) -> w.toFloat() to h.toFloat() }

        if (example) {
            hoverHandler.handle(x, y, width * scale, height * scale)
            context.hollowFill(0f, 0f, width, height, 1 / scale, Colors.WHITE.rgba)
        }
        context.matrices.pop()

        this.width = width
        this.height = height
    }

    fun isHovered(): Boolean = hoverHandler.percent() > 0
}