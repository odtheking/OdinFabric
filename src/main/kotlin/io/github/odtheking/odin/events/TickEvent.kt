package io.github.odtheking.odin.events

import io.github.odtheking.odin.events.core.Event

abstract class TickEvent() : Event() {
    class Start() : TickEvent()

    class End() : TickEvent()
}