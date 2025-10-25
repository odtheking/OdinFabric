package com.odtheking.odin.utils.render

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Colors
import net.minecraft.client.gui.DrawContext
import org.joml.Matrix3x2f
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.hypot
import kotlin.math.max

fun DrawContext.drawString(text: String, x: Int, y: Int, color: Int = Colors.WHITE.rgba, shadow: Boolean = true) {
    this.drawText(mc.textRenderer, text, x, y, color, shadow)
}

fun DrawContext.drawStringWidth(text: String, x: Int, y: Int, color: Color = Colors.WHITE, shadow: Boolean = true): Int {
    drawString(text, x, y, color.rgba, shadow)
    return mc.textRenderer.getWidth(text)
}

fun getStringWidth(text: String): Int = mc.textRenderer.getWidth(text)

fun DrawContext.hollowFill(x: Int, y: Int, width: Int, height: Int, thickness: Int, color: Color) {
    fill(x, y, x + width, y + thickness, color.rgba)
    fill(x, y + height - thickness, x + width, y + height, color.rgba)
    fill(x, y + thickness, x + thickness, y + height - thickness, color.rgba)
    fill(x + width - thickness, y + thickness, x + width, y + height - thickness, color.rgba)
}

fun DrawContext.drawLine(
    x1: Float,
    y1: Float,
    x2: Float,
    y2: Float,
    color: Color,
    lineWidth: Float = 1f
) {
    val dx = x2 - x1
    val dy = y2 - y1

    val half = max(1, (lineWidth / 2f).toInt())

    matrices.pushMatrix()
    matrices.translate(x1, y1)
    matrices.mul(Matrix3x2f().identity().rotate(atan2(dy, dx)))
    fill(0, -half, ceil(hypot(dx, dy)).toInt(), half, color.rgba)
    matrices.popMatrix()
}