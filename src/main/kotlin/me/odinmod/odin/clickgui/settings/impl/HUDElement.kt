package me.odinmod.odin.clickgui.settings.impl

import me.odinmod.odin.utils.Colors
import me.odinmod.odin.utils.ui.HoverHandler
import net.minecraft.client.gui.DrawContext

typealias Render = DrawContext.(Boolean) -> Pair<Float, Float>

open class HudElement(
    var x: Float,
    var y: Float,
    var scale: Float,
    var enabled: Boolean = true,
    val render: DrawContext.(Boolean) -> Pair<Float, Float> = { _ -> 0f to 0f }
) {
    private val hoverHandler = HoverHandler(200)

    var width = 0f
        private set
    var height = 0f
        private set

    fun draw(context: DrawContext, example: Boolean) {
        context.matrices.translate(x.toDouble(), y.toDouble(), 0.0)
        context.matrices.scale(scale, scale, 1f)
        val (width, height) = context.render(example)

        if (example) {
            hoverHandler.handle(x, y, width * scale, height * scale)

            context.fill(0, 0, width.toInt(), 1, Colors.WHITE.rgba)
            context.fill(0, (height - 1).toInt(), width.toInt(), height.toInt(), Colors.WHITE.rgba)
            context.fill(0, 1, 1, (height - 1).toInt(), Colors.WHITE.rgba)
            context.fill((width - 1).toInt(), 1, width.toInt(), (height - 1).toInt(), Colors.WHITE.rgba)
        }

        this.width = width
        this.height = height
    }

    fun isHovered(): Boolean = hoverHandler.percent() > 0
}