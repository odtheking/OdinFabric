package com.odtheking.mixin.mixins;

import com.odtheking.odin.events.GuiEvent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    protected void onInit(CallbackInfo ci) {
        if (new GuiEvent.Open((Screen) (Object) this).postAndCatch()) ci.cancel();
    }

    @Inject(method = "onClose", at = @At("HEAD"), cancellable = true)
    protected void onClose(CallbackInfo ci) {
        if (new GuiEvent.Close((Screen) (Object) this).postAndCatch()) ci.cancel();
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    protected void onRender(GuiGraphics context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        if (new GuiEvent.Draw((Screen) (Object) this, context, mouseX, mouseY).postAndCatch()) ci.cancel();
    }

    @Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
    protected void onRenderBackground(GuiGraphics context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        if (new GuiEvent.DrawBackground((Screen) (Object) this, context, mouseX, mouseY).postAndCatch()) ci.cancel();
    }

    @Inject(method = "renderSlot", at = @At("HEAD"), cancellable = true)
    private void onDrawSlot(GuiGraphics guiGraphics, Slot slot, CallbackInfo ci) {
        if (new GuiEvent.DrawSlot((Screen) (Object) this, guiGraphics, slot).postAndCatch()) ci.cancel();
    }

    @Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
    public void onMouseClickedSlot(Slot slot, int slotId, int button, ClickType actionType, CallbackInfo ci) {
        if (new GuiEvent.SlotClick((Screen) (Object) this, slotId, button).postAndCatch()) ci.cancel();
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    public void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (new GuiEvent.MouseClick((Screen) (Object) this, (int) mouseX, (int) mouseY, button).postAndCatch())
            cir.cancel();
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    public void onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (new GuiEvent.KeyPress((Screen) (Object) this, keyCode, scanCode, modifiers).postAndCatch()) cir.cancel();
    }

    @Inject(method = "renderTooltip", at = @At("HEAD"), cancellable = true)
    public void onDrawMouseoverTooltip(GuiGraphics context, int mouseX, int mouseY, CallbackInfo ci) {
        if (new GuiEvent.DrawTooltip((Screen) (Object) this, context, mouseX, mouseY).postAndCatch()) {
            ci.cancel();
        }
    }
}
