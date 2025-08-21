package com.odtheking.odin.features.impl.floor7.terminalhandler

import com.odtheking.odin.features.impl.floor7.TerminalTypes
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket

class PanesHandler: TerminalHandler(TerminalTypes.PANES) {

    override fun handleSlotUpdate(packet: ScreenHandlerSlotUpdateS2CPacket): Boolean {
        if (packet.slot != type.windowSize - 1) return false
        solution.clear()
        solution.addAll(solvePanes(items))
        return true
    }

    override fun simulateClick(slotIndex: Int, clickType: Int) {
        solution.removeAt(solution.indexOf(slotIndex).takeIf { it != -1 } ?: return)
    }

    private fun solvePanes(items: Array<ItemStack?>): List<Int> =
        items.mapIndexedNotNull { index, item -> if (item?.item == Items.RED_STAINED_GLASS_PANE.asItem()) index else null }
}