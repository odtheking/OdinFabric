package me.odinmod.odin.events

import me.odinmod.odin.events.core.CancellableEvent

class MessageSentEvent(val message: String): CancellableEvent()