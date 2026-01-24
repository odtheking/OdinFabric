package com.odtheking.odin.events

import com.odtheking.odin.events.core.CancellableEvent
import com.odtheking.odin.utils.skyblock.dungeon.terminals.terminalhandler.TerminalHandler

open class TerminalEvent(val terminal: TerminalHandler) : CancellableEvent() {
    class Open(terminal: TerminalHandler) : TerminalEvent(terminal)
    class Close(terminal: TerminalHandler) : TerminalEvent(terminal)
    class Solve(terminal: TerminalHandler) : TerminalEvent(terminal)
}