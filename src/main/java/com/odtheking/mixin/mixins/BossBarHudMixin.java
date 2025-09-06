package com.odtheking.mixin.mixins;

import com.odtheking.odin.events.RenderBossBarEvent;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.entity.boss.BossBar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BossBarHud.class)
public abstract class BossBarHudMixin {

    @Inject(method = "renderBossBar(Lnet/minecraft/client/gui/DrawContext;IILnet/minecraft/entity/boss/BossBar;)V", at = @At("HEAD"), cancellable = true)
    private void onRenderBossBar(DrawContext context, int x, int y, BossBar bossBar, CallbackInfo ci) {
        if (new RenderBossBarEvent(bossBar).postAndCatch()) ci.cancel();
    }
}