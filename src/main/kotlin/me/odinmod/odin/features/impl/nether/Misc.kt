package me.odinmod.odin.features.impl.nether

import me.odinmod.odin.clickgui.settings.impl.BooleanSetting
import me.odinmod.odin.events.PacketEvent
import me.odinmod.odin.features.Module
import me.odinmod.odin.utils.noControlCodes
import me.odinmod.odin.utils.sendCommand
import meteordevelopment.orbit.EventHandler
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket

object Misc : Module(
    name = "Misc",
    description = "Miscellaneous Nether features."
) {
    private val manaDrain by BooleanSetting("Mana Drain", true, desc = "Sends in party chat when you drain mana near players.")
    private val endStoneRegex = Regex("^Used Extreme Focus! \\((\\d+) Mana\\)$")

    @EventHandler
    fun onChat(event: PacketEvent.Receive) = with (event.packet) {
        if (this !is GameMessageS2CPacket || overlay) return
        val message = content.string.noControlCodes


        if (manaDrain) endStoneRegex.find(message)?.groupValues?.getOrNull(1)?.let { mana ->
            val players = mc.world?.players?.filter { it.squaredDistanceTo(mc.player) < 49 && it.uuid.version() == 4 } ?: return
            sendCommand("pc Used $mana mana (${players.size} players nearby)")
        }

        Unit
    }
}