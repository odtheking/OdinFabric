package com.odtheking.odin.features.impl.dungeon

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.ColorSetting
import com.odtheking.odin.clickgui.settings.impl.SelectorSetting
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.WorldLoadEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Color.Companion.multiplyAlpha
import com.odtheking.odin.utils.Color.Companion.withAlpha
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.noControlCodes
import com.odtheking.odin.utils.render.drawStyledBox
import com.odtheking.odin.utils.renderBoundingBox
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.boss.wither.WitherBoss
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.ClipContext

object Highlight : Module(
    name = "Highlight",
    description = "Allows you to highlight selected mobs."
) {
    private val color by ColorSetting("Highlight color", Colors.WHITE.withAlpha(0.75f), true, desc = "The color of the highlight.")
    private val renderStyle by SelectorSetting("Render Style", "Outline", listOf("Filled", "Outline", "Filled Outline"), desc = "Style of the box.")
    private val hideNonNames by BooleanSetting("Hide non-starred names", true, desc = "Hides names of entities that are not starred.")

    // https://regex101.com/r/QQf502/1
    private val starredRegex = Regex("^.*✯ .*\\d{1,3}(?:,\\d{3})*(?:\\.\\d+)?(?:[kM])?❤$")
    private const val DEFAULT_STAND_NAME = "Armor Stand"
    private val entities = mutableSetOf<Entity>()

    init {
        on<TickEvent.End> {
            if (!DungeonUtils.inDungeons || DungeonUtils.inBoss) return@on

            val entitiesToRemove = mutableListOf<Entity>()
            mc.level?.entitiesForRendering()?.forEach { e ->
                val entity = e ?: return@forEach
                val entityName = mc.level?.getEntity(entity.id)?.takeIf { entity.isAlive }?.name?.string?.noControlCodes ?: return@forEach

                if (hideNonNames && entity is ArmorStand && entity.isInvisible && entityName != DEFAULT_STAND_NAME && !starredRegex.matches(entityName))
                    entitiesToRemove.add(entity)

                if (entity !is ArmorStand || entityName == DEFAULT_STAND_NAME || !starredRegex.matches(entityName)) return@forEach

                mc.level?.getEntities(entity, entity.boundingBox.move(0.0, -1.0, 0.0)) { isValidEntity(it) }
                    ?.firstOrNull()?.let { entities.add(it) }
            }
            entitiesToRemove.forEach { it.remove(Entity.RemovalReason.DISCARDED) }
            entities.removeIf { entity -> !entity.isAlive }
        }

        on<RenderEvent.Last> {
            if (!DungeonUtils.inDungeons || DungeonUtils.inBoss) return@on

            entities.forEach { entity ->
                if (!entity.isAlive) return@forEach
                val canSee = mc.player?.hasLineOfSight(
                    entity, ClipContext.Block.VISUAL,
                    ClipContext.Fluid.NONE, entity.eyeY
                ) ?: false

                context.drawStyledBox(entity.renderBoundingBox, color.multiplyAlpha(0.5f), renderStyle, !canSee)
            }
        }

        on<WorldLoadEvent> {
            entities.clear()
        }
    }

    private fun isValidEntity(entity: Entity): Boolean =
        when (entity) {
            is ArmorStand -> false
            is WitherBoss -> false
            is Player -> entity.uuid.version() == 2 && entity != mc.player
            else -> !entity.isInvisible
        }
}