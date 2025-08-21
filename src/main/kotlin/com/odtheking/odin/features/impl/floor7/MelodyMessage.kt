package com.odtheking.odin.features.impl.floor7

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.StringSetting
import com.odtheking.odin.events.TerminalEvent
import com.odtheking.odin.features.Module
import com.odtheking.odin.features.impl.floor7.terminalhandler.TerminalTypes
import com.odtheking.odin.features.impl.floor7.termsim.TermSimGUI
import com.odtheking.odin.utils.handlers.TickTask
import com.odtheking.odin.utils.sendCommand
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.M7Phases
import meteordevelopment.orbit.EventHandler
import net.minecraft.item.Items

object MelodyMessage : Module(
    name = "Melody Message",
    description = "Helpful messages for the melody terminal in floor 7."
) {
    private val sendMelodyMessage by BooleanSetting("Send Melody Message", true, desc = "Sends a message when the melody terminal opens.")
    private val melodyMessage by StringSetting("Melody Message", "Melody Terminal start!", 128, desc = "Message sent when the melody terminal opens.").withDependency { sendMelodyMessage }
    private val melodyProgress by BooleanSetting("Melody Progress", false, desc = "Tells the party about melody terminal progress.")
    private val melodySendCoords by BooleanSetting("Melody Send Coords", false, desc = "Sends the coordinates of the melody terminal.").withDependency { melodyProgress }

    private var claySlots = hashMapOf(25 to "Melody 25%", 34 to "Melody 50%", 43 to "Melody 75%")

    @EventHandler
    fun onTermLoad(event: TerminalEvent.Opened) {
        if (DungeonUtils.getF7Phase() != M7Phases.P3 || event.terminal.type != TerminalTypes.MELODY || mc.currentScreen is TermSimGUI) return
        if (sendMelodyMessage) sendCommand("pc $melodyMessage")
        if (melodySendCoords) sendCommand("od sendcoords")

        claySlots = hashMapOf(25 to "Melody 25%", 34 to "Melody 50%", 43 to "Melody 75%")
    }

    init {
        TickTask(5) {
            if (DungeonUtils.getF7Phase() != M7Phases.P3 || TerminalSolver.currentTerm?.type != TerminalTypes.MELODY || mc.currentScreen is TermSimGUI || !melodyProgress) return@TickTask

            val greenClayIndices = claySlots.keys.filter { index -> TerminalSolver.currentTerm?.items?.get(index) == Items.GREEN_TERRACOTTA }.ifEmpty { return@TickTask }

            sendCommand("pc ${claySlots[greenClayIndices.last()] ?: return@TickTask}")
            greenClayIndices.forEach { claySlots.remove(it) }
        }
    }
}