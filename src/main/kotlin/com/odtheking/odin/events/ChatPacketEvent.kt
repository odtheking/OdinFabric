package com.odtheking.odin.events

import com.odtheking.odin.events.core.Event
import net.minecraft.network.chat.Component

class ChatPacketEvent(val value: String, val component: Component) : Event()