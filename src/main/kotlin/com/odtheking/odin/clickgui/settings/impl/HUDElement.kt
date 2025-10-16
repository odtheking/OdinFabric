package com.odtheking.odin.clickgui.settings.impl

import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.hollowFill
import com.odtheking.odin.utils.ui.isAreaHovered
import net.minecraft.client.gui.DrawContext

open class HudElement(
    var x: Int,
    var y: Int,
    var scale: Float,
    var enabled: Boolean = true,
    val render: DrawContext.(Boolean) -> Pair<Int, Int> = { _ -> 0 to 0 }
) {
    var width: Int = 0
        private set
    var height: Int = 0
        private set

    fun draw(context: DrawContext, example: Boolean) {
        context.matrices.pushMatrix()
        context.matrices.translate(x.toFloat(), y.toFloat())

        context.matrices.scale(scale, scale)
        val (width, height) = context.render(example).let { (w, h) -> w to h }

        context.matrices.popMatrix()
        if (example) context.hollowFill(x, y, (width * scale).toInt(), (height * scale).toInt(), if (isHovered()) 2 else 1, Colors.WHITE)

        this.width = width
        this.height = height
    }

    fun isHovered(): Boolean = isAreaHovered(x.toFloat(), y.toFloat(), width * scale, height * scale)
}