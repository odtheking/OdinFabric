package com.odtheking.odin.features.impl.floor7

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.utils.modMessage
import net.minecraft.entity.boss.dragon.EnderDragonEntity
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.item.Items
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket
import net.minecraft.util.math.Vec3d
import java.util.concurrent.CopyOnWriteArrayList

object DragonCheck {

    var lastDragonDeath: WitherDragonsEnum = WitherDragonsEnum.None
    val dragonEntityList = CopyOnWriteArrayList<EnderDragonEntity>()

    fun dragonUpdate(packet: EntityTrackerUpdateS2CPacket) {
        val dragon = WitherDragonsEnum.entries.find { it.entityId == packet.id }?.apply {
            if (entity == null) updateEntity(packet.id)
        } ?: return

        val entity = mc.world?.getEntityById(packet.id) as? EnderDragonEntity ?: return
        if (entity.health <= 0 && dragon.state != WitherDragonState.DEAD) dragon.setDead()
    }

    fun dragonSpawn(packet: EntitySpawnS2CPacket) {
        WitherDragonsEnum.entries.find {
            it.boxesDimensions.contains(Vec3d(packet.x, packet.y, packet.z)) &&
            it.state == WitherDragonState.SPAWNING
        }?.setAlive(packet.entityId)
    }

    fun dragonSprayed(packet: EntityEquipmentUpdateS2CPacket) {
        if (packet.equipmentList.none { it.second.item == Items.PACKED_ICE }) return

        val sprayedEntity = mc.world?.getEntityById(packet.entityId) as? ArmorStandEntity ?: return

        WitherDragonsEnum.entries.forEach { dragon ->
            if (dragon.isSprayed || dragon.state != WitherDragonState.ALIVE || dragon.entity == null || sprayedEntity.distanceTo(dragon.entity) > 8) return@forEach

            if (WitherDragons.sendSpray) {
                modMessage("§${dragon.colorCode}${dragon.name} §fdragon was sprayed in §c${(WitherDragons.currentTick - dragon.spawnedTime).let { 
                    "$it §ftick${if (it > 1) "s" else ""}" 
                }}.")
            }
            dragon.isSprayed = true
        }
    }
}

