package com.odtheking.odin.features.impl.dungeon.map

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.utils.Vec2
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.ScanUtils
import com.odtheking.odin.utils.skyblock.dungeon.tiles.*
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks

object MapScanner {
    val topLeftRoom = Vec2i(-185, -185)
    var loadedAllRooms = false

    val allRooms = HashMap<String, MapRoom>()
    val roomTiles = mutableListOf<Tile>()
    val doors = mutableListOf<Door>()
    var blood: MapRoom? = null

    var list = Array<Tile>(121) { Unknown(Vec2i(it / 11, it % 11)) }
        private set

    private val doorPositions = HashSet<Vec2i>()

    fun unload() {
        allRooms.clear()
        roomTiles.clear()
        doors.clear()
        doorPositions.clear()
        list = Array(121) { Unknown(Vec2i(it / 11, it % 11)) }
        loadedAllRooms = false
        blood = null
    }

    fun scan(world: Level) {
        if (!DungMap.shouldScan) return

        if (loadedAllRooms) {
            scanDoors(world)
            return
        }

        scanRooms()
        scanRoomRotations()
        scanDoors(world)

        loadedAllRooms = checkIfFullyLoaded()
    }

    private fun checkIfFullyLoaded(): Boolean {
        val size = DungMap.mapSize ?: return false

        if (SpecialColumn.column != -1 && allRooms.values.count { it.data.type == RoomType.PUZZLE && it.rotation != Rotations.NONE } != DungeonUtils.puzzleCount)
            return false

        val zMax = if (SpecialColumn.column != -1) size.z - 1 else size.z
        for (x in 0 until size.x) {
            for (z in 0 until zMax) {
                if (list[Vec2i(x, z).roomListIndex()] is Unknown) return false
            }
        }

        return allRooms.none { (_, room) -> room.rotation == Rotations.NONE }
    }

    private fun scanRooms() {
        for (x in 0..5) {
            for (z in 0..5) {
                val tile = Vec2i(x, z)
                val listIndex = tile.roomListIndex()

                if (list[listIndex] !is Unknown) continue

                val curr = topLeftRoom.add(tile.multiply(32))

                ScanUtils.scanRoom(Vec2(curr.x, curr.z))?.let { room ->
                    val chunk = mc.level?.getChunk(curr.x shr 4, curr.z shr 4) ?: return@let
                    val roomHeight = ScanUtils.getTopLayerOfRoom(Vec2(curr.x, curr.z), chunk)
                    val found = allRooms.getOrPut(room.data.name) { MapRoom(room.data, roomHeight) }

                    if (found.tiles.none { it.pos == curr }) {
                        found.roomTile(curr)?.let { roomTile ->
                            roomTiles.add(roomTile)
                            list[listIndex] = roomTile
                        }
                    }
                }
            }
        }
    }

    private fun scanRoomRotations() {
        allRooms.forEach { (_, room) ->
            if (room.rotation != Rotations.NONE) return@forEach

            val requiredTiles = when (room.data.shape) {
                RoomShape.S4x1 -> 7
                RoomShape.S3x1 -> 5
                else -> 0
            }

            if (requiredTiles > 0 && room.tiles.size != requiredTiles) return@forEach

            val tempRoom = Room(
                data = room.data,
                roomComponents = room.tiles.map { RoomComponent(it.pos.x, it.pos.z) }.toMutableSet()
            )

            ScanUtils.updateRotation(tempRoom, room.height)

            if (tempRoom.rotation != Rotations.NONE) room.rotation = tempRoom.rotation
        }
    }

    private fun scanDoors(world: Level) {
        fun handleDoor(pos: Vec2i, a: Vec2i, b: Vec2i) {
            if (pos in doorPositions) return

            val tileA = list[a.x * 11 + a.z]
            val tileB = list[b.x * 11 + b.z]

            val rooms = listOfNotNull(tileA as? MapRoom.RoomTile, tileB as? MapRoom.RoomTile)
            if (rooms.size != 2) return

            val chunk = world.getChunk(pos.x shr 4, pos.z shr 4)
            val height = ScanUtils.getTopLayerOfRoom(Vec2(pos.x, pos.z), chunk)

            val x = (pos.x + 185) / 16
            val z = (pos.z + 185) / 16
            val listIndex = x * 11 + z

            if (height !in arrayOf(73, 81)) {
                if (height <= 73) return

                if (rooms.any { it.owner.data.type == RoomType.ENTRANCE }) {
                    val tile = Door(pos, Door.Type.NORMAL, rooms)
                    doors.add(tile)
                    doorPositions.add(pos)
                    list[listIndex] = tile
                    return
                }

                rooms[0].owner.separator(pos)?.let { list[listIndex] = it }
                return
            }

            val type = when (world.getBlockState(BlockPos(pos.x, 69, pos.z)).block) {
                Blocks.COAL_BLOCK -> {
                    rooms.forEach { it.owner.rushRoom = true }
                    Door.Type.WITHER
                }
                Blocks.RED_TERRACOTTA -> Door.Type.BLOOD
                else -> Door.Type.NORMAL
            }

            val tile = Door(pos, type, rooms)
            doors.add(tile)
            doorPositions.add(pos)
            list[listIndex] = tile
        }

        for (a in 0..5) {
            for (b in 0..4) {
                val goingRight = Vec2i(topLeftRoom.x + a * 32, topLeftRoom.z + 16 + 32 * b)
                val x1 = (goingRight.x + 185) / 16
                val z1 = (goingRight.z + 185) / 16
                handleDoor(goingRight, Vec2i(x1, z1 + 1), Vec2i(x1, z1 - 1))

                val goingDown = Vec2i(goingRight.z, goingRight.x)
                val x2 = (goingDown.x + 185) / 16
                val z2 = (goingDown.z + 185) / 16
                handleDoor(goingDown, Vec2i(x2 + 1, z2), Vec2i(x2 - 1, z2))
            }
        }

        for (a in 0..4) {
            for (b in 0..4) {
                val x = a * 2 + 1
                val z = b * 2 + 1
                val pos = Vec2i(topLeftRoom.x + x * 16, topLeftRoom.z + z * 16)

                val roomsList = listOf(
                    list[(x - 1) * 11 + z - 1],
                    list[(x - 1) * 11 + z + 1],
                    list[(x + 1) * 11 + z - 1],
                    list[(x + 1) * 11 + z + 1]
                )

                if (roomsList.any { it !is MapRoom.RoomTile }) continue
                val mapRoom = roomsList[0] as MapRoom.RoomTile
                if (mapRoom.owner.tiles.any { it.pos == pos }) continue

                val chunk = world.getChunk(pos.x shr 4, pos.z shr 4)
                val height = ScanUtils.getTopLayerOfRoom(Vec2(pos.x, pos.z), chunk)

                if (height > 73 && height != 140)
                    mapRoom.owner.separator(pos)?.let { list[x * 11 + z] = it }
            }
        }
    }
}

