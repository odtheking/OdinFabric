package com.odtheking.odin.events

import com.odtheking.odin.events.core.Event
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos

class BlockUpdateEvent(val pos: BlockPos, val old: BlockState, val updated: BlockState) : Event()