package com.odtheking.mixin.mixins;

import com.odtheking.odin.features.impl.render.HidePlayers;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {

    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void onRender(T entity, Frustum frustum, double d, double e, double f, CallbackInfoReturnable<Boolean> cir) {
        if (!HidePlayers.shouldRenderPlayer(entity)) {
            cir.setReturnValue(false); // only return false if shouldRenderPlayer returns false - future reference: if any more things that hide entitiess are added in future, modify the condition to be true if any of them returns false
        }
        // return value not overridden for other cases (see https://github.com/odtheking/OdinFabric/issues/43 - overriding the value to true can create performance regressions)
    }

}

