package com.odtheking.odin.utils.render

import com.mojang.blaze3d.vertex.PoseStack
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.core.EventPriority
import com.odtheking.odin.events.core.on
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
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

private class RenderBatch {
    val lines = listOf(mutableListOf<LineData>(), mutableListOf<LineData>())
    val filledBoxes = listOf(mutableListOf<BoxData>(), mutableListOf<BoxData>())
    val wireBoxes = listOf(mutableListOf<BoxData>(), mutableListOf<BoxData>())

    val beaconBeams = mutableListOf<BeaconData>()
    val texts = mutableListOf<TextData>()

    fun clear() {
        lines.forEach { it.clear() }
        filledBoxes.forEach { it.clear() }
        wireBoxes.forEach { it.clear() }
        beaconBeams.clear()
        texts.clear()
    }

    data class LineData(val from: Vec3, val to: Vec3, val color: Int, val thickness: Float)
    data class BoxData(val aabb: AABB, val color: Color, val thickness: Float)
    data class BeaconData(val pos: BlockPos, val color: Color)
    data class TextData(val text: String, val pos: Vec3, val scale: Float, val depth: Boolean)
}

private const val DEPTH = 0
private const val NO_DEPTH = 1

private val currentBatch = RenderBatch()
private var isRendering = false

object RenderBatchManager {
    init {
        on<RenderEvent.Last>(EventPriority.LOWEST) {
            if (!isRendering && (currentBatch.lines.any { it.isNotEmpty() } ||
                        currentBatch.filledBoxes.any { it.isNotEmpty() } ||
                        currentBatch.wireBoxes.any { it.isNotEmpty() } ||
                        currentBatch.beaconBeams.isNotEmpty() ||
                        currentBatch.texts.isNotEmpty())) {

                isRendering = true
                try {
                    flushBatch(context)
                } finally {
                    isRendering = false
                }
            }
        }
    }
}

private fun flushBatch(ctx: WorldRenderContext) {
    val matrix = ctx.matrixStack() ?: return
    val bufferSource = ctx.consumers() as? MultiBufferSource.BufferSource ?: return
    val camera = ctx.camera()?.position ?: return
    val frustum = ctx.frustum()

    matrix.pushPose()
    matrix.translate(-camera.x, -camera.y, -camera.z)

    val lineRenderLayers = listOf(CustomRenderLayer.LINE_LIST, CustomRenderLayer.LINE_LIST_ESP)
    for ((depthState, lines) in currentBatch.lines.withIndex()) {
        if (lines.isEmpty()) continue

        val buffer = bufferSource.getBuffer(lineRenderLayers[depthState])

        for (line in lines) {
            val dirX = (line.to.x - line.from.x).toFloat()
            val dirY = (line.to.y - line.from.y).toFloat()
            val dirZ = (line.to.z - line.from.z).toFloat()

            ShapeRenderer.renderVector(
                matrix, buffer,
                Vector3f(line.from.x.toFloat(), line.from.y.toFloat(), line.from.z.toFloat()),
                Vec3(dirX.toDouble(), dirY.toDouble(), dirZ.toDouble()),
                line.color
            )
        }
    }

    for ((depthState, boxes) in currentBatch.wireBoxes.withIndex()) {
        if (boxes.isEmpty()) continue
        val buffer = bufferSource.getBuffer(lineRenderLayers[depthState])

        for (box in boxes) {
           if (frustum?.isVisible(box.aabb) == false) continue

           ShapeRenderer.renderLineBox(
               matrix, buffer, box.aabb,
               box.color.redFloat, box.color.greenFloat, box.color.blueFloat, box.color.alphaFloat
           )
        }
    }

    val filledBoxRenderLayers = listOf(CustomRenderLayer.TRIANGLE_STRIP, CustomRenderLayer.TRIANGLE_STRIP_ESP)
    for ((depthState, boxes) in currentBatch.filledBoxes.withIndex()) {
        if (boxes.isEmpty()) continue

        val buffer = bufferSource.getBuffer(filledBoxRenderLayers[depthState])
        for (box in boxes) {
            if (frustum?.isVisible(box.aabb) == false) continue

            ShapeRenderer.addChainedFilledBoxVertices(
                matrix, buffer,
                box.aabb.minX, box.aabb.minY, box.aabb.minZ,
                box.aabb.maxX, box.aabb.maxY, box.aabb.maxZ,
                box.color.redFloat, box.color.greenFloat, box.color.blueFloat, box.color.alphaFloat
            )
        }
    }

    matrix.popPose()

    bufferSource.endBatch(CustomRenderLayer.LINE_LIST)
    bufferSource.endBatch(CustomRenderLayer.LINE_LIST_ESP)
    bufferSource.endBatch(CustomRenderLayer.TRIANGLE_STRIP)
    bufferSource.endBatch(CustomRenderLayer.TRIANGLE_STRIP_ESP)

    renderBeaconBeams(matrix, bufferSource, camera)
    renderTexts(matrix, bufferSource, camera)

    currentBatch.clear()
}

private fun renderBeaconBeams(matrix: PoseStack, bufferSource: MultiBufferSource.BufferSource, camera: Vec3) {
    val gameTime = mc.level?.gameTime ?: 0L
    val gameTimeFloat = gameTime.toFloat()
    val isScoping = mc.player?.isScoping == true

    for (beacon in currentBatch.beaconBeams) {
        matrix.pushPose()
        matrix.translate(beacon.pos.x - camera.x, beacon.pos.y - camera.y, beacon.pos.z - camera.z)

        val centerX = beacon.pos.x + 0.5
        val centerZ = beacon.pos.z + 0.5
        val dx = camera.x - centerX
        val dz = camera.z - centerZ
        val length = sqrt(dx * dx + dz * dz).toFloat()

        val scale = if (isScoping) 1.0f else maxOf(1.0f, length * 0.010416667f)

        BeaconRenderer.renderBeaconBeam(
            matrix, bufferSource, BeaconRenderer.BEAM_LOCATION,
            gameTimeFloat, scale, gameTime, 0, 319,
            beacon.color.rgba, 0.2f * scale, 0.25f * scale
        )
        matrix.popPose()
    }
}

private fun renderTexts(matrix: PoseStack, bufferSource: MultiBufferSource.BufferSource, camera: Vec3) {
    if (currentBatch.texts.isEmpty()) return

    val cameraRotation = mc.gameRenderer.mainCamera.rotation()
    val cameraPos = -camera
    val font = mc.font ?: return

    for (textData in currentBatch.texts) {
        matrix.pushPose()
        val pose = matrix.last().pose()
        val scaleFactor = textData.scale * 0.025f

        pose.translate(textData.pos.toVector3f())
            .translate(cameraPos.x.toFloat(), cameraPos.y.toFloat(), cameraPos.z.toFloat())
            .rotate(cameraRotation)
            .scale(scaleFactor, -scaleFactor, scaleFactor)

        font.drawInBatch(
            textData.text, -font.width(textData.text) / 2f, 0f, -1, true, pose, bufferSource,
            if (textData.depth) Font.DisplayMode.NORMAL else Font.DisplayMode.SEE_THROUGH,
            0, LightTexture.FULL_BRIGHT
        )

        matrix.popPose()
    }
}

fun WorldRenderContext.drawLine(points: Collection<Vec3>, color: Color, depth: Boolean, thickness: Float = 3f) {
    if (points.size < 2) return

    val rgba = color.rgba
    val batch = currentBatch.lines[if (depth) DEPTH else NO_DEPTH]

    val iterator = points.iterator()
    var current = iterator.next()

    while (iterator.hasNext()) {
        val next = iterator.next()
        batch.add(RenderBatch.LineData(current, next, rgba, thickness))
        current = next
    }
}

fun WorldRenderContext.drawWireFrameBox(aabb: AABB, color: Color, thickness: Float = 3f, depth: Boolean = false) {
    currentBatch.wireBoxes[if (depth) DEPTH else NO_DEPTH].add(RenderBatch.BoxData(aabb, color, thickness))
}

fun WorldRenderContext.drawFilledBox(box: AABB, color: Color, depth: Boolean = false) {
    currentBatch.filledBoxes[if (depth) DEPTH else NO_DEPTH].add(RenderBatch.BoxData(box, color, 3f))
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
    currentBatch.beaconBeams.add(RenderBatch.BeaconData(position, color))
}

fun WorldRenderContext.drawText(text: String, pos: Vec3, scale: Float, depth: Boolean) {
    currentBatch.texts.add(RenderBatch.TextData(text, pos, scale, depth))
}

fun WorldRenderContext.drawCustomBeacon(
    title: String,
    position: BlockPos,
    color: Color,
    increase: Boolean = true,
    distance: Boolean = true
) {
    val dist = mc.player?.blockPosition()?.distManhattan(position) ?: return

    drawWireFrameBox(AABB(position), color, depth = false)
    drawBeaconBeam(position, color)
    drawText(
        (if (distance) ("$title §r§f(§3${dist}m§f)") else title),
        position.center.addVec(y = 1.7),
        if (increase) max(1f, dist * 0.05f) else 2f,
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
    val batch = currentBatch.lines[if (depth) DEPTH else NO_DEPTH]
    val angleStep = 2.0 * Math.PI / segments
    val rgba = color.rgba

    for (i in 0 until segments) {
        val angle1 = i * angleStep
        val angle2 = (i + 1) * angleStep

        val x1 = (radius * cos(angle1)).toFloat()
        val z1 = (radius * sin(angle1)).toFloat()
        val x2 = (radius * cos(angle2)).toFloat()
        val z2 = (radius * sin(angle2)).toFloat()

        val p1Top = center.add(x1.toDouble(), height.toDouble(), z1.toDouble())
        val p2Top = center.add(x2.toDouble(), height.toDouble(), z2.toDouble())
        val p1Bottom = center.add(x1.toDouble(), 0.0, z1.toDouble())
        val p2Bottom = center.add(x2.toDouble(), 0.0, z2.toDouble())

        batch.add(RenderBatch.LineData(p1Top, p2Top, rgba, thickness))
        batch.add(RenderBatch.LineData(p1Bottom, p2Bottom, rgba, thickness))
        batch.add(RenderBatch.LineData(p1Bottom, p1Top, rgba, thickness))
    }
}

fun WorldRenderContext.drawBoxes(waypoints: Collection<DungeonWaypoints.DungeonWaypoint>, disableDepth: Boolean) {
    if (waypoints.isEmpty()) return

    for (waypoint in waypoints) {
        val color = waypoint.color
        if (waypoint.isClicked || color.isTransparent) continue

        val aabb = waypoint.aabb.move(waypoint.blockPos)
        val depth = waypoint.depth && !disableDepth
        val depthState = if (depth) DEPTH else NO_DEPTH

        if (waypoint.filled) currentBatch.filledBoxes[depthState].add(RenderBatch.BoxData(aabb, color, 3f))
        else currentBatch.wireBoxes[depthState].add(RenderBatch.BoxData(aabb, color, 3f))
    }
}