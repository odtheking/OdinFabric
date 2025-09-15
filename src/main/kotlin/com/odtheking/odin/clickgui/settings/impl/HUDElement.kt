package com.odtheking.odin.clickgui.settings.impl

import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.hollowFill
import com.odtheking.odin.utils.ui.isAreaHovered
import net.minecraft.client.gui.DrawContext

open class HudElement(
    var x: Int,
    var y: Int,
    var scale: Int,
    var enabled: Boolean = true,
    val render: DrawContext.(Boolean) -> Pair<Number, Number> = { _ -> 0f to 0f }
) {
    var width: Int = 0
        private set
    var height: Int = 0
        private set

    fun draw(context: DrawContext, example: Boolean) {
        context.matrices.pushMatrix()
        context.matrices.translate(x.toFloat(), y.toFloat())
        context.matrices.scale(scale.toFloat(), scale.toFloat())
        val (width, height) = context.render(example).let { (w, h) -> w to h }

        if (example) context.hollowFill(0, 0, width.toInt(), height.toInt(), 1 / scale, Colors.WHITE)

        context.matrices.popMatrix()

        this.width = width.toInt()
        this.height = height.toInt()
    }

    fun isHovered(): Boolean = isAreaHovered(x.toFloat(), y.toFloat(), (width * scale).toFloat(), (height * scale).toFloat())
}