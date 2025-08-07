package com.odtheking.mixin.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.odtheking.odin.events.GuiEvent;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
public class HandledScreenMixin {

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    protected void onInit(CallbackInfo ci) {
        if (new GuiEvent.Open((Screen) (Object) this).postAndCatch()) ci.cancel();
    }

    @Inject(method = "close", at = @At("HEAD"), cancellable = true)
    protected void onClose(CallbackInfo ci) {
        if (new GuiEvent.Close((Screen) (Object) this).postAndCatch()) ci.cancel();
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    protected void onRender(DrawContext context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        if (new GuiEvent.Render((Screen) (Object) this, context, mouseX, mouseY).postAndCatch()) ci.cancel();
    }

    @WrapOperation(method = "drawSlots", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawSlot(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/screen/slot/Slot;)V"))
    private void onDrawSlot(HandledScreen<?> instance, DrawContext context, Slot slot, Operation<Void> original) {
        if (!new GuiEvent.DrawSlot((Screen) (Object) this, context, slot).postAndCatch())
            original.call(instance, context, slot);
    }

    @Inject(method = "onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V", at = @At("HEAD"), cancellable = true)
    public void onMouseClickedSlot(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
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
}
