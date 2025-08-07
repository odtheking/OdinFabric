package com.odtheking.odin.events

import com.odtheking.odin.events.core.CancellableEvent
import com.odtheking.odin.utils.skyblock.dungeon.tiles.Room

class RoomEnterEvent(val room: Room?) : CancellableEvent()