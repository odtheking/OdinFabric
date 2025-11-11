package com.odtheking.odin.utils.handlers

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.WorldLoadEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.events.core.onReceive
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.world.entity.Entity
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
        val entity = mc.level?.getEntity(entityID + entityOffset()) ?: return
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
                val entity = mc.level?.getEntity(it) ?: return@forEach
                toRemove.add(it)

                mobCaches.forEach { cache ->
                    if (cache.predicate(entity)) cache.addEntityToCache(it)
                }
            }
        }

        onReceive<ClientboundAddEntityPacket> {
            mobProcessQueue.add(id)
        }

        onReceive<ClientboundRemoveEntitiesPacket> {
            entityIds.forEach { id ->
                mobProcessQueue.removeIf { it == id }

                mobCaches.forEach { cache ->
                    cache.removeIf { it.id == id }
                }
            }
        }

        on<WorldLoadEvent> {
            mobProcessQueue.clear()

            mobCaches.forEach { it.clear() }
        }
    }

    fun registerMobCache(mobCache: MobCache) {
        mobCaches.add(mobCache)
    }
}