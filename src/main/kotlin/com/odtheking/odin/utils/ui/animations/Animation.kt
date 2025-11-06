package com.odtheking.odin.utils.ui.animations

abstract class Animation<T>(private val duration: Long) {

    private var animationState: AnimationState? = null

    private class AnimationState(
        var startTime: Long,
        var reversed: Boolean
    )

    fun start() {
        val currentTime = System.currentTimeMillis()
        val state = animationState

        if (state == null) {
            animationState = AnimationState(currentTime, false)
            return
        }

        val percent = ((currentTime - state.startTime) / duration.toFloat()).coerceIn(0f, 1f)
        state.reversed = !state.reversed
        state.startTime = currentTime - ((1f - percent) * duration).toLong()
    }

    fun getPercent(): Float {
        val state = animationState ?: return 100f

        val percent = ((System.currentTimeMillis() - state.startTime) / duration.toFloat() * 100f)
        if (percent >= 100f) {
            animationState = null
            return 100f
        }
        return percent.coerceAtMost(100f)
    }

    fun isAnimating(): Boolean = animationState != null

    abstract fun get(start: T, end: T, reverse: Boolean = false): T
}