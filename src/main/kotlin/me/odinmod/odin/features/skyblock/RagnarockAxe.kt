package me.odinmod.odin.features.skyblock

import me.odinmod.odin.OdinMod.mc
import me.odinmod.odin.config.categories.SkyblockConfig
import me.odinmod.odin.events.PacketEvent
import me.odinmod.odin.utils.alert
import me.odinmod.odin.utils.getItemId
import meteordevelopment.orbit.EventHandler
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket
import net.minecraft.sound.SoundEvents

object RagnarockAxe {

    private val ragAxeCancelRegex = Regex("Ragnarock was cancelled due to (?:being hit|taking damage)!")

    @EventHandler
    fun onPacketReceive(event: PacketEvent.Receive) = with (event.packet) {
        when (this) {
            is GameMessageS2CPacket ->
                if (SkyblockConfig.ragnarockAlert && content?.string?.matches(ragAxeCancelRegex) == true) alert("§cRagnarock Axe Cancelled!")

            is PlaySoundS2CPacket -> {
                if (SkyblockConfig.ragnarockCancelAlert && pitch == 1.4920635f && mc.player?.mainHandStack?.getItemId() == "RAGNAROCK_AXE" &&
                    SoundEvents.WOLF_SOUNDS.entries.any { it.value.deathSound.value().id == sound.value().id }) alert("§aCasted Rag Axe")
            }
        }
    }
}