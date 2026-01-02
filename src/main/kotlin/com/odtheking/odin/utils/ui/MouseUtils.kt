package com.odtheking.odin.utils.ui

import com.odtheking.odin.OdinMod.mc

inline val mouseX: Float
    get() =
        mc.mouseHandler.xpos().toFloat() * (1920f / mc.window.width.toFloat())

inline val mouseY: Float
    get() =
        mc.mouseHandler.ypos().toFloat() * (1080f / mc.window.height.toFloat())

fun isAreaHovered(x: Float, y: Float, w: Float, h: Float): Boolean =
    mouseX in x..(x + w) && mouseY in y..(y + h)

fun isAreaHovered(x: Float, y: Float, w: Float): Boolean =
    mouseX in x..(x + w) && mouseY >= y

fun getQuadrant(): Int =
    when {
        mouseX >= 1920f / 2 -> if (mouseY >= 1080f / 2) 4 else 2
        else -> if (mouseY >= 1080f / 2) 3 else 1
    }