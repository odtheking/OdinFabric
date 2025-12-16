package com.odtheking.odin.features.impl.render

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.events.core.onReceive
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.texture
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

object RenderOptimizer : Module(
    name = "Render Optimizer",
    description = "Optimizes rendering by disabling unnecessary features."
) {
    private val disableFallingBlocks by BooleanSetting("Hide Falling Blocks", true, desc = "Hides rendering of falling blocks to improve performance.")
    private val disableLighting by BooleanSetting("Hide Lighting", true, desc = "Hides lighting updates to improve performance.")
    private val disableExplosion by BooleanSetting("Hide Explosion Particles", false, desc = "Hides explosion particles to improve performance.")
    private val hideDyingMobs by BooleanSetting("Hide Dying Mobs", true, desc = "Hides mobs that are dying.")
    private val hideArcher by BooleanSetting("Hide Archer Passive", false, desc = "Hides the archer passive's floating bone meal.")
    private val hideFairy by BooleanSetting("Hide Healer Fairy", false, desc = "Hides the healer fairy held by some mobs.")
    private val hideWeaver by BooleanSetting("Hide Soul Weaver", false, desc = "Hides the soul weaver helmet worn by some mobs.")
    private val hideTentacle by BooleanSetting("Hide Tentacle Head", false, desc = "Hides the tentacle head worn by some mobs.")

    private val disableFireOverlay by BooleanSetting("Hide Fire Overlay", true, desc = "Hides the fire overlay to improve disability.")

    private const val TENTACLE_TEXTURE = "ewogICJ0aW1lc3RhbXAiIDogMTcxOTg1NzI3NzI0OSwKICAicHJvZmlsZUlkIiA6ICIxODA1Y2E2MmM0ZDI0M2NiOWQxYmY4YmM5N2E1YjgyNCIsCiAgInByb2ZpbGVOYW1lIiA6ICJSdWxsZWQiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzdkODM2NzQ5MjZiODk3MTRlNmI1YTU1NDcwNTAxYzA0YjA2NmRkODdiZjZjMzM1Y2RkYzZlNjBhMWExYTVmNSIKICAgIH0KICB9Cn0="
    private const val HEALER_FAIRY_TEXTURE = "ewogICJ0aW1lc3RhbXAiIDogMTcxOTQ2MzA5MTA0NywKICAicHJvZmlsZUlkIiA6ICIyNjRkYzBlYjVlZGI0ZmI3OTgxNWIyZGY1NGY0OTgyNCIsCiAgInByb2ZpbGVOYW1lIiA6ICJxdWludHVwbGV0IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzJlZWRjZmZjNmExMWEzODM0YTI4ODQ5Y2MzMTZhZjdhMjc1MmEzNzZkNTM2Y2Y4NDAzOWNmNzkxMDhiMTY3YWUiCiAgICB9CiAgfQp9"
    private const val SOUL_WEAVER_TEXTURE = "eyJ0aW1lc3RhbXAiOjE1NTk1ODAzNjI1NTMsInByb2ZpbGVJZCI6ImU3NmYwZDlhZjc4MjQyYzM5NDY2ZDY3MjE3MzBmNDUzIiwicHJvZmlsZU5hbWUiOiJLbGxscmFoIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8yZjI0ZWQ2ODc1MzA0ZmE0YTFmMGM3ODViMmNiNmE2YTcyNTYzZTlmM2UyNGVhNTVlMTgxNzg0NTIxMTlhYTY2In19fQ=="

    init {
        onReceive<ClientboundAddEntityPacket> {
            if (disableFallingBlocks && type == EntityType.FALLING_BLOCK)
                it.cancel()
            else if (disableLighting && type == EntityType.LIGHTNING_BOLT)
                it.cancel()
        }

        onReceive<ClientboundSetEntityDataPacket> {
            mc.execute {
                if (hideDyingMobs) (packedItems.find { it.id == 9 }?.value as? Float)?.let {
                    if (it <= 0f) mc.level?.removeEntity(id, Entity.RemovalReason.DISCARDED)
                }
                if (hideArcher) (packedItems.find { it.id == 8 }?.value as? ItemStack)?.let {
                    if (!it.isEmpty && it.item == Items.BONE_MEAL) mc.level?.removeEntity(id, Entity.RemovalReason.DISCARDED)
                }
            }
        }

        onReceive<ClientboundSetEquipmentPacket> {
            slots.forEach { slot ->
                if (slot.second.isEmpty) return@forEach
                val texture = slot.second.texture ?: return@forEach

                if (
                    (hideFairy && slot.first == EquipmentSlot.MAINHAND && texture == HEALER_FAIRY_TEXTURE) ||
                    (hideWeaver && slot.first == EquipmentSlot.HEAD && texture == SOUL_WEAVER_TEXTURE) ||
                    (hideTentacle && slot.first == EquipmentSlot.HEAD && texture == TENTACLE_TEXTURE)
                ) mc.execute { mc.level?.removeEntity(entity, Entity.RemovalReason.DISCARDED) }
            }
        }

        onReceive<ClientboundLevelParticlesPacket> {
            if (disableExplosion && particle == ParticleTypes.EXPLOSION)
                it.cancel()
        }
    }

    @JvmStatic
    val shouldDisableFire get() = enabled && disableFireOverlay
}