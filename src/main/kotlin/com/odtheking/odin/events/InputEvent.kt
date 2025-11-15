package com.odtheking.odin.events

import com.mojang.blaze3d.platform.InputConstants
import com.odtheking.odin.events.core.CancellableEvent

class InputEvent(val key: InputConstants.Key) : CancellableEvent()