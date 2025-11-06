package com.odtheking.odin.features.impl.skyblock

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.events.ChatPacketEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.events.core.onReceive
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.alert
import com.odtheking.odin.utils.itemId
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket
import net.minecraft.sound.SoundEvents

object Ragnarock : Module(
    name = "Ragnarock",
    description = "Alerts when you cast the Ragnarock or it gets cancelled."
) {
    private val castAlert by BooleanSetting("Cast alert", true, "Alerts when you cast Ragnarock.")
    private val cancelAlert by BooleanSetting("Cancel alert", true, "Alerts when Ragnarock is cancelled.")

    private val cancelRegex = Regex("Ragnarock was cancelled due to (?:being hit|taking damage)!")

    init {
        on<ChatPacketEvent> {
            if (castAlert && value.matches(cancelRegex)) alert("§aCasted Rag")
        }

        onReceive<PlaySoundS2CPacket> {
            if (cancelAlert && pitch == 1.4920635f && mc.player?.mainHandStack?.itemId == "RAGNAROCK_AXE" &&
                SoundEvents.WOLF_SOUNDS.entries.any { it.value.deathSound.value().id == sound.value().id }
            ) alert("§cRagnarock Cancelled!")
        }
    }
}