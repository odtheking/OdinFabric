package me.odinmod.odin.clickgui.settings.impl

import me.odinmod.odin.OdinMod.mc
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
        context.matrices.push()
        val sf = mc.window.scaleFactor.toFloat()
        context.matrices.scale(1f / sf, 1f / sf, 1f)
        context.matrices.translate(x.toDouble(), y.toDouble(), 0.0)
        context.matrices.scale(scale, scale, 1f)
        val (width, height) = context.render(example)

        if (example) {
            hoverHandler.handle(x, y, width * scale, height * scale)

            context.fill(0, 0, width.toInt(), 1, Colors.WHITE.rgba) // Top line
            context.fill(0, (height - 1).toInt(), width.toInt(), height.toInt(), Colors.WHITE.rgba) // Bottom line
            context.fill(0, 1, 1, (height - 1).toInt(), Colors.WHITE.rgba) // Left line
            context.fill((width - 1).toInt(), 1, width.toInt(), (height - 1).toInt(), Colors.WHITE.rgba) // Right line
        }

        this.width = width
        this.height = height
        context.matrices.pop()
    }

    fun isHovered(): Boolean = hoverHandler.percent() > 0
}