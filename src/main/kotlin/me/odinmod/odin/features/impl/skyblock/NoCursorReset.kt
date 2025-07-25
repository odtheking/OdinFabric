package me.odinmod.odin.features.impl.skyblock

import me.odinmod.odin.events.TickEvent
import me.odinmod.odin.features.Module
import meteordevelopment.orbit.EventHandler

object NoCursorReset : Module(
    name = "No Cursor Reset",
    description = "Prevents the cursor from being reset when opening a GUI."
) {
    private var clock: Long = System.currentTimeMillis()
    private var wasNotNull = false

    @EventHandler
    fun onTick(event: TickEvent.Start) {
        if (mc.currentScreen != null) {
            wasNotNull = true
            clock = System.currentTimeMillis()
        } else if (wasNotNull && mc.currentScreen == null) {
            wasNotNull = false
            clock = System.currentTimeMillis()
        }
    }

    @JvmStatic
    fun shouldHookMouse(): Boolean =
        System.currentTimeMillis() - clock < 150 && enabled
}