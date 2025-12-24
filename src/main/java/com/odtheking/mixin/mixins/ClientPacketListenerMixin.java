package com.odtheking.mixin.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.odtheking.odin.events.GuiEvent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.odtheking.odin.OdinMod.getMc;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    @ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/DebugScreenOverlay;showNetworkCharts()Z"))
    private boolean alwaysSendPing(boolean original) {
        return true;
    }

    @Inject(method = "handleContainerSetSlot", at = @At("TAIL"))
    private void onSetSlot(ClientboundContainerSetSlotPacket clientboundContainerSetSlotPacket, CallbackInfo ci) {
        if (getMc().screen instanceof AbstractContainerScreen<?> container) {
            new GuiEvent.SlotUpdate(getMc().screen, clientboundContainerSetSlotPacket, container.getMenu()).postAndCatch();
        }
    }
}