package com.odtheking.odin.features.impl.floor7.terminalhandler

import com.odtheking.odin.features.impl.floor7.TerminalTypes
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket

class MelodyHandler: TerminalHandler(TerminalTypes.MELODY) {

    override fun handleSlotUpdate(packet: ScreenHandlerSlotUpdateS2CPacket): Boolean {
        return packet.stack?.let {
            solution.clear()
            solution.addAll(solveMelody(items))
        } != null
    }

    private fun solveMelody(items: Array<ItemStack?>): List<Int> {
        val greenPane = items.indexOfLast { it?.item == Items.LIME_STAINED_GLASS_PANE }.takeIf { it != -1 } ?: return emptyList()
        val magentaPane = items.indexOfFirst { it?.item == Items.MAGENTA_STAINED_GLASS_PANE }.takeIf { it != -1 } ?: return emptyList()
        val greenClay = items.indexOfLast { it?.item == Items.LIME_TERRACOTTA }.takeIf { it != -1 } ?: return emptyList()
        return items.mapIndexedNotNull { index, item ->
            when {
                index == greenPane || item?.item == Items.MAGENTA_STAINED_GLASS_PANE -> index
                index == greenClay && greenPane % 9 == magentaPane % 9 -> index
                else -> null
            }
        }
    }
}