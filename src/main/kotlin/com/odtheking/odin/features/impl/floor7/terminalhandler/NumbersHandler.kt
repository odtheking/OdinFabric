package com.odtheking.odin.features.impl.floor7.terminalhandler

import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket

class NumbersHandler: TerminalHandler(TerminalTypes.NUMBERS) {

    override fun handleSlotUpdate(packet: ScreenHandlerSlotUpdateS2CPacket): Boolean {
        if (packet.slot != type.windowSize - 1) return false
        solution.clear()
        solution.addAll(solveNumbers(items))
        return true
    }

    override fun simulateClick(slotIndex: Int, clickType: Int) {
        if (solution.indexOf(slotIndex) == 0) solution.removeAt(0)
    }

    private fun solveNumbers(items: Array<ItemStack?>): List<Int> {
        return items.mapIndexedNotNull { index, item ->
            if (item?.item == Items.RED_STAINED_GLASS_PANE.asItem()) index else null
        }.sortedBy { items[it]?.count }
    }
}