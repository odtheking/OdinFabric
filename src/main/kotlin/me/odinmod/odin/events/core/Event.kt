package me.odinmod.odin.events.core

import me.odinmod.odin.OdinMod
import me.odinmod.odin.utils.logError

abstract class Event {

    fun postAndCatch() {
        runCatching {
            OdinMod.EVENT_BUS.post(this)
        }.onFailure {
            logError(it, this)
        }
    }
}