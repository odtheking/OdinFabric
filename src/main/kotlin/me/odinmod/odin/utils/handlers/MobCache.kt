package me.odinmod.odin.utils.handlers

import me.odinmod.odin.OdinMod.mc
import me.odinmod.odin.events.PacketEvent
import me.odinmod.odin.events.WorldLoadEvent
import meteordevelopment.orbit.EventHandler
import net.minecraft.entity.Entity
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket
import java.util.concurrent.CopyOnWriteArrayList

class MobCache(
    private val maxSize: Int = 0,
    private val entityOffset: Int = 0,
    val predicate: (Entity) -> Boolean = { true }
) : CopyOnWriteArrayList<Entity>() {

    fun addEntityToCache(entityID: Int) {
        val entity = mc.world?.getEntityById(entityID + entityOffset) ?: return

        if (!this.any { it.id == entity.id }) {
            if (maxSize != 0 && size >= maxSize) {
                removeAt(0)
            }
            add(entity)
        }
    }

    init {
        MobCaches.registerMobCache(this)
    }
}

object MobCaches {
    private val mobCaches = CopyOnWriteArrayList<MobCache>()

    fun registerMobCache(mobCache: MobCache) {
        mobCaches.add(mobCache)
    }

    @EventHandler
    fun onMobMetadata(event: PacketEvent.Receive) {
        if (event.packet !is EntityTrackerUpdateS2CPacket) return

        val entity = mc.world?.getEntityById(event.packet.id) ?: return

        mobCaches.forEach { cache ->
            if (cache.predicate(entity)) cache.addEntityToCache(event.packet.id)
        }
    }

    @EventHandler
    fun onMobDespawn(event: PacketEvent.Receive) {
        if (event.packet !is EntitiesDestroyS2CPacket) return

        event.packet.entityIds.forEach { id ->
            mobCaches.forEach { cache ->
                cache.removeIf { it.id == id }
            }
        }
    }

    @EventHandler
    fun onWorldLoad(event: WorldLoadEvent) {
        mobCaches.forEach { it.clear() }
    }
}