package me.odinmod.odin.events

import me.odinmod.odin.events.core.CancellableEvent
import net.minecraft.client.util.InputUtil

class MessageSentEvent(val message: String): CancellableEvent()