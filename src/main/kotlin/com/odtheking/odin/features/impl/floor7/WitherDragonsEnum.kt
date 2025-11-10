package com.odtheking.odin.features.impl.floor7

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.features.impl.floor7.DragonCheck.dragonEntityList
import com.odtheking.odin.features.impl.floor7.DragonCheck.lastDragonDeath
import com.odtheking.odin.features.impl.floor7.DragonPriority.displaySpawningDragon
import com.odtheking.odin.features.impl.floor7.DragonPriority.findPriority
import com.odtheking.odin.features.impl.floor7.WitherDragons.currentTick
import com.odtheking.odin.features.impl.floor7.WitherDragons.priorityDragon
import com.odtheking.odin.features.impl.floor7.WitherDragons.sendArrowHit
import com.odtheking.odin.features.impl.floor7.WitherDragons.sendSpawned
import com.odtheking.odin.features.impl.floor7.WitherDragons.sendSpawning
import com.odtheking.odin.features.impl.floor7.WitherDragons.sendTime
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.handlers.TickTask
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.toFixed
import net.minecraft.world.entity.boss.enderdragon.EnderDragon
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket
import net.minecraft.core.BlockPos
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.phys.AABB

enum class WitherDragonsEnum(
    val spawnPos: BlockPos,
    val aabbDimensions: AABB,
    val colorCode: Char,
    val color: Color,
    val xRange: ClosedFloatingPointRange<Double>,
    val zRange: ClosedFloatingPointRange<Double>,
    var timeToSpawn: Int = 100,
    var state: WitherDragonState = WitherDragonState.DEAD,
    var timesSpawned: Int = 0,
    var entityId: Int? = null,
    var entity: EnderDragon? = null,
    var isSprayed: Boolean = false,
    var spawnedTime: Long = 0,
    val skipKillTime: Int = 0,
    val arrowsHit: HashMap<String, ArrowsHit> = HashMap()
) {
    Red(BlockPos(27, 14, 59), AABB(14.5, 13.0, 45.5, 39.5, 28.0, 70.5), 'c', Colors.MINECRAFT_RED, 24.0..30.0, 56.0..62.0, skipKillTime = 50),
    Orange(BlockPos(85, 14, 56), AABB(72.0, 8.0, 47.0, 102.0, 28.0, 77.0), '6', Colors.MINECRAFT_GOLD, 82.0..88.0, 53.0..59.0, skipKillTime = 62),
    Green(BlockPos(27, 14, 94), AABB(7.0, 8.0, 80.0, 37.0, 28.0, 110.0), 'a', Colors.MINECRAFT_GREEN, 23.0..29.0, 91.0..97.0, skipKillTime = 52),
    Blue(BlockPos(84, 14, 94), AABB(71.5, 16.0, 82.5, 96.5, 26.0, 107.5), 'b', Colors.MINECRAFT_AQUA, 82.0..88.0, 91.0..97.0, skipKillTime = 47),
    Purple(BlockPos(56, 14, 125), AABB(45.5, 13.0, 113.5, 68.5, 23.0, 136.5), '5', Colors.MINECRAFT_DARK_PURPLE, 53.0..59.0, 122.0..128.0, skipKillTime = 38),
    None(BlockPos(0, 0, 0), AABB(0.0, 0.0, 0.0, 0.0, 0.0, 0.0), 'f', Colors.WHITE, 0.0..0.0, 0.0..0.0);

    fun setAlive(entityId: Int) {
        state = WitherDragonState.ALIVE

        timeToSpawn = 100
        timesSpawned++
        this.entityId = entityId
        spawnedTime = currentTick
        isSprayed = false
        arrowsHit.clear()

        if (sendArrowHit && WitherDragons.enabled) {
            TickTask(skipKillTime) {
                if (entity?.isAlive == true)
                    modMessage("§fArrows Hit on §${colorCode}${name}§f in §c${(skipKillTime / 20f).toFixed(2)}s§7: ${
                        arrowsHit.entries.joinToString(", ") { 
                            "§f${it.key}§7: §6${it.value.good}${it.value.late.let { late -> 
                                if (late > 0) " §8(§7${late}§8)" else "" 
                            }}§7" 
                        }
                    }.")

            }
        }

        if (sendSpawned && WitherDragons.enabled) {
            val numberSuffix = when (timesSpawned) {
                1 -> "st"
                2 -> "nd"
                3 -> "rd"
                else -> "th"
            }
            modMessage("§${colorCode}${name} §fdragon spawned. This is the §${colorCode}${timesSpawned}${numberSuffix}§f time it has spawned.")
        }
    }

    fun setDead(deathless: Boolean = false) {
        state = WitherDragonState.DEAD
        dragonEntityList.remove(entity)
        entityId = null
        entity = null
        if (!deathless) lastDragonDeath = this

        if (sendArrowHit && WitherDragons.enabled && currentTick - spawnedTime < skipKillTime) {
            modMessage("§fArrows Hit on §${colorCode}${name}§7: ${
                arrowsHit.entries.joinToString(", ") { 
                    "§f${it.key}§7: §6${it.value.good}${it.value.late.let { late -> 
                        if (late > 0) " §8(§7${late}§8)" else "" 
                    }}§7" 
                }
            }.")
        }

        if (priorityDragon == this) priorityDragon = None

        if (sendTime && WitherDragons.enabled) {
            WitherDragons.dragonPBs.time(name, ((currentTick - spawnedTime) / 20f), "s§7!", "§${colorCode}${name} §7was alive for §6")
        }
    }

    fun updateEntity(entityId: Int) {
        entity = (mc.level?.getEntity(entityId) as? EnderDragon)?.also { dragonEntityList.add(it) }
    }

    companion object {
        fun reset(soft: Boolean = false) {
            if (soft) {
                WitherDragonsEnum.entries.forEach {
                    it.state = WitherDragonState.DEAD
                    it.timesSpawned++
                }
                return
            }

            WitherDragonsEnum.entries.forEach {
                it.timeToSpawn = 100
                it.timesSpawned = 0
                it.state = WitherDragonState.DEAD
                it.entityId = null
                it.entity = null
                it.isSprayed = false
                it.spawnedTime = 0
            }
            dragonEntityList.clear()
            priorityDragon = None
            lastDragonDeath = None
        }
    }
}

data class ArrowsHit(var good: Int = 0, var late: Int = 0)

enum class WitherDragonState {
    SPAWNING,
    ALIVE,
    DEAD
}

fun handleSpawnPacket(particle: ClientboundLevelParticlesPacket) {
    if (
        particle.count != 20 ||
        particle.y != 19.0 ||
        particle.particle.type != ParticleTypes.FLAME ||
        particle.xDist != 2f ||
        particle.yDist != 3f ||
        particle.zDist != 2f ||
        particle.maxSpeed != 0f ||
        particle.x % 1 != 0.0 ||
        particle.z % 1 != 0.0
    ) return

    val (spawned, dragons) = WitherDragonsEnum.entries.fold(0 to mutableListOf<WitherDragonsEnum>()) { (spawned, dragons), dragon ->
        val newSpawned = spawned + dragon.timesSpawned

        if (dragon.state == WitherDragonState.SPAWNING) {
            if (dragon !in dragons) dragons.add(dragon)
            return@fold newSpawned to dragons
        }

        if (particle.x !in dragon.xRange || particle.z !in dragon.zRange) return@fold newSpawned to dragons

        if (sendSpawning && WitherDragons.enabled) modMessage("§${dragon.colorCode}$dragon §fdragon is spawning.")

        dragon.state = WitherDragonState.SPAWNING
        dragons.add(dragon)
        newSpawned to dragons
    }

    if (dragons.isNotEmpty() && (dragons.size == 2 || spawned >= 2) && (priorityDragon == WitherDragonsEnum.None || priorityDragon.entity?.isDeadOrDying == false))
        priorityDragon = findPriority(dragons).also { displaySpawningDragon(it) }
}

