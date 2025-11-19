package com.odtheking.odin.features.impl.render

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.events.core.onReceive
import com.odtheking.odin.features.Module
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType

object RenderOptimizer : Module(
    name = "Render Optimizer",
    description = "Optimizes rendering by disabling unnecessary features."
) {
    private val disableFallingBlocks by BooleanSetting("Hide Falling Blocks", true, desc = "Hides rendering of falling blocks to improve performance.")
    private val disableLighting by BooleanSetting("Hide Lighting", true, desc = "Hides lighting updates to improve performance.")
    private val disableExplosion by BooleanSetting("Hide Explosion Particles", false, desc = "Hides explosion particles to improve performance.")
    private val hideDyingMobs by BooleanSetting("Hide Dying Mobs", false, desc = "Hides mobs that are dying.")

    private val disableFireOverlay by BooleanSetting("Hide Fire Overlay", true, desc = "Hides the fire overlay to improve disability.")

    init {
        onReceive<ClientboundAddEntityPacket> {
            if (disableFallingBlocks && type == EntityType.FALLING_BLOCK)
                it.cancel()
            else if (disableLighting && type == EntityType.LIGHTNING_BOLT)
                it.cancel()
        }

        onReceive<ClientboundSetEntityDataPacket> {
            if (hideDyingMobs)
                mc.level?.getEntity(id)?.let { if (!it.isAlive) it.remove(Entity.RemovalReason.DISCARDED) }
        }

        onReceive<ClientboundLevelParticlesPacket> {
            if (disableExplosion && particle == ParticleTypes.EXPLOSION)
                it.cancel()
        }
    }

    @JvmStatic
    val shouldDisableFire get() = enabled && disableFireOverlay
}