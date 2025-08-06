package io.github.odtheking.odin.utils.ui

import io.github.odtheking.odin.OdinMod.mc

inline val mouseX: Float
    get() =
        mc.mouse.x.toFloat()

inline val mouseY: Float
    get() =
        mc.mouse.y.toFloat()

fun isAreaHovered(x: Float, y: Float, w: Float, h: Float): Boolean =
    mouseX in x..(x + w) && mouseY in y..(y + h)

fun isAreaHovered(x: Float, y: Float, w: Float): Boolean =
    mouseX in x..(x + w) && mouseY >= y