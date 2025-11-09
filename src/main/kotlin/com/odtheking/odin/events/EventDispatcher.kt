package com.odtheking.odin.events

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.core.onReceive
import com.odtheking.odin.events.core.onSend
import com.odtheking.odin.utils.ChatManager
import com.odtheking.odin.utils.containsOneOf
import com.odtheking.odin.utils.equalsOneOf
import com.odtheking.odin.utils.noControlCodes
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils.isSecret
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents
import net.minecraft.entity.ItemEntity
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket
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

        ClientReceiveMessageEvents.ALLOW_GAME.register { text, overlay ->
            if (overlay) return@register false
            !ChatManager.shouldCancelMessage(text)
        }

        WorldRenderEvents.END_MAIN.register { context ->
            RenderEvent.Last(context).postAndCatch()
        }

        onReceive<ItemPickupAnimationS2CPacket> {
            if (!DungeonUtils.inDungeons || DungeonUtils.inBoss) return@onReceive
            val itemEntity = mc.world?.getEntityById(entityId) as? ItemEntity ?: return@onReceive
            if (itemEntity.stack?.name?.string?.containsOneOf(dungeonItemDrops, true) == true && itemEntity.distanceTo(mc.player) <= 6)
                SecretPickupEvent.Item(itemEntity).postAndCatch()
        }

        onReceive<EntitiesDestroyS2CPacket> {
            if (!DungeonUtils.inDungeons || DungeonUtils.inBoss) return@onReceive
            entityIds.forEach { id ->
                val entity = mc.world?.getEntityById(id) as? ItemEntity ?: return@forEach
                if (entity.stack?.name?.string?.containsOneOf(dungeonItemDrops, true) == true && entity.distanceTo(mc.player) <= 6)
                    SecretPickupEvent.Item(entity).postAndCatch()
            }
        }

        onReceive<PlaySoundS2CPacket> {
            if (!DungeonUtils.inDungeons || DungeonUtils.inBoss) return@onReceive
            if (sound.equalsOneOf(SoundEvents.ENTITY_BAT_HURT, SoundEvents.ENTITY_BAT_DEATH) && volume == 0.1f)
                SecretPickupEvent.Bat(this).postAndCatch()
        }

        onSend<PlayerInteractBlockC2SPacket> {
            if (!DungeonUtils.inDungeons) return@onSend
            SecretPickupEvent.Interact(
                blockHitResult.blockPos,
                mc.world?.getBlockState(blockHitResult.blockPos)?.takeIf { isSecret(it, blockHitResult.blockPos) } ?: return@onSend
            ).postAndCatch()
        }

        onReceive<GameMessageS2CPacket> {
            if (!overlay) content?.string?.noControlCodes?.let { ChatPacketEvent(it, content).postAndCatch() }
        }
    }

    private val dungeonItemDrops = listOf(
        "Health Potion VIII Splash Potion", "Healing Potion 8 Splash Potion", "Healing Potion VIII Splash Potion", "Healing VIII Splash Potion", "Healing 8 Splash Potion",
        "Decoy", "Inflatable Jerry", "Spirit Leap", "Trap", "Training Weights", "Defuse Kit", "Dungeon Chest Key", "Treasure Talisman", "Revive Stone", "Architect's First Draft",
        "Secret Dye", "Candycomb"
    )
}