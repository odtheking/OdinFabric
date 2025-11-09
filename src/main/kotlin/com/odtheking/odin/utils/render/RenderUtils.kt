package com.odtheking.odin.utils.render

import com.mojang.blaze3d.systems.RenderSystem
import com.odtheking.mixin.accessors.BeaconBeamAccessor
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Color.Companion.multiplyAlpha
import com.odtheking.odin.utils.addVec
import com.odtheking.odin.utils.translate
import com.odtheking.odin.utils.unaryMinus
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.render.LightmapTextureManager
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.VertexRendering
import net.minecraft.client.util.BufferAllocator
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.OrderedText
import net.minecraft.text.OrderedText.concat
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import org.joml.Vector3f
import kotlin.math.max
import kotlin.math.pow

private val ALLOCATOR = BufferAllocator(1536)

private val BEAM_TEXTURE = Identifier.ofVanilla("textures/entity/beacon_beam.png")

fun RenderEvent.drawLine(points: Collection<Vec3d>, color: Color, depth: Boolean, thickness: Float = 3f) {
    if (points.size < 2) return
    val matrix = context.matrices()
    val bufferSource = context.consumers() as? VertexConsumerProvider.Immediate ?: return
    val layer = if (depth) CustomRenderLayer.LINE_LIST else CustomRenderLayer.LINE_LIST_ESP
    RenderSystem.lineWidth(thickness)

    matrix.push()
    with(context.gameRenderer().camera.pos) { matrix.translate(-x, -y, -z) }

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

fun RenderEvent.drawWireFrameBox(box: Box, color: Color, thickness: Float = 5f, depth: Boolean = false) {
    val matrix = context.matrices()
    val bufferSource = context.consumers() as? VertexConsumerProvider.Immediate ?: return
    val layer = if (depth) CustomRenderLayer.LINE_LIST else CustomRenderLayer.LINE_LIST_ESP
    val cameraPosition = context.gameRenderer().camera.pos
    RenderSystem.lineWidth((thickness / cameraPosition.squaredDistanceTo(box.center).pow(0.15)).toFloat())

    matrix.push()
    with(cameraPosition) { matrix.translate(-x, -y, -z) }

    val entry: MatrixStack.Entry = matrix.peek()

    VertexRendering.drawBox(
        entry,
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

fun RenderEvent.drawFilledBox(box: Box, color: Color, depth: Boolean = false) {
    val matrix = context.matrices()
    val bufferSource = context.consumers() as? VertexConsumerProvider.Immediate ?: return
    val layer = if (depth) CustomRenderLayer.TRIANGLE_STRIP else CustomRenderLayer.TRIANGLE_STRIP_ESP

    matrix.push()
    with(context.gameRenderer().camera.pos) { matrix.translate(-x, -y, -z) }
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

fun RenderEvent.drawStyledBox(
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

fun RenderEvent.drawBeaconBeam(position: BlockPos, color: Color) {
    if (mc.world == null) return

    val matrix = context.matrices()
    val camera = context.gameRenderer().camera.pos

    matrix.push()
    matrix.translate(position.x - camera.x, position.y - camera.y, position.z - camera.z)
    val length = camera.subtract(position.toCenterPos()).horizontalLength().toFloat()
    val scale = if (mc.player != null && mc.player?.isUsingSpyglass == true) 1.0f else maxOf(1.0f, length / 96.0f)

    BeaconBeamAccessor.invokeRenderBeam(
        matrix,
        mc.gameRenderer.entityRenderDispatcher.queue,
        BEAM_TEXTURE,
        1f,
        mc.world!!.time.toFloat(),
        0,
        319,
        color.rgba,
        0.2f * scale,
        0.25f * scale
    )
    matrix.pop()
}

fun RenderEvent.drawText(text: OrderedText?, pos: Vec3d, scale: Float, depth: Boolean) {
    val stack = context.matrices()

    stack.push()
    val matrix = stack.peek().positionMatrix
    val camera = context.gameRenderer().camera
    with(scale * 0.025f) {
        matrix.translate(pos).translate(-camera.pos).rotate(camera.rotation).scale(this, -this, this)
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

fun RenderEvent.drawCustomBeacon(
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

fun RenderEvent.drawCylinder(
    center: Vec3d,
    radius: Float,
    height: Float,
    color: Color,
    segments: Int = 32,
    thickness: Float = 5f,
    depth: Boolean = false
) {
    val matrix = context.matrices()
    val bufferSource = context.consumers() as? VertexConsumerProvider.Immediate ?: return
    val layer = if (depth) CustomRenderLayer.LINE_LIST else CustomRenderLayer.LINE_LIST_ESP
    val camera = context.gameRenderer().camera.pos

    matrix.push()
    matrix.translate(center.x - camera.x, center.y - camera.y, center.z - camera.z)
    RenderSystem.lineWidth((thickness / camera.squaredDistanceTo(center).pow(0.15)).toFloat())

    val angleStep = 2.0 * Math.PI / segments
    val buffer = bufferSource.getBuffer(layer)

    for (i in 0 until segments) {
        val angle1 = i * angleStep
        val angle2 = (i + 1) * angleStep

        val x1 = (radius * kotlin.math.cos(angle1)).toFloat()
        val z1 = (radius * kotlin.math.sin(angle1)).toFloat()
        val x2 = (radius * kotlin.math.cos(angle2)).toFloat()
        val z2 = (radius * kotlin.math.sin(angle2)).toFloat()

        VertexRendering.drawVector(matrix, buffer, Vector3f(x1, height, z1), Vec3d((x2 - x1).toDouble(), 0.0, (z2 - z1).toDouble()), color.rgba)
        VertexRendering.drawVector(matrix, buffer, Vector3f(x1, 0f, z1), Vec3d((x2 - x1).toDouble(), 0.0, (z2 - z1).toDouble()), color.rgba)
        VertexRendering.drawVector(matrix, buffer, Vector3f(x1, 0f, z1), Vec3d(0.0, height.toDouble(), 0.0), color.rgba)
    }


    matrix.pop()
    bufferSource.draw()
}

//fun RenderEvent.drawConnectedBlockOutlines(
//    blocks: Collection<BlockPos>,
//    color: Color,
//    thickness: Float = 5f,
//    depth: Boolean = false
//) {
//    if (blocks.isEmpty()) return
//
//    val matrix = matrixStack
//    val bufferSource = buffer as? VertexConsumerProvider.Immediate ?: return
//    val layer = if (depth) CustomRenderLayer.LINE_LIST else CustomRenderLayer.LINE_LIST_ESP
//
//    val blockSet = blocks as? Set ?: blocks.toSet()
//
//    val blockLongs = blockSet.mapTo(HashSet(blockSet.size)) { it.asLong() }
//
//    val avgDist = blockSet.fold(0.0) { acc, pos ->
//        acc + cameraPosition.squaredDistanceTo(Vec3d.ofCenter(pos))
//    } / blockSet.size
//    RenderSystem.lineWidth((thickness / avgDist.pow(0.15)).toFloat())
//
//    matrix.push()
//    with(cameraPosition) { matrix.translate(-x, -y, -z) }
//
//    val buffer = bufferSource.getBuffer(layer)
//
//    val drawnEdges = HashSet<Long>(blockSet.size * 6)
//
//    val rgba = color.rgba
//
//    for (pos in blockSet) {
//        val px = pos.x
//        val py = pos.y
//        val pz = pos.z
//
//        val hasNorth = blockLongs.contains(BlockPos.asLong(px, py, pz - 1))
//        val hasSouth = blockLongs.contains(BlockPos.asLong(px, py, pz + 1))
//        val hasEast = blockLongs.contains(BlockPos.asLong(px + 1, py, pz))
//        val hasWest = blockLongs.contains(BlockPos.asLong(px - 1, py, pz))
//        val hasUp = blockLongs.contains(BlockPos.asLong(px, py + 1, pz))
//        val hasDown = blockLongs.contains(BlockPos.asLong(px, py - 1, pz))
//
//        val x = px.toDouble()
//        val y = py.toDouble()
//        val z = pz.toDouble()
//
//        if (!hasDown) {
//            if (!hasNorth && drawnEdges.add(edgeHash(px, py, pz, 0)))
//                drawEdgeFast(matrix, buffer, rgba, x, y, z, x + 1, y, z)
//            if (!hasSouth && drawnEdges.add(edgeHash(px, py, pz, 1)))
//                drawEdgeFast(matrix, buffer, rgba, x, y, z + 1, x + 1, y, z + 1)
//            if (!hasWest && drawnEdges.add(edgeHash(px, py, pz, 2)))
//                drawEdgeFast(matrix, buffer, rgba, x, y, z, x, y, z + 1)
//            if (!hasEast && drawnEdges.add(edgeHash(px, py, pz, 3)))
//                drawEdgeFast(matrix, buffer, rgba, x + 1, y, z, x + 1, y, z + 1)
//        }
//
//        if (!hasUp) {
//            if (!hasNorth && drawnEdges.add(edgeHash(px, py, pz, 4)))
//                drawEdgeFast(matrix, buffer, rgba, x, y + 1, z, x + 1, y + 1, z)
//            if (!hasSouth && drawnEdges.add(edgeHash(px, py, pz, 5)))
//                drawEdgeFast(matrix, buffer, rgba, x, y + 1, z + 1, x + 1, y + 1, z + 1)
//            if (!hasWest && drawnEdges.add(edgeHash(px, py, pz, 6)))
//                drawEdgeFast(matrix, buffer, rgba, x, y + 1, z, x, y + 1, z + 1)
//            if (!hasEast && drawnEdges.add(edgeHash(px, py, pz, 7)))
//                drawEdgeFast(matrix, buffer, rgba, x + 1, y + 1, z, x + 1, y + 1, z + 1)
//        }
//
//        if (!hasNorth && !hasWest && drawnEdges.add(edgeHash(px, py, pz, 8)))
//            drawEdgeFast(matrix, buffer, rgba, x, y, z, x, y + 1, z)
//        if (!hasNorth && !hasEast && drawnEdges.add(edgeHash(px, py, pz, 9)))
//            drawEdgeFast(matrix, buffer, rgba, x + 1, y, z, x + 1, y + 1, z)
//        if (!hasSouth && !hasWest && drawnEdges.add(edgeHash(px, py, pz, 10)))
//            drawEdgeFast(matrix, buffer, rgba, x, y, z + 1, x, y + 1, z + 1)
//        if (!hasSouth && !hasEast && drawnEdges.add(edgeHash(px, py, pz, 11)))
//            drawEdgeFast(matrix, buffer, rgba, x + 1, y, z + 1, x + 1, y + 1, z + 1)
//    }
//
//    matrix.pop()
//    bufferSource.draw(layer)
//}
//
//@Suppress("NOTHING_TO_INLINE")
//private inline fun edgeHash(x: Int, y: Int, z: Int, edge: Int): Long {
//    return (x.toLong() shl 38) or (y.toLong() shl 26) or (z.toLong() shl 14) or edge.toLong()
//}
//
//@Suppress("NOTHING_TO_INLINE")
//private inline fun drawEdgeFast(
//    matrix: MatrixStack,
//    buffer: net.minecraft.client.render.VertexConsumer,
//    rgba: Int,
//    x1: Double,
//    y1: Double,
//    z1: Double,
//    x2: Double,
//    y2: Double,
//    z2: Double
//) {
//    VertexRendering.drawVector(
//        matrix, buffer,
//        Vector3f(x1.toFloat(), y1.toFloat(), z1.toFloat()),
//        Vec3d(x2 - x1, y2 - y1, z2 - z1), rgba
//    )
//}