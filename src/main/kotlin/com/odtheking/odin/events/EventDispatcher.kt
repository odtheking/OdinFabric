package com.odtheking.odin.events

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.utils.equalsOneOf
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils.isSecret
import meteordevelopment.orbit.EventHandler
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.minecraft.entity.ItemEntity
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket
import net.minecraft.sound.SoundEvents

object EventDispatcher {

    init {
        ClientPlayConnectionEvents.JOIN.register { handler, _, _ ->
            ServerEvent.Connect(handler.serverInfo?.address ?: "SinglePlayer").postAndCatch()
        }

        ClientPlayConnectionEvents.DISCONNECT.register { handler, _ ->
            ServerEvent.Disconnect(handler.serverInfo?.address ?: "SinglePlayer").postAndCatch()
        }

        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register { _, _ ->
            WorldLoadEvent().postAndCatch()
        }

        ClientTickEvents.START_CLIENT_TICK.register { _ ->
            mc.world?.let { TickEvent.Start().postAndCatch() }
        }

        ClientTickEvents.END_CLIENT_TICK.register { _ ->
            mc.world?.let { TickEvent.End().postAndCatch() }
        }

        WorldRenderEvents.AFTER_TRANSLUCENT.register { context ->
            mc.world?.let { RenderEvent.Last(context).postAndCatch() }
        }
    }

    @EventHandler
    fun onEntityLeave(event: EntityLeaveWorldEvent) = with(event.entity) {
        if (DungeonUtils.inDungeons && this is ItemEntity && name.string == "Air" && distanceTo(mc.player) <= 6)
            SecretPickupEvent.Item(this).postAndCatch()
    }

    @EventHandler
    fun onPacketReceive(event: PacketEvent.Receive) = with (event.packet) {
        if (!DungeonUtils.inDungeons || DungeonUtils.inBoss) return
        when (this) {
            is PlaySoundS2CPacket -> {
                if (sound.value().equalsOneOf(SoundEvents.ENTITY_BAT_HURT, SoundEvents.ENTITY_BAT_DEATH) && volume == 0.1f)
                    SecretPickupEvent.Bat(this).postAndCatch()
            }
        }
    }

    @EventHandler
    fun onPacketSend(event: PacketEvent.Send) = with (event.packet) {
        if (!DungeonUtils.inDungeons) return
        if (this is PlayerInteractBlockC2SPacket)
            SecretPickupEvent.Interact(blockHitResult.blockPos, mc.world?.getBlockState(blockHitResult.blockPos)?.takeIf { isSecret(it, blockHitResult.blockPos) } ?: return).postAndCatch()
    }
}