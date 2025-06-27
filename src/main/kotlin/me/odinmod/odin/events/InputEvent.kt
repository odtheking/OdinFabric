package me.odinmod.odin.events

import me.odinmod.odin.events.core.CancellableEvent
import net.minecraft.client.util.InputUtil

class InputEvent(val key: InputUtil.Key): CancellableEvent()