package io.github.odtheking.odin.utils.skyblock.dungeon

import com.google.gson.GsonBuilder
import com.google.gson.JsonIOException
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import io.github.odtheking.odin.OdinMod.mc
import io.github.odtheking.odin.events.RoomEnterEvent
import io.github.odtheking.odin.events.TickEvent
import io.github.odtheking.odin.events.WorldLoadEvent
import io.github.odtheking.odin.utils.Vec2
import io.github.odtheking.odin.utils.equalsOneOf
import io.github.odtheking.odin.utils.modMessage
import io.github.odtheking.odin.utils.skyblock.Island
import io.github.odtheking.odin.utils.skyblock.LocationUtils
import io.github.odtheking.odin.utils.skyblock.dungeon.DungeonListener.inBoss
import io.github.odtheking.odin.utils.skyblock.dungeon.tiles.*
import meteordevelopment.orbit.EventHandler
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkSectionPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Direction.Axis
import net.minecraft.world.Heightmap
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
                    (ScanUtils::class.java.getResourceAsStream("/rooms.json")
                        ?: throw FileNotFoundException()).bufferedReader(),
                    object : TypeToken<Set<RoomData>>() {}.type
                )
        } catch (e: Exception) {
            handleRoomDataError(e)
            setOf()
        }
    }

    private fun handleRoomDataError(e: Exception) {
        when (e) {
            is JsonSyntaxException -> println("Error parsing room data.")
            is JsonIOException -> println("Error reading room data.")
            is FileNotFoundException -> println("Room data not found, something went wrong! Please report this!")
            else -> {
                println("Unknown error while reading room data.")
//                logger.error("Error reading room data", e)
                println(e.message)
            }
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
            room.clayPos =
                room.roomComponents.firstOrNull()?.let { BlockPos(it.x - 15, roomHeight, it.z - 15) } ?: return
            room.rotation = Rotations.SOUTH
            return
        }
        room.rotation = Rotations.entries.dropLast(1).find { rotation ->
            room.roomComponents.any { component ->
                BlockPos(component.x + rotation.x, roomHeight, component.z + rotation.z).let { blockPos ->
                    mc.world?.getBlockState(blockPos)?.block == Blocks.BLUE_TERRACOTTA && (room.roomComponents.size == 1 || horizontals.all { facing ->
                        mc.world?.getBlockState(
                            blockPos.add(
                                (if (facing.axis == Axis.X) facing.offsetX else 0),
                                0,
                                (if (facing.axis == Axis.Z) facing.offsetZ else 0)
                            )
                        )?.block?.equalsOneOf(Blocks.AIR, Blocks.BLUE_TERRACOTTA) == true
                    }).also { isCorrectClay -> if (isCorrectClay) room.clayPos = blockPos }
                }
            }
        } ?: Rotations.NONE // Rotation isn't found if we can't find the clay block
    }

    private fun scanRoom(vec2: Vec2): Room? =
        getCore(vec2).let { core ->
            getRoomData(core)?.let {
                Room(
                    data = it,
                    roomComponents = findRoomComponentsRecursively(vec2, it.cores)
                )
            }?.apply { updateRotation(this) }
        }

    private fun findRoomComponentsRecursively(
        vec2: Vec2,
        cores: List<Int>,
        visited: MutableSet<Vec2> = mutableSetOf(),
        tiles: MutableSet<RoomComponent> = mutableSetOf()
    ): MutableSet<RoomComponent> {
        if (vec2 in visited) return tiles else visited.add(vec2)
        tiles.add(
            RoomComponent(
                vec2.x,
                vec2.z,
                getCore(vec2).takeIf { it in cores } ?: return tiles))
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

    private fun getRoomData(hash: Int): RoomData? =
        roomList.find { hash in it.cores }

    fun getRoomCenter(posX: Int, posZ: Int): Vec2 {
        val roomX = (posX - START + (1 shl (ROOM_SIZE_SHIFT - 1))) shr ROOM_SIZE_SHIFT
        val roomZ = (posZ - START + (1 shl (ROOM_SIZE_SHIFT - 1))) shr ROOM_SIZE_SHIFT
        return Vec2(((roomX shl ROOM_SIZE_SHIFT) + START), ((roomZ shl ROOM_SIZE_SHIFT) + START))
    }

    fun getCore(vec2: Vec2): Int {
        val sb = StringBuilder(150)
        val chunk = mc.world?.getChunk(ChunkSectionPos.getSectionCoord(vec2.x), ChunkSectionPos.getSectionCoord(vec2.z))
            ?: return 0
        val height =
            chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE).get(vec2.x and 15, vec2.z and 15).coerceIn(11..140)
        sb.append(CharArray(140 - height) { '0' })
        var bedrock = 0
        for (y in height downTo 12) {
            val id = Block.getRawIdFromState(mc.world?.getBlockState(BlockPos(vec2.x, y, vec2.z)))
            if (id == 0 && bedrock >= 2 && y < 69) {
                sb.append(CharArray(y - 11) { '0' })
                break
            }

            if (id == 7) bedrock++
            else {
                bedrock = 0
                if (id.equalsOneOf(5, 54, 146)) continue
            }
            sb.append(id)
        }
        return sb.toString().hashCode()
    }

    private fun getTopLayerOfRoom(vec2: Vec2): Int {
        val chunk = mc.world?.getChunk(vec2.x shr 4, vec2.z shr 4) ?: return 0
        val height = chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE).get(vec2.x and 15, vec2.z and 15) - 1
        return if (mc.world?.getBlockState(
                BlockPos(
                    vec2.x,
                    height,
                    vec2.z
                )
            )?.block == Blocks.GOLD_BLOCK
        ) height - 1 else height
    }

    @EventHandler
    fun onRoomEnter(event: RoomEnterEvent) {
        currentRoom = event.room
        if (passedRooms.none { it.data.name == currentRoom?.data?.name }) passedRooms.add(currentRoom ?: return)
        modMessage("${event.room?.data?.name} - ${event.room?.rotation} || clay: ${event.room?.clayPos}")
    }

    @EventHandler
    fun onWorldLoad(event: WorldLoadEvent) {
        passedRooms.clear()
        currentRoom = null
        lastRoomPos = Vec2(0, 0)
    }
}