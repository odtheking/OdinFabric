package com.odtheking.odin.features.impl.nether

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.events.PacketEvent
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.noControlCodes
import com.odtheking.odin.utils.render.drawString
import com.odtheking.odin.utils.render.drawStringWidth
import com.odtheking.odin.utils.sendCommand
import com.odtheking.odin.utils.skyblock.KuudraUtils
import com.odtheking.odin.utils.toFixed
import meteordevelopment.orbit.EventHandler
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket

object FreshTools : Module(
    name = "Fresh Tools",
    description = "Tracks your party's fresh tools time.",
) {
    private val notifyFresh by BooleanSetting("Notify Fresh", true, desc = "Notifies your party when you get fresh timer.")
    private val hud by HUD("Fresh timer", "Displays how long players have fresh for.") { example ->
        if (!example && (!KuudraUtils.inKuudra || KuudraUtils.phase != 2 || KuudraUtils.freshers.isEmpty())) return@HUD 0 to 0

        var yOffset = 1
        var maxWidth = 0

        if (example) {
            drawString("§6Player1§f: 9s", 1, yOffset)
            yOffset += mc.textRenderer.fontHeight
            maxWidth = drawStringWidth("§6Player2§f: 5s", 1, yOffset)
            yOffset += mc.textRenderer.fontHeight
        } else {
            KuudraUtils.freshers.forEach { fresher ->
                val timeLeft = fresher.value?.let { (10000L - (System.currentTimeMillis() - it)) }?.takeIf { it > 0 }
                    ?: return@forEach
                val text = "§6${fresher.key}§f: ${(timeLeft / 1000f).toFixed()}s"
                val width = drawStringWidth(text, 1, yOffset)
                maxWidth = maxOf(maxWidth, width + 2)
                yOffset += mc.textRenderer.fontHeight
            }
        }

        maxWidth to yOffset
    }

    private val ownFreshRegex = Regex("^Your Fresh Tools Perk bonus doubles your building speed for the next 10 seconds!$")

    @EventHandler
    fun onChat(event: PacketEvent.Receive) = with(event.packet) {
        if (this is GameMessageS2CPacket && !overlay && notifyFresh && KuudraUtils.inKuudra && ownFreshRegex.matches(content.string.noControlCodes))
            sendCommand("pc FRESH")
    }
}