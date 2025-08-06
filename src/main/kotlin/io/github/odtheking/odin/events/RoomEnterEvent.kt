package io.github.odtheking.odin.events

import io.github.odtheking.odin.events.core.CancellableEvent
import io.github.odtheking.odin.utils.skyblock.dungeon.tiles.Room

class RoomEnterEvent(val room: Room?) : CancellableEvent()