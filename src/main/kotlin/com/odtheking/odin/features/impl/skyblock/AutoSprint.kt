package com.odtheking.odin.features.impl.skyblock

import com.odtheking.mixin.accessors.KeyBindingAccessor
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.features.Module
import meteordevelopment.orbit.EventHandler
import net.minecraft.client.option.KeyBinding

object AutoSprint : Module(
    name = "Auto Sprint",
    description = "Automatically makes you sprint."
) {
    @EventHandler
    fun onTick(event: TickEvent.End) {
        KeyBinding.setKeyPressed((mc.options.sprintKey as KeyBindingAccessor).boundKey, true)
    }
}