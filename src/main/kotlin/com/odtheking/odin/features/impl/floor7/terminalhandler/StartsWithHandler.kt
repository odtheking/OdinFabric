package com.odtheking.odin.features.impl.floor7.terminalhandler

import com.odtheking.odin.features.impl.floor7.TerminalTypes
import net.minecraft.item.ItemStack
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket

class StartsWithHandler(private val letter: String): TerminalHandler(TerminalTypes.STARTS_WITH) {

    override fun handleSlotUpdate(packet: ScreenHandlerSlotUpdateS2CPacket): Boolean {
        if (packet.slot != type.windowSize - 1) return false
        solution.clear()
        solution.addAll(solveStartsWith(items, letter))
        return true
    }

    override fun simulateClick(slotIndex: Int, clickType: Int) {
        solution.removeAt(solution.indexOf(slotIndex).takeIf { it != -1 } ?: return)
    }

    private fun solveStartsWith(items: Array<ItemStack?>, letter: String): List<Int> =
        items.mapIndexedNotNull { index, item -> if (item?.name?.string?.startsWith(letter, true) == true && !item.hasGlint()) index else null }
}