package mixins;

import io.github.odtheking.odin.features.impl.skyblock.OverlayType;
import io.github.odtheking.odin.features.impl.skyblock.PlayerDisplay;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
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

    @Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
    private void cancelXPBar(DrawContext context, int x, CallbackInfo ci) {
        if (PlayerDisplay.shouldCancelOverlay(OverlayType.XP)) ci.cancel();
    }

    @Inject(method = "renderExperienceLevel", at = @At("HEAD"), cancellable = true)
    private void cancelXPLevel(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (PlayerDisplay.shouldCancelOverlay(OverlayType.XP)) ci.cancel();
    }
}

