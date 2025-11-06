package com.odtheking.odin.utils

import com.odtheking.odin.events.ChatPacketEvent
import net.minecraft.text.Text
import java.util.concurrent.ConcurrentLinkedQueue

object ChatManager {
    private val cancelQueue = ConcurrentLinkedQueue<Text>()

    fun ChatPacketEvent.hideMessage() {
        cancelQueue.add(text)
    }

    internal fun shouldCancelMessage(message: Text): Boolean {
        return cancelQueue.remove(message)
    }
}