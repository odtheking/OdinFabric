package com.odtheking.odin.features.impl.dungeon.dungeonwaypoints

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.utils.Colors
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
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

        focused = textField
    }

    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        context.drawCenteredString(font, title, width / 2, height / 2 - 40, Colors.WHITE.rgba)
    }

    override fun keyPressed(input: KeyEvent): Boolean {
        if (input.key == GLFW.GLFW_KEY_ESCAPE) {
            mc.setScreen(null)
            return true
        }

        if (input.key == GLFW.GLFW_KEY_ENTER || input.key == GLFW.GLFW_KEY_KP_ENTER) {
            callback.invoke(textField.value)
            mc.setScreen(null)
            return true
        }

        return super.keyPressed(input)
    }

    override fun charTyped(input: CharacterEvent): Boolean {
        return super.charTyped(input)
    }

    override fun isPauseScreen(): Boolean = false

    fun setCallback(callback: (String) -> Unit): TextPromptScreen {
        this.callback = callback
        return this
    }
}