package me.odinmod.odin.events

import me.odinmod.odin.events.core.Event

abstract class TickEvent(): Event() {
    class Start() : TickEvent()

    class End() : TickEvent()
}