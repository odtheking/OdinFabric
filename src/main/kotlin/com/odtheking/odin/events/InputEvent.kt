package com.odtheking.odin.events

import com.odtheking.odin.events.core.CancellableEvent
import net.minecraft.client.util.InputUtil

class InputEvent(val key: InputUtil.Key) : CancellableEvent()