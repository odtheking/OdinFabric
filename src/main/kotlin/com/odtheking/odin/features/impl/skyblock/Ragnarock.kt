package com.odtheking.odin.features.impl.skyblock

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.events.PacketEvent
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.alert
import com.odtheking.odin.utils.itemId
import meteordevelopment.orbit.EventHandler
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket
import net.minecraft.sound.SoundEvents

object Ragnarock : Module(
    name = "Ragnarock",
    description = "Alerts when you cast the Ragnarock or it gets cancelled."
) {
    private val castAlert by BooleanSetting("Cast alert", true, "Alerts when you cast Ragnarock.")
    private val cancelAlert by BooleanSetting("Cancel alert", true, "Alerts when Ragnarock is cancelled.")

    private val cancelRegex = Regex("Ragnarock was cancelled due to (?:being hit|taking damage)!")

    @EventHandler
    fun onPacketReceive(event: PacketEvent.Receive) = with(event.packet) {
        when (this) {
            is GameMessageS2CPacket ->
                if (castAlert && content?.string?.matches(cancelRegex) == true) alert("§cRagnarock Cancelled!")

            is PlaySoundS2CPacket ->
                if (cancelAlert && pitch == 1.4920635f && mc.player?.mainHandStack?.itemId == "RAGNAROCK_AXE" &&
                    SoundEvents.WOLF_SOUNDS.entries.any { it.value.deathSound.value().id == sound.value().id }
                ) alert("§aCasted Rag")
        }
    }
}