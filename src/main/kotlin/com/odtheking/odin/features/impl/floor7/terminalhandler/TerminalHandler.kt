package com.odtheking.odin.features.impl.floor7.terminalhandler

import com.google.common.primitives.Shorts
import com.google.common.primitives.SignedBytes
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.GuiEvent
import com.odtheking.odin.events.PacketEvent
import com.odtheking.odin.events.core.EventBus
import com.odtheking.odin.events.core.on
import com.odtheking.odin.events.core.onReceive
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
import kotlin.math.min

open class TerminalHandler(val type: TerminalTypes) {
    val solution: CopyOnWriteArrayList<Int> = CopyOnWriteArrayList()
    val timeOpened = System.currentTimeMillis()
    var isClicked = false
    var windowCount = 0

    init {
        @Suppress("LeakingThis")
        EventBus.subscribe(this)

        on<GuiEvent.SlotUpdate> {
            if (packet.slot !in 0 until type.windowSize) return@on
            handleSlotUpdate(packet, menu.items.subList(0, min(menu.items.size, type.windowSize)))
        }

        onReceive<ClientboundOpenScreenPacket> {
            isClicked = false
            windowCount++
        }
    }

    open fun handleSlotUpdate(packet: ClientboundContainerSetSlotPacket, items: List<ItemStack>): Boolean = false

    open fun simulateClick(slotIndex: Int, clickType: Int) {}

    open fun click(slotIndex: Int, button: Int, simulateClick: Boolean = true) {
        val screenHandler = (mc.screen as? ContainerScreen)?.menu ?: return
        if (simulateClick) simulateClick(slotIndex, button)
        isClicked = true

        if (mc.screen is TermSimGUI) {
            PacketEvent.Send(
                ServerboundContainerClickPacket(
                    -1, -1,
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