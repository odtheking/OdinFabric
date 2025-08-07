package com.odtheking.odin.features.impl.render

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.ColorSetting
import com.odtheking.odin.clickgui.settings.impl.SelectorSetting
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.ServerUtils
import com.odtheking.odin.utils.render.drawStringWidth
import com.odtheking.odin.utils.toFixed
import net.minecraft.client.gui.DrawContext

object PerformanceHUD : Module(
    name = "Performance HUD",
    description = "Shows performance information on the screen."
) {
    private val nameColor by ColorSetting("Name Color", Color(50, 150, 220), desc = "The color of the stat information.")
    private val valueColor by ColorSetting("Value Color", Colors.WHITE, desc = "The color of the stat values.")
    private val direction by SelectorSetting("Direction", "Horizontal", listOf("Horizontal", "Vertical"), "Direction the information is displayed.")
    private val showFPS by BooleanSetting("Show FPS", true, desc = "Shows the FPS in the HUD.")
    private val showTPS by BooleanSetting("Show TPS", true, desc = "Shows the TPS in the HUD.")
    private val showPing by BooleanSetting("Show Ping", true, desc = "Shows the ping in the HUD.")

    private const val HORIZONTAL = 0

    private val hud by HUD("Performance HUD", "Shows performance information on the screen.") {
        if (!showFPS && !showTPS && !showPing) return@HUD 0f to 0f

        var width = 1f
        var height = 1f
        val lineHeight = mc.textRenderer.fontHeight

        fun renderMetric(label: String, value: String) {
            val w = drawText(this, label, value, if (direction == HORIZONTAL) width else 1f, height)
            if (direction == HORIZONTAL) width += w
            else {
                width = maxOf(width, w)
                height += lineHeight
            }
        }

        if (showTPS) renderMetric("TPS: ", "${ServerUtils.averageTps.toFixed(1)} ")
        if (showFPS) renderMetric("FPS: ", "${mc.currentFps} ")
        if (showPing) renderMetric("Ping: ", "${ServerUtils.averagePing}ms ")

        width to if (direction == HORIZONTAL) lineHeight else height
    }

    private fun drawText(context: DrawContext, name: String, value: String, x: Float, y: Float): Float {
        var width = 0f
        width += context.drawStringWidth(name, x + width, y, nameColor, true)
        width += context.drawStringWidth(value, x + width, y, valueColor, true)
        return width
    }
}