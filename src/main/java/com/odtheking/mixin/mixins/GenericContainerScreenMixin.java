package com.odtheking.mixin.mixins;

import com.odtheking.odin.events.GuiEvent;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GenericContainerScreen.class)
public class GenericContainerScreenMixin {

    @Inject(method = "drawBackground", at = @At("RETURN"), cancellable = true)
    protected void onDrawBackground(DrawContext context, float deltaTicks, int mouseX, int mouseY, CallbackInfo ci) {
        if (new GuiEvent.DrawBackground((Screen) (Object) this, context).postAndCatch()) ci.cancel();
    }
}
