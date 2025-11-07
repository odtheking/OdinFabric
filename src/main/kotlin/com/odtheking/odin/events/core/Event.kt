package com.odtheking.odin.events.core

import com.odtheking.odin.utils.logError

abstract class Event {

    open fun postAndCatch(): Boolean {
        runCatching {
            EventBus.post(this)
        }.onFailure {
            logError(it, this)
        }
        return false
    }
}