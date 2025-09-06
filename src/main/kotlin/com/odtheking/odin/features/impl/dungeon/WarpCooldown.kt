package com.odtheking.odin.features.impl.dungeon

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.StringSetting
import com.odtheking.odin.events.PacketEvent
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.drawStringWidth
import com.odtheking.odin.utils.sendCommand
import com.odtheking.odin.utils.toFixed
import meteordevelopment.orbit.EventHandler
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket

object WarpCooldown : Module(
    name = "Warp Cooldown",
    description = "Displays the time until you can warp into a new dungeon."
) {
    private val announceKick by BooleanSetting("Announce Kick", false, desc = "Announce when you get kicked from skyblock.")
    private val kickText by StringSetting("Kick Text", "Kicked!", desc = "The text sent in party chat when you get kicked from skyblock.").withDependency { announceKick }
    private val hud by HUD("Warp Timer Hud", "Displays the warp timer in the HUD.") {
        if (warpTimer - System.currentTimeMillis() <= 0 && !it) return@HUD 0f to 0f
        drawStringWidth("§eWarp: §a${if (it) "30" else ((warpTimer - System.currentTimeMillis()) / 1000f).toFixed()}s", 1, 1, Colors.WHITE) + 2f to 10f
    }

    private val enterRegex = Regex("^-*\\n\\[[^]]+] (\\w+) entered (?:MM )?\\w+ Catacombs, Floor (\\w+)!\\n-*$")
    private val kickedInstanceRegex = Regex("^You are no longer allowed to access this instance!$")
    private val kickedJoiningRegex = Regex("^You were kicked while joining that server!$")
    private var warpTimer = 0L

    @EventHandler
    fun onPacketReceive(event: PacketEvent.Receive) = with (event.packet) {
        if (this !is GameMessageS2CPacket || overlay) return
        val contentString = content.string
        when {
            announceKick && (contentString.matches(kickedJoiningRegex) || contentString.matches(kickedInstanceRegex)) -> sendCommand("pc $kickText")
            contentString.matches(enterRegex) -> warpTimer = System.currentTimeMillis() + 30_000L
        }
    }
}