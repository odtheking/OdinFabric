package com.odtheking.mixin.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.odtheking.odin.events.RenderEvent;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.state.WorldRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Unique
    private final ThreadLocal<RenderTickCounter> renderTickCounter = new ThreadLocal<>();


    @Inject(method = "renderMain", at = @At("HEAD"))
    public void saveRenderTickCounter(CallbackInfo ci, @Local(argsOnly = true) RenderTickCounter renderTickCounter) {
        this.renderTickCounter.set(renderTickCounter);
    }

    @Inject(method = "method_62214", at = @At(value = "CONSTANT", args = "stringValue=string"))
    public void afterTranslucent(
            CallbackInfo ci,
            @Local MatrixStack matrixStack,
            @Local(ordinal = 0) VertexConsumerProvider.Immediate provider,
            @Local(argsOnly = true) WorldRenderState worldRenderState
    ) {
        var renderTickCounter = this.renderTickCounter.get();
        if (renderTickCounter == null) {
            return;
        }
        new RenderEvent.Last(
                matrixStack,
                provider,
                worldRenderState.cameraRenderState.pos,
                worldRenderState.cameraRenderState.orientation,
                renderTickCounter.getTickProgress(false)
        ).postAndCatch();
    }
}
