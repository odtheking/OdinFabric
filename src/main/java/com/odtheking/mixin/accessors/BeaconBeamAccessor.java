package com.odtheking.mixin.accessors;

import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BeaconBlockEntityRenderer.class)
public interface BeaconBeamAccessor {
    @Invoker("renderBeam")
    static void invokeRenderBeam(
            MatrixStack matrices, OrderedRenderCommandQueue queue, Identifier textureId, float beamHeight, float beamRotationDegrees, int minHeight, int maxHeight, int color, float innerScale, float outerScale
    ) {
        throw new UnsupportedOperationException();
    }
}
