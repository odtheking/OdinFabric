package com.odtheking.odin.features.impl.render

import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import net.minecraft.client.option.Perspective

object Camera : Module(
    name = "Camera",
    description = "Disables front camera when enabled."
) {

    init {
        on<TickEvent.End> {
            if (mc.options.perspective == Perspective.THIRD_PERSON_FRONT)
                mc.options.perspective = Perspective.FIRST_PERSON
        }
    }
}