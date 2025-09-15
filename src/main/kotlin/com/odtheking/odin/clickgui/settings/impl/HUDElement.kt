package com.odtheking.odin.clickgui.settings.impl

import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.hollowFill
import com.odtheking.odin.utils.ui.isAreaHovered
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
        context.matrices.pushMatrix()
        context.matrices.translate(x, y)
        context.matrices.scale(scale, scale)
        val (width, height) = context.render(example).let { (w, h) -> w.toFloat() to h.toFloat() }

        if (example) context.hollowFill(0f, 0f, width, height, 1 / scale + if (isHovered()) 0.5f else 0f, Colors.WHITE)

        context.matrices.popMatrix()

        this.width = width
        this.height = height
    }

    fun isHovered(): Boolean = isAreaHovered(x, y, width * scale, height * scale)
}