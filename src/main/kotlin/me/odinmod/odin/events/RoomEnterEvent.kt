package me.odinmod.odin.events

import me.odinmod.odin.events.core.CancellableEvent
import me.odinmod.odin.utils.skyblock.dungeon.tiles.Room

class RoomEnterEvent(val room: Room?): CancellableEvent()