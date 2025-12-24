package com.odtheking.odin.features.impl.floor7.terminalhandler

import com.odtheking.odin.utils.equalsOneOf
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

class NumbersHandler: TerminalHandler(TerminalTypes.NUMBERS) {

    override fun handleSlotUpdate(packet: ClientboundContainerSetSlotPacket, items: List<ItemStack>): Boolean {
        if (items.count { it.equalsOneOf(Items.RED_STAINED_GLASS_PANE, Items.LIME_STAINED_GLASS_PANE) } == 14) return false
        solution.clear()
        solution.addAll(solveNumbers(items))
        return true
    }

    override fun simulateClick(slotIndex: Int, clickType: Int) {
        if (solution.indexOf(slotIndex) == 0) solution.removeAt(0)
    }

    private fun solveNumbers(items: List<ItemStack>): List<Int> {
        return items.mapIndexedNotNull { index, item ->
            if (item.item == Items.RED_STAINED_GLASS_PANE) index else null
        }.sortedBy { items[it].count }
    }
}