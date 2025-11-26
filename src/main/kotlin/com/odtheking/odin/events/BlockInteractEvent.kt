package com.odtheking.odin.events

import com.odtheking.odin.events.core.CancellableEvent
import net.minecraft.core.BlockPos

class BlockInteractEvent(val pos: BlockPos) : CancellableEvent()