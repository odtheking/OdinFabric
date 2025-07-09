package me.odinmod.odin.utils.ui

import me.odinmod.odin.OdinMod.mc
import net.minecraft.client.util.Window

const val VIRTUAL_WIDTH = 1920f
const val VIRTUAL_HEIGHT = 1080f

inline val scaledMouseX: Float get() =
    (mc.mouse.x.toFloat() / mc.window.width.toFloat()) * VIRTUAL_WIDTH

inline val scaledMouseY: Float get() =
    (mc.mouse.y.toFloat() / mc.window.height.toFloat()) * VIRTUAL_HEIGHT

fun isAreaHovered(x: Float, y: Float, w: Float, h: Float): Boolean =
    scaledMouseX in x..(x + w) && scaledMouseY in y..(y + h)

fun isAreaHovered(x: Float, y: Float, w: Float): Boolean =
    scaledMouseX in x..(x + w) && scaledMouseY >= y

inline val Window.widthResFactor: Float get() =
    width / VIRTUAL_WIDTH

inline val Window.heightResFactor: Float get() =
    height / VIRTUAL_HEIGHT