package com.odtheking.odin.features.impl.floor7.termsim

import com.odtheking.odin.events.TerminalEvent
import com.odtheking.odin.features.impl.floor7.TerminalSolver
import com.odtheking.odin.features.impl.floor7.terminalhandler.TerminalTypes
import com.odtheking.odin.utils.hasGlint
import com.odtheking.odin.utils.modMessage
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import kotlin.math.floor

class StartsWithSim(private val letter: String = listOf("A", "B", "C", "G", "D", "M", "N", "R", "S", "T", "W").random()) : TermSimGUI(
    "What starts with: \'$letter\'?",
    TerminalTypes.STARTS_WITH.windowSize
) {
    override fun create() {
        createNewGui {
            when {
                floor(it.index / 9f) !in 1f..3f || it.index % 9 !in 1..7 -> blackPane
                it.index == (10..16).random() -> getLetterItemStack()
                Math.random() > .7f -> getLetterItemStack()
                else -> getLetterItemStack(true)
            }
        }
    }

    override fun slotClick(slot: Slot, button: Int) = with(slot.item) {
        if (hoverName.string?.startsWith(letter, true) == false || hasGlint()) return@with modMessage("§cThat item does not start with: \'$letter\'!")

        createNewGui { if (it == slot) apply { set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, false) } else it.item }
        playTermSimSound()
        if (guiInventorySlots.none { it?.item?.hoverName?.string?.startsWith(letter, true) == true && !it.item.hasGlint() })
            TerminalSolver.lastTermOpened?.let { TerminalEvent.Solved(it).postAndCatch() }
    }

    private fun getLetterItemStack(filterNot: Boolean = false): ItemStack {
        val matchingItem = BuiltInRegistries.ITEM
            .filter { item ->
                val id = item?.name?.string ?: return@filter false
                id.startsWith(letter, true) != filterNot && !id.contains("pane", true)
            }.randomOrNull() ?: return ItemStack.EMPTY

        return ItemStack(matchingItem)
    }
}