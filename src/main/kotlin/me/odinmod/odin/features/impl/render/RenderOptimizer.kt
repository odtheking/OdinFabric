package me.odinmod.odin.features.impl.render

import me.odinmod.odin.clickgui.settings.impl.BooleanSetting
import me.odinmod.odin.events.PacketEvent
import me.odinmod.odin.features.Module
import meteordevelopment.orbit.EventHandler
import net.minecraft.entity.EntityType
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket
import net.minecraft.particle.ParticleTypes

object RenderOptimizer : Module(
    name = "Render Optimizer",
    description = "Optimizes rendering by disabling unnecessary features."
) {
    private val disableFallingBlocks by BooleanSetting("Disable Falling Blocks", true, desc = "Disables rendering of falling blocks to improve performance.")
    private val disableLighting by BooleanSetting("Disable Lighting", true, desc = "Disables lighting updates to improve performance.")
    private val disableExplosion by BooleanSetting("Disable Explosion Particles", true, desc = "Disables explosion particles to improve performance.")

    @EventHandler
    fun onMobMetadata(event: PacketEvent.Receive) = with (event.packet) {
        when (this) {
            is EntitySpawnS2CPacket -> {
                if (disableFallingBlocks && entityType == EntityType.FALLING_BLOCK) event.cancel()

                if (disableLighting && entityType == EntityType.LIGHTNING_BOLT) event.cancel()
            }

            is ParticleS2CPacket -> {
                if (disableExplosion && parameters == ParticleTypes.EXPLOSION) event.cancel()
            }
        }
    }
}