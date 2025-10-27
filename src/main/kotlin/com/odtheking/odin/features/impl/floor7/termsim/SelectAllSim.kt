package com.odtheking.odin.features.impl.floor7.termsim

import com.odtheking.odin.events.TerminalEvent
import com.odtheking.odin.features.impl.floor7.TerminalSolver
import com.odtheking.odin.features.impl.floor7.terminalhandler.TerminalTypes
import com.odtheking.odin.utils.modMessage
import net.minecraft.component.DataComponentTypes
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.registry.Registries
import net.minecraft.screen.slot.Slot
import net.minecraft.util.DyeColor
import net.minecraft.util.Identifier
import kotlin.math.floor

class SelectAllSim(
    private val color: DyeColor = DyeColor.entries.random()
) : TermSimGUI(
    "Select all the ${color.name.replace("_", " ")} items!",
    TerminalTypes.SELECT.windowSize
) {
    override fun create() {
        val guaranteed = (10..16).plus(19..25).plus(28..34).plus(37..43).random()
        createNewGui { slot ->
            if (floor(slot.index / 9.0) in 1.0..4.0 && slot.index % 9 in 1..7) {
                val item = ItemStack(getPossibleItems(color).random())

                if (slot.index == guaranteed) item
                else {
                    if (Math.random() > 0.75) item
                    else ItemStack(getPossibleItems(DyeColor.entries.filter { it != color }.random()).random())
                }
            } else blackPane
        }
    }

    private fun getPossibleItems(color: DyeColor): List<Item> {
        return listOf(
            Registries.ITEM.get(Identifier.of("minecraft", "${color.name.lowercase()}_stained_glass")),
            Registries.ITEM.get(Identifier.of("minecraft", "${color.name.lowercase()}_wool")),
            Registries.ITEM.get(Identifier.of("minecraft", "${color.name.lowercase()}_concrete")),
            when (color) {
                DyeColor.WHITE -> Items.BONE_MEAL
                DyeColor.BLUE -> Items.LAPIS_LAZULI
                DyeColor.BLACK -> Items.INK_SAC
                DyeColor.BROWN -> Items.COCOA_BEANS
                else -> Registries.ITEM.get(Identifier.of("minecraft", "${color.name.lowercase()}_dye"))
            }
        )
    }

    override fun slotClick(slot: Slot, button: Int) {
        val stack = slot.stack ?: return
        val possibleItems = getPossibleItems(color)
        if (!possibleItems.contains(stack.item)) return modMessage("§cThat item is not: ${color.name.uppercase()}!")

        createNewGui {
            if (it == slot) {
                stack.apply { set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true) }
            } else it.stack
        }

        playTermSimSound()

        if (guiInventorySlots.none { it?.stack?.hasGlint() == false && possibleItems.contains(it.stack?.item) })
            TerminalSolver.lastTermOpened?.let { TerminalEvent.Solved(it).postAndCatch() }
    }
}
