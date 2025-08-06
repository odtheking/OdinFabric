package io.github.odtheking.odin.features.impl.render

import io.github.odtheking.odin.clickgui.ClickGUI
import io.github.odtheking.odin.clickgui.HudManager
import io.github.odtheking.odin.clickgui.settings.impl.ActionSetting
import io.github.odtheking.odin.clickgui.settings.impl.BooleanSetting
import io.github.odtheking.odin.clickgui.settings.impl.ColorSetting
import io.github.odtheking.odin.clickgui.settings.impl.MapSetting
import io.github.odtheking.odin.features.Category
import io.github.odtheking.odin.features.Module
import io.github.odtheking.odin.utils.Color
import org.lwjgl.glfw.GLFW

object ClickGUIModule : Module(
    name = "Click GUI",
    description = "Allows you to customize the UI.",
    key = GLFW.GLFW_KEY_RIGHT_SHIFT
) {
    val enableNotification by BooleanSetting(
        "Chat notifications",
        true,
        desc = "Sends a message when you toggle a module with a keybind"
    )
    val clickGUIColor by ColorSetting("Color", Color(50, 150, 220), desc = "The color of the Click GUI.")
    private val action by ActionSetting("Open HUD Editor", desc = "Opens the HUD editor when clicked.") {
        mc.setScreen(
            HudManager
        )
    }

    override fun onKeybind() {
        toggle()
    }

    override fun onEnable() {
        mc.setScreen(ClickGUI)
        super.onEnable()
        toggle()
    }

    data class PanelData(var x: Float = 10f, var y: Float = 10f, var extended: Boolean = true)

    val panelSetting by MapSetting("Panel Settings", mutableMapOf<Category, PanelData>())

    init {
        resetPositions()
    }

    fun resetPositions() {
        Category.entries.forEach {
            panelSetting[it] = PanelData(10f + 260f * it.ordinal, 10f, true)
        }
    }
}