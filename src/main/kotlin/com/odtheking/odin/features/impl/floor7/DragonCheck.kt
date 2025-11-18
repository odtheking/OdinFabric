package com.odtheking.odin.features.impl.floor7

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.utils.modMessage
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.boss.enderdragon.EnderDragon
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.item.Items
import net.minecraft.world.phys.Vec3

object DragonCheck {

    var lastDragonDeath: WitherDragonsEnum = WitherDragonsEnum.None

    fun dragonUpdate(packet: ClientboundSetEntityDataPacket) {
        val dragon = WitherDragonsEnum.entries.find { it.entityId == packet.id }?.apply {
            if (entity == null) updateEntity(packet.id)
        } ?: return

        val entity = mc.level?.getEntity(packet.id) as? EnderDragon ?: return
        if (entity.isDeadOrDying && dragon.state != WitherDragonState.DEAD) dragon.setDead()
    }

    fun dragonSpawn(packet: ClientboundAddEntityPacket) {
        if (packet.type != EntityType.ENDER_DRAGON) return
        WitherDragonsEnum.entries.find {
            it.aabbDimensions.contains(Vec3(packet.x, packet.y, packet.z)) &&
            it.state == WitherDragonState.SPAWNING
        }?.setAlive(packet.id)
    }

    fun dragonSprayed(packet: ClientboundSetEquipmentPacket) {
        if (packet.slots.none { it.second.item == Items.PACKED_ICE }) return

        val sprayedEntity = mc.level?.getEntity(packet.entity) as? ArmorStand ?: return

        WitherDragonsEnum.entries.forEach { dragon ->
            val entity = dragon.entity ?: return@forEach
            if (dragon.isSprayed || dragon.state != WitherDragonState.ALIVE || sprayedEntity.distanceTo(entity) > 8) return@forEach

            if (WitherDragons.sendSpray) {
                modMessage("§${dragon.colorCode}${dragon.name} §fdragon was sprayed in §c${(WitherDragons.currentTick - dragon.spawnedTime).let { 
                    "$it §ftick${if (it > 1) "s" else ""}" 
                }}.")
            }
            dragon.isSprayed = true
        }
    }
}

