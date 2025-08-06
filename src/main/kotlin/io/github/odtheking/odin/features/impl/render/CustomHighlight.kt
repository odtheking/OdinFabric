package io.github.odtheking.odin.features.impl.render

import io.github.odtheking.odin.clickgui.settings.impl.ColorSetting
import io.github.odtheking.odin.clickgui.settings.impl.MapSetting
import io.github.odtheking.odin.clickgui.settings.impl.NumberSetting
import io.github.odtheking.odin.clickgui.settings.impl.SelectorSetting
import io.github.odtheking.odin.events.RenderEvent
import io.github.odtheking.odin.features.Module
import io.github.odtheking.odin.utils.Color
import io.github.odtheking.odin.utils.Color.Companion.multiplyAlpha
import io.github.odtheking.odin.utils.Color.Companion.withAlpha
import io.github.odtheking.odin.utils.Colors
import io.github.odtheking.odin.utils.handlers.MobCache
import io.github.odtheking.odin.utils.render.drawFilledBox
import io.github.odtheking.odin.utils.render.drawWireFrameBox
import io.github.odtheking.odin.utils.renderBoundingBox
import meteordevelopment.orbit.EventHandler
import net.minecraft.world.RaycastContext

object CustomHighlight : Module(
    name = "Custom Highlight",
    description = "Allows you to highlight selected mobs. (/highlight)"
) {
    private val color by ColorSetting(
        "Color",
        Colors.WHITE.withAlpha(0.75f),
        true,
        desc = "The color of the highlight."
    )
    private val renderStyle by SelectorSetting(
        "Render Style",
        "Outline",
        listOf("Outline", "Filled", "Filled Outline"),
        desc = "Style of the box."
    )
    private val entityIDOffset by NumberSetting(
        "Entity ID Offset",
        0,
        -10,
        10,
        1,
        desc = "Offset to apply to entity IDs different mobs require different entity ID offsets."
    )
    val highlightMap by MapSetting("highlightMap", mutableMapOf<String, Color>())

    val entities = MobCache(entityOffset = { entityIDOffset }) { entity ->
        highlightMap.any { entity.displayName?.string?.contains(it.key, true) == true }
    }

    @EventHandler
    fun onRenderWorld(event: RenderEvent.Last) {
        entities.forEach { entity ->
            val boundingBox = entity.renderBoundingBox
            val color = highlightMap[entity.displayName?.string] ?: this.color
            val canSee = mc.player?.canSee(
                entity,
                RaycastContext.ShapeType.VISUAL,
                RaycastContext.FluidHandling.NONE,
                entity.eyeY
            ) ?: false

            when (renderStyle) {
                0 -> event.context.drawWireFrameBox(boundingBox, color, depth = !canSee)
                1 -> event.context.drawFilledBox(boundingBox, color, depth = !canSee)
                2 -> {
                    event.context.drawWireFrameBox(boundingBox, color, depth = !canSee)
                    event.context.drawFilledBox(boundingBox, color.multiplyAlpha(0.5f), depth = !canSee)
                }
            }
        }
    }
}