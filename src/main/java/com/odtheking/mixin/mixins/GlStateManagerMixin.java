package com.odtheking.mixin.mixins;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.odtheking.odin.utils.ui.rendering.TextureTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlStateManager.class)
public class GlStateManagerMixin {

    @Inject(method = "_bindTexture", at = @At("HEAD"))
    private static void onBindTexture(int texture, CallbackInfo ci) {
        TextureTracker.setPreviousBoundTexture(texture);
    }

    @Inject(method = "_activeTexture", at = @At("HEAD"))
    private static void onActiveTexture(int texture, CallbackInfo ci) {
        TextureTracker.setPreviousActiveTexture(texture);
    }
}