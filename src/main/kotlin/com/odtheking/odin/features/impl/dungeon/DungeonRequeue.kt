package com.odtheking.odin.features.impl.dungeon

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.events.PacketEvent
import com.odtheking.odin.events.WorldLoadEvent
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.handlers.LimitedTickTask
import com.odtheking.odin.utils.noControlCodes
import com.odtheking.odin.utils.sendCommand
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import meteordevelopment.orbit.EventHandler
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket

object DungeonRequeue : Module(
    name = "Dungeon Requeue",
    description = "Automatically starts a new dungeon at the end of a dungeon."
) {
    private val delay by NumberSetting("Delay", 2, 0, 30, 1, desc = "The delay in seconds before requeuing.", unit = "s")
    private val type by BooleanSetting("Type", true, desc = "The type of command to execute to fulfill the requeue request. (true for Normal, false for Requeue)")
    private val disablePartyLeave by BooleanSetting("Disable on leave/kick", true, desc = "Disables the requeue on party leave message.")

    private val transferredRegex = Regex("The party was transferred to (\\[.+])? ?(.{1,16}) because (\\[.+])? ?(.{1,16}) left")
    private val disbandedEmptyRegex = Regex("The party was disbanded because all invites expired and the party was empty.")
    private val leftOrRemovedRegex = Regex("(\\[.+])? ?(.{1,16}) has (left|been removed from) the party.")
    private val kickedOfflineRegex = Regex("Kicked (\\[.+])? ?(.{1,16}) because they were offline.")
    private val kickedByRegex = Regex("You have been kicked from the party by (\\[.+])? ?(.{1,16})")
    private val disbandedByRegex = Regex("(\\[.+])? ?(.{1,16}) has disbanded the party.")
    private val extraStatsRegex = Regex(" {29}> EXTRA STATS <")
    private val youLeftRegex = Regex("You left the party.")
    var disableRequeue = false

    @EventHandler
    fun onPacketReceive(event: PacketEvent.Receive) = with(event.packet) {
        if (this !is GameMessageS2CPacket || overlay) return@with

        when {
            content.string.noControlCodes.matches(extraStatsRegex) -> {
                if (disableRequeue) {
                    disableRequeue = false
                    return@with
                }
                LimitedTickTask(delay * 20, 1) {
                    if (!disableRequeue) {
                        val command = if (type) "instancerequeue" else "od ${DungeonUtils.floor?.name?.lowercase()}"
                        sendCommand(command)
                    }
                }
            }
            disablePartyLeave && (content.string.matches(transferredRegex) || content.string.matches(leftOrRemovedRegex) || content.string.matches(disbandedEmptyRegex)
                    || content.string.matches(kickedOfflineRegex) || content.string.matches(kickedByRegex) || content.string.matches(youLeftRegex)
                    || content.string.matches(disbandedByRegex)) -> disableRequeue = true
        }
    }

    @EventHandler
    fun onWorldLoad(event: WorldLoadEvent) {
        disableRequeue = false
    }
}