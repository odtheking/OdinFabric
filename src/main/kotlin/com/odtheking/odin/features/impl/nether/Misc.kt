package com.odtheking.odin.features.impl.nether

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.events.PacketEvent
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.handlers.LimitedTickTask
import com.odtheking.odin.utils.noControlCodes
import com.odtheking.odin.utils.sendCommand
import meteordevelopment.orbit.EventHandler
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket

object Misc : Module(
    name = "Misc",
    description = "Miscellaneous Nether features."
) {
    private val manaDrain by BooleanSetting("Mana Drain", true, desc = "Sends in party chat when you drain mana near players.")
    private val autoRequeue by BooleanSetting("Auto Requeue", true, desc = "Automatically requeues you after a Kuudra run.")
    private val requeueDelay by NumberSetting("Requeue Delay", 20, 0, 100, 1, unit = "ticks", desc = "Delay before requeuing after a run ends.").withDependency { autoRequeue }

    private val endRunRegex =
        Regex("^\\[NPC] Elle: Good job everyone. A hard fought battle come to an end. Let's get out of here before we run into any more trouble!$")
    private val endStoneRegex = Regex("^Used Extreme Focus! \\((\\d+) Mana\\)$")

    @EventHandler
    fun onChat(event: PacketEvent.Receive) = with(event.packet) {
        if (this !is GameMessageS2CPacket || overlay) return
        val message = content.string.noControlCodes

        if (manaDrain) endStoneRegex.find(message)?.groupValues?.getOrNull(1)?.let { mana ->
            val players =
                mc.world?.players?.filter { it.squaredDistanceTo(mc.player) < 49 && it.uuid.version() == 4 } ?: return
            sendCommand("pc Used $mana mana (${players.size} players nearby)")
        }

        if (autoRequeue && endRunRegex.matches(message))
            LimitedTickTask(requeueDelay, 1) { sendCommand("instancerequeue") }
    }
}