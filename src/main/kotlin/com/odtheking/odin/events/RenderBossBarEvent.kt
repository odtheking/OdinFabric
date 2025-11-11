package com.odtheking.odin.events

import com.odtheking.odin.events.core.CancellableEvent
import net.minecraft.world.BossEvent

class RenderBossBarEvent(val bossBar: BossEvent) : CancellableEvent()