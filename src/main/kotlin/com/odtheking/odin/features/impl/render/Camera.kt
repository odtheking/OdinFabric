package com.odtheking.odin.features.impl.render

import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import net.minecraft.client.CameraType

object Camera : Module(
    name = "Camera",
    description = "Disables front camera when enabled."
) {

    init {
        on<TickEvent.End> {
            if (mc.options.cameraType == CameraType.THIRD_PERSON_FRONT)
                mc.options.cameraType = CameraType.FIRST_PERSON
        }
    }
}
