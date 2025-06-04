package me.odinmod.odin.events.core

import me.odinmod.odin.OdinMod

abstract class Event {

    fun postAndCatch() {
        runCatching {
            OdinMod.EVENT_BUS.post(this)
        }.onFailure { throwable ->
            throwable.printStackTrace()
        }
    }
}