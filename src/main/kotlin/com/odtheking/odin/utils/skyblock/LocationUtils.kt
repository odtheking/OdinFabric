package com.odtheking.odin.utils.skyblock

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.PacketEvent
import com.odtheking.odin.events.ServerEvent
import com.odtheking.odin.events.WorldLoadEvent
import com.odtheking.odin.utils.equalsOneOf
import com.odtheking.odin.utils.startsWithOneOf
import meteordevelopment.orbit.EventHandler
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket
import net.minecraft.network.packet.s2c.play.ScoreboardObjectiveUpdateS2CPacket
import net.minecraft.network.packet.s2c.play.TeamS2CPacket

object LocationUtils {
    var isOnHypixel: Boolean = false
        private set
    var isInSkyblock: Boolean = false
        private set
    var currentArea: Island = Island.Unknown
        private set

    var lobbyId: String? = null
        private set

    private val lobbyRegex = Regex("\\d\\d/\\d\\d/\\d\\d (\\w{0,6}) *")

    @EventHandler
    fun onConnect(event: ServerEvent.Connect) {
        if (mc.isInSingleplayer) {
            currentArea = Island.SinglePlayer
            return
        }
        isOnHypixel = mc.runCatching { event.serverAddress.contains("hypixel", true) }.getOrDefault(false)
    }

    @EventHandler
    fun onPacket(event: PacketEvent.Receive) = with(event.packet) {
        when (this) {
            is PlayerListS2CPacket -> {
                if (!currentArea.isArea(Island.Unknown) || actions.none { it.equalsOneOf(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME,) }) return
                val area = entries?.find { it?.displayName?.string?.startsWithOneOf("Area: ", "Dungeon: ") == true }?.displayName?.string ?: return
                currentArea = Island.entries.firstOrNull { area.contains(it.displayName, true) } ?: Island.Unknown
            }

            is ScoreboardObjectiveUpdateS2CPacket ->
                if (!isInSkyblock) isInSkyblock = isOnHypixel && name == "SBScoreboard"

            is TeamS2CPacket -> {
                if (!currentArea.isArea(Island.Unknown)) return
                val team = team?.orElse(null) ?: return
                val text = team.prefix?.string?.plus(team.suffix?.string) ?: return

                lobbyRegex.find(text)?.groupValues?.get(1)?.let {
                    lobbyId = it
                }
            }
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