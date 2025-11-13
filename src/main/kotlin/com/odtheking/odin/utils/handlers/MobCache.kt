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
    private val entityIds = hashSetOf<Int>()

    init {
        MobCaches.registerMobCache(this)
    }

    fun addEntityToCache(entityID: Int) {
        val entity = mc.level?.getEntity(entityID + entityOffset()) ?: return
        if (entityIds.add(entity.id)) {
            if (maxSize != 0 && size >= maxSize) entityIds.remove(removeAt(0).id)
            add(entity)
        }
    }

    fun removeEntityById(entityID: Int) {
        if (entityIds.remove(entityID)) removeIf { it.id == entityID }
    }

    override fun clear() {
        super.clear()
        entityIds.clear()
    }
}

object MobCaches {
    private val mobCaches = CopyOnWriteArrayList<MobCache>()
    private val mobProcessQueue = hashSetOf<Int>()
    private val queueLock = Any()

    init {
        TickTask(20) {
            val level = mc.level ?: return@TickTask
            val toProcess = synchronized(queueLock) {
                mobProcessQueue.toList().also { mobProcessQueue.clear() }
            }

            for (entityId in toProcess) {
                val entity = level.getEntity(entityId) ?: continue

                for (cache in mobCaches) {
                    if (cache.predicate(entity)) cache.addEntityToCache(entityId)
                }
            }
        }

        onReceive<ClientboundAddEntityPacket> {
            synchronized(queueLock) {
                mobProcessQueue.add(id)
            }
        }

        onReceive<ClientboundRemoveEntitiesPacket> {
            synchronized(queueLock) {
                entityIds.forEach { id ->
                    mobProcessQueue.remove(id)
                }
            }

            mobCaches.forEach { cache ->
                entityIds.forEach { id ->
                    cache.removeEntityById(id)
                }
            }
        }

        on<WorldLoadEvent> {
            synchronized(queueLock) {
                mobProcessQueue.clear()
            }

            mobCaches.forEach { it.clear() }
        }
    }

    fun registerMobCache(mobCache: MobCache) {
        mobCaches.add(mobCache)
    }
}