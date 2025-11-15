package com.odtheking.odin.features.impl.dungeon

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.StringSetting
import com.odtheking.odin.events.ChatPacketEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.textDim
import com.odtheking.odin.utils.sendCommand
import com.odtheking.odin.utils.toFixed

object WarpCooldown : Module(
    name = "Warp Cooldown",
    description = "Displays the time until you can warp into a new dungeon."
) {
    private val announceKick by BooleanSetting("Announce Kick", false, desc = "Announce when you get kicked from skyblock.")
    private val kickText by StringSetting("Kick Text", "Kicked!", desc = "The text sent in party chat when you get kicked from skyblock.").withDependency { announceKick }
    private val hud by HUD(name, "Displays the warp timer in the HUD.", false) {
        if (warpTimer - System.currentTimeMillis() <= 0 && !it) return@HUD 0 to 0
        textDim("§eWarp: §a${if (it) "30" else ((warpTimer - System.currentTimeMillis()) / 1000f).toFixed()}s", 0, 0, Colors.WHITE)
    }

    private val enterRegex = Regex("^-*\\n\\[[^]]+] (\\w+) entered (?:MM )?\\w+ Catacombs, Floor (\\w+)!\\n-*$")
    private val kickedInstanceRegex = Regex("^You are no longer allowed to access this instance!$")
    private val kickedJoiningRegex = Regex("^You were kicked while joining that server!$")
    private var warpTimer = 0L

    init {
        on<ChatPacketEvent> {
            when {
                announceKick && (value.matches(kickedJoiningRegex) || value.matches(kickedInstanceRegex)) -> sendCommand("pc $kickText")
                value.matches(enterRegex) -> warpTimer = System.currentTimeMillis() + 30_000L
            }
        }
    }
}