package me.odinmod.odin.utils.render

import com.mojang.blaze3d.systems.RenderSystem
import me.odinmod.odin.OdinMod.mc
import me.odinmod.odin.utils.Color
import me.odinmod.odin.utils.translate
import me.odinmod.odin.utils.unaryMinus
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.render.LightmapTextureManager
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.VertexRendering
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer
import net.minecraft.client.util.BufferAllocator
import net.minecraft.text.OrderedText
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sin

private val ALLOCATOR = BufferAllocator(1536)

fun WorldRenderContext.drawWireFrameBox(box: Box, color: Color, thickness: Float = 5f, depth: Boolean = false) {
    val matrix = matrixStack() ?: return
    val bufferSource = consumers() as? VertexConsumerProvider.Immediate ?: return
    val layer = if (depth) CustomRenderLayer.LINE_LIST else CustomRenderLayer.LINE_LIST_ESP
    val camera = camera() ?: return
    RenderSystem.lineWidth((thickness / camera.pos.squaredDistanceTo(box.center).pow(0.15)).toFloat())

    matrix.push()
    with(camera.pos) { matrix.translate(-x, -y, -z) }
    VertexRendering.drawBox(matrix, bufferSource.getBuffer(layer), box, color.redFloat, color.greenFloat, color.blueFloat, color.alphaFloat)
    matrix.pop()

    bufferSource.draw(layer)
}

fun WorldRenderContext.drawFilledBox(box: Box, color: Color, depth: Boolean = false) {
    val matrix = matrixStack() ?: return
    val bufferSource = consumers() as? VertexConsumerProvider.Immediate ?: return
    val layer = if (depth) CustomRenderLayer.TRIANGLE_STRIP else CustomRenderLayer.TRIANGLE_STRIP_ESP

    matrix.push()
    with(camera().pos) { matrix.translate(-x, -y, -z) }
    VertexRendering.drawFilledBox(matrix, bufferSource.getBuffer(layer), box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, color.redFloat, color.greenFloat, color.blueFloat, color.alphaFloat)
    matrix.pop()

    bufferSource.draw(layer)
}

fun WorldRenderContext.drawBeaconBeam(position: BlockPos, color: Color) {
    val matrix = matrixStack() ?: return
    val bufferSource = consumers() as? VertexConsumerProvider.Immediate ?: return
    val camera = camera()?.pos ?: return

    matrix.push()
    matrix.translate(position.x - camera.x, position.y -camera.y, position.z -camera.z)
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
    with (scale * 0.025f) {
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

fun WorldRenderContext.drawSphere(
    center: Vec3d,
    radius: Double,
    segments: Int,
    color: Color
) {
    val matrixStack = matrixStack() ?: return
    val bufferSource = consumers() as? VertexConsumerProvider.Immediate ?: return
    val buffer = bufferSource.getBuffer(CustomRenderLayer.LINE_LIST)
    val camPos = camera().pos

    matrixStack.push()
    matrixStack.translate(-camPos.x, -camPos.y, -camPos.z)

    val matrix = matrixStack.peek().positionMatrix

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

            buffer.vertex(matrix, x1.toFloat(), y1.toFloat(), z1.toFloat()).color(color.red, color.green, color.blue, color.alpha).normal(1f, 1f, 1f)
            buffer.vertex(matrix, x2.toFloat(), y1.toFloat(), z2.toFloat()).color(color.red, color.green, color.blue, color.alpha).normal(1f, 1f, 1f)
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

            buffer.vertex(matrix, x1.toFloat(), y1.toFloat(), z1.toFloat()).color(color.red, color.green, color.blue, color.alpha).normal(1f, 1f, 1f)
            buffer.vertex(matrix, x2.toFloat(), y2.toFloat(), z2.toFloat()).color(color.red, color.green, color.blue, color.alpha).normal(1f, 1f, 1f)
        }
    }

    matrixStack.pop()
    bufferSource.draw(CustomRenderLayer.LINE_LIST)
}

fun WorldRenderContext.drawCustomBeacon(
    title: String,
    position: BlockPos,
    color: Color,
    increase: Boolean = true
) {
    val dist = mc.player?.blockPos?.getManhattanDistance(position) ?: return

    drawWireFrameBox(Box(position), color, depth = false)
    drawBeaconBeam(position, color)

    drawText(
        Text.literal("$title §r§f(§3${dist}m§f)").asOrderedText(),
        position.toCenterPos().add(0.0, 1.7, 0.0),
        if (increase) max(1f, (dist / 20.0).toFloat()) else 2f,
        true
    )
}
