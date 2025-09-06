package com.odtheking.odin.events

import com.odtheking.odin.events.core.CancellableEvent
import net.minecraft.entity.boss.BossBar

class RenderBossBarEvent(val bossBar: BossBar) : CancellableEvent()