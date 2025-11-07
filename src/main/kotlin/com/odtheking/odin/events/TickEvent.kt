package com.odtheking.odin.events

import com.odtheking.odin.events.core.Event

abstract class TickEvent : Event() {
    class Start : TickEvent()

    class End : TickEvent()
}