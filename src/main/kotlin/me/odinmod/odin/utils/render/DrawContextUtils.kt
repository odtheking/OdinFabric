package me.odinmod.odin.utils.render

import me.odinmod.odin.OdinMod.mc
import mixins.DrawContextAccessor
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderLayer

fun DrawContext.drawString(text: String, x: Float, y: Float, color: Int = 0xFFFFFF, shadow: Boolean = true) {
    mc.textRenderer.draw(text, x, y, color, shadow, matrices.peek().positionMatrix, (this as DrawContextAccessor).vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, 15728880)
}

fun DrawContext.drawStringWidth(text: String, x: Float, y: Float, color: Int = 0xFFFFFF, shadow: Boolean = true): Float {
    drawString(text, x, y, color, shadow)
    return mc.textRenderer.getWidth(text).toFloat()
}

fun DrawContext.fill(x1: Float, y1: Float, x2: Float, y2: Float, color: Int) {
    val matrix = matrices.peek().positionMatrix

    val minX = x1.coerceAtMost(x2)
    val maxX = x1.coerceAtLeast(x2)
    val minY = y1.coerceAtMost(y2)
    val maxY = y1.coerceAtLeast(y2)

    with ((this as DrawContextAccessor).vertexConsumers.getBuffer(RenderLayer.getGui())) {
        vertex(matrix, minX, minY, 0f).color(color)
        vertex(matrix, minX, maxY, 0f).color(color)
        vertex(matrix, maxX, maxY, 0f).color(color)
        vertex(matrix, maxX, minY, 0f).color(color)
    }
}

fun DrawContext.hollowFill(x: Float, y: Float, width: Float, height: Float, thickness: Float, color: Int) {
    fill(x, y, x + width, y + thickness, color)
    fill(x, y + height - thickness, x + width, y + height, color)
    fill(x, y + thickness, x + thickness, y + height - thickness, color)
    fill(x + width - thickness, y + thickness, x + width, y + height - thickness, color)
}
