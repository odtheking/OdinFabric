package me.odinmod.odin.events

import me.odinmod.odin.events.core.Event

abstract class ServerEvent(val serverAddress: String): Event() {
    class Connect(serverAddress: String) : ServerEvent(serverAddress)

    class Disconnect(serverAddress: String) : ServerEvent(serverAddress)
}