package com.odtheking.odin.features.impl.floor7.terminalhandler

import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

class MelodyHandler: TerminalHandler(TerminalTypes.MELODY) {

    override fun handleSlotUpdate(packet: ClientboundContainerSetSlotPacket, items: List<ItemStack>): Boolean {
        return packet.item?.let {
            val newSolution = solveMelody(items)
            if (newSolution.isNotEmpty()) {
                solution.clear()
                solution.addAll(newSolution)
            }
        } != null
    }

    private fun solveMelody(items: List<ItemStack>): List<Int> {
        val greenPane = items.indexOfLast { it.item == Items.LIME_STAINED_GLASS_PANE }
        val magentaPane = items.indexOfFirst { it.item == Items.MAGENTA_STAINED_GLASS_PANE }
        val greenClay = items.indexOfLast { it.item == Items.LIME_TERRACOTTA }
        return items.mapIndexedNotNull { index, item ->
            when {
                index == greenPane || item.item == Items.MAGENTA_STAINED_GLASS_PANE -> index
                index == greenClay && greenPane % 9 == magentaPane % 9 -> index
                else -> null
            }
        }
    }
}