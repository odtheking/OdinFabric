package me.odinmod.odin.utils

import me.odinmod.odin.OdinMod.mc
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gl.RenderPipelines
import net.minecraft.client.render.*
import net.minecraft.client.util.BufferAllocator
import net.minecraft.text.OrderedText
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

private val ALLOCATOR = BufferAllocator(1536)

fun drawBox(box: Box, context: WorldRenderContext, color: List<Float>) {
    val matrix = context.matrixStack() ?: return
    val bufferSource = context.consumers() as? VertexConsumerProvider.Immediate ?: return
    val buffer = bufferSource.getBuffer(CUSTOM_LINE_LAYER)
    val camPos = context.camera().pos

    matrix.push()
    matrix.translate(-camPos.x, -camPos.y, -camPos.z)
    VertexRendering.drawBox(matrix, buffer, box, color[0], color[1], color[2], color[3])
    matrix.pop()

    bufferSource.draw(CUSTOM_LINE_LAYER)
}

fun drawFilledBox(box: Box, context: WorldRenderContext, color: List<Float>) {
    val matrix = context.matrixStack() ?: return
    val bufferSource = context.consumers() as? VertexConsumerProvider.Immediate ?: return
    val buffer = bufferSource.getBuffer(FILLED_BOX_LAYER)
    val camPos = context.camera().pos

    matrix.push()
    matrix.translate(-camPos.x, -camPos.y, -camPos.z)
    VertexRendering.drawFilledBox(matrix, buffer, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, color[0], color[1], color[2], color[3])
    matrix.pop()

    bufferSource.draw(CUSTOM_LINE_LAYER)
}

fun renderText(
    context: WorldRenderContext,
    text: OrderedText?,
    pos: Vec3d,
    scale: Float,
    yOffset: Float,
    throughWalls: Boolean
) {
    val stack = context.matrixStack() ?: return

    stack.push()
    val matrix = stack.peek().positionMatrix
    with (scale * 0.025f) {
        matrix.translate(pos).translate(-context.camera().pos).rotate(context.camera().rotation).scale(this, -this, this)
    }

    val xOffset = -mc.textRenderer.getWidth(text) / 2f
    val consumers = VertexConsumerProvider.immediate(ALLOCATOR)

    mc.textRenderer.draw(
        text, xOffset, yOffset, -1, false, matrix, consumers,
        if (throughWalls) TextRenderer.TextLayerType.SEE_THROUGH else TextRenderer.TextLayerType.NORMAL,
        0, LightmapTextureManager.MAX_LIGHT_COORDINATE
    )
    consumers.draw()
    stack.pop()
}

fun drawSphere(
    center: Vec3d,
    radius: Double,
    segments: Int,
    context: WorldRenderContext,
    color: List<Float>
) {
    val matrixStack = context.matrixStack() ?: return
    val bufferSource = context.consumers() as? VertexConsumerProvider.Immediate ?: return
    val buffer = bufferSource.getBuffer(CUSTOM_LINE_LAYER)
    val camPos = context.camera().pos

    matrixStack.push()
    matrixStack.translate(-camPos.x, -camPos.y, -camPos.z)

    val matrix = matrixStack.peek().positionMatrix

    val (r, g, b, a) = color

    // Draw latitude lines
    for (i in 0..segments) {
        val theta = Math.PI * i / segments
        val sinTheta = sin(theta)
        val cosTheta = cos(theta)

        for (j in 0 until segments) {
            val phi1 = 2 * Math.PI * j / segments
            val phi2 = 2 * Math.PI * (j + 1) / segments

            val x1 = center.x + radius * sinTheta * cos(phi1)
            val y1 = center.y + radius * cosTheta
            val z1 = center.z + radius * sinTheta * sin(phi1)

            val x2 = center.x + radius * sinTheta * cos(phi2)
            val z2 = center.z + radius * sinTheta * sin(phi2)

            buffer.vertex(matrix, x1.toFloat(), y1.toFloat(), z1.toFloat()).color(r, g, b, a).normal(1f, 1f, 1f)
            buffer.vertex(matrix, x2.toFloat(), y1.toFloat(), z2.toFloat()).color(r, g, b, a).normal(1f, 1f, 1f)
        }
    }

    // Draw longitude lines
    for (j in 0..segments) {
        val phi = 2 * Math.PI * j / segments

        for (i in 0 until segments) {
            val theta1 = Math.PI * i / segments
            val theta2 = Math.PI * (i + 1) / segments

            val x1 = center.x + radius * sin(theta1) * cos(phi)
            val y1 = center.y + radius * cos(theta1)
            val z1 = center.z + radius * sin(theta1) * sin(phi)

            val x2 = center.x + radius * sin(theta2) * cos(phi)
            val y2 = center.y + radius * cos(theta2)
            val z2 = center.z + radius * sin(theta2) * sin(phi)

            buffer.vertex(matrix, x1.toFloat(), y1.toFloat(), z1.toFloat()).color(r, g, b, a).normal(1f, 1f, 1f)
            buffer.vertex(matrix, x2.toFloat(), y2.toFloat(), z2.toFloat()).color(r, g, b, a).normal(1f, 1f, 1f)
        }
    }

    matrixStack.pop()
    bufferSource.draw(CUSTOM_LINE_LAYER)
}

val CUSTOM_LINE_LAYER: RenderLayer = RenderLayer.of(
    "lines", RenderLayer.DEFAULT_BUFFER_SIZE, false, false, RenderPipelines.LINES,
    RenderLayer.MultiPhaseParameters.builder()
        .lineWidth(RenderPhase.LineWidth(OptionalDouble.of(1.0)))
        .layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
        .texture(RenderPhase.NO_TEXTURE)
        .build(false))

val FILLED_BOX_LAYER: RenderLayer = RenderLayer.of(
    "filled_box", RenderLayer.DEFAULT_BUFFER_SIZE, false, false, RenderPipelines.DEBUG_FILLED_BOX,
    RenderLayer.MultiPhaseParameters.builder()
        .layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
        .build(false))

