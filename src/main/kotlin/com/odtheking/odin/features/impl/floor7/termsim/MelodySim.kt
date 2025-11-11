package com.odtheking.odin.features.impl.floor7.termsim

import com.odtheking.odin.events.TerminalEvent
import com.odtheking.odin.features.impl.floor7.TerminalSolver
import com.odtheking.odin.features.impl.floor7.terminalhandler.TerminalTypes
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

object MelodySim : TermSimGUI(
    TerminalTypes.MELODY.windowName, TerminalTypes.MELODY.windowSize
) {
    private val magentaPane get() = ItemStack(Items.MAGENTA_STAINED_GLASS_PANE).apply { set(DataComponents.CUSTOM_NAME, Component.literal("")) }
    private val greenPane   get() = ItemStack(Items.LIME_STAINED_GLASS_PANE).apply { set(DataComponents.CUSTOM_NAME, Component.literal("")) }
    private val redPane     get() = ItemStack(Items.RED_STAINED_GLASS_PANE).apply { set(DataComponents.CUSTOM_NAME, Component.literal("")) }
    private val whitePane   get() = ItemStack(Items.WHITE_STAINED_GLASS_PANE).apply { set(DataComponents.CUSTOM_NAME, Component.literal("")) }
    private val redClay     get() = ItemStack(Items.RED_TERRACOTTA).apply { set(DataComponents.CUSTOM_NAME, Component.literal("")) }
    private val greenClay   get() = ItemStack(Items.LIME_TERRACOTTA).apply { set(DataComponents.CUSTOM_NAME, Component.literal("")) }

    private var magentaColumn = 1
    private var limeColumn = 2
    private var currentRow = 1
    private var limeDirection = 1

    override fun create() {
        currentRow = 1
        magentaColumn = (1..5).random()
        limeColumn = 1
        limeDirection = 1
        createNewGui { it.generateItemStack() }
    }

    private var counter = 0

    override fun containerTick() {
        if (counter++ % 10 != 0) return
        limeColumn += limeDirection
        if (limeColumn == 1 || limeColumn == 5) limeDirection *= -1
        updateGui()
        super.containerTick()
    }

    override fun slotClick(slot: Slot, button: Int) {
        if (slot.index % 9 != 7 || limeColumn != magentaColumn || slot.index / 9 != currentRow) return

        magentaColumn = (1 until 5).random()
        currentRow++
        updateGui()

        playTermSimSound()
        if (currentRow >= 5) TerminalSolver.lastTermOpened?.let { TerminalEvent.Solved(it).postAndCatch() }
    }

    private fun updateGui() {
        guiInventorySlots.forEachIndexed { index, currentStack ->
            currentStack?.setSlot(guiInventorySlots.map { it.generateItemStack() }.getOrNull(index)?.takeIf { it != currentStack.item } ?: return@forEachIndexed)
        }
    }

    private fun Slot.generateItemStack(): ItemStack {
        return when {
            index % 9 == magentaColumn && index / 9 !in 1..4 -> magentaPane
            index % 9 == limeColumn && index / 9 == currentRow -> greenPane
            index % 9 in 1..5 && index / 9 == currentRow -> redPane
            index % 9 == 7 && index / 9 == currentRow -> greenClay
            index % 9 == 7 && index / 9 in 1..4 -> redClay
            index % 9 in 1..5 && index / 9 in 1..4 -> whitePane
            else -> blackPane
        }
    }
}


