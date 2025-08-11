package com.odtheking.odin.utils.render

import com.mojang.blaze3d.systems.RenderSystem
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Color.Companion.multiplyAlpha
import com.odtheking.odin.utils.addVec
import com.odtheking.odin.utils.translate
import com.odtheking.odin.utils.unaryMinus
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.render.LightmapTextureManager
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.VertexRendering
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer
import net.minecraft.client.util.BufferAllocator
import net.minecraft.text.OrderedText
import net.minecraft.text.OrderedText.concat
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import org.joml.Vector3f
import kotlin.math.max
import kotlin.math.pow

private val ALLOCATOR = BufferAllocator(1536)

fun WorldRenderContext.drawLine(points: Collection<Vec3d>, color: Color, depth: Boolean, thickness: Float = 3f) {
    if (points.size < 2) return
    val matrix = matrixStack() ?: return
    val bufferSource = consumers() as? VertexConsumerProvider.Immediate ?: return
    val layer = if (depth) CustomRenderLayer.LINE_LIST else CustomRenderLayer.LINE_LIST_ESP
    RenderSystem.lineWidth(thickness)

    matrix.push()
    with(camera().pos) { matrix.translate(-x, -y, -z) }

    val pointList = points.toList()
    for (i in 0 until pointList.size - 1) {
        val start = pointList[i]
        val end = pointList[i + 1]
        val startOffset = Vector3f(start.x.toFloat(), start.y.toFloat(), start.z.toFloat())
        val direction = end.subtract(start)
        VertexRendering.drawVector(
            matrix,
            bufferSource.getBuffer(layer),
            startOffset,
            direction,
            color.rgba
        )
    }

    matrix.pop()
    bufferSource.draw(layer)
}

fun WorldRenderContext.drawWireFrameBox(box: Box, color: Color, thickness: Float = 5f, depth: Boolean = false) {
    val matrix = matrixStack() ?: return
    val bufferSource = consumers() as? VertexConsumerProvider.Immediate ?: return
    val layer = if (depth) CustomRenderLayer.LINE_LIST else CustomRenderLayer.LINE_LIST_ESP
    val camera = camera() ?: return
    RenderSystem.lineWidth((thickness / camera.pos.squaredDistanceTo(box.center).pow(0.15)).toFloat())

    matrix.push()
    with(camera.pos) { matrix.translate(-x, -y, -z) }
    VertexRendering.drawBox(
        matrix,
        bufferSource.getBuffer(layer),
        box,
        color.redFloat,
        color.greenFloat,
        color.blueFloat,
        color.alphaFloat
    )
    matrix.pop()

    bufferSource.draw(layer)
}

fun WorldRenderContext.drawFilledBox(box: Box, color: Color, depth: Boolean = false) {
    val matrix = matrixStack() ?: return
    val bufferSource = consumers() as? VertexConsumerProvider.Immediate ?: return
    val layer = if (depth) CustomRenderLayer.TRIANGLE_STRIP else CustomRenderLayer.TRIANGLE_STRIP_ESP

    matrix.push()
    with(camera().pos) { matrix.translate(-x, -y, -z) }
    VertexRendering.drawFilledBox(
        matrix,
        bufferSource.getBuffer(layer),
        box.minX,
        box.minY,
        box.minZ,
        box.maxX,
        box.maxY,
        box.maxZ,
        color.redFloat,
        color.greenFloat,
        color.blueFloat,
        color.alphaFloat
    )
    matrix.pop()

    bufferSource.draw(layer)
}

fun WorldRenderContext.drawStyledBox(
    box: Box,
    color: Color,
    style: Int = 0,
    depth: Boolean = true
) {
    when (style) {
        0 -> drawFilledBox(box, color, depth = depth)
        1 -> drawWireFrameBox(box, color, depth = depth)
        2 -> {
            drawWireFrameBox(box, color, thickness = 2f, depth = depth)
            drawFilledBox(box, color.multiplyAlpha(0.5f), depth = depth)
        }
    }
}

fun WorldRenderContext.drawBeaconBeam(position: BlockPos, color: Color) {
    val matrix = matrixStack() ?: return
    val bufferSource = consumers() as? VertexConsumerProvider.Immediate ?: return
    val camera = camera()?.pos ?: return

    matrix.push()
    matrix.translate(position.x - camera.x, position.y - camera.y, position.z - camera.z)
    val length = camera.subtract(position.toCenterPos()).horizontalLength().toFloat()
    val scale = if (mc.player != null && mc.player?.isUsingSpyglass == true) 1.0f else maxOf(1.0f, length / 96.0f)

    BeaconBlockEntityRenderer.renderBeam(
        matrix, bufferSource, BeaconBlockEntityRenderer.BEAM_TEXTURE,
        tickCounter().getTickProgress(true), scale, world().time, 0, 319, color.rgba, 0.2f * scale, 0.25f * scale
    )
    matrix.pop()
}

fun WorldRenderContext.drawText(text: OrderedText?, pos: Vec3d, scale: Float, depth: Boolean) {
    val stack = matrixStack() ?: return

    stack.push()
    val matrix = stack.peek().positionMatrix
    with(scale * 0.025f) {
        matrix.translate(pos).translate(-camera().pos).rotate(camera().rotation).scale(this, -this, this)
    }

    val consumers = VertexConsumerProvider.immediate(ALLOCATOR)

    mc.textRenderer.draw(
        text, -mc.textRenderer.getWidth(text) / 2f, 0f, -1, true, matrix, consumers,
        if (depth) TextRenderer.TextLayerType.NORMAL else TextRenderer.TextLayerType.SEE_THROUGH,
        0, LightmapTextureManager.MAX_LIGHT_COORDINATE
    )
    consumers.draw()
    stack.pop()
}

fun WorldRenderContext.drawCustomBeacon(
    title: OrderedText,
    position: BlockPos,
    color: Color,
    increase: Boolean = true,
    distance: Boolean = true
) {
    val dist = mc.player?.blockPos?.getManhattanDistance(position) ?: return

    drawWireFrameBox(Box(position), color, depth = false)
    drawBeaconBeam(position, color)

    drawText(
        (if (distance) concat(title, Text.of(" §r§f(§3${dist}m§f)").asOrderedText()) else title),
        position.toCenterPos().addVec(y = 1.7),
        if (increase) max(1f, (dist / 20.0).toFloat()) else 2f,
        false
    )
}
