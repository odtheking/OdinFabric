package com.odtheking.odin.features.impl.dungeon

import com.odtheking.odin.events.PacketEvent
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.itemId
import com.odtheking.odin.utils.lore
import com.odtheking.odin.utils.noControlCodes
import com.odtheking.odin.utils.render.drawStringWidth
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import meteordevelopment.orbit.EventHandler
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket

object BreakerDisplay : Module(
    name = "Breaker Display",
    description = "Displays the amount of charges left in your Dungeon Breaker"
) {
    private val chargesRegex = Regex("Charges: (\\d+)/(\\d+)⸕")
    private var charges = 0
    private var max = 0

    private val hud by HUD("Display", "Shows the amount of charges left in your Dungeon Breaker.") {
        if (it || (max != 0 && DungeonUtils.inDungeons)) drawStringWidth("§cCharges: §e${if (it) 17 else charges}§7/§e${if (it) 20 else max}§c⸕", 1, 1, Colors.WHITE, false) + 2f to 10f
        else 0f to 0f
    }

    @EventHandler
    fun onPacketReceive(event: PacketEvent.Receive) {
        if (event.packet !is ScreenHandlerSlotUpdateS2CPacket || !DungeonUtils.inDungeons) return
        val stack = event.packet.stack ?: return
        if (stack.itemId != "DUNGEONBREAKER") return

        stack.lore.firstNotNullOfOrNull { chargesRegex.find(it.string.noControlCodes) }?.let { match ->
            charges = match.groupValues[1].toIntOrNull() ?: 0
            max = match.groupValues[2].toIntOrNull() ?: 0
        }
    }
}
