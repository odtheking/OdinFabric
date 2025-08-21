package com.odtheking.odin.features.impl.floor7.termsim

import com.odtheking.odin.events.TerminalEvent
import com.odtheking.odin.features.impl.floor7.TerminalSolver
import com.odtheking.odin.features.impl.floor7.TerminalTypes
import net.minecraft.block.StainedGlassPaneBlock
import net.minecraft.component.DataComponentTypes
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.screen.slot.Slot
import net.minecraft.text.Text
import net.minecraft.util.DyeColor
import kotlin.math.floor

object RubixSim : TermSimGUI(
    TerminalTypes.RUBIX.windowName, TerminalTypes.RUBIX.windowSize
) {
    private val order = listOf(DyeColor.ORANGE, DyeColor.YELLOW, DyeColor.GREEN, DyeColor.BLUE, DyeColor.RED)
    private val panes = listOf(Items.ORANGE_STAINED_GLASS_PANE, Items.YELLOW_STAINED_GLASS_PANE, Items.GREEN_STAINED_GLASS_PANE, Items.BLUE_STAINED_GLASS_PANE, Items.RED_STAINED_GLASS_PANE)
    private val indices = listOf(12, 13, 14, 21, 22, 23, 30, 31, 32)

    override fun create() {
        createNewGui {
            if (floor(it.index / 9f) in 1f..3f && it.index % 9 in 3..5) getPane()
            else blackPane
        }
    }

    override fun slotClick(slot: Slot, button: Int) {
        val current = order.find { it == ((slot.stack?.item as? BlockItem)?.block as? StainedGlassPaneBlock)?.color } ?: return
        createNewGui {
            if (it == slot) {
                if (button == 1) genStack(order.indexOf(current) - 1)
                else genStack((order.indexOf(current) + 1) % order.size)
            } else it.stack ?: blackPane
        }

        playTermSimSound()
        if (indices.all { guiInventorySlots[it]?.stack?.item == guiInventorySlots[12]?.stack?.item })
            TerminalSolver.lastTermOpened?.let { TerminalEvent.Solved(it).postAndCatch() }
    }

    private fun getPane(): ItemStack {
        return when (Math.random()) {
            in 0.0..0.2 -> genStack(0)
            in 0.2..0.4 -> genStack(1)
            in 0.4..0.6 -> genStack(2)
            in 0.6..0.8 -> genStack(3)
            else -> genStack(4)
        }
    }

    private fun genStack(meta: Int): ItemStack =
        ItemStack(panes[if (meta in panes.indices) meta else panes.lastIndex]).apply { set(DataComponentTypes.CUSTOM_NAME, Text.literal("")) }
}