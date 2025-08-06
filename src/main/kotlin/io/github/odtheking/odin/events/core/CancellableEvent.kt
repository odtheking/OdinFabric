package io.github.odtheking.odin.events.core

import io.github.odtheking.odin.OdinMod
import io.github.odtheking.odin.utils.logError
import meteordevelopment.orbit.ICancellable

abstract class CancellableEvent : ICancellable {
    private var cancelled = false

    override fun isCancelled(): Boolean {
        return cancelled
    }

    override fun setCancelled(cancelled: Boolean) {
        this.cancelled = cancelled
    }

    fun postAndCatch(): Boolean {
        runCatching {
            OdinMod.EVENT_BUS.post(this)
        }.onFailure {
            logError(it, this)
        }
        return isCancelled
    }
}