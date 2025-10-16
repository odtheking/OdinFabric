package com.odtheking.odin.events

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.utils.containsOneOf
import com.odtheking.odin.utils.equalsOneOf
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils.isSecret
import meteordevelopment.orbit.EventHandler
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.minecraft.entity.ItemEntity
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket
import net.minecraft.network.packet.s2c.play.ItemPickupAnimationS2CPacket
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
    }

    @EventHandler
    fun onPacketReceive(event: PacketEvent.Receive) = with (event.packet) {
        if (!DungeonUtils.inDungeons || DungeonUtils.inBoss) return
        when (this) {
            is PlaySoundS2CPacket -> {
                if (sound.value().equalsOneOf(SoundEvents.ENTITY_BAT_HURT, SoundEvents.ENTITY_BAT_DEATH) && volume == 0.1f)
                    SecretPickupEvent.Bat(this).postAndCatch()
            }
            is ItemPickupAnimationS2CPacket -> {
                val itemEntity = mc.world?.getEntityById(entityId) as? ItemEntity ?: return@with
                if (itemEntity.stack?.name?.string?.containsOneOf(dungeonItemDrops, true) == true && itemEntity.distanceTo(mc.player) <= 6)
                    SecretPickupEvent.Item(itemEntity).postAndCatch()
            }
        }
    }

    @EventHandler
    fun onPacketSend(event: PacketEvent.Send) = with (event.packet) {
        if (!DungeonUtils.inDungeons) return
        if (this is PlayerInteractBlockC2SPacket)
            SecretPickupEvent.Interact(blockHitResult.blockPos, mc.world?.getBlockState(blockHitResult.blockPos)?.takeIf { isSecret(it, blockHitResult.blockPos) } ?: return).postAndCatch()
    }

    private val dungeonItemDrops = listOf(
        "Health Potion VIII Splash Potion", "Healing Potion 8 Splash Potion", "Healing Potion VIII Splash Potion", "Healing VIII Splash Potion", "Healing 8 Splash Potion",
        "Decoy", "Inflatable Jerry", "Spirit Leap", "Trap", "Training Weights", "Defuse Kit", "Dungeon Chest Key", "Treasure Talisman", "Revive Stone", "Architect's First Draft",
        "Secret Dye", "Candycomb"
    )
}