package com.odtheking.odin.features.impl.floor7.terminalhandler

import com.google.common.primitives.Shorts
import com.google.common.primitives.SignedBytes
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.PacketEvent
import com.odtheking.odin.events.TerminalEvent
import com.odtheking.odin.features.impl.floor7.termsim.TermSimGUI
import com.odtheking.odin.utils.clickSlot
import com.odtheking.odin.utils.equalsOneOf
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.minecraft.client.gui.screens.inventory.ContainerScreen
import net.minecraft.network.HashedStack
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.item.ItemStack
import org.lwjgl.glfw.GLFW
import java.util.concurrent.CopyOnWriteArrayList

open class TerminalHandler(val type: TerminalTypes) {
    val solution: CopyOnWriteArrayList<Int> = CopyOnWriteArrayList()
    val items: Array<ItemStack?> = arrayOfNulls(type.windowSize)
    val timeOpened = System.currentTimeMillis()
    var isClicked = false
    var syncId = -1

    fun setSlot(packet: ClientboundContainerSetSlotPacket) {
        if (packet.slot !in 0 until type.windowSize) return
        items[packet.slot] = packet.item
        if (handleSlotUpdate(packet)) TerminalEvent.Updated(this@TerminalHandler).postAndCatch()
    }

    fun openScreen(packet: ClientboundOpenScreenPacket) {
        syncId = packet.containerId
        isClicked = false
        items.fill(null)
    }

    open fun handleSlotUpdate(packet: ClientboundContainerSetSlotPacket): Boolean = false

    open fun simulateClick(slotIndex: Int, clickType: Int) {}

    open fun click(slotIndex: Int, button: Int, simulateClick: Boolean = true) {
        val screenHandler = (mc.screen as? ContainerScreen)?.menu ?: return
        if (simulateClick) simulateClick(slotIndex, button)
        isClicked = true

        if (mc.screen is TermSimGUI) {
            PacketEvent.Send(
                ServerboundContainerClickPacket(
                    screenHandler.containerId, mc.player?.containerMenu?.stateId ?: 0,
                    Shorts.checkedCast(slotIndex.toLong()), SignedBytes.checkedCast(button.toLong()),
                    if (button == GLFW.GLFW_MOUSE_BUTTON_3) ClickType.CLONE else ClickType.PICKUP,
                    Int2ObjectOpenHashMap(), HashedStack.EMPTY
                )
            ).postAndCatch()
            return
        }
        mc.player?.clickSlot(screenHandler.containerId, slotIndex, button, if (button == GLFW.GLFW_MOUSE_BUTTON_3) ClickType.CLONE else ClickType.PICKUP)
    }

    fun canClick(slotIndex: Int, button: Int, needed: Int = solution.count { it == slotIndex }): Boolean = when {
        type == TerminalTypes.MELODY -> slotIndex.equalsOneOf(16, 25, 34, 43)
        slotIndex !in solution -> false
        type == TerminalTypes.NUMBERS && slotIndex != solution.firstOrNull() -> false
        type == TerminalTypes.RUBIX && ((needed < 3 && button == 1) || (needed.equalsOneOf(3, 4) && button != 1)) -> false
        else -> true
    }
}