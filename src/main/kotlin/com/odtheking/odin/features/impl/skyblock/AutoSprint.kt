package com.odtheking.odin.features.impl.skyblock

import com.odtheking.mixin.accessors.KeyMappingAccessor
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import net.minecraft.client.KeyMapping

object AutoSprint : Module(
    name = "Auto Sprint",
    description = "Automatically makes you sprint."
) {

    init {
        on<TickEvent.End> {
            KeyMapping.set((mc.options.keySprint as KeyMappingAccessor).boundKey, true)
        }
    }
}