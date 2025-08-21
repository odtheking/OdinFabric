package com.odtheking.odin.events

import com.odtheking.odin.events.core.CancellableEvent
import com.odtheking.odin.features.impl.floor7.terminalhandler.TerminalHandler

open class TerminalEvent(val terminal: TerminalHandler) : CancellableEvent() {
    class Opened(terminal: TerminalHandler) : TerminalEvent(terminal)
    class Updated(terminal: TerminalHandler) : TerminalEvent(terminal)
    class Closed(terminal: TerminalHandler) : TerminalEvent(terminal)
    class Solved(terminal: TerminalHandler) : TerminalEvent(terminal)
}