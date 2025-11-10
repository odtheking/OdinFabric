package com.odtheking.odin.features.impl.dungeon.dungeonwaypoints

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.utils.Colors
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW

object TextPromptScreen : Screen(Component.literal("Enter Waypoint Text")) {
    private lateinit var textField: EditBox
    private var callback: (String) -> Unit = {}

    override fun init() {
        super.init()

        val fieldWidth = 200
        val fieldHeight = 20
        textField = EditBox(
            font, width / 2 - fieldWidth / 2, height / 2 - 10,
            fieldWidth, fieldHeight, Component.literal("Enter text")
        )

        val submitButton = Button.builder(Component.literal("Submit")) { _ ->
            callback.invoke(textField.value)
            mc.setScreen(null)
        }.bounds(width / 2 - 50, height / 2 + 20, 100, 20).build()

        addRenderableWidget(textField)
        addRenderableWidget(submitButton)

        setFocused(textField)
    }

    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        context.drawCenteredString(font, title, width / 2, height / 2 - 40, Colors.WHITE.rgba)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            mc.setScreen(null)
            return true
        }

        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            callback.invoke(textField.value)
            mc.setScreen(null)
            return true
        }

        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun charTyped(chr: Char, modifiers: Int): Boolean {
        return super.charTyped(chr, modifiers)
    }

    override fun isPauseScreen(): Boolean = false

    fun setCallback(callback: (String) -> Unit): TextPromptScreen {
        this.callback = callback
        return this
    }
}