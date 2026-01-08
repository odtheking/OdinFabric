package com.odtheking.odin.utils.ui

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.features.impl.render.ClickGUIModule

inline val mouseX: Float
    get() =
        mc.mouseHandler.xpos().toFloat() / ClickGUIModule.guiScale

inline val mouseY: Float
    get() =
        mc.mouseHandler.ypos().toFloat() / ClickGUIModule.guiScale

fun isAreaHovered(x: Float, y: Float, w: Float, h: Float): Boolean =
    mouseX in x..(x + w) && mouseY in y..(y + h)

fun isAreaHovered(x: Float, y: Float, w: Float): Boolean =
    mouseX in x..(x + w) && mouseY >= y

fun getQuadrant(): Int =
    when {
        mouseX >= mc.window.width / 2 -> if (mouseY >= mc.window.height / 2) 4 else 2
        else -> if (mouseY >= mc.window.height / 2) 3 else 1
    }