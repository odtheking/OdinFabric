package com.odtheking.odin.utils.render

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.ByteBufferBuilder
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Color.Companion.multiplyAlpha
import com.odtheking.odin.utils.addVec
import com.odtheking.odin.utils.unaryMinus
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.minecraft.client.gui.Font
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.ShapeRenderer
import net.minecraft.client.renderer.blockentity.BeaconRenderer
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.util.FormattedCharSequence
import net.minecraft.util.FormattedCharSequence.composite
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import kotlin.math.max
import kotlin.math.pow

private val ALLOCATOR = ByteBufferBuilder(1536)

fun WorldRenderContext.drawLine(points: Collection<Vec3>, color: Color, depth: Boolean, thickness: Float = 3f) {
    if (points.size < 2) return
    val matrix = matrixStack() ?: return
    val bufferSource = consumers() as? MultiBufferSource.BufferSource ?: return
    val layer = if (depth) CustomRenderLayer.LINE_LIST else CustomRenderLayer.LINE_LIST_ESP
    RenderSystem.lineWidth(thickness)

    matrix.pushPose()
    with(camera().position) { matrix.translate(-x, -y, -z) }

    val pointList = points.toList()
    for (i in 0 until pointList.size - 1) {
        val start = pointList[i]
        val end = pointList[i + 1]
        val startOffset = Vector3f(start.x.toFloat(), start.y.toFloat(), start.z.toFloat())
        val direction = end.subtract(start)
        ShapeRenderer.renderVector(
            matrix,
            bufferSource.getBuffer(layer),
            startOffset,
            direction,
            color.rgba
        )
    }

    matrix.popPose()
    bufferSource.endBatch(layer)
}

fun WorldRenderContext.drawWireFrameBox(aabb: AABB, color: Color, thickness: Float = 6f, depth: Boolean = false) {
    val matrix = matrixStack() ?: return
    val bufferSource = consumers() as? MultiBufferSource.BufferSource ?: return
    val layer = if (depth) CustomRenderLayer.LINE_LIST else CustomRenderLayer.LINE_LIST_ESP
    val camera = camera() ?: return
    RenderSystem.lineWidth((thickness / camera.position.distanceToSqr(aabb.center).pow(0.15)).toFloat())

    matrix.pushPose()
    with(camera.position) { matrix.translate(-x, -y, -z) }
    ShapeRenderer.renderLineBox(
        matrix,
        bufferSource.getBuffer(layer),
        aabb,
        color.redFloat,
        color.greenFloat,
        color.blueFloat,
        color.alphaFloat
    )

    matrix.popPose()
    bufferSource.endBatch(layer)
}

fun WorldRenderContext.drawFilledBox(box: AABB, color: Color, depth: Boolean = false) {
    val matrix = matrixStack() ?: return
    val bufferSource = consumers() as? MultiBufferSource.BufferSource ?: return
    val layer = if (depth) CustomRenderLayer.TRIANGLE_STRIP else CustomRenderLayer.TRIANGLE_STRIP_ESP

    matrix.pushPose()
    with(camera().position) { matrix.translate(-x, -y, -z) }
    ShapeRenderer.addChainedFilledBoxVertices(
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

    matrix.popPose()
    bufferSource.endBatch(layer)
}

fun WorldRenderContext.drawStyledBox(
    aabb: AABB,
    color: Color,
    style: Int = 0,
    depth: Boolean = true
) {
    when (style) {
        0 -> drawFilledBox(aabb, color, depth = depth)
        1 -> drawWireFrameBox(aabb, color, depth = depth)
        2 -> {
            drawFilledBox(aabb, color.multiplyAlpha(0.5f), depth = depth)
            drawWireFrameBox(aabb, color, depth = depth)
        }
    }
}

fun WorldRenderContext.drawBeaconBeam(position: BlockPos, color: Color) {
    val matrix = matrixStack() ?: return
    val bufferSource = consumers() as? MultiBufferSource.BufferSource ?: return
    val camera = camera()?.position ?: return

    matrix.pushPose()
    matrix.translate(position.x - camera.x, position.y - camera.y, position.z - camera.z)
    val length = camera.subtract(position.center).horizontalDistance().toFloat()
    val scale = if (mc.player != null && mc.player?.isScoping == true) 1.0f else maxOf(1.0f, length / 96.0f)

    BeaconRenderer.renderBeaconBeam(
        matrix, bufferSource, BeaconRenderer.BEAM_LOCATION,
        tickCounter().getGameTimeDeltaPartialTick(true), scale, world().gameTime, 0, 319, color.rgba, 0.2f * scale, 0.25f * scale
    )
    matrix.popPose()
}

fun WorldRenderContext.drawText(text: FormattedCharSequence, pos: Vec3, scale: Float, depth: Boolean) {
    val stack = matrixStack() ?: return

    stack.pushPose()
    val matrix = stack.last().pose()
    with(scale * 0.025f) {
        val cameraPos = -camera().position
        matrix.translate(pos.toVector3f()).translate(cameraPos.x.toFloat() , cameraPos.y.toFloat(), cameraPos.z.toFloat()).rotate(camera().rotation()).scale(this, -this, this)
    }

    val consumers = MultiBufferSource.immediate(ALLOCATOR)

    mc.font?.let {
        it.drawInBatch(
            text, -it.width(text) / 2f, 0f, -1, true, matrix, consumers,
            if (depth) Font.DisplayMode.NORMAL else Font.DisplayMode.SEE_THROUGH,
            0, LightTexture.FULL_BRIGHT
        )
    }

    consumers.endBatch()
    stack.popPose()
}

fun WorldRenderContext.drawCustomBeacon(
    title: FormattedCharSequence,
    position: BlockPos,
    color: Color,
    increase: Boolean = true,
    distance: Boolean = true
) {
    val dist = mc.player?.blockPosition()?.distManhattan(position) ?: return

    drawWireFrameBox(AABB(position), color, depth = false)
    drawBeaconBeam(position, color)

    drawText(
        (if (distance) composite(title, Component.literal(" §r§f(§3${dist}m§f)").visualOrderText) else title),
        position.center.addVec(y = 1.7),
        if (increase) max(1f, (dist / 20.0).toFloat()) else 2f,
        false
    )
}

fun WorldRenderContext.drawCylinder(
    center: Vec3,
    radius: Float,
    height: Float,
    color: Color,
    segments: Int = 32,
    thickness: Float = 5f,
    depth: Boolean = false
) {
    val matrix = matrixStack() ?: return
    val bufferSource = consumers() as? MultiBufferSource.BufferSource ?: return
    val layer = if (depth) CustomRenderLayer.LINE_LIST else CustomRenderLayer.LINE_LIST_ESP
    val camera = camera()?.position ?: return

    matrix.pushPose()
    matrix.translate(center.x - camera.x, center.y - camera.y, center.z - camera.z)
    RenderSystem.lineWidth((thickness / camera.distanceToSqr(center).pow(0.15)).toFloat())

    val angleStep = 2.0 * Math.PI / segments
    val buffer = bufferSource.getBuffer(layer)

    for (i in 0 until segments) {
        val angle1 = i * angleStep
        val angle2 = (i + 1) * angleStep

        val x1 = (radius * kotlin.math.cos(angle1)).toFloat()
        val z1 = (radius * kotlin.math.sin(angle1)).toFloat()
        val x2 = (radius * kotlin.math.cos(angle2)).toFloat()
        val z2 = (radius * kotlin.math.sin(angle2)).toFloat()

        ShapeRenderer.renderVector(matrix, buffer, Vector3f(x1, height, z1), Vec3((x2 - x1).toDouble(), 0.0, (z2 - z1).toDouble()), color.rgba)
        ShapeRenderer.renderVector(matrix, buffer, Vector3f(x1, 0f, z1), Vec3((x2 - x1).toDouble(), 0.0, (z2 - z1).toDouble()), color.rgba)
        ShapeRenderer.renderVector(matrix, buffer, Vector3f(x1, 0f, z1), Vec3(0.0, height.toDouble(), 0.0), color.rgba)
    }


    matrix.popPose()
    bufferSource.endBatch()
}

fun WorldRenderContext.drawBoxes(
    waypoints: Collection<DungeonWaypoints.DungeonWaypoint>,
    disableDepth: Boolean,
) {
    if (waypoints.isEmpty()) return

    val matrix = matrixStack() ?: return
    val bufferSource = consumers() as? MultiBufferSource.BufferSource ?: return
    val camera = camera()?.position ?: return

    matrix.pushPose()
    matrix.translate(-camera.x, -camera.y, -camera.z)

    for (waypoint in waypoints) {
        val color = waypoint.color
        if (waypoint.isClicked || color.isTransparent) continue

        val aabb = waypoint.aabb.move(waypoint.blockPos)
        val depth = waypoint.depth && !disableDepth

        if (waypoint.filled) {
            val layer = if (depth) CustomRenderLayer.TRIANGLE_STRIP else CustomRenderLayer.TRIANGLE_STRIP_ESP
            ShapeRenderer.addChainedFilledBoxVertices(
                matrix,
                bufferSource.getBuffer(layer),
                aabb.minX, aabb.minY, aabb.minZ,
                aabb.maxX, aabb.maxY, aabb.maxZ,
                color.redFloat, color.greenFloat, color.blueFloat, color.alphaFloat
            )
        } else {
            val layer = if (depth) CustomRenderLayer.LINE_LIST else CustomRenderLayer.LINE_LIST_ESP
            RenderSystem.lineWidth((3f / camera.distanceToSqr(aabb.center).pow(0.15)).toFloat())
            ShapeRenderer.renderLineBox(
                matrix,
                bufferSource.getBuffer(layer),
                aabb,
                color.redFloat, color.greenFloat, color.blueFloat, color.alphaFloat
            )
        }
    }

    matrix.popPose()
    bufferSource.endBatch()
}