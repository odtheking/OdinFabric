package me.odinmod.odin.features.impl.skyblock

import me.odinmod.odin.clickgui.settings.impl.BooleanSetting
import me.odinmod.odin.events.PacketEvent
import me.odinmod.odin.features.Module
import me.odinmod.odin.utils.alert
import me.odinmod.odin.utils.getItemId
import meteordevelopment.orbit.EventHandler
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket
import net.minecraft.sound.SoundEvents

object RagnarockAxe: Module(
    name = "Ragnarock Axe",
    description = "Alerts when you cast the Ragnarock Axe or it gets cancelled."
) {
    private val ragAxeAlert by BooleanSetting("Cast alert", true, "Alerts when you cast the Ragnarock Axe.")
    private val ragAxeCancelAlert by BooleanSetting("Cancel alert", true, "Alerts when Ragnarock Axe is cancelled.")

    private val ragAxeCancelRegex = Regex("Ragnarock was cancelled due to (?:being hit|taking damage)!")

    @EventHandler
    fun onPacketReceive(event: PacketEvent.Receive) = with (event.packet) {
        when (this) {
            is GameMessageS2CPacket ->
                if (ragAxeAlert && content?.string?.matches(ragAxeCancelRegex) == true) alert("§cRagnarock Axe Cancelled!")

            is PlaySoundS2CPacket -> {
                if (ragAxeCancelAlert && pitch == 1.4920635f && mc.player?.mainHandStack?.getItemId() == "RAGNAROCK_AXE" &&
                    SoundEvents.WOLF_SOUNDS.entries.any { it.value.deathSound.value().id == sound.value().id }) alert("§aCasted Rag Axe")
            }
        }
    }
}