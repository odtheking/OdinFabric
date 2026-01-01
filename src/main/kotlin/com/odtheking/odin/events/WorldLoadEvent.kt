package com.odtheking.odin.events

import com.odtheking.odin.events.core.Event

abstract class WorldEvent : Event() {
    class Load : WorldEvent()

    class Unload : WorldEvent()
}