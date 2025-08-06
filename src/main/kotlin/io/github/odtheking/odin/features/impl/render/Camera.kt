package io.github.odtheking.odin.features.impl.render

import io.github.odtheking.odin.events.TickEvent
import io.github.odtheking.odin.features.Module
import meteordevelopment.orbit.EventHandler
import net.minecraft.client.option.Perspective

object Camera : Module(
    name = "Camera",
    description = "Disables front camera when enabled."
) {

    @EventHandler
    fun onTick(event: TickEvent.End) {
        if (mc.options.perspective == Perspective.THIRD_PERSON_FRONT)
            mc.options.perspective = Perspective.FIRST_PERSON
    }
}