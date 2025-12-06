package com.odtheking.odin.events

import com.odtheking.odin.events.core.CancellableEvent

abstract class PartyEvent(val members: List<String>) : CancellableEvent() {

    class Leave(members: List<String>) : PartyEvent(members)
}