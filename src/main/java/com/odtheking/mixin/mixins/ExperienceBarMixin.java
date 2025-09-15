package com.odtheking.mixin.mixins;

import com.odtheking.odin.features.impl.skyblock.OverlayType;
import com.odtheking.odin.features.impl.skyblock.PlayerDisplay;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.bar.ExperienceBar;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExperienceBar.class)
public class ExperienceBarMixin {
    @Inject(method = "renderBar", at = @At("HEAD"), cancellable = true)
    private void cancelXPBar(DrawContext par1, RenderTickCounter par2, CallbackInfo ci) {
        if (PlayerDisplay.shouldCancelOverlay(OverlayType.XP)) ci.cancel();
    }
}
