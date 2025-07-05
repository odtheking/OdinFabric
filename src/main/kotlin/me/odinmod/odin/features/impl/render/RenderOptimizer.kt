package me.odinmod.odin.features.impl.render

import me.odinmod.odin.clickgui.settings.impl.BooleanSetting
import me.odinmod.odin.events.PacketEvent
import me.odinmod.odin.features.Module
import meteordevelopment.orbit.EventHandler
import net.minecraft.entity.EntityType
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket

object RenderOptimizer : Module(
    name = "Render Optimizer",
    description = "Optimizes rendering by disabling unnecessary features."
) {
    private val disableFallingBlocks by BooleanSetting("Disable Falling Blocks", true, desc = "Disables rendering of falling blocks to improve performance.")
    private val disableLighting by BooleanSetting("Disable Lighting", true, desc = "Disables lighting updates to improve performance.")

    @EventHandler
    fun onMobMetadata(event: PacketEvent.Receive) = with (event.packet) {
        if (this is EntitySpawnS2CPacket) {
            if (disableFallingBlocks && this.entityType == EntityType.FALLING_BLOCK) event.cancel()

            if (disableLighting && this.entityType == EntityType.LIGHTNING_BOLT) event.cancel()
        }
    }
}