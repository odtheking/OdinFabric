package com.odtheking.mixin.mixins;

import com.odtheking.odin.events.PacketEvent;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixin {

    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void channelRead0(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci) {
        if (new PacketEvent.Receive(packet).postAndCatch()) ci.cancel();
    }

    @Inject(method = "sendImmediately(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;Z)V", at = @At("HEAD"))
    private void sendImmediately(Packet<?> packet, @Nullable PacketCallbacks callbacks, boolean flush, CallbackInfo ci) {
        new PacketEvent.Send(packet).postAndCatch();
    }
}