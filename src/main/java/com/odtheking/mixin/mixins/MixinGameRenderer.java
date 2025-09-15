package com.odtheking.mixin.mixins;

import com.odtheking.odin.OdinMod;
import com.odtheking.odin.events.GuiEvent;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/toast/ToastManager;draw(Lnet/minecraft/client/gui/DrawContext;)V", shift = At.Shift.AFTER), cancellable = true)
    public void hookRender(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        if (OdinMod.INSTANCE.getMc().currentScreen != null && new GuiEvent.NVGRender(OdinMod.INSTANCE.getMc().currentScreen).postAndCatch())
            ci.cancel();
    }
}