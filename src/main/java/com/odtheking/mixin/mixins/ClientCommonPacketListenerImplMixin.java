package com.odtheking.mixin.mixins;

import com.odtheking.odin.events.TickEvent;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonPacketListenerImpl.class)
public abstract class ClientCommonPacketListenerImplMixin {

    @Inject(method = "handlePing", at = @At("TAIL"))
    private void handlePing(ClientboundPingPacket clientboundPingPacket, CallbackInfo ci) {
        if (clientboundPingPacket.getId() != 0) TickEvent.Server.INSTANCE.postAndCatch();
    }
}