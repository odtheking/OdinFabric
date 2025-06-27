package me.odinmod.odin.utils.ui

import me.odinmod.odin.OdinMod.mc

object MouseUtils {
    val virtualWidth = 1920f
    val virtualHeight = 1080f

    fun getScaledMouseX(): Float {
        val mouseX = mc.mouse.x.toFloat()
        val windowWidth = mc.window.width.toFloat()
        return (mouseX / windowWidth) * virtualWidth
    }

    fun getScaledMouseY(): Float {
        val mouseY = mc.mouse.y.toFloat()
        val windowHeight = mc.window.height.toFloat()
        return (mouseY / windowHeight) * virtualHeight
    }

    fun isAreaHovered(x: Float, y: Float, w: Float, h: Float): Boolean {
        val mouseX = getScaledMouseX()
        val mouseY = getScaledMouseY()
        return mouseX in x..(x + w) && mouseY in y..(y + h)
    }

    fun isAreaHovered(x: Float, y: Float, w: Float): Boolean {
        val mouseX = getScaledMouseX()
        val mouseY = getScaledMouseY()
        return mouseX in x..(x + w) && mouseY >= y
    }
}