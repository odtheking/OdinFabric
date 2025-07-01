package me.odinmod.odin.features.impl.skyblock

import me.odinmod.odin.events.TickEvent
import me.odinmod.odin.features.Module
import meteordevelopment.orbit.EventHandler
import mixins.KeyBindingAccessor
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