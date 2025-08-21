package me.odinmain.features.impl.floor7.p3.termsim

import com.odtheking.odin.events.TerminalEvent
import com.odtheking.odin.features.impl.floor7.TerminalSolver
import com.odtheking.odin.features.impl.floor7.terminalhandler.TerminalTypes
import com.odtheking.odin.features.impl.floor7.termsim.TermSimGUI
import com.odtheking.odin.utils.modMessage
import net.minecraft.component.DataComponentTypes
import net.minecraft.item.BlockItem
import net.minecraft.item.DyeItem
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
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
    private val items: List<Item> = listOf(
        Registries.ITEM.get(Identifier.of("minecraft", "${color.name.lowercase()}_stained_glass")),
        Registries.ITEM.get(Identifier.of("minecraft", "${color.name.lowercase()}_wool")),
        Registries.ITEM.get(Identifier.of("minecraft", "${color.name.lowercase()}_concrete")),
        Registries.ITEM.get(Identifier.of("minecraft", "${color.name.lowercase()}_dye"))
    )

    override fun create() {
        val guaranteed = (10..16).plus(19..25).plus(28..34).plus(37..43).random()
        createNewGui { slot ->
            if (floor(slot.index / 9.0) in 1.0..4.0 && slot.index % 9 in 1..7) {
                val item = items.random()

                if (slot.index == guaranteed) ItemStack(item)
                else {
                    if (Math.random() > 0.75) ItemStack(item)
                    else {
                        val wrongColor = DyeColor.entries.filter { it != color }.random()
                        val wrongItemId = when (item) {
                            is DyeItem -> Identifier.of("minecraft", "${wrongColor.name.lowercase()}_dye")
                            is BlockItem -> {
                                val id = Registries.ITEM.getId(item)
                                Identifier.of(id.namespace, id.path.replace(color.name.lowercase(), wrongColor.name.lowercase()))
                            }
                            else -> Registries.ITEM.getId(item)
                        }
                        ItemStack(Registries.ITEM.get(wrongItemId))
                    }
                }
            } else blackPane
        }
    }

    override fun slotClick(slot: Slot, button: Int) {
        val stack = slot.stack ?: return
        if (!items.contains(stack.item)) return modMessage("§cThat item is not: ${color.name.uppercase()}!")

        createNewGui {
            if (it == slot) {
                stack.apply { set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true) }
            } else it.stack
        }

        playTermSimSound()

        if (guiInventorySlots.none { it?.stack?.hasGlint() == false && items.contains(it.stack?.item) })
            TerminalSolver.lastTermOpened?.let { TerminalEvent.Solved(it).postAndCatch() }
    }
}
