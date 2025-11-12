package com.odtheking.odin.features.impl.dungeon

import com.odtheking.odin.events.core.onReceive
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.itemId
import com.odtheking.odin.utils.lore
import com.odtheking.odin.utils.noControlCodes
import com.odtheking.odin.utils.render.textDim
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket

object BreakerDisplay : Module(
    name = "Breaker Display",
    description = "Displays the amount of charges left in your Dungeon Breaker"
) {
    private val chargesRegex = Regex("Charges: (\\d+)/(\\d+)⸕")
    private var charges = 0
    private var max = 0

    private val hud by HUD(name, "Shows the amount of charges left in your Dungeon Breaker.", false) {
        if (!it && (max == 0 || !DungeonUtils.inDungeons)) 0 to 0
        else textDim("§cCharges: §e${if (it) 17 else charges}§7/§e${if (it) 20 else max}§c⸕", 0, 0, Colors.WHITE)
    }

    init {
        onReceive<ClientboundContainerSetSlotPacket> {
            if (item?.itemId != "DUNGEONBREAKER" || !DungeonUtils.inDungeons) return@onReceive
            item?.lore?.firstNotNullOfOrNull { chargesRegex.find(it.string.noControlCodes) }?.let { match ->
                charges = match.groupValues[1].toIntOrNull() ?: 0
                max = match.groupValues[2].toIntOrNull() ?: 0
            }
        }
    }
}
