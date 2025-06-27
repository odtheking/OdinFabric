package me.odinmod.odin.features.impl.render

import me.odinmod.odin.events.TickEvent
import me.odinmod.odin.features.Module
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
}