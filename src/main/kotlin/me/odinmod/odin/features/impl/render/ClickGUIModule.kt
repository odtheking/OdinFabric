package me.odinmod.odin.features.impl.render

import me.odinmod.odin.clickgui.ClickGUI
import me.odinmod.odin.clickgui.settings.impl.*
import me.odinmod.odin.features.Category
import me.odinmod.odin.features.Module
import me.odinmod.odin.utils.Color
import me.odinmod.odin.utils.modMessage
import org.lwjgl.glfw.GLFW

object ClickGUIModule : Module(
    name = "Click GUI",
    key = GLFW.GLFW_KEY_RIGHT_SHIFT,
    description = "Allows you to customize the UI."
) {
    val enableNotification by BooleanSetting("Chat notifications", true, desc = "Sends a message when you toggle a module with a keybind")
    val clickGUIColor by ColorSetting("Click GUI Color", Color(50, 150, 220), desc = "The color of the Click GUI.")
    private val action by ActionSetting("Open HUD Editor", desc = "Opens the HUD editor when clicked.") { modMessage("Test action executed!") }

    private val testTextInput by StringSetting(
        "Test Text Input",
        default = "Hello, Odin!",
        desc = "This is a test text input setting."
    )

    private val testNumberInput by NumberSetting(
        "Test Number Input",
        default = 42,
        min = 0,
        max = 100,
        desc = "This is a test number input setting."
    )

    override fun onKeybind() {
        toggle()
    }

    override fun onEnable() {
        mc.setScreen(ClickGUI)
        super.onEnable()
        toggle()
    }

    val panelX = mutableMapOf<Category, NumberSetting<Float>>()
    val panelY = mutableMapOf<Category, NumberSetting<Float>>()
    val panelExtended = mutableMapOf<Category, BooleanSetting>()

    fun resetPositions() {
        Category.entries.forEach {
            val incr = 10f + 260f * it.ordinal
            panelX.getOrPut(it) { +NumberSetting(it.name + ",x", default = incr, desc = "", hidden = true) }.value = incr
            panelY.getOrPut(it) { +NumberSetting(it.name + ",y", default = 10f, desc = "", hidden = true) }.value = 10f
            panelExtended.getOrPut(it) { +BooleanSetting(it.name + ",extended", default = true, desc = "", hidden = true) }.enabled = true
        }
    }
}