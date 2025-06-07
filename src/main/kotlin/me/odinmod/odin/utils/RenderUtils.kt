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
import org.joml.Matrix4f
import java.util.*

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

val CUSTOM_LINE_LAYER: RenderLayer = RenderLayer.of(
    "lines", RenderLayer.DEFAULT_BUFFER_SIZE, false, false, RenderPipelines.LINES,
    RenderLayer.MultiPhaseParameters.builder()
        .lineWidth(RenderPhase.LineWidth(OptionalDouble.of(1.0)))
        .layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
        .texture(RenderPhase.NO_TEXTURE)
        .build(false));

