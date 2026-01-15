package com.odtheking.odin.utils.skyblock.dungeon.tiles

import com.odtheking.odin.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints
import com.odtheking.odin.utils.Vec2
import net.minecraft.core.BlockPos

data class Room(
    var rotation: Rotations = Rotations.NONE,
    var data: RoomData,
    var clayPos: BlockPos = BlockPos(0, 0, 0),
    val roomComponents: MutableSet<RoomComponent>,
    var waypoints: MutableSet<DungeonWaypoints.DungeonWaypoint> = mutableSetOf(),
)

data class RoomComponent(val x: Int, val z: Int, val core: Int = 0) {
    val vec2 = Vec2(x, z)
    val blockPos = BlockPos(x, 70, z)
}

data class RoomData(
    val name: String, val type: RoomType, val cores: List<Int>,
    val crypts: Int, val secrets: Int, val trappedChests: Int, val shape: RoomShape
)