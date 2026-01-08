package com.odtheking.odin.utils.ui

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.features.impl.render.ClickGUIModule

inline val mouseX: Float
    get() =
        mc.mouseHandler.xpos().toFloat() / ClickGUIModule.guiScale

inline val mouseY: Float
    get() =
        mc.mouseHandler.ypos().toFloat() / ClickGUIModule.guiScale

inline val unscaledMouseX: Float
    get() =
        mc.mouseHandler.xpos().toFloat()

inline val unscaledMouseY: Float
    get() =
        mc.mouseHandler.ypos().toFloat()

fun isAreaHovered(x: Float, y: Float, w: Float, h: Float, scaled: Boolean = true): Boolean =
    if (scaled) mouseX in x..(x + w) && mouseY in y..(y + h)
    else unscaledMouseX in x..(x + w) && unscaledMouseY in y..(y + h)

fun isAreaHovered(x: Float, y: Float, w: Float, scaled: Boolean = true): Boolean =
    if (scaled) mouseX in x..(x + w) && mouseY >= y
    else unscaledMouseX in x..(x + w) && unscaledMouseY >= y

fun getQuadrant(): Int =
    when {
        unscaledMouseX >= mc.window.width / 2 -> if (unscaledMouseY >= mc.window.height / 2) 4 else 2
        else -> if (unscaledMouseY >= mc.window.height / 2) 3 else 1
    }