package com.odtheking.odin.utils.skyblock.dungeon.terminals

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.ChatPacketEvent
import com.odtheking.odin.events.GuiEvent
import com.odtheking.odin.events.TerminalEvent
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.core.EventPriority
import com.odtheking.odin.events.core.on
import com.odtheking.odin.events.core.onReceive
import com.odtheking.odin.events.core.onSend
import com.odtheking.odin.features.impl.floor7.TerminalSolver
import com.odtheking.odin.utils.skyblock.dungeon.terminals.terminalhandler.TerminalHandler
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.protocol.game.*
import net.minecraft.world.item.ItemStack

object TerminalUtils {

    private val termSolverRegex = Regex("^(.{1,16}) activated a terminal! \\((\\d)/(\\d)\\)$")
    private var lastClickTime = 0L

    @JvmStatic var currentTerm: TerminalHandler? = null
        private set
    var lastTermOpened: TerminalHandler? = null
        private set

    init {
        onReceive<ClientboundOpenScreenPacket> (EventPriority.HIGHEST) {
            val windowName = title.string ?: return@onReceive
            currentTerm?.let { if (!it.isClicked && it.windowCount <= 2) leftTerm() }
            val newType = TerminalTypes.entries.find { it.regex.matches(windowName) } ?: return@onReceive

            if (newType != currentTerm?.type) newType.openHandler(windowName)?.let {
                currentTerm = it
                TerminalEvent.Open(it).postAndCatch()
                lastTermOpened = it
            }
            currentTerm?.openScreen()
        }

        on<GuiEvent.SlotUpdate> {
            currentTerm?.updateSlot(this)
        }

        onReceive<ClientboundContainerClosePacket> { leftTerm() }
        onSend<ServerboundContainerClosePacket> { leftTerm() }

        onSend<ServerboundContainerClickPacket> {
            lastClickTime = System.currentTimeMillis()
            currentTerm?.isClicked = true
        }

        on<TickEvent.End> {
            if (System.currentTimeMillis() - lastClickTime >= TerminalSolver.terminalReloadThreshold && currentTerm?.isClicked == true) currentTerm?.let {
                val screen = (mc.screen as? AbstractContainerScreen<*>) ?: return@let
                GuiEvent.SlotUpdate(
                    screen,
                    ClientboundContainerSetSlotPacket(screen.menu.containerId, 0, 0, ItemStack.EMPTY),
                    screen.menu
                ).postAndCatch()
                it.isClicked = false
            }
        }

        on<ChatPacketEvent> {
            termSolverRegex.find(value)?.let { message ->
                if (message.groupValues[1] == mc.player?.name?.string) lastTermOpened?.let {
                    TerminalEvent.Solve(it).postAndCatch()
                }
            }
        }
    }

    private fun leftTerm() {
        currentTerm?.let {
            currentTerm = null
            TerminalEvent.Close(it).postAndCatch()
        }
    }
}