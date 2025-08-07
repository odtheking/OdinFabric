package com.odtheking.odin.utils.render

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Colors
import com.odtheking.mixin.accessors.DrawContextAccessor
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderLayer
import net.minecraft.util.math.RotationAxis
import kotlin.math.atan2
import kotlin.math.sqrt

fun DrawContext.drawString(text: String, x: Float, y: Float, color: Int = 0xFFFFFF, shadow: Boolean = true) {
    mc.textRenderer.draw(
        text,
        x,
        y,
        color,
        shadow,
        matrices.peek().positionMatrix,
        (this as DrawContextAccessor).vertexConsumers,
        TextRenderer.TextLayerType.NORMAL,
        0,
        15728880
    )
}

fun DrawContext.drawStringWidth(
    text: String,
    x: Float,
    y: Float,
    color: Color = Colors.WHITE,
    shadow: Boolean = true
): Float {
    drawString(text, x, y, color.rgba, shadow)
    return mc.textRenderer.getWidth(text).toFloat()
}

fun DrawContext.fill(x1: Float, y1: Float, x2: Float, y2: Float, color: Color) {
    val matrix = matrices.peek().positionMatrix

    val minX = x1.coerceAtMost(x2)
    val maxX = x1.coerceAtLeast(x2)
    val minY = y1.coerceAtMost(y2)
    val maxY = y1.coerceAtLeast(y2)

    with((this as DrawContextAccessor).vertexConsumers.getBuffer(RenderLayer.getGui())) {
        vertex(matrix, minX, minY, 0f).color(color.rgba)
        vertex(matrix, minX, maxY, 0f).color(color.rgba)
        vertex(matrix, maxX, maxY, 0f).color(color.rgba)
        vertex(matrix, maxX, minY, 0f).color(color.rgba)
    }
}

fun DrawContext.hollowFill(x: Float, y: Float, width: Float, height: Float, thickness: Float, color: Color) {
    fill(x, y, x + width, y + thickness, color)
    fill(x, y + height - thickness, x + width, y + height, color)
    fill(x, y + thickness, x + thickness, y + height - thickness, color)
    fill(x + width - thickness, y + thickness, x + width, y + height - thickness, color)
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

    matrices.push()
    matrices.translate(x1.toDouble(), y1.toDouble(), 0.0)
    matrices.multiply(RotationAxis.POSITIVE_Z.rotation(atan2(dy.toDouble(), dx.toDouble()).toFloat()))
    fill(0f, -lineWidth / 2f, sqrt((dx * dx + dy * dy)), lineWidth / 2f, color)
    matrices.pop()
}