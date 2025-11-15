package com.odtheking.odin.features.impl.floor7.terminalhandler

import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

class PanesHandler: TerminalHandler(TerminalTypes.PANES) {

    override fun handleSlotUpdate(packet: ClientboundContainerSetSlotPacket): Boolean {
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