package com.odtheking.odin.features.impl.floor7.terminalhandler

import com.odtheking.odin.utils.hasGlint
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
import net.minecraft.world.item.ItemStack

class StartsWithHandler(private val letter: String): TerminalHandler(TerminalTypes.STARTS_WITH) {

    private val clickedSlots = mutableSetOf<Int>()

    override fun handleSlotUpdate(packet: ClientboundContainerSetSlotPacket): Boolean {
        if (packet.slot != type.windowSize - 1) return false
        solution.clear()
        solution.addAll(solveStartsWith(items, letter))
        return true
    }

    override fun click(slotIndex: Int, button: Int, simulateClick: Boolean) {
        if (canClick(slotIndex, button) && !isClicked) clickedSlots.add(slotIndex)
        super.click(slotIndex, button, simulateClick)
    }

    override fun simulateClick(slotIndex: Int, clickType: Int) {
        solution.removeAt(solution.indexOf(slotIndex).takeIf { it != -1 } ?: return)
    }

    private fun solveStartsWith(items: Array<ItemStack?>, letter: String): List<Int> =
        items.mapIndexedNotNull { index, item ->
            if (item?.hoverName?.string?.startsWith(letter, true) == true && !item.hasGlint() && index !in clickedSlots) index else null
        }
}