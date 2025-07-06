package me.odinmod.odin.features.impl.nether

import me.odinmod.odin.clickgui.settings.impl.BooleanSetting
import me.odinmod.odin.events.PacketEvent
import me.odinmod.odin.features.Module
import me.odinmod.odin.utils.noControlCodes
import me.odinmod.odin.utils.render.drawString
import me.odinmod.odin.utils.sendCommand
import me.odinmod.odin.utils.skyblock.KuudraUtils
import me.odinmod.odin.utils.toFixed
import meteordevelopment.orbit.EventHandler
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket

object FreshTools : Module(
    name = "Fresh Tools",
    description = "Tracks your party's fresh tools time.",
){
    private val notifyFresh by BooleanSetting("Notify Fresh", true, desc = "Notifies your party when you get fresh timer.")
    private val hud by HUD("Fresh timer", "Displays how long players have fresh for.") { example ->
        if (!example && (!KuudraUtils.inKuudra || KuudraUtils.phase != 2 || KuudraUtils.freshers.isEmpty())) return@HUD 0f to 0f

        var yOffset = 1f
        var maxWidth = 0f

        if (example) {
            drawString("§6Player1§f: 9s", 1f, yOffset)
            yOffset += mc.textRenderer.fontHeight
            drawString("§6Player2§f: 5s", 1f, yOffset)
            maxWidth = mc.textRenderer.getWidth("Player2: 5s") + 2f
            yOffset += mc.textRenderer.fontHeight
        } else {
            KuudraUtils.freshers.forEach { fresher ->
                val timeLeft = fresher.value?.let { (10000L - (System.currentTimeMillis() - it)) }?.takeIf { it > 0 } ?: return@forEach
                val text = "§6${fresher.key}§f: ${(timeLeft / 1000f).toFixed()}s"
                drawString(text, 1f, yOffset)
                maxWidth = maxOf(maxWidth, mc.textRenderer.getWidth(text) + 2f)
                yOffset += mc.textRenderer.fontHeight
            }
        }

        maxWidth to yOffset
    }

    private val ownFreshRegex = Regex("^Your Fresh Tools Perk bonus doubles your building speed for the next 10 seconds!\$")

    @EventHandler
    fun onChat(event: PacketEvent.Receive) = with (event.packet) {
        if (this is GameMessageS2CPacket && !overlay && notifyFresh && KuudraUtils.inKuudra && ownFreshRegex.matches(content.string.noControlCodes))
            sendCommand("pc FRESH")
    }
}