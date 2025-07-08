package me.odinmod.odin.features.impl.render

import me.odinmod.odin.clickgui.settings.impl.ColorSetting
import me.odinmod.odin.clickgui.settings.impl.MapSetting
import me.odinmod.odin.clickgui.settings.impl.NumberSetting
import me.odinmod.odin.clickgui.settings.impl.SelectorSetting
import me.odinmod.odin.events.RenderEvent
import me.odinmod.odin.features.Module
import me.odinmod.odin.utils.Color
import me.odinmod.odin.utils.Color.Companion.multiplyAlpha
import me.odinmod.odin.utils.Color.Companion.withAlpha
import me.odinmod.odin.utils.Colors
import me.odinmod.odin.utils.handlers.MobCache
import me.odinmod.odin.utils.render.drawFilledBox
import me.odinmod.odin.utils.render.drawWireFrameBox
import me.odinmod.odin.utils.renderBoundingBox
import meteordevelopment.orbit.EventHandler

object CustomHighlight : Module(
    name = "Custom Highlight",
    description = "Allows you to highlight selected mobs. (/highlight)"
) {
    private val color by ColorSetting("Color", Colors.WHITE.withAlpha(0.75f), true, desc = "The color of the highlight.")
    private val renderStyle by SelectorSetting("Render Style", "Outline", listOf("Outline", "Filled", "Filled Outline"), desc = "Style of the box.")
    private val entityIDOffset by NumberSetting("Entity ID Offset", 0, -100, 100, 1, desc = "Offset to apply to entity IDs different mobs require different entity ID offsets.")
    val highlightMap by MapSetting("highlightMap", mutableMapOf<String, Color>())

    val entities = MobCache(entityOffset = { entityIDOffset }) { entity ->
        highlightMap.any { entity.displayName?.string?.contains(it.key, true) == true }
    }

    @EventHandler
    fun onRenderWorld(event: RenderEvent.Last) {
        entities.forEach { entity ->
            val boundingBox = entity.renderBoundingBox
            val color = highlightMap[entity.displayName?.string] ?: this.color
            if (mc.player?.canSee(entity) == false) return@forEach

            when (renderStyle) {
                0 -> event.context.drawWireFrameBox(boundingBox, color, depth = false)
                1 -> event.context.drawFilledBox(boundingBox, color, depth = false)
                2 -> {
                    event.context.drawWireFrameBox(boundingBox, color, depth = false)
                    event.context.drawFilledBox(boundingBox, color.multiplyAlpha(0.5f), depth = false)
                }
            }
        }
    }
}