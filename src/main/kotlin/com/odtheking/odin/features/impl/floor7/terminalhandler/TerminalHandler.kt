package com.odtheking.odin.features.impl.floor7.terminalhandler

import com.odtheking.odin.OdinMod.EVENT_BUS
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.PacketEvent
import com.odtheking.odin.events.TerminalEvent
import com.odtheking.odin.utils.equalsOneOf
import meteordevelopment.orbit.EventHandler
import meteordevelopment.orbit.EventPriority
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.item.ItemStack
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket
import net.minecraft.screen.slot.SlotActionType
import java.util.concurrent.CopyOnWriteArrayList

open class TerminalHandler(val type: TerminalTypes) {
    val solution: CopyOnWriteArrayList<Int> = CopyOnWriteArrayList()
    val items: Array<ItemStack?> = arrayOfNulls(type.windowSize)
    val timeOpened = System.currentTimeMillis()
    var isClicked = false

    @EventHandler(priority = EventPriority.LOW)
    fun onPacketReceive(event: PacketEvent.Receive) = with (event.packet) {
        when (this) {
            is ScreenHandlerSlotUpdateS2CPacket -> {
                if (slot !in 0 until type.windowSize) return@with
                items[slot] = stack
                if (handleSlotUpdate(this)) TerminalEvent.Updated(this@TerminalHandler).postAndCatch()
            }
            is OpenScreenS2CPacket -> {
                isClicked = false
                items.fill(null)
            }
        }
    }

    init {
        @Suppress("LeakingThis")
        EVENT_BUS.subscribe(this)
    }

    open fun handleSlotUpdate(packet: ScreenHandlerSlotUpdateS2CPacket): Boolean = false

    open fun simulateClick(slotIndex: Int, clickType: Int) {}

    fun click(slotIndex: Int, button: Int, simulateClick: Boolean = true) {
        if (simulateClick) simulateClick(slotIndex, button)
        isClicked = true
        val screenHandler = (mc.currentScreen as? GenericContainerScreen)?.screenHandler ?: return
        mc.interactionManager?.clickSlot(screenHandler.syncId, slotIndex, button, SlotActionType.PICKUP, mc.player)
    //PlayerUtils.windowClick(slotIndex, clickType)
    }

    fun canClick(slotIndex: Int, button: Int, needed: Int = solution.count { it == slotIndex }): Boolean = when {
        type == TerminalTypes.MELODY -> slotIndex.equalsOneOf(16, 25, 34, 43)
        slotIndex !in solution -> false
        type == TerminalTypes.NUMBERS && slotIndex != solution.firstOrNull() -> false
        type == TerminalTypes.RUBIX && ((needed < 3 && button == 1) || (needed.equalsOneOf(3, 4) && button != 1)) -> false
        else -> true
    }
}