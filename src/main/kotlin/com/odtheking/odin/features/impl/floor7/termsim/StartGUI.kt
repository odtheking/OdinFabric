package com.odtheking.odin.features.impl.floor7.termsim

import com.odtheking.odin.features.impl.floor7.TerminalSimulator
import com.odtheking.odin.features.impl.floor7.TerminalSimulator.openRandomTerminal
import com.odtheking.odin.utils.handlers.LimitedTickTask
import com.odtheking.odin.utils.modMessage
import net.minecraft.component.DataComponentTypes
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.screen.slot.Slot
import net.minecraft.text.Text

object StartGUI : TermSimGUI(
    "Terminal Simulator", 27
) {
    private val termItems = listOf(
        ItemStack(Items.LIME_DYE).apply { set(DataComponentTypes.CUSTOM_NAME, Text.literal("§aCorrect all the panes!")) },
        ItemStack(Items.RED_DYE).apply { set(DataComponentTypes.CUSTOM_NAME, Text.literal("§6Change all to same color!")) },
        ItemStack(Items.CYAN_DYE).apply { set(DataComponentTypes.CUSTOM_NAME, Text.literal("§3Click in order!")) },
        ItemStack(Items.PINK_DYE).apply { set(DataComponentTypes.CUSTOM_NAME, Text.literal("§5What starts with: \"*\"?")) },
        ItemStack(Items.BROWN_DYE).apply { set(DataComponentTypes.CUSTOM_NAME, Text.literal("§bSelect all the \"*\" items!")) },
        ItemStack(Items.PURPLE_DYE).apply { set(DataComponentTypes.CUSTOM_NAME, Text.literal("§dClick the button on time!")) }
    )
    private val resetButton = ItemStack(Items.BLACK_DYE).apply { set(DataComponentTypes.CUSTOM_NAME, Text.literal("§cReset PBs!")) }
    private val randomButton = ItemStack(Items.WHITE_DYE).apply { set(DataComponentTypes.CUSTOM_NAME, Text.literal("§7Random")) }

    override fun create() {
        createNewGui {
            when (it.index) {
                4 -> resetButton
                13  -> randomButton
                in 10..12 -> termItems[it.index - 10]
                in 14..16 -> termItems[it.index - 11]
                else -> blackPane
            }
        }
    }

//    @SubscribeEvent
//    fun onTooltip(event: ItemTooltipEvent) {
//        if (event.itemStack.item != dye || event.toolTip.isEmpty()) return
//        val index = termItems.indexOfFirst { it.displayName == event.itemStack?.displayName }.takeIf { it != -1 } ?: return
//        event.toolTip.add(1, "§7Personal Best: §d${TerminalSimulator.termSimPBs.pb?.get(index)?.round(2) ?: 999.0}")
//    }

    private var areYouSure = false

    override fun slotClick(slot: Slot, button: Int) {
        when (slot.index) {
            4 -> {
                if (!areYouSure) {
                    modMessage("§cAre you sure you want to reset your PBs? Click again to confirm.")
                    areYouSure = true
                    LimitedTickTask(60, 1) {
                        modMessage("§aPBs reset cancelled.")
                        areYouSure = false
                    }
                    return
                }
                repeat(6) { i -> TerminalSimulator.termSimPBs.set(i, 9999f) }
                modMessage("§cPBs reset!")
            }
            10 -> PanesSim.open(ping)
            11 -> RubixSim.open(ping)
            12 -> NumbersSim.open(ping)
            13 -> openRandomTerminal(ping)
            14 -> StartsWithSim().open(ping)
            15 -> SelectAllSim().open(ping)
            16 -> MelodySim.open(ping)
        }
    }
}