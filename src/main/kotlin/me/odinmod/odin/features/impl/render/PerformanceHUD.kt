package me.odinmod.odin.features.impl.render

import me.odinmod.odin.clickgui.settings.impl.ColorSetting
import me.odinmod.odin.features.Module
import me.odinmod.odin.utils.Color
import me.odinmod.odin.utils.Colors
import me.odinmod.odin.utils.ServerUtils
import me.odinmod.odin.utils.drawStringWidth

object PerformanceHUD: Module(
    name = "Performance HUD",
    description = "Shows performance information on the screen."
) {
    private val nameColor by ColorSetting("Name Color", Color(50, 150, 220), desc = "The color of the stat information.")
    private val valueColor by ColorSetting("Value Color", Colors.WHITE, desc = "The color of the stat values.")

    private val hud by HUD("Performance HUD", "Shows performance information on the screen.") {
        var width = 1f
        width += drawStringWidth("TPS: ", width, 1f, nameColor.rgba, true)
        width += drawStringWidth("${ServerUtils.averageTps.toInt()} ", width, 1f, valueColor.rgba, true)

        width += drawStringWidth("FPS: ", width, 1f, nameColor.rgba, true)
        width += drawStringWidth("${mc.currentFps} ", width, 1f, valueColor.rgba, true)

        width += drawStringWidth("Ping: ", width, 1f, nameColor.rgba, true)
        width += drawStringWidth("${ServerUtils.currentPing}ms", width, 1f, valueColor.rgba, true)

        width to mc.textRenderer.fontHeight
    }
}