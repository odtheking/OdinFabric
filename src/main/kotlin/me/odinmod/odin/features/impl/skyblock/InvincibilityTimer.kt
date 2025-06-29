package me.odinmod.odin.features.impl.skyblock

import me.odinmod.odin.clickgui.settings.impl.BooleanSetting
import me.odinmod.odin.events.GuiEvent
import me.odinmod.odin.events.PacketEvent
import me.odinmod.odin.events.WorldLoadEvent
import me.odinmod.odin.features.Module
import me.odinmod.odin.utils.getItemId
import me.odinmod.odin.utils.handlers.TickTask
import me.odinmod.odin.utils.renderDurabilityBar
import me.odinmod.odin.utils.sendCommand
import me.odinmod.odin.utils.skyblock.LocationUtils
import meteordevelopment.orbit.EventHandler
import net.minecraft.network.packet.s2c.common.CommonPingS2CPacket
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket

object InvincibilityTimer : Module(
    name = "Invincibility Timer",
    description = "Timer to show how long you have left Invincible."
)  {
    private val showCooldown by BooleanSetting("Show Cooldown", true, desc = "Shows the cooldown of the mask.")
    private val invincibilityAnnounce by BooleanSetting("Announce Invincibility", true, desc = "Announces when you get invincibility.")
//    private val hud by HudSetting("Timer Hud", 10f, 10f, 1f, true) {
//        if (it) {
//            RenderUtils.drawText("${if(showPrefix) "§bBonzo§f: " else ""}59t", 1f, 1f, 1f, Colors.WHITE, center = false)
//            getMCTextWidth("Bonzo: 59t") + 2f to 12f
//        } else {
//            if (invincibilityTime.time <= 0) return@HudSetting 0f to 0f
//            val invincibilityType = if (invincibilityTime.type == "Bonzo") "§bBonzo§f:" else if (invincibilityTime.type == "Phoenix") "§6Phoenix§f:" else "§5Spirit§f:"
//
//            RenderUtils.drawText("${if (showPrefix) invincibilityType else ""} ${invincibilityTime.time}t", 1f, 1f, 1f, Colors.WHITE, center = false)
//            getMCTextWidth("Bonzo: 59t") + 2f to 12f
//        }
//    }
    private val showPrefix by BooleanSetting("Show Prefix", true, desc = "Shows the prefix of the timer.")

    private data class Timer(var time: Int, var type: String)
    private var invincibilityTime = Timer(0, "")
    private val bonzoMaskRegex = Regex("^Your (?:. )?Bonzo's Mask saved your life!$")
    private val phoenixPetRegex = Regex("^Your Phoenix Pet saved you from certain death!$")
    private val spiritPetRegex = Regex("^Second Wind Activated! Your Spirit Mask saved your life!\$")

    private var spiritMaskProc = 0L
    private var bonzoMaskProc = 0L

    init {
        TickTask(1, true) {
            invincibilityTime.time--
        }
    }

    @EventHandler
    fun PacketReceive(event: PacketEvent.Receive) {
        if (event.packet !is GameMessageS2CPacket) return

        val message = event.packet.content.string
        val type = when {
            message.matches(bonzoMaskRegex) -> {
                bonzoMaskProc = System.currentTimeMillis()
                "Bonzo"
            }
            message.matches(spiritPetRegex) -> {
                spiritMaskProc = System.currentTimeMillis()
                "Spirit"
            }
            message.matches(phoenixPetRegex) -> "Phoenix"
            else -> return
        }
        if (invincibilityAnnounce) sendCommand("p chat $type Proceed")
        invincibilityTime = Timer(60, type)
    }

    @EventHandler
    fun onWorldLoad(event: WorldLoadEvent) {
        invincibilityTime = Timer(0, "")
        spiritMaskProc = 0L
        bonzoMaskProc = 0L
    }

    @EventHandler
    fun onDrawSlotOverlay(event: GuiEvent.DrawSlotOverlay) {
        if (!LocationUtils.isInSkyblock || !showCooldown) return
        val durability = when (event.stack?.getItemId()) {
            "BONZO_MASK", "STARRED_BONZO_MASK" -> (System.currentTimeMillis() - bonzoMaskProc) / 180_000.0
            "SPIRIT_MASK", "STARRED_SPIRIT_MASK" -> (System.currentTimeMillis() - spiritMaskProc) / 30_000.0
            else -> return
        }.takeIf { it < 1.0 } ?: return
        renderDurabilityBar(event.drawContext, event.x ?: return, event.y ?: return, durability)
    }
}