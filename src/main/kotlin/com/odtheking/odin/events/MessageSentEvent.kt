package com.odtheking.odin.events

import com.odtheking.odin.events.core.CancellableEvent

class MessageSentEvent(val message: String) : CancellableEvent()