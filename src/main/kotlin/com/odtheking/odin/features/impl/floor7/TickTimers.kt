package com.odtheking.odin.features.impl.floor7

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.HudElement
import com.odtheking.odin.events.PacketEvent
import com.odtheking.odin.events.WorldLoadEvent
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.drawStringWidth
import com.odtheking.odin.utils.toFixed
import meteordevelopment.orbit.EventHandler
import net.minecraft.network.packet.s2c.common.CommonPingS2CPacket
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket

object TickTimers : Module(
    name = "Tick Timers",
    description = "Displays timers for Necron, Goldor, and Storm."
) {
    private val displayInTicks by BooleanSetting("Display in Ticks", false, desc = "Display the timers in ticks instead of seconds.")
    private val symbolDisplay by BooleanSetting("Display Symbol", true, desc = "Displays s or t after the timers.")
    private val showPrefix by BooleanSetting("Show Prefix", true, desc = "Shows the prefix of the timers.")

    private val necronRegex = Regex("^\\[BOSS] Necron: I'm afraid, your journey ends now\\.$")
    private val goldorRegex = Regex("^\\[BOSS] Goldor: Who dares trespass into my domain\\?$")
    private val coreOpeningRegex = Regex("^The Core entrance is opening!$")
    private val stormStartRegex = Regex("^\\[BOSS] Storm: I should have known that I stood no chance\\.$")
    private val stormPadRegex = Regex("^\\[BOSS] Storm: Pathetic Maxor, just like expected\\.$")

    private val necronHud by HUD("Necron Hud", "Displays a timer for Necron's drop.") {
        if (it)                   drawStringWidth(formatTimer(35, 60, "§4Necron dropping in"), 1, 1, Colors.MINECRAFT_DARK_RED) + 2f to 10f
        else if (necronTime >= 0) drawStringWidth(formatTimer(necronTime.toInt(), 60, "§4Necron dropping in"), 1, 1, Colors.MINECRAFT_DARK_RED) + 2f to 10f
        else 0f to 0f
    }

    private var necronTime: Byte = -1

    private val goldorHud: HudElement by HUD("Goldor Hud", "Displays a timer for Goldor's Core entrance opening.") {
        if (it) drawStringWidth(formatTimer(35, 60, "§7Tick:"), 1, 1, Colors.MINECRAFT_DARK_RED) + 2f to 10f
        else if ((goldorStartTime >= 0 && startTimer) || goldorTickTime >= 0) {
            val (prefix: String, time: Int, max: Int) = if (goldorStartTime >= 0 && startTimer) Triple("§aStart:", goldorStartTime, 104) else Triple("§7Tick:", goldorTickTime, 60)
            drawStringWidth(formatTimer(time, max, prefix), 1, 1, Colors.MINECRAFT_DARK_RED) + 2f to 10f
        } else 0f to 0f
    }
    private val startTimer by BooleanSetting("Start timer", false, desc = "Displays a timer counting down until devices/terms are able to be activated/completed.").withDependency { goldorHud.enabled }

    private var goldorTickTime: Int = -1
    private var goldorStartTime: Int = -1

    private val stormHud by HUD("Storm Pad Hud", "Displays a timer for Storm's Pad.") {
        if (it)                    drawStringWidth(formatTimer(15, 20, "§bPad:"), 1, 1, Colors.MINECRAFT_DARK_RED) + 2f to 10f
        else if (padTickTime >= 0) drawStringWidth(formatTimer(padTickTime, 20, "§bPad:"), 1, 1, Colors.MINECRAFT_DARK_RED) + 2f to 10f
        else 0f to 0f
    }

    private var padTickTime: Int = -1

    @EventHandler
    fun onPacketReceive(event: PacketEvent.Receive) = with (event.packet) {
       // if (!DungeonUtils.inBoss) return@with
        if (this is CommonPingS2CPacket) {
            if (goldorTickTime == 0 && goldorStartTime <= 0 && goldorHud.enabled) goldorTickTime = 60
            if (goldorStartTime >= 0 && goldorHud.enabled) goldorStartTime--
            if (goldorTickTime >= 0 && goldorHud.enabled) goldorTickTime--
            if (padTickTime == 0 && stormHud.enabled) padTickTime = 20
            if (padTickTime >= 0 && stormHud.enabled) padTickTime--
            if (necronTime >= 0 && necronHud.enabled) necronTime--
            return@with
        }

        if (this !is GameMessageS2CPacket || overlay) return
        when {
            necronHud.enabled && content.string.matches(necronRegex) -> necronTime = 60
            goldorHud.enabled && content.string.matches(goldorRegex) -> goldorTickTime = 60
            goldorHud.enabled && content.string.matches(coreOpeningRegex) -> {
                goldorStartTime = -1
                goldorTickTime = -1
            }
            content.string.matches(stormStartRegex) -> {
                if (goldorHud.enabled) goldorStartTime = 104
                if (stormHud.enabled) padTickTime = -1
            }
            stormHud.enabled && content.string.matches(stormPadRegex) -> padTickTime = 20
        }
    }

    @EventHandler
    fun onServerTick(event: WorldLoadEvent) {
        goldorStartTime = -1
        goldorTickTime = -1
        padTickTime = -1
        necronTime = -1
    }

    private fun formatTimer(time: Int, max: Int, prefix: String): String {
        val color = when {
            time.toFloat() >= max * 0.66 -> "§a"
            time.toFloat() >= max * 0.33 -> "§6"
            else -> "§c"
        }
        val timeDisplay = if (displayInTicks) "$time${if (symbolDisplay) "t" else ""}" else "${(time / 20f).toFixed()}${if (symbolDisplay) "s" else ""}"
        return "${if (showPrefix) "$prefix " else ""}$color$timeDisplay"
    }
}