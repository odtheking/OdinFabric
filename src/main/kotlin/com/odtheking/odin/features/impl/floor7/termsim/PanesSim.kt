package com.odtheking.odin.features.impl.floor7.termsim

import com.odtheking.odin.events.TerminalEvent
import com.odtheking.odin.features.impl.floor7.TerminalSolver
import com.odtheking.odin.features.impl.floor7.TerminalTypes
import net.minecraft.component.DataComponentTypes
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.screen.slot.Slot
import net.minecraft.text.Text
import kotlin.math.floor

object PanesSim : TermSimGUI(
    TerminalTypes.PANES.windowName, TerminalTypes.PANES.windowSize
) {
    private val greenPane get() = ItemStack(Items.LIME_STAINED_GLASS_PANE).apply { set(DataComponentTypes.CUSTOM_NAME, Text.literal("")) }
    private val redPane   get() = ItemStack(Items.RED_STAINED_GLASS_PANE).apply { set(DataComponentTypes.CUSTOM_NAME, Text.literal("")) }

    override fun create() {
        createNewGui {
            if (floor(it.index / 9f) in 1f..3f && it.index % 9 in 2..6) if (Math.random() > 0.75) greenPane else redPane else blackPane
        }
    }

    override fun slotClick(slot: Slot, button: Int) {
        createNewGui { if (it == slot) { if (slot.stack?.item == Items.RED_STAINED_GLASS_PANE) greenPane else redPane } else it.stack }

        playTermSimSound()
        if (guiInventorySlots.none { it?.stack?.item == Items.RED_STAINED_GLASS_PANE })
            TerminalSolver.lastTermOpened?.let { TerminalEvent.Solved(it).postAndCatch() }
    }
}