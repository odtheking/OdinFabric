package com.odtheking.odin.utils.skyblock

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.PacketEvent
import com.odtheking.odin.events.WorldLoadEvent
import com.odtheking.odin.utils.handlers.LimitedTickTask
import com.odtheking.odin.utils.handlers.TickTask
import com.odtheking.odin.utils.noControlCodes
import meteordevelopment.orbit.EventHandler
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.entity.mob.GiantEntity
import net.minecraft.entity.mob.MagmaCubeEntity
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket
import net.minecraft.network.packet.s2c.play.TeamS2CPacket
import kotlin.jvm.optionals.getOrNull

object KuudraUtils {

    inline val inKuudra get() = LocationUtils.currentArea.isArea(Island.Kuudra)

    val freshers: MutableMap<String, Long?> = mutableMapOf()
    val giantZombies: ArrayList<GiantEntity> = arrayListOf()
    var kuudraEntity: MagmaCubeEntity? = null
        private set
    var phase = 0
        private set

    val buildingPiles = arrayListOf<ArmorStandEntity>()
    var playersBuildingAmount = 0
        private set
    var buildDonePercentage = 0
        private set

    var kuudraTier: Int = 0
        private set

    private val ownFreshRegex =
        Regex("^Your Fresh Tools Perk bonus doubles your building speed for the next 10 seconds!\$")
    private val buildRegex = Regex("Building Progress (\\d+)% \\((\\d+) Players Helping\\)")
    private val partyFreshRegex = Regex("^Party > (\\[[^]]*?])? ?(\\w{1,16}): FRESH\$")
    private val tierRegex = Regex("Kuudra's Hollow \\(T(\\d)\\)\$")
    private val progressRegex = Regex("PROGRESS: (\\d+)%")

    @EventHandler
    fun onChat(event: PacketEvent.Receive) = with(event.packet) {
        if (this !is GameMessageS2CPacket || overlay || !inKuudra) return
        val message = content.string.noControlCodes

        when (message) {
            "[NPC] Elle: Okay adventurers, I will go and fish up Kuudra!" -> phase = 1
            "[NPC] Elle: OMG! Great work collecting my supplies!" -> phase = 2
            "[NPC] Elle: Phew! The Ballista is finally ready! It should be strong enough to tank Kuudra's blows now!" -> phase =
                3

            "[NPC] Elle: POW! SURELY THAT'S IT! I don't think he has any more in him!" -> phase = 4
        }

        partyFreshRegex.find(message)?.groupValues?.get(2)?.let { playerName ->
            freshers[playerName] = System.currentTimeMillis()
            LimitedTickTask(200, 1, true) {
                freshers[playerName] = null
            }
        }

        ownFreshRegex.find(message)?.let {
            freshers[mc.player?.name?.string ?: "self"] = System.currentTimeMillis()
            LimitedTickTask(200, 1, true) {
                freshers[mc.player?.name?.string ?: "self"] = null
            }
        }
        Unit
    }

    init {
        TickTask(10) {
            if (!inKuudra) return@TickTask
            val entities = mc.world?.entities ?: return@TickTask

            giantZombies.clear()
            buildingPiles.clear()

            entities.forEach { entity ->
                when (entity) {
                    is GiantEntity ->
                        if (entity.mainHandStack?.name?.string?.endsWith("Head") == true) giantZombies.add(entity)

                    is MagmaCubeEntity ->
                        if (entity.size == 30 && entity.getAttributeBaseValue(EntityAttributes.MAX_HEALTH) == 100000.0) kuudraEntity =
                            entity

                    is ArmorStandEntity -> {
                        if (entity.name.string.matches(progressRegex)) buildingPiles.add(entity)

                        if (phase == 2) {
                            buildRegex.find(entity.name.string)?.let {
                                playersBuildingAmount = it.groupValues[2].toIntOrNull() ?: 0
                                buildDonePercentage = it.groupValues[1].toIntOrNull() ?: 0
                            }
                        }
                        if (phase != 1 || entity.name.string != "✓ SUPPLIES RECEIVED ✓") return@forEach
                        val x = entity.x.toInt()
                        val z = entity.z.toInt()
                        when {
                            x == -98 && z == -112 -> Supply.Shop.isActive = false
                            x == -98 && z == -99 -> Supply.Equals.isActive = false
                            x == -110 && z == -106 -> Supply.xCannon.isActive = false
                            x == -106 && z == -112 -> Supply.X.isActive = false
                            x == -94 && z == -106 -> Supply.Triangle.isActive = false
                            x == -106 && z == -99 -> Supply.Slash.isActive = false
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    fun handleTabListPacket(event: PacketEvent.Receive) = with(event.packet) {
        if (!inKuudra) return
        when (this) {
            is TeamS2CPacket -> {
                val teamLine = team.getOrNull() ?: return
                val text = teamLine.prefix.string?.plus(teamLine.suffix.string) ?: return

                tierRegex.find(text)?.groupValues?.get(1)?.let { kuudraTier = it.toInt() }
            }
        }
        Unit
    }

    @EventHandler
    fun onWorldLoad(event: WorldLoadEvent) {
        Supply.entries.forEach { it.isActive = true }
        playersBuildingAmount = 0
        buildDonePercentage = 0
        buildingPiles.clear()
        giantZombies.clear()
        kuudraEntity = null
        freshers.clear()
        kuudraTier = 0
        phase = 0
    }
}