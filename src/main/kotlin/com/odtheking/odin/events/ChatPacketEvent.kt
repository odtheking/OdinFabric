package com.odtheking.odin.events

import com.odtheking.odin.events.core.Event
import net.minecraft.text.Text

class ChatPacketEvent(val value: String, val text: Text) : Event()