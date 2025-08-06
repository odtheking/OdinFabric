package io.github.odtheking.odin.events

import io.github.odtheking.odin.events.core.CancellableEvent
import net.minecraft.client.util.InputUtil

class InputEvent(val key: InputUtil.Key) : CancellableEvent()