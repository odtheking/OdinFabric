package com.odtheking.odin.utils.skyblock

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.ServerEvent
import com.odtheking.odin.events.WorldLoadEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.events.core.onReceive
import com.odtheking.odin.utils.equalsOneOf
import com.odtheking.odin.utils.startsWithOneOf
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket

object LocationUtils {
    var isOnHypixel: Boolean = false
        private set
    var isInSkyblock: Boolean = false
        private set
    var currentArea: Island = Island.Unknown
        private set

    init {
        on<ServerEvent.Connect> {
            if (mc.isSingleplayer) {
                currentArea = Island.SinglePlayer
                return@on
            }
            isOnHypixel = mc.runCatching { serverAddress.contains("hypixel", true) }.getOrDefault(false)
        }

        onReceive<ClientboundPlayerInfoUpdatePacket> {
            if (!currentArea.isArea(Island.Unknown) || actions().none { it.equalsOneOf(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME,) }) return@onReceive
            val area = entries()?.find { it?.displayName?.string?.startsWithOneOf("Area: ", "Dungeon: ") == true }?.displayName?.string ?: return@onReceive
            currentArea = Island.entries.firstOrNull { area.contains(it.displayName, true) } ?: Island.Unknown
        }

        onReceive<ClientboundSetObjectivePacket> {
            if (!isInSkyblock) isInSkyblock = isOnHypixel && objectiveName == "SBScoreboard"
        }

        on<WorldLoadEvent> {
            currentArea = Island.Unknown
            isInSkyblock = false
        }

        on<ServerEvent.Disconnect> {
            currentArea = Island.Unknown
            isInSkyblock = false
            isOnHypixel = false
        }
    }
}