package com.odtheking.odin.events

import com.odtheking.odin.events.core.Event
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Vec3d
import org.joml.Quaternionf

abstract class RenderEvent(val matrixStack: MatrixStack, val buffer: VertexConsumerProvider, val cameraPosition: Vec3d, val cameraRotation: Quaternionf, val partialTicks: Float) : Event() {

    class Last(
        matrixStack: MatrixStack,
        provider: VertexConsumerProvider,
        cameraPosition: Vec3d,
        cameraRotation: Quaternionf,
        partialTicks: Float
    ) : RenderEvent(matrixStack, provider, cameraPosition, cameraRotation, partialTicks)
}