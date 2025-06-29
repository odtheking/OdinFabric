package me.odinmod.odin.features.impl.render

import me.odinmod.odin.events.TickEvent
import me.odinmod.odin.features.Module
import me.odinmod.odin.utils.Color.Companion.withAlpha
import me.odinmod.odin.utils.Colors
import meteordevelopment.orbit.EventHandler
import net.minecraft.client.option.Perspective

object Camera: Module(
    name = "Camera",
    description = "Disables front camera when enabled"
) {

    @EventHandler
    fun onTick(event: TickEvent.End) {
        if (mc.options.perspective == Perspective.THIRD_PERSON_FRONT)
            mc.options.perspective = Perspective.FIRST_PERSON
    }

    private val hud by HUD("Test HUD", "Test HUD description") {
        fill(0, 0, 200, 50, Colors.MINECRAFT_GREEN.withAlpha(0.5f).rgba)

        200f to 50f
    }
}