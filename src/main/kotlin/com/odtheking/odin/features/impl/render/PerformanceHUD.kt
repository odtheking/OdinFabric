package com.odtheking.odin.features.impl.render

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.ColorSetting
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.ServerUtils
import com.odtheking.odin.utils.render.drawStringWidth

object PerformanceHUD : Module(
    name = "Performance HUD",
    description = "Shows performance information on the screen."
) {
    private val nameColor by ColorSetting(
        "Name Color",
        Color(50, 150, 220),
        desc = "The color of the stat information."
    )
    private val valueColor by ColorSetting("Value Color", Colors.WHITE, desc = "The color of the stat values.")
    private val showFPS by BooleanSetting("Show FPS", true, desc = "Shows the FPS in the HUD.")
    private val showTPS by BooleanSetting("Show TPS", true, desc = "Shows the TPS in the HUD.")
    private val showPing by BooleanSetting("Show Ping", true, desc = "Shows the ping in the HUD.")

    private val hud by HUD("Performance HUD", "Shows performance information on the screen.") {
        if (!showFPS && !showTPS && !showPing) return@HUD 0f to 0f
        var width = 1f

        if (showTPS) {
            width += drawStringWidth("TPS: ", width, 1f, nameColor, true)
            width += drawStringWidth("${ServerUtils.averageTps.toInt()} ", width, 1f, valueColor, true)
        }
        if (showFPS) {
            width += drawStringWidth("FPS: ", width, 1f, nameColor, true)
            width += drawStringWidth("${mc.currentFps} ", width, 1f, valueColor, true)
        }
        if (showPing) {
            width += drawStringWidth("Ping: ", width, 1f, nameColor, true)
            width += drawStringWidth("${ServerUtils.averagePing}ms", width, 1f, valueColor, true)
        }

        width to mc.textRenderer.fontHeight
    }
}