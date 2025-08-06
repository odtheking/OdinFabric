package io.github.odtheking.odin.features.impl.skyblock

import io.github.odtheking.odin.events.TickEvent
import io.github.odtheking.odin.features.Module
import meteordevelopment.orbit.EventHandler
import mixins.accessors.KeyBindingAccessor
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