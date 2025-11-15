package com.odtheking.mixin.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.odtheking.odin.features.impl.skyblock.OverlayType;
import com.odtheking.odin.features.impl.skyblock.PlayerDisplay;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {

    @Inject(method = "renderArmor", at = @At("HEAD"), cancellable = true)
    private static void cancelArmorBar(GuiGraphics guiGraphics, Player player, int i, int j, int k, int l, CallbackInfo ci) {
        if (PlayerDisplay.shouldCancelOverlay(OverlayType.ARMOR)) ci.cancel();
    }

    @Inject(method = "renderHearts", at = @At("HEAD"), cancellable = true)
    private void cancelHealthBar(GuiGraphics guiGraphics, Player player, int i, int j, int k, int l, float f, int m, int n, int o, boolean bl, CallbackInfo ci) {
        if (PlayerDisplay.shouldCancelOverlay(OverlayType.HEARTS)) ci.cancel();
    }

    @Inject(method = "renderFood", at = @At("HEAD"), cancellable = true)
    private void cancelFoodBar(GuiGraphics guiGraphics, Player player, int i, int j, CallbackInfo ci) {
        if (PlayerDisplay.shouldCancelOverlay(OverlayType.FOOD)) ci.cancel();
    }

    @ModifyExpressionValue(method = "renderHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;hasExperience()Z"))
    private boolean cancelXPLevelRender(boolean original) {
        if (PlayerDisplay.shouldCancelOverlay(OverlayType.XP)) return false;
        return original;
    }
}

