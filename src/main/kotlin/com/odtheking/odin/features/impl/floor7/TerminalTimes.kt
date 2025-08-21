package com.odtheking.odin.features.impl.floor7

import com.odtheking.odin.clickgui.settings.impl.ActionSetting
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.MapSetting
import com.odtheking.odin.events.PacketEvent
import com.odtheking.odin.events.TerminalEvent
import com.odtheking.odin.events.WorldLoadEvent
import com.odtheking.odin.features.Module
import com.odtheking.odin.features.impl.floor7.termsim.TermSimGUI
import com.odtheking.odin.utils.PersonalBest
import com.odtheking.odin.utils.modMessage
import meteordevelopment.orbit.EventHandler
import net.minecraft.network.packet.s2c.common.CommonPingS2CPacket
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket

object TerminalTimes : Module(
    name = "Terminal Times",
    description = "Records the time taken to complete terminals in floor 7."
) {
    private val sendMessage by BooleanSetting("Send Message", false, desc = "Send a message when a terminal is completed.")
    private val reset by ActionSetting("Reset pbs", desc = "Resets the terminal PBs.") {
        repeat(6) { i -> terminalPBs.set(i, 9999f) }
        modMessage("§6Terminal PBs §fhave been reset.")
    }

    private val terminalSplits by BooleanSetting("Terminal Splits", true, desc = "Adds the time when a term was completed to its message, and sends the total term time after terms are done.")
    private val useRealTime by BooleanSetting("Use Real Time", true, desc = "Use real time rather than server ticks.")

    private val terminalPBs = PersonalBest(+MapSetting("TerminalPBs", mutableMapOf<Int, Float>()))

    @EventHandler
    fun onTerminalSolved(event: TerminalEvent.Solved) {
        val pbs = if (mc.currentScreen is TermSimGUI) TerminalSimulator.termSimPBs else terminalPBs
        pbs.time(event.terminal.type.ordinal, (System.currentTimeMillis() - event.terminal.timeOpened) / 1000f, "s§7!", "§a${event.terminal.type.windowName}${if (mc.currentScreen is TermSimGUI) " §7(termsim)" else ""} §7solved in §6", sendOnlyPB = sendMessage)
    }

    private val terminalCompleteRegex = Regex("^(.{1,16}) (activated|completed) a (terminal|lever|device)! \\((\\d)/(\\d)\\)$")
    private val gateDestroyedRegex = Regex("The gate has been destroyed!")
    private val goldorRegex = Regex("^\\[BOSS] Goldor: Who dares trespass into my domain\\?$")
    private val coreOpeningRegex = Regex("^The Core entrance is opening!$")

    private var completed: Pair<Int, Int> = Pair(0, 7)
    private val times = mutableListOf<Float>()
    private var gateBlown = false
    private var sectionTimer = 0L
    private var currentTick = 0L
    private var phaseTimer = 0L

    @EventHandler
    fun onMessage(event: PacketEvent.Receive) = with (event.packet) {
        if (this is CommonPingS2CPacket && terminalSplits && !useRealTime) currentTick += 50
        if (this !is GameMessageS2CPacket || !terminalSplits) return

        val text = content?.string ?: return

        terminalCompleteRegex.find(text)?.let {
            event.cancel()

            val (name, activated, type, current, total) = it.destructured
            modMessage("§6$name §a$activated a $type! (§c${current}§a/${total}) §8(§7${sectionTimer.seconds}s §8| §7${phaseTimer.seconds}s§8)", "")

            if ((current == total && gateBlown) || (current.toIntOrNull() ?: return) < completed.first) resetSection()
            else completed = Pair(current.toIntOrNull() ?: return, total.toIntOrNull() ?: return)
            return
        }

        when {
            gateDestroyedRegex.matches(text) -> if (completed.first == completed.second) resetSection() else gateBlown = true

            goldorRegex.matches(text) -> resetSection(true)

            coreOpeningRegex.matches(text) -> {
                modMessage("§bTimes: §a${times.joinToString(" §8| ") { "§a${it}s" }}§8, §bTotal: §a${phaseTimer.seconds}s")
                resetSection()
            }
        }
    }

    @EventHandler
    fun onWorldLoad(world: WorldLoadEvent) {
        resetSection(true)
    }

    private inline val Long.seconds
        get() = ((if (useRealTime) System.currentTimeMillis() else currentTick) - this) / 1000f

    private fun resetSection(full: Boolean = false) {
        if (full) {
            times.clear()
            phaseTimer = if (useRealTime) System.currentTimeMillis() else currentTick
        } else times.add(sectionTimer.seconds)
        completed = Pair(0, 7)
        sectionTimer = if (useRealTime) System.currentTimeMillis() else currentTick
        gateBlown = false
    }
}