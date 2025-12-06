package com.odtheking.odin.features.impl.skyblock

import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module

object NoCursorReset : Module(
    name = "No Cursor Reset",
    description = "Prevents the cursor from being reset when opening a GUI."
) {
    private var clock = System.currentTimeMillis()
    private var wasNotNull = false

    init {
        on<TickEvent.End> {
            if (mc.screen != null) {
                wasNotNull = true
                clock = System.currentTimeMillis()
            } else if (wasNotNull && mc.screen == null) {
                wasNotNull = false
                clock = System.currentTimeMillis()
            }
        }
    }

    @JvmStatic
    fun shouldHookMouse(): Boolean =
        System.currentTimeMillis() - clock < 150 && enabled
}