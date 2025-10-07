package com.odtheking.mixin.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.odtheking.odin.features.impl.skyblock.OverlayType;
import com.odtheking.odin.features.impl.skyblock.PlayerDisplay;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Inject(method = "renderArmor", at = @At("HEAD"), cancellable = true)
    private static void cancelArmorBar(DrawContext context, PlayerEntity player, int i, int j, int k, int x, CallbackInfo ci) {
        if (PlayerDisplay.shouldCancelOverlay(OverlayType.ARMOR)) ci.cancel();
    }

    @Inject(method = "renderHealthBar", at = @At("HEAD"), cancellable = true)
    private void cancelHealthBar(DrawContext context, PlayerEntity player, int x, int y, int lines, int regeneratingHeartIndex, float maxHealth, int lastHealth, int health, int absorption, boolean blinking, CallbackInfo ci) {
        if (PlayerDisplay.shouldCancelOverlay(OverlayType.HEARTS)) ci.cancel();
    }

    @Inject(method = "renderFood", at = @At("HEAD"), cancellable = true)
    private void cancelFoodBar(DrawContext context, PlayerEntity player, int top, int right, CallbackInfo ci) {
        if (PlayerDisplay.shouldCancelOverlay(OverlayType.FOOD)) ci.cancel();
    }

    @ModifyExpressionValue(method = "renderMainHud", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;hasExperienceBar()Z"))
    private boolean cancelXPLevelRender(boolean original) {
        if (PlayerDisplay.shouldCancelOverlay(OverlayType.XP)) return false;
        return original;
    }
}

