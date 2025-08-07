package com.odtheking.odin.utils.ui

import com.odtheking.odin.utils.ui.animations.LinearAnimation

class HoverHandler(private val startDelay: Long, delay: Long) {

    constructor(delay: Long) : this(0, delay)

    val anim = LinearAnimation<Float>(delay)

    private var hoverStartTime: Long? = null
    var hasStarted = false

    fun percent(): Float {
        if (!hasStarted) return 100f - anim.getPercent()
        return anim.getPercent()
    }

    fun handle(x: Float, y: Float, w: Float, h: Float) {
        if (isAreaHovered(x, y, w, h)) {
            if (hoverStartTime == null) hoverStartTime = System.currentTimeMillis()

            if (System.currentTimeMillis() - hoverStartTime!! >= startDelay && !hasStarted) {
                anim.start()
                hasStarted = true
            }
        } else {
            hoverStartTime = null
            if (hasStarted) {
                anim.start()
                hasStarted = false
            }
        }
    }

    fun reset() {
        hoverStartTime = null
        hasStarted = false
    }
}