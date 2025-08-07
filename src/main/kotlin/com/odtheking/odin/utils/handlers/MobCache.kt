package com.odtheking.odin.utils.handlers

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.PacketEvent
import com.odtheking.odin.events.WorldLoadEvent
import meteordevelopment.orbit.EventHandler
import net.minecraft.entity.Entity
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket
import java.util.concurrent.CopyOnWriteArrayList

class MobCache(
    private val maxSize: Int = 0,
    private val entityOffset: () -> Int = { 0 },
    val predicate: (Entity) -> Boolean = { true }
) : CopyOnWriteArrayList<Entity>() {
    init {
        MobCaches.registerMobCache(this)
    }

    fun addEntityToCache(entityID: Int) {
        val entity = mc.world?.getEntityById(entityID + entityOffset()) ?: return
        if (!this.any { it.id == entity.id }) {
            if (maxSize != 0 && size >= maxSize) removeAt(0)
            add(entity)
        }
    }

    fun getClosestEntity(): Entity? {
        var closestDistance = Float.MAX_VALUE
        var closestEntity: Entity? = null

        for (entity in this) {
            val player = mc.player ?: continue
            val distance = player.distanceTo(entity)

            if (distance < closestDistance) {
                closestEntity = entity
                closestDistance = distance
            }
        }

        return closestEntity
    }
}

object MobCaches {
    private val mobCaches = CopyOnWriteArrayList<MobCache>()
    private val mobProcessQueue = CopyOnWriteArrayList<Int>()

    init {
        TickTask(20) {
            val toRemove = mutableListOf<Int>()

            mobProcessQueue.forEach {
                val entity = mc.world?.getEntityById(it) ?: return@forEach
                toRemove.add(it)

                mobCaches.forEach { cache ->
                    if (cache.predicate(entity)) cache.addEntityToCache(it)
                }
            }
        }
    }

    fun registerMobCache(mobCache: MobCache) {
        mobCaches.add(mobCache)
    }

    @EventHandler
    fun onMobMetadata(event: PacketEvent.Receive) {
        if (event.packet is EntitySpawnS2CPacket)
            mobProcessQueue.add(event.packet.entityId)
    }

    @EventHandler
    fun onMobDespawn(event: PacketEvent.Receive) {
        if (event.packet !is EntitiesDestroyS2CPacket) return

        event.packet.entityIds.forEach { id ->
            mobProcessQueue.removeIf { it == id }

            mobCaches.forEach { cache ->
                cache.removeIf { it.id == id }
            }
        }
    }

    @EventHandler
    fun onWorldLoad(event: WorldLoadEvent) {
        mobProcessQueue.clear()

        mobCaches.forEach { it.clear() }
    }
}