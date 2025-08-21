package com.odtheking.odin.features.impl.floor7.terminalhandler

import com.odtheking.odin.features.impl.floor7.TerminalTypes
import net.minecraft.block.StainedGlassPaneBlock
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket
import net.minecraft.util.DyeColor

class RubixHandler : TerminalHandler(TerminalTypes.RUBIX) {

    override fun handleSlotUpdate(packet: ScreenHandlerSlotUpdateS2CPacket): Boolean {
        if (items.lastOrNull() == null || packet.slot != type.windowSize - 1) return false
        solution.clear()
        solution.addAll(solveRubix(items))
        return true
    }

    override fun simulateClick(slotIndex: Int, clickType: Int) {
        if (slotIndex !in solution) return
        if (clickType == 1) solution.add(slotIndex)
        else solution.remove(slotIndex)
    }

    private val rubixColorOrder = listOf(DyeColor.ORANGE, DyeColor.YELLOW, DyeColor.GREEN, DyeColor.BLUE, DyeColor.RED)
    private var lastRubixSolution: DyeColor? = null

    private fun solveRubix(items: Array<ItemStack?>): List<Int> {
        val panes = items.mapNotNull { item ->
            if (((item?.item as? BlockItem)?.block as? StainedGlassPaneBlock)?.color != DyeColor.BLACK) return@mapNotNull item
            null
        }

        var temp: List<Int> = List(100) { i -> i }

        if (lastRubixSolution != null) {
            val lastIndex = rubixColorOrder.indexOf(lastRubixSolution)
            temp = panes.flatMap { pane ->
                val paneDye = ((pane.item as? BlockItem)?.block as? StainedGlassPaneBlock)?.color ?: return@flatMap emptyList()
                val paneIdx = rubixColorOrder.indexOf(paneDye)
                if (paneIdx != lastIndex) List(dist(paneIdx, lastIndex)) { pane } else emptyList()
            }.map { items.indexOf(it) }
        } else {
            for (color in rubixColorOrder) {
                val goalIndex = rubixColorOrder.indexOf(color)
                val temp2 = panes.flatMap { pane ->
                    val paneDye = ((pane.item as? BlockItem)?.block as? StainedGlassPaneBlock)?.color ?: return@flatMap emptyList()
                    val paneIdx = rubixColorOrder.indexOf(paneDye)
                    if (paneIdx != goalIndex) List(dist(paneIdx, goalIndex)) { pane } else emptyList()
                }.map { items.indexOf(it) }
                if (getRealSize(temp2) < getRealSize(temp)) {
                    temp = temp2
                    lastRubixSolution = color
                }
            }
        }
        return temp
    }

    private fun getRealSize(list: List<Int>): Int {
        var size = 0
        list.distinct().forEach { pane ->
            val count = list.count { it == pane }
            size += if (count >= 3) 5 - count else count
        }
        return size
    }

    private fun dist(pane: Int, most: Int): Int =
        if (pane > most) (most + rubixColorOrder.size) - pane else most - pane
}
