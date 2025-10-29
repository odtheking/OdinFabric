package com.odtheking.odin.features.impl.floor7

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.*
import com.odtheking.odin.events.PacketEvent
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.WorldLoadEvent
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.*
import com.odtheking.odin.utils.render.drawStringWidth
import com.odtheking.odin.utils.render.drawText
import com.odtheking.odin.utils.render.drawWireFrameBox
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.M7Phases
import meteordevelopment.orbit.EventHandler
import net.minecraft.network.packet.s2c.common.CommonPingS2CPacket
import net.minecraft.network.packet.s2c.play.*
import net.minecraft.text.Text

object WitherDragons : Module(
    name = "Wither Dragons",
    description = "Tools for managing M7 dragons timers, boxes, priority, health and alerts."
) {
    private val dragonTimerDropDown by DropdownSetting("Dragon Timer Dropdown")
    private val dragonTimer by BooleanSetting("Dragon Timer", true, desc = "Displays a timer for when M7 dragons spawn.").withDependency { dragonTimerDropDown }
    private val dragonTimerStyle by SelectorSetting("Timer Style", "Milliseconds", arrayListOf("Milliseconds", "Seconds", "Ticks"), desc = "The style of the dragon timer.").withDependency { dragonTimer && dragonTimerDropDown }
    private val showSymbol by BooleanSetting("Timer Symbol", true, desc = "Displays a symbol for the timer.").withDependency { dragonTimer && dragonTimerDropDown }
    private val hud by HUD("Dragon Timer HUD", "Displays the dragon timer in the HUD.") { example ->
        if (example) (drawStringWidth("§5P §a4.5s", 1, 1, Colors.WHITE) + 2) to 10
        else {
            priorityDragon.takeIf { it != WitherDragonsEnum.None }?.let { dragon ->
                if (dragon.state != WitherDragonState.SPAWNING || dragon.timeToSpawn <= 0) return@HUD 0 to 0
                val width = drawStringWidth("§${dragon.colorCode}${dragon.name.first()}: ${getDragonTimer(dragon.timeToSpawn)}", 1, 1, Colors.WHITE)
                (width + 2) to 10
            } ?: (0 to 0)
        }
    }.withDependency { dragonTimerDropDown }

    private val dragonBoxesDropDown by DropdownSetting("Dragon Boxes Dropdown")
    private val dragonBoxes by BooleanSetting("Dragon Boxes", true, desc = "Displays boxes for where M7 dragons spawn.").withDependency { dragonBoxesDropDown }

    private val dragonTitleDropDown by DropdownSetting("Dragon Spawn Dropdown")
    val dragonTitle by BooleanSetting("Dragon Title", true, desc = "Displays a title for spawning dragons.").withDependency { dragonTitleDropDown }

    private val dragonAlerts by DropdownSetting("Dragon Alerts Dropdown")
    private val sendNotification by BooleanSetting("Send Dragon Confirmation", true, desc = "Sends a confirmation message when a dragon dies.").withDependency { dragonAlerts }
    val sendTime by BooleanSetting("Send Dragon Time Alive", true, desc = "Sends a message when a dragon dies with the time it was alive.").withDependency { dragonAlerts }
    val sendSpawning by BooleanSetting("Send Dragon Spawning", true, desc = "Sends a message when a dragon is spawning.").withDependency { dragonAlerts }
    val sendSpawned by BooleanSetting("Send Dragon Spawned", true, desc = "Sends a message when a dragon has spawned.").withDependency { dragonAlerts }
    val sendSpray by BooleanSetting("Send Ice Sprayed", true, desc = "Sends a message when a dragon has been ice sprayed.").withDependency { dragonAlerts }
    val sendArrowHit by BooleanSetting("Send Arrows Hit", true, desc = "Sends a message when a dragon dies with how many arrows were hit.").withDependency { dragonAlerts }

    private val dragonHealth by BooleanSetting("Dragon Health", true, desc = "Displays the health of M7 dragons.")

    private val dragonPriorityDropDown by DropdownSetting("Dragon Priority Dropdown")
    val dragonPriorityToggle by BooleanSetting("Dragon Priority", false, desc = "Displays the priority of dragons spawning.").withDependency { dragonPriorityDropDown }
    val normalPower by NumberSetting("Normal Power", 22.0f, 0.0, 32.0, desc = "Power needed to split.").withDependency { dragonPriorityToggle && dragonPriorityDropDown }
    val easyPower by NumberSetting("Easy Power", 19.0f, 0.0, 32.0, desc = "Power needed when its Purple and another dragon.").withDependency { dragonPriorityToggle && dragonPriorityDropDown }
    val soloDebuff by SelectorSetting("Purple Solo Debuff", "Tank", arrayListOf("Tank", "Healer"), desc = "The class that solo debuffs purple, the other class helps b/m.").withDependency { dragonPriorityToggle && dragonPriorityDropDown }
    val soloDebuffOnAll by BooleanSetting("Solo Debuff on All Splits", true, desc = "Same as Purple Solo Debuff but for all dragons (A will only have 1 debuff).").withDependency { dragonPriorityToggle && dragonPriorityDropDown }
    val paulBuff by BooleanSetting("Paul Buff", false, desc = "Multiplies the power in your run by 1.25.").withDependency { dragonPriorityToggle && dragonPriorityDropDown }

    val witherKingRegex = Regex("^\\[BOSS] Wither King: (Oh, this one hurts!|I have more of those\\.|My soul is disposable\\.)$")
    var priorityDragon = WitherDragonsEnum.None
    var currentTick = 0L

    val dragonPBs = PersonalBest(+MapSetting("DragonPBs", mutableMapOf<Int, Float>()))

    @EventHandler
    fun onWorldLoad(event: WorldLoadEvent) {
        WitherDragonsEnum.reset()
    }

    @EventHandler
    fun onPacketReceive(event: PacketEvent.Receive) {
        if (DungeonUtils.getF7Phase() != M7Phases.P5) return

        when (event.packet) {
            is CommonPingS2CPacket -> {
                WitherDragonsEnum.entries.forEach { if (it.state == WitherDragonState.SPAWNING && it.timeToSpawn > 0) it.timeToSpawn-- }
                currentTick++
            }
            is ParticleS2CPacket -> handleSpawnPacket(event.packet)
            is EntityEquipmentUpdateS2CPacket -> DragonCheck.dragonSprayed(event.packet)
            is EntitySpawnS2CPacket -> DragonCheck.dragonSpawn(event.packet)
            is EntityTrackerUpdateS2CPacket -> DragonCheck.dragonUpdate(event.packet)
            is GameMessageS2CPacket -> {
                val text = event.packet.content?.string ?: return

                if (witherKingRegex.matches(text)) {
                    WitherDragonsEnum.entries.find { DragonCheck.lastDragonDeath == it && DragonCheck.lastDragonDeath != WitherDragonsEnum.None }?.let {
                        if (sendNotification) modMessage("§${it.colorCode}${it.name} dragon counts.")
                        DragonCheck.lastDragonDeath = WitherDragonsEnum.None
                    } ?: WitherDragonsEnum.entries.find { it.state == WitherDragonState.ALIVE }?.setDead(true)
                }
            }
        }
    }

    @EventHandler
    fun onRenderWorld(event: RenderEvent.Last) {
        if (DungeonUtils.getF7Phase() != M7Phases.P5) return

        if (dragonHealth) {
            DragonCheck.dragonEntityList.forEach { dragon ->
                if (dragon.health > 0) {
                    event.context.drawText(
                        Text.of(colorHealth(dragon.health)).asOrderedText(),
                        dragon.pos.addVec(y = 1.5), 1f, false
                    )
                }
            }
        }

        WitherDragonsEnum.entries.forEach { dragon ->
            if (dragonTimer && dragon.state == WitherDragonState.SPAWNING && dragon.timeToSpawn > 0) {
                event.context.drawText(
                    Text.of("§${dragon.colorCode}${dragon.name.first()}: ${getDragonTimer(dragon.timeToSpawn)}").asOrderedText(),
                    dragon.spawnPos.toCenterPos(), 1f, false
                )
            }

            if (dragonBoxes && dragon.state != WitherDragonState.DEAD)
                event.context.drawWireFrameBox(dragon.boxesDimensions, dragon.color, depth = true)
        }
    }

    private fun getDragonTimer(spawnTime: Int): String = when {
        spawnTime <= 20 -> "§c"
        spawnTime <= 60 -> "§e"
        else -> "§a"
    } + when (dragonTimerStyle) {
        0 -> "${spawnTime * 50}${if (showSymbol) "ms" else ""}"
        1 -> "${(spawnTime / 20f).toFixed(1)}${if (showSymbol) "s" else ""}"
        else -> "${spawnTime}${if (showSymbol) "t" else ""}"
    }

    private fun colorHealth(health: Float): String {
        return when {
            health >= 750_000_000 -> "§a${formatHealth(health)}"
            health >= 500_000_000 -> "§e${formatHealth(health)}"
            health >= 250_000_000 -> "§6${formatHealth(health)}"
            else -> "§c${formatHealth(health)}"
        }
    }

    private fun formatHealth(health: Float): String {
        return when {
            health >= 1_000_000_000 -> "${(health / 1_000_000_000).toFixed(1)}b"
            health >= 1_000_000 -> "${(health / 1_000_000).toFixed(1)}m"
            health >= 1_000 -> "${(health / 1_000).toFixed(1)}k"
            else -> "${health.toInt()}"
        }
    }
}