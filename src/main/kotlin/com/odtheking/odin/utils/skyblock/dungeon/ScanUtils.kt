package com.odtheking.odin.utils.skyblock.dungeon

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.odtheking.odin.OdinMod.logger
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.RoomEnterEvent
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.WorldLoadEvent
import com.odtheking.odin.utils.Vec2
import com.odtheking.odin.utils.devMessage
import com.odtheking.odin.utils.equalsOneOf
import com.odtheking.odin.utils.skyblock.Island
import com.odtheking.odin.utils.skyblock.LocationUtils
import com.odtheking.odin.utils.skyblock.dungeon.DungeonListener.inBoss
import com.odtheking.odin.utils.skyblock.dungeon.tiles.*
import meteordevelopment.orbit.EventHandler
import net.minecraft.block.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Direction.Axis
import java.io.FileNotFoundException

object ScanUtils {
    private const val ROOM_SIZE_SHIFT = 5  // Since ROOM_SIZE = 32 (2^5) so we can perform bitwise operations
    private const val START = -185

    private var lastRoomPos: Vec2 = Vec2(0, 0)
    private val roomList: Set<RoomData> = loadRoomData()
    var currentRoom: Room? = null
        private set
    var passedRooms: MutableSet<Room> = mutableSetOf()
        private set

    private fun loadRoomData(): Set<RoomData> {
        return try {
            GsonBuilder()
                .registerTypeAdapter(
                    RoomData::class.java,
                    RoomDataDeserializer()
                )
                .create().fromJson(
                    (ScanUtils::class.java.getResourceAsStream("/assets/odin/rooms.json")
                        ?: throw FileNotFoundException()).bufferedReader(),
                    object : TypeToken<Set<RoomData>>() {}.type
                )
        } catch (e: Exception) {
            logger.error("Error reading room data", e)
            println(e.message)
            setOf()
        }
    }

    @EventHandler
    fun onTick(event: TickEvent.End) {
        if (mc.world == null || mc.player == null) return

        if ((!DungeonUtils.inDungeons && !LocationUtils.currentArea.isArea(Island.SinglePlayer)) || inBoss) {
            currentRoom?.let { RoomEnterEvent(null).postAndCatch() }
            return
        } // We want the current room to register as null if we are not in a dungeon

        val roomCenter = getRoomCenter(mc.player?.x?.toInt() ?: return, mc.player?.z?.toInt() ?: return)
        if (roomCenter == lastRoomPos && LocationUtils.currentArea.isArea(Island.SinglePlayer)) return // extra SinglePlayer caching for invalid placed rooms
        lastRoomPos = roomCenter

        passedRooms.find { previousRoom -> previousRoom.roomComponents.any { it.vec2 == roomCenter } }?.let { room ->
            if (currentRoom?.roomComponents?.none { it.vec2 == roomCenter } == true) RoomEnterEvent(room).postAndCatch()
            return
        } // We want to use cached rooms instead of scanning it again if we have already passed through it and if we are already in it we don't want to trigger the event

        scanRoom(roomCenter)?.let { room -> if (room.rotation != Rotations.NONE) RoomEnterEvent(room).postAndCatch() }
    }

    private val horizontals = Direction.entries.filter { it.axis.isHorizontal }

    private fun updateRotation(room: Room) {
        val roomHeight = getTopLayerOfRoom(room.roomComponents.first().vec2)
        if (room.data.name == "Fairy") { // Fairy room doesn't have a clay block so we need to set it manually
            room.clayPos = room.roomComponents.firstOrNull()?.let { BlockPos(it.x - 15, roomHeight, it.z - 15) } ?: return
            room.rotation = Rotations.SOUTH
            return
        }
        room.rotation = Rotations.entries.dropLast(1).find { rotation ->
            room.roomComponents.any { component ->
                BlockPos(component.x + rotation.x, roomHeight, component.z + rotation.z).let { blockPos ->
                    mc.world?.getBlockState(blockPos)?.block == Blocks.BLUE_TERRACOTTA && (room.roomComponents.size == 1 || horizontals.all { facing ->
                        mc.world?.getBlockState(
                            blockPos.add((if (facing.axis == Axis.X) facing.offsetX else 0), 0, (if (facing.axis == Axis.Z) facing.offsetZ else 0))
                        )?.block?.equalsOneOf(Blocks.AIR, Blocks.BLUE_TERRACOTTA) == true
                    }).also { isCorrectClay -> if (isCorrectClay) room.clayPos = blockPos }
                }
            }
        } ?: Rotations.NONE // Rotation isn't found if we can't find the clay block
    }

    private fun scanRoom(vec2: Vec2): Room? =
        getCore(vec2).let { core ->
            getRoomData(core)?.let {
                Room(data = it, roomComponents = findRoomComponentsRecursively(vec2, it.cores))
            }?.apply { updateRotation(this) }
        }

    private fun findRoomComponentsRecursively(vec2: Vec2, cores: List<Int>, visited: MutableSet<Vec2> = mutableSetOf(), tiles: MutableSet<RoomComponent> = mutableSetOf()): MutableSet<RoomComponent> {
        if (vec2 in visited) return tiles else visited.add(vec2)
        tiles.add(RoomComponent(vec2.x, vec2.z, getCore(vec2).takeIf { it in cores } ?: return tiles))
        horizontals.forEach { facing ->
            findRoomComponentsRecursively(
                Vec2(
                    vec2.x + ((if (facing.axis == Axis.X) facing.offsetX else 0) shl ROOM_SIZE_SHIFT),
                    vec2.z + ((if (facing.axis == Axis.Z) facing.offsetZ else 0) shl ROOM_SIZE_SHIFT)
                ), cores, visited, tiles
            )
        }
        return tiles
    }

    fun getRoomData(hash: Int): RoomData? =
        roomList.find { hash in it.cores }

    fun getRoomCenter(posX: Int, posZ: Int): Vec2 {
        val roomX = (posX - START + (1 shl (ROOM_SIZE_SHIFT - 1))) shr ROOM_SIZE_SHIFT
        val roomZ = (posZ - START + (1 shl (ROOM_SIZE_SHIFT - 1))) shr ROOM_SIZE_SHIFT
        return Vec2(((roomX shl ROOM_SIZE_SHIFT) + START), ((roomZ shl ROOM_SIZE_SHIFT) + START))
    }

    fun getCore(vec2: Vec2): Int {
        val sb = StringBuilder(150)
        val height = getTopLayerOfRoom(vec2)
        sb.append(CharArray(140 - height) { '0' })
        var bedrock = 0
        for (y in height downTo 12) {
            val id = mc.world?.getBlockState(BlockPos(vec2.x, y, vec2.z))?.block
            if (id == Blocks.AIR && bedrock >= 2 && y < 69) {
                sb.append(CharArray(y - 11) { '0' })
                break
            }

            if (id == Blocks.BEDROCK) bedrock++
            else {
                bedrock = 0
                if (id.equalsOneOf(Blocks.OAK_PLANKS, Blocks.TRAPPED_CHEST, Blocks.CHEST)) continue
            }
            sb.append(id)
        }
        return sb.toString().hashCode()
    }

    fun getTopLayerOfRoom(vec2: Vec2): Int {
        for (y in 140 downTo 12) {
            val block = mc.world?.getBlockState(BlockPos(vec2.x, y, vec2.z))?.block
            if (block != Blocks.AIR) return if (block == Blocks.GOLD_BLOCK) y - 1 else y
        }
        return 0
    }

        /*
        if (false) HeightMap.getHeight(vec2.x and 15, vec2.z and 15)
        else {
            val chunk = mc.world?.getChunk(ChunkSectionPos.getSectionCoord(vec2.x), ChunkSectionPos.getSectionCoord(vec2.z)) ?: return 0
            chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE).get(vec2.x and 15, vec2.z and 15).coerceIn(11..140) - 1
        }*/

    @EventHandler
    fun onRoomEnter(event: RoomEnterEvent) {
        currentRoom = event.room
        if (passedRooms.none { it.data.name == currentRoom?.data?.name }) passedRooms.add(currentRoom ?: return)
        devMessage("${event.room?.data?.name} - ${event.room?.rotation} || clay: ${event.room?.clayPos}")
    }

    @EventHandler
    fun onWorldLoad(event: WorldLoadEvent) {
        passedRooms.clear()
        currentRoom = null
        lastRoomPos = Vec2(0, 0)
    }
}