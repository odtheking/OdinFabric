package com.odtheking.odin.features.impl.dungeon.map

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.utils.equalsOneOf
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes
import kotlin.jvm.optionals.getOrNull

object DungMap {
    var mapCenter: Vec2i = Vec2i(0, 0)
    var startCoords: Vec2i? = null
    var mapSize: Vec2i? = null
    var roomSize: Int? = null
    var shouldScan = false

    fun unload() {
        mapCenter = Vec2i(0, 0)
        shouldScan = false
        startCoords = null
        mapSize = null
        roomSize = null
    }

    fun onChunkLoad() {
        shouldScan = true
        if (mapSize == null) {
            mapSize = when (DungeonUtils.floor?.floorNumber) {
                0 -> Vec2i(4, 4)
                1 -> Vec2i(4, 5)
                2, 3 -> Vec2i(5, 5)
                else -> Vec2i(6, 6)
            }
        }
    }

    fun rescanMapItem(packet: ClientboundMapItemDataPacket) {
        if (!DungeonUtils.inClear || packet.mapId().id and 1000 != 0) return
        val state = mc.level?.getMapData(packet.mapId) ?: return

        if (startCoords == null) {
            if (!initializeMapCoordinates(state.colors)) return
        }

        updatePlayerPositions(packet)
        updateRoomStates(state.colors)
        updateDoorStates(state.colors)

        if (mapSize != null) SpecialColumn.updateSpecialColumn()
    }

    private fun initializeMapCoordinates(colors: ByteArray): Boolean {
        val (greenStart, greenLength) = findGreenRoom(colors)
        if (!greenLength.equalsOneOf(16, 18)) return false

        val (start, center, size) = when (DungeonUtils.floor?.floorNumber) {
            0 -> Triple(Vec2i(22, 22), Vec2i(-137, -137), Vec2i(4, 4))
            1 -> Triple(Vec2i(22, 11), Vec2i(-137, -121), Vec2i(4, 5))
            2, 3 -> Triple(Vec2i(11, 11), Vec2i(-121, -121), Vec2i(5, 5))
            else -> calculateDynamicMapSize(greenStart, greenLength)
        }

        roomSize = greenLength
        startCoords = start
        mapCenter = center
        mapSize = size

        if ((DungeonUtils.isFloor(6, 5) && size.x == 6 && size.z == 6) || (DungeonUtils.isFloor(4) && size.x == 6 && size.z == 5))
            SpecialColumn.column = 5

        return true
    }

    private fun calculateDynamicMapSize(greenStart: Int, greenLength: Int): Triple<Vec2i, Vec2i, Vec2i> {
        val start = Vec2i((greenStart and 127) % (greenLength + 4), (greenStart shr 7) % (greenLength + 4))

        val extra = Vec2i(if (start.x == 5) 1 else 0, if (start.z == 5) 1 else 0)
        val size = Vec2i(5, 5).add(extra)
        val center = Vec2i(-121, -121).add(Vec2i(extra.x * 16, extra.z * 16))

        return Triple(start, center, size)
    }

    private fun updatePlayerPositions(packet: ClientboundMapItemDataPacket) {
        packet.decorations.getOrNull()?.let { decorations ->
            val playerIterator = DungeonUtils.dungeonTeammatesNoSelf.iterator()

            decorations.forEach { decoration ->
                if (decoration.type.value() == MapDecorationTypes.FRAME.value()) return@forEach

                val player = playerIterator.asSequence().firstOrNull { !it.isDead } ?: return@forEach
                player.mapPos = Vec2i(decoration.x.toInt(), decoration.y.toInt())
                player.yaw = decoration.rot() * 360 / 16f
            }
        }
    }

    private fun updateRoomStates(colors: ByteArray) {
        val rs = roomSize ?: return
        val halfRoomSize = rs / 2
        val startCenter = startCoords?.add(Vec2i(halfRoomSize, halfRoomSize)) ?: return
        val tileSize = rs + 4

        MapScanner.allRooms.forEach { (_, room) ->
            val topLeftPlacement = room.places.minWith { a, b ->
                a.x * 1000 + a.z - b.x * 1000 - b.z
            }

            var color = getColorAtPlacement(topLeftPlacement, startCenter, tileSize, colors).toInt()
            var placement = topLeftPlacement

            if (color == 0) {
                room.places.firstNotNullOfOrNull { testPlacement ->
                    val testColor = getColorAtPlacement(testPlacement, startCenter, tileSize, colors)
                    if (testColor != 0.toByte()) Pair(testPlacement, testColor) else null
                }?.let { (visiblePlacement, visibleColor) ->
                    placement = visiblePlacement
                    color = visibleColor.toInt()
                }
            }

            room.updateState(placement, color)
        }
    }

    private fun updateDoorStates(colors: ByteArray) {
        val rs = roomSize ?: return
        val halfRoomSize = rs / 2
        val startCenter = startCoords?.add(Vec2i(halfRoomSize, halfRoomSize)) ?: return

        for (a in 0..4) {
            for (b in 0..5) {
                val doorOffset = halfRoomSize + a * (rs + 4)
                val midRoomOffset = b * (rs + 4)

                val horizontalDoorIndex = startCenter.add(Vec2i(doorOffset, midRoomOffset)).mapIndex()
                if (horizontalDoorIndex < colors.size) {
                    (MapScanner.list[(a * 2 + 1) * 11 + b * 2] as? Door)?.let { door ->
                        updateDoorFromColor(door, colors[horizontalDoorIndex])
                    }
                }

                val verticalDoorIndex = startCenter.add(Vec2i(midRoomOffset, doorOffset)).mapIndex()
                if (verticalDoorIndex < colors.size) {
                    (MapScanner.list[(b * 2) * 11 + (a * 2 + 1)] as? Door)?.let { door ->
                        updateDoorFromColor(door, colors[verticalDoorIndex])
                    }
                }
            }
        }
    }

    fun findGreenRoom(mapData: ByteArray): Pair<Int, Int> {
        var start = -1
        var length = 0

        for (i in mapData.indices) {
            if (mapData[i].toInt() == 30) {
                if (length++ == 0) start = i
            } else {
                if (length >= 16) return start to length
                length = 0
            }
        }

        return start to length
    }

    private fun getColorAtPlacement(placement: Vec2i, startCenter: Vec2i, tileSize: Int, colors: ByteArray): Byte {
        val mapIndex = startCenter.add(placement.multiply(tileSize)).mapIndex()
        return if (mapIndex < colors.size) colors[mapIndex] else 0
    }

    private fun updateDoorFromColor(door: Door, color: Byte) {
        door.locked = when (color.toInt()) {
            119 -> {
                door.rooms.forEach { it.owner.rushRoom = true }
                door.type = Door.Type.WITHER
                true
            }
            82 -> {
                door.rooms.forEach { it.owner.rushRoom = true }
                false
            }
            18 -> {
                door.rooms.forEach { it.owner.rushRoom = true }
                true
            }
            0 -> true
            else -> false
        }
    }

    fun calculateMapSize(): Vec2i = mapSize ?: Vec2i(6, 6)
}