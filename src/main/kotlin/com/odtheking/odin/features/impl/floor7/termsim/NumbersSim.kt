package com.odtheking.odin.features.impl.floor7.termsim

import com.odtheking.odin.events.TerminalEvent
import com.odtheking.odin.features.impl.floor7.TerminalSolver
import com.odtheking.odin.features.impl.floor7.terminalhandler.TerminalTypes
import net.minecraft.component.DataComponentTypes
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.screen.slot.Slot
import net.minecraft.text.Text
import kotlin.math.floor

object NumbersSim : TermSimGUI(
    TerminalTypes.NUMBERS.windowName, TerminalTypes.NUMBERS.windowSize
) {
    override fun create() {
        val used = (1..14).shuffled().toMutableList()
        createNewGui {
            if (floor(it.index / 9f) in 1f..2f && it.index % 9 in 1..7) ItemStack(Items.RED_STAINED_GLASS_PANE, used.removeFirst()).apply { set(DataComponentTypes.CUSTOM_NAME, Text.literal("")) }
            else blackPane
        }
    }

    override fun slotClick(slot: Slot, button: Int) {
        if (guiInventorySlots.minByOrNull { if (it?.stack?.item == Items.RED_STAINED_GLASS_PANE) it.stack?.count ?: 999 else 1000 } != slot) return
        createNewGui {
            if (it == slot) ItemStack(Items.LIME_STAINED_GLASS_PANE, slot.stack.count).apply { set(DataComponentTypes.CUSTOM_NAME, Text.literal("")) }
            else it.stack ?: blackPane
        }
        playTermSimSound()
        if (guiInventorySlots.none { it?.stack?.item == Items.RED_STAINED_GLASS_PANE })
            TerminalSolver.lastTermOpened?.let { TerminalEvent.Solved(it).postAndCatch() }
    }
}