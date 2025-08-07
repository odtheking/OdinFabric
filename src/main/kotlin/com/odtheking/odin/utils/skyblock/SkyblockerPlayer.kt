package com.odtheking.odin.utils.skyblock

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.PacketEvent
import com.odtheking.odin.utils.noControlCodes
import meteordevelopment.orbit.EventHandler
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket
import kotlin.math.floor

object SkyblockPlayer {
    /*
    in module there should be:
    health display current/Max
    health bar
    defense display
    mana display current/Max
    mana bar
    current speed
    current ehp
    current overflow mana
     */

    private val HEALTH_REGEX = Regex("([\\d|,]+)/([\\d|,]+)❤")
    private val MANA_REGEX = Regex("([\\d|,]+)/([\\d|,]+)✎")
    private val OVERFLOW_MANA_REGEX = Regex("([\\d|,]+)ʬ")
    private val DEFENSE_REGEX = Regex("([\\d|,]+)❈ Defense")

    inline val currentHealth: Int
        get() = (mc.player?.let { player -> (maxHealth * player.health / player.maxHealth).toInt() } ?: 0)
    var maxHealth: Int = 0
    var currentMana: Int = 0
    var maxMana: Int = 0
    private var currentSpeed: Int = 0
    var currentDefense: Int = 0
    var overflowMana: Int = 0
    var effectiveHP: Int = 0

    @EventHandler
    fun onPacket(event: PacketEvent.Receive) = with(event.packet) {
        if (this !is GameMessageS2CPacket || !overlay) return
        val msg = content.string.noControlCodes

        HEALTH_REGEX.find(msg)?.destructured?.let { (_, maxHp) ->
            maxHealth = maxHp.replace(",", "").toIntOrNull() ?: maxHealth
        }

        MANA_REGEX.find(msg)?.destructured?.let { (cMana, mMana) ->
            currentMana = cMana.replace(",", "").toIntOrNull() ?: currentMana
            maxMana = mMana.replace(",", "").toIntOrNull() ?: maxMana
        }

        OVERFLOW_MANA_REGEX.find(msg)?.groupValues?.get(1)?.let {
            overflowMana = it.replace(",", "").toIntOrNull() ?: overflowMana
        }

        DEFENSE_REGEX.find(msg)?.groupValues?.get(1)?.let {
            currentDefense = it.replace(",", "").toIntOrNull() ?: currentDefense
        }

        effectiveHP = (currentHealth * (1 + currentDefense / 100))
        currentSpeed = floor(
            (mc.player?.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED)?.baseValue?.toFloat() ?: 0f) * 1000f
        ).toInt()
    }
}