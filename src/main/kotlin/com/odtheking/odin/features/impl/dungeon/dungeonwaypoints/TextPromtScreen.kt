package com.odtheking.odin.features.impl.dungeon.dungeonwaypoints

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.utils.Colors
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW

object TextPromptScreen : Screen(Text.of("Enter Waypoint Text")) {
    private lateinit var textField: TextFieldWidget
    private var callback: (String) -> Unit = {}

    override fun init() {
        super.init()

        val fieldWidth = 200
        val fieldHeight = 20
        textField = TextFieldWidget(
            textRenderer, width / 2 - fieldWidth / 2, height / 2 - 10,
            fieldWidth, fieldHeight, Text.of("Enter text")
        )

        val submitButton = ButtonWidget.builder(Text.of("Submit")) { _ ->
            callback.invoke(textField.text)
            mc.setScreen(null)
        }.dimensions(width / 2 - 50, height / 2 + 20, 100, 20).build()

        addDrawableChild(textField)
        addDrawableChild(submitButton)

        setFocused(textField)
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, height / 2 - 40, Colors.WHITE.rgba)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            mc.setScreen(null)
            return true
        }

        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            callback.invoke(textField.text)
            mc.setScreen(null)
            return true
        }

        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun charTyped(chr: Char, modifiers: Int): Boolean {
        return super.charTyped(chr, modifiers)
    }

    override fun shouldPause(): Boolean = false

    fun setCallback(callback: (String) -> Unit): TextPromptScreen {
        this.callback = callback
        return this
    }
}