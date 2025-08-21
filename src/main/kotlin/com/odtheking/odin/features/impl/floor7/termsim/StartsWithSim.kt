package com.odtheking.odin.features.impl.floor7.termsim

import com.odtheking.odin.events.TerminalEvent
import com.odtheking.odin.features.impl.floor7.TerminalSolver
import com.odtheking.odin.features.impl.floor7.TerminalTypes
import com.odtheking.odin.utils.modMessage
import net.minecraft.component.DataComponentTypes
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.screen.slot.Slot
import kotlin.math.floor

class StartsWithSim(private val letter: String = listOf("A", "B", "C", "G", "D", "M", "N", "R", "S", "T").random()) : TermSimGUI(
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

    override fun slotClick(slot: Slot, button: Int) = with(slot.stack) {
        if (name.string?.startsWith(letter, true) == false || hasGlint()) return modMessage("§cThat item does not start with: \'$letter\'!")

        createNewGui { if (it == slot && it.stack != null) ItemStack(item).apply { set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true) } else it.stack }
        playTermSimSound()
        if (guiInventorySlots.none { it?.stack?.name?.string?.startsWith(letter, true) == true && !it.stack.hasGlint() })
            TerminalSolver.lastTermOpened?.let { TerminalEvent.Solved(it).postAndCatch() }
    }

    private fun getLetterItemStack(filterNot: Boolean = false): ItemStack {
        val matchingItem = Registries.ITEM
            .filter { item ->
                val id = Registries.ITEM.getId(item).path // e.g. "blue_wool"
                id.startsWith(letter, ignoreCase = true) != filterNot && !id.contains("pane", ignoreCase = true)
            }.random()

        return ItemStack(matchingItem)
    }
}