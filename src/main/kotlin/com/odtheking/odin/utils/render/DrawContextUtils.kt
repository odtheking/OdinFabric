package com.odtheking.odin.utils.render

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Colors
import net.minecraft.client.gui.DrawContext
import org.joml.Matrix3x2f
import kotlin.math.atan2
import kotlin.math.sqrt

fun DrawContext.drawString(text: String, x: Int, y: Int, color: Int = 0xFFFFFF, shadow: Boolean = true) {
    this.drawString(text, x, y, color, shadow)
}

fun DrawContext.drawStringWidth(
    text: String,
    x: Int,
    y: Int,
    color: Color = Colors.WHITE,
    shadow: Boolean = true
): Int {
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

    matrices.pushMatrix()
    matrices.translate(x1, y1)
    val angle = atan2(dy.toDouble(), dx.toDouble()).toFloat()
    val right = Matrix3x2f().rotate(angle)
    matrices.mul(right)
    this.fill(0, (-lineWidth / 2f).toInt(), sqrt((dx * dx + dy * dy)).toInt(), (lineWidth / 2f).toInt(), color.rgba)
    matrices.popMatrix()
}