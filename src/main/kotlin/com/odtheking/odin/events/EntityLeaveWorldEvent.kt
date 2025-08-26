package com.odtheking.odin.events

import com.odtheking.odin.events.core.CancellableEvent
import net.minecraft.entity.Entity

data class EntityLeaveWorldEvent(val entity: Entity, val removalReason: Entity.RemovalReason) : CancellableEvent()