package io.github.odtheking.odin.events

import io.github.odtheking.odin.events.core.CancellableEvent

class MessageSentEvent(val message: String) : CancellableEvent()