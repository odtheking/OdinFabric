package com.odtheking.odin.features.impl.floor7.terminalhandler

import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
import net.minecraft.world.item.ItemStack

class StartsWithHandler(private val letter: String): TerminalHandler(TerminalTypes.STARTS_WITH) {

    private val clickedSlots = mutableSetOf<Int>()
    private var lastContainerId = -1

    override fun handleSlotUpdate(packet: ClientboundContainerSetSlotPacket): Boolean {
        if (packet.slot != type.windowSize - 1) return false
        solution.clear()
        solution.addAll(solveStartsWith(items, letter))
        return true
    }

    override fun onClick(slotIndex: Int, button: Int) {
        if (canClick(slotIndex, button) && lastContainerId != containerId) {
            clickedSlots.add(slotIndex)
            lastContainerId = containerId
        }
        super.onClick(slotIndex, button)
    }

    override fun simulateClick(slotIndex: Int, clickType: Int) {
        solution.removeAt(solution.indexOf(slotIndex).takeIf { it != -1 } ?: return)
    }

    private fun solveStartsWith(items: Array<ItemStack?>, letter: String): List<Int> =
        items.mapIndexedNotNull { index, item ->
            if (item?.hoverName?.string?.startsWith(letter, true) == true && index !in clickedSlots) index else null
        }
}