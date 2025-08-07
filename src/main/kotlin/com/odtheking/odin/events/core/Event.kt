package com.odtheking.odin.events.core

import com.odtheking.odin.OdinMod
import com.odtheking.odin.utils.logError

abstract class Event {

    fun postAndCatch() {
        runCatching {
            OdinMod.EVENT_BUS.post(this)
        }.onFailure {
            logError(it, this)
        }
    }
}