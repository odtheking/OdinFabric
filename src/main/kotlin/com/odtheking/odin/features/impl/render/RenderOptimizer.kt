package com.odtheking.odin.features.impl.render

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.events.PacketEvent
import com.odtheking.odin.features.Module
import meteordevelopment.orbit.EventHandler
import net.minecraft.entity.EntityType
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket
import net.minecraft.particle.ParticleTypes

object RenderOptimizer : Module(
    name = "Render Optimizer",
    description = "Optimizes rendering by disabling unnecessary features."
) {
    private val disableFallingBlocks by BooleanSetting(
        "Hide Falling Blocks",
        true,
        desc = "Hides rendering of falling blocks to improve performance."
    )
    private val disableLighting by BooleanSetting(
        "Hide Lighting",
        true,
        desc = "Hides lighting updates to improve performance."
    )
    private val disableExplosion by BooleanSetting(
        "Hide Explosion Particles",
        false,
        desc = "Hides explosion particles to improve performance."
    )

    private val disableFireOverlay by BooleanSetting(
        "Hide Fire Overlay",
        true,
        desc = "Hides the fire overlay to improve disability."
    )

    @EventHandler
    fun onMobMetadata(event: PacketEvent.Receive) = with(event.packet) {
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

    @JvmStatic
    val shouldDisableFire get() = enabled && disableFireOverlay
}