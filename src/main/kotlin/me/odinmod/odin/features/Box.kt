package me.odinmod.odin.features

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.VertexFormat
import me.odinmod.odin.OdinMod.mc
import me.odinmod.odin.events.RenderEvent
import meteordevelopment.orbit.EventHandler
import net.minecraft.client.gl.RenderPipelines
import net.minecraft.client.render.BufferBuilder
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.RenderPhase
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Box
import org.joml.Matrix4f
import java.util.OptionalDouble

object Box {
    val box = Box(0.0, 100.0, 0.0, 1.0, 101.0, 1.0)

    @EventHandler
    fun onRenderLast(event: RenderEvent.Last) {
        val matrixStack = event.context.matrixStack() ?: return
        val bufferSource = mc.bufferBuilders.entityVertexConsumers
        val buffer = bufferSource.getBuffer(CUSTOM_LINE_LAYER)

        val entry = matrixStack.peek()
        val matrix = entry.positionMatrix
        val camPos = event.context.camera().pos
        drawBoxLines(buffer, matrix, box.offset(-camPos.x, -camPos.y, -camPos.z), 1f, 1f , 1f, 1f)
        bufferSource.draw()
    }

    private fun drawBoxLines(
        buffer: VertexConsumer,
        entry: Matrix4f,
        box: Box,
        r: Float, g: Float, b: Float, a: Float
    ) {
        val x1 = box.minX.toFloat()
        val y1 = box.minY.toFloat()
        val z1 = box.minZ.toFloat()
        val x2 = box.maxX.toFloat()
        val y2 = box.maxY.toFloat()
        val z2 = box.maxZ.toFloat()

        fun vertex(x: Float, y: Float, z: Float) {
            buffer.vertex(entry, x, y, z).color(r, g, b, a)
        }

        // Bottom face
        vertex(x1, y1, z1); vertex(x2, y1, z1)
        vertex(x2, y1, z1); vertex(x2, y1, z2)
        vertex(x2, y1, z2); vertex(x1, y1, z2)
        vertex(x1, y1, z2); vertex(x1, y1, z1)

        // Top face
        vertex(x1, y2, z1); vertex(x2, y2, z1)
        vertex(x2, y2, z1); vertex(x2, y2, z2)
        vertex(x2, y2, z2); vertex(x1, y2, z2)
        vertex(x1, y2, z2); vertex(x1, y2, z1)

        // Vertical edges
        vertex(x1, y1, z1); vertex(x1, y2, z1)
        vertex(x2, y1, z1); vertex(x2, y2, z1)
        vertex(x2, y1, z2); vertex(x2, y2, z2)
        vertex(x1, y1, z2); vertex(x1, y2, z2)

    }

    val CUSTOM_LINE_LAYER: RenderLayer = RenderLayer.of(
        "lines",
        RenderLayer.DEFAULT_BUFFER_SIZE, false, false, RenderPipelines.LINES, RenderLayer.MultiPhaseParameters.builder()
        .lineWidth(RenderPhase.LineWidth(OptionalDouble.of(1.0)))
        .layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
        .build(false));
}