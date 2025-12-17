package com.odtheking.odin.utils.render

import com.mojang.blaze3d.vertex.PoseStack
import com.odtheking.mixin.accessors.BeaconBeamAccessor
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Color.Companion.multiplyAlpha
import com.odtheking.odin.utils.addVec
import com.odtheking.odin.utils.unaryMinus
import net.minecraft.client.gui.Font
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.ShapeRenderer
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

private const val DEPTH = 0
private const val NO_DEPTH = 1

private val BEAM_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/beacon_beam.png")

internal data class LineData(val from: Vec3, val to: Vec3, val color: Int, val thickness: Float)
internal data class BoxData(val aabb: AABB, val r: Float, val g: Float, val b: Float, val a: Float, val thickness: Float)
internal data class BeaconData(val pos: BlockPos, val color: Color, val isScoping: Boolean, val gameTime: Long)
internal data class TextData(val text: String, val pos: Vec3, val scale: Float, val depth: Boolean, val cameraRotation: org.joml.Quaternionf, val font: Font, val textWidth: Float)

class RenderConsumer {
    internal val lines = listOf(mutableListOf<LineData>(), mutableListOf())
    internal val filledBoxes = listOf(mutableListOf<BoxData>(), mutableListOf())
    internal val wireBoxes = listOf(mutableListOf<BoxData>(), mutableListOf())

    internal val beaconBeams = mutableListOf<BeaconData>()
    internal val texts = mutableListOf<TextData>()

    fun clear() {
        lines.forEach { it.clear() }
        filledBoxes.forEach { it.clear() }
        wireBoxes.forEach { it.clear() }
        beaconBeams.clear()
        texts.clear()
    }
}

object RenderBatchManager {
    val renderConsumer = RenderConsumer()

    init {
        on<RenderEvent.Last> {
            val matrix = context.matrices() ?: return@on
            val bufferSource = context.consumers() as? MultiBufferSource.BufferSource ?: return@on
            val camera = context.gameRenderer().mainCamera?.position ?: return@on

            matrix.pushPose()
            matrix.translate(-camera.x, -camera.y, -camera.z)

            matrix.renderBatchedLinesAndWireBoxes(renderConsumer.lines, renderConsumer.wireBoxes, bufferSource)
            matrix.renderBatchedFilledBoxes(renderConsumer.filledBoxes, bufferSource)

            matrix.popPose()

            matrix.renderBatchedBeaconBeams(renderConsumer.beaconBeams, camera)
            matrix.renderBatchedTexts(renderConsumer.texts, bufferSource, camera)
            renderConsumer.clear()
        }
    }
}

private fun PoseStack.renderBatchedLinesAndWireBoxes(
    lines: List<List<LineData>>,
    wireBoxes: List<List<BoxData>>,
    bufferSource: MultiBufferSource.BufferSource
) {
    val lineRenderLayers = listOf(CustomRenderLayer.LINE_LIST, CustomRenderLayer.LINE_LIST_ESP)
    for (depthState in 0..1) {
        if (lines[depthState].isEmpty() && wireBoxes[depthState].isEmpty()) continue
        val buffer = bufferSource.getBuffer(lineRenderLayers[depthState])

        for (line in lines[depthState]) {
            val dirX = line.to.x - line.from.x
            val dirY = line.to.y - line.from.y
            val dirZ = line.to.z - line.from.z

            ShapeRenderer.renderVector(
                this, buffer,
                Vector3f(line.from.x.toFloat(), line.from.y.toFloat(), line.from.z.toFloat()),
                Vec3(dirX, dirY, dirZ),
                line.color
            )
        }

        for (box in wireBoxes[depthState]) {
            ShapeRenderer.renderLineBox(
                last(), buffer, box.aabb,
                box.r, box.g, box.b, box.a
            )
        }

        bufferSource.endBatch(lineRenderLayers[depthState])
    }
}

private fun PoseStack.renderBatchedFilledBoxes(consumer: List<List<BoxData>>, bufferSource: MultiBufferSource.BufferSource) {
    val filledBoxRenderLayers = listOf(CustomRenderLayer.TRIANGLE_STRIP, CustomRenderLayer.TRIANGLE_STRIP_ESP)
    for ((depthState, boxes) in consumer.withIndex()) {
        if (boxes.isEmpty()) continue
        val buffer = bufferSource.getBuffer(filledBoxRenderLayers[depthState])

        for (box in boxes) {
            ShapeRenderer.addChainedFilledBoxVertices(
                this, buffer,
                box.aabb.minX, box.aabb.minY, box.aabb.minZ,
                box.aabb.maxX, box.aabb.maxY, box.aabb.maxZ,
                box.r, box.g, box.b, box.a
            )
        }

        bufferSource.endBatch(filledBoxRenderLayers[depthState])
    }
}

private fun PoseStack.renderBatchedBeaconBeams(consumer: List<BeaconData>, camera: Vec3) {
    for (beacon in consumer) {
        pushPose()
        translate(beacon.pos.x - camera.x, beacon.pos.y - camera.y, beacon.pos.z - camera.z)

        val centerX = beacon.pos.x + 0.5
        val centerZ = beacon.pos.z + 0.5
        val dx = camera.x - centerX
        val dz = camera.z - centerZ
        val length = sqrt(dx * dx + dz * dz).toFloat()

        val scale = if (beacon.isScoping) 1.0f else maxOf(1.0f, length * 0.010416667f)

        BeaconBeamAccessor.invokeRenderBeam(
            this,
            mc.gameRenderer.featureRenderDispatcher.submitNodeStorage,
            BEAM_TEXTURE,
            1f,
            beacon.gameTime.toFloat(),
            0,
            319,
            beacon.color.rgba,
            0.2f * scale,
            0.25f * scale
        )
        popPose()
    }
}

private fun PoseStack.renderBatchedTexts(consumer: List<TextData>, bufferSource: MultiBufferSource.BufferSource, camera: Vec3) {
    val cameraPos = -camera

    for (textData in consumer) {
        pushPose()
        val pose = last().pose()
        val scaleFactor = textData.scale * 0.025f

        pose.translate(textData.pos.toVector3f())
            .translate(cameraPos.x.toFloat(), cameraPos.y.toFloat(), cameraPos.z.toFloat())
            .rotate(textData.cameraRotation)
            .scale(scaleFactor, -scaleFactor, scaleFactor)

        textData.font.drawInBatch(
            textData.text, -textData.textWidth / 2f, 0f, -1, true, pose, bufferSource,
            if (textData.depth) Font.DisplayMode.NORMAL else Font.DisplayMode.SEE_THROUGH,
            0, LightTexture.FULL_BRIGHT
        )

        popPose()
    }
}

fun RenderEvent.Extract.drawLine(points: Collection<Vec3>, color: Color, depth: Boolean, thickness: Float = 3f) {
    if (points.size < 2) return

    val rgba = color.rgba
    val batch = consumer.lines[if (depth) DEPTH else NO_DEPTH]

    val iterator = points.iterator()
    var current = iterator.next()

    while (iterator.hasNext()) {
        val next = iterator.next()
        batch.add(LineData(current, next, rgba, thickness))
        current = next
    }
}

fun RenderEvent.Extract.drawWireFrameBox(aabb: AABB, color: Color, thickness: Float = 3f, depth: Boolean = false) {
    consumer.wireBoxes[if (depth) DEPTH else NO_DEPTH].add(
        BoxData(aabb, color.redFloat, color.greenFloat, color.blueFloat, color.alphaFloat, thickness)
    )
}

fun RenderEvent.Extract.drawFilledBox(aabb: AABB, color: Color, depth: Boolean = false) {
    consumer.filledBoxes[if (depth) DEPTH else NO_DEPTH].add(
        BoxData(aabb, color.redFloat, color.greenFloat, color.blueFloat, color.alphaFloat, 3f)
    )
}

fun RenderEvent.Extract.drawStyledBox(
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

fun RenderEvent.Extract.drawBeaconBeam(position: BlockPos, color: Color) {
    val isScoping = mc.player?.isScoping == true
    val gameTime = mc.level?.gameTime ?: 0L

    consumer.beaconBeams.add(BeaconData(position, color, isScoping, gameTime))
}

fun RenderEvent.Extract.drawText(text: String, pos: Vec3, scale: Float, depth: Boolean) {
    val cameraRotation = mc.gameRenderer.mainCamera.rotation()
    val font = mc.font ?: return
    val textWidth = font.width(text).toFloat()

    consumer.texts.add(TextData(text, pos, scale, depth, cameraRotation, font, textWidth))
}

fun RenderEvent.Extract.drawCustomBeacon(
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

fun RenderEvent.Extract.drawCylinder(
    center: Vec3,
    radius: Float,
    height: Float,
    color: Color,
    segments: Int = 32,
    thickness: Float = 5f,
    depth: Boolean = false
) {
    val batch = consumer.lines[if (depth) DEPTH else NO_DEPTH]
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

        batch.add(LineData(p1Top, p2Top, rgba, thickness))
        batch.add(LineData(p1Bottom, p2Bottom, rgba, thickness))
        batch.add(LineData(p1Bottom, p1Top, rgba, thickness))
    }
}

fun RenderEvent.Extract.drawBoxes(waypoints: Collection<DungeonWaypoints.DungeonWaypoint>, disableDepth: Boolean) {
    if (waypoints.isEmpty()) return

    for (waypoint in waypoints) {
        val color = waypoint.color
        if (waypoint.isClicked || color.isTransparent) continue

        val aabb = waypoint.aabb.move(waypoint.blockPos)
        val depth = waypoint.depth && !disableDepth

        if (waypoint.filled) drawFilledBox(aabb, color, depth = depth)
        else drawWireFrameBox(aabb, color, depth = depth)
    }
}