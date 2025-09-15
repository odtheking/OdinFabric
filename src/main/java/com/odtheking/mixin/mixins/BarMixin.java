package com.odtheking.mixin.mixins;

import com.odtheking.odin.features.impl.skyblock.OverlayType;
import com.odtheking.odin.features.impl.skyblock.PlayerDisplay;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.bar.Bar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Bar.class)
public class BarMixin {
    @Inject(method = "drawExperienceLevel", at = @At("HEAD"), cancellable = true)
    private static void cancelXPLevel(DrawContext context, TextRenderer textRenderer, int level, CallbackInfo ci) {
        if (PlayerDisplay.shouldCancelOverlay(OverlayType.XP)) ci.cancel();
    }
}
