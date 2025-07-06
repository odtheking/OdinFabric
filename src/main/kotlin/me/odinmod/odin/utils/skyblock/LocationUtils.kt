package me.odinmod.odin.utils.skyblock

import me.odinmod.odin.OdinMod.mc
import me.odinmod.odin.events.PacketEvent
import me.odinmod.odin.events.ServerEvent
import me.odinmod.odin.events.WorldLoadEvent
import me.odinmod.odin.utils.equalsOneOf
import me.odinmod.odin.utils.startsWithOneOf
import meteordevelopment.orbit.EventHandler
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket
import net.minecraft.network.packet.s2c.play.ScoreboardObjectiveUpdateS2CPacket

object LocationUtils {
    var isOnHypixel: Boolean = false
        private set
    var isInSkyblock: Boolean = false
        private set
    var currentArea: Island = Island.Unknown
        private set

    @EventHandler
    fun onConnect(event: ServerEvent.Connect) {
        if (mc.isInSingleplayer) {
            currentArea = Island.SinglePlayer
            return
        }
        isOnHypixel = mc.runCatching { event.serverAddress.contains("hypixel", true) }.getOrDefault(false)
    }

    @EventHandler
    fun onPacket(event: PacketEvent.Receive) = with (event.packet) {
        when (this) {
            is PlayerListS2CPacket -> {
                if (!currentArea.isArea(Island.Unknown) || actions.none { it.equalsOneOf(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, PlayerListS2CPacket.Action.ADD_PLAYER) }) return
                val area = entries?.find { it?.displayName?.string?.startsWithOneOf("Area: ", "Dungeon: ") == true }?.displayName?.string ?: return
                currentArea = Island.entries.firstOrNull { area.contains(it.displayName, true) } ?: Island.Unknown
            }

            is ScoreboardObjectiveUpdateS2CPacket ->
                if (!isInSkyblock) isInSkyblock = isOnHypixel && name == "SBScoreboard"
        }
    }

    @EventHandler
    fun onWorldChange(event: WorldLoadEvent) {
        currentArea = Island.Unknown
        isInSkyblock = false
    }

    @EventHandler
    fun onDisconnect(event: ServerEvent.Disconnect) {
        currentArea = Island.Unknown
        isInSkyblock = false
        isOnHypixel = false
    }
}