package com.odtheking.odin.features.impl.floor7.terminalhandler

import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

class MelodyHandler: TerminalHandler(TerminalTypes.MELODY) {

    override fun handleSlotUpdate(packet: ClientboundContainerSetSlotPacket): Boolean {
        return packet.item?.let {
            val newSolution = solveMelody(items)
            if (newSolution.isNotEmpty()) {
                solution.clear()
                solution.addAll(newSolution)
            }
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