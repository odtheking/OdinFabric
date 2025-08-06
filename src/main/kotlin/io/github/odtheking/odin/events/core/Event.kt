package io.github.odtheking.odin.events.core

import io.github.odtheking.odin.OdinMod
import io.github.odtheking.odin.utils.logError

abstract class Event {

    fun postAndCatch() {
        runCatching {
            OdinMod.EVENT_BUS.post(this)
        }.onFailure {
            logError(it, this)
        }
    }
}