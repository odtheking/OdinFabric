package me.odinmod.odin.events.core

import me.odinmod.odin.OdinMod
import meteordevelopment.orbit.ICancellable

abstract class CancellableEvent: ICancellable {
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
        }.onFailure { throwable ->
            throwable.printStackTrace()
        }
        return isCancelled
    }
}