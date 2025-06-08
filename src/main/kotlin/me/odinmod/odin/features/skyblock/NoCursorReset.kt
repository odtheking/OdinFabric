package me.odinmod.odin.features.skyblock

import me.odinmod.odin.OdinMod.mc
import me.odinmod.odin.events.TickEvent
import meteordevelopment.orbit.EventHandler

object NoCursorReset {
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
        System.currentTimeMillis() - clock < 150
}