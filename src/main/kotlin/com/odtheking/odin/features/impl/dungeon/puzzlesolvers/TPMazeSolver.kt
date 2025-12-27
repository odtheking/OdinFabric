package com.odtheking.odin.features.impl.dungeon.puzzlesolvers

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.RoomEnterEvent
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Color.Companion.withAlpha
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.getBlockBounds
import com.odtheking.odin.utils.isXZInterceptable
import com.odtheking.odin.utils.render.drawFilledBox
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils.getRealCoords
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import java.util.concurrent.CopyOnWriteArraySet

object TPMazeSolver {
    private var tpPads = setOf<BlockPos>()
    private var correctPortals = listOf<BlockPos>()
    private var visited = CopyOnWriteArraySet<BlockPos>()

    fun onRoomEnter(event: RoomEnterEvent) = with(event.room) {
        if (this?.data?.name == "Teleport Maze") tpPads = endPortalFrameLocations.map { getRealCoords(BlockPos(it.x, it.y, it.z)) }.toSet()
    }

    fun tpPacket(event: ClientboundPlayerPositionPacket) = with (event.change.position) {
        if (DungeonUtils.currentRoomName != "Teleport Maze" || x % 0.5 != 0.0 || y != 69.5 || z % 0.5 != 0.0 || tpPads.isEmpty()) return@with
        visited.addAll(tpPads.filter { AABB.unitCubeFromLowerCorner(Vec3(x, y, z)).inflate(1.0, 0.0, 1.0).intersects(AABB(it)) ||
                mc.player?.boundingBox?.inflate(1.0, 0.0, 1.0)?.intersects(AABB(it)) == true })
        getCorrectPortals(Vec3(x, y, z), event.change.yRot, event.change.xRot)
    }

    private fun getCorrectPortals(pos: Vec3, yaw: Float, pitch: Float) {
        if (correctPortals.isEmpty()) correctPortals = correctPortals.plus(tpPads)

        correctPortals = correctPortals.filter {
            it !in visited &&
            isXZInterceptable(
                AABB(it.x.toDouble(), it.y.toDouble(), it.z.toDouble(), it.x + 1.0, it.y + 4.0, it.z + 1.0).inflate(0.75, 0.0, 0.75),
                32.0, pos, yaw, pitch
            ) && !AABB(it).inflate(.5, .0, .5).intersects(mc.player?.boundingBox ?: return@filter false)
        }
    }

    fun onRenderWorld(event: RenderEvent.Extract, mazeColorOne: Color, mazeColorMultiple: Color, mazeColorVisited: Color) {
        if (DungeonUtils.currentRoomName != "Teleport Maze") return
        tpPads.forEach {
            val aabb = it.getBlockBounds()?.move(it) ?: AABB(it)
            when (it) {
                in correctPortals -> event.drawFilledBox(aabb, if (correctPortals.size == 1) mazeColorOne else mazeColorMultiple, false)
                in visited -> event.drawFilledBox(aabb, mazeColorVisited, true)
                else -> event.drawFilledBox(aabb, Colors.WHITE.withAlpha(0.5f), true)
            }
        }
    }

    fun reset() {
        correctPortals = listOf()
        visited = CopyOnWriteArraySet()
    }

    private val endPortalFrameLocations = setOf(
        BlockPos(4, 69, 28), BlockPos(4, 69, 22), BlockPos(4, 69, 20),
        BlockPos(4, 69, 14), BlockPos(4, 69, 12), BlockPos(4, 69, 6),
        BlockPos(10, 69, 28), BlockPos(10, 69, 22), BlockPos(10, 69, 20),
        BlockPos(10, 69, 14), BlockPos(10, 69, 12), BlockPos(10, 69, 6),
        BlockPos(12, 69, 28), BlockPos(12, 69, 22), BlockPos(15, 69, 14),
        BlockPos(15, 69, 12), BlockPos(18, 69, 28), BlockPos(18, 69, 22),
        BlockPos(20, 69, 28), BlockPos(20, 69, 22), BlockPos(20, 69, 20),
        BlockPos(20, 69, 14), BlockPos(20, 69, 12), BlockPos(20, 69, 6),
        BlockPos(26, 69, 28), BlockPos(26, 69, 22), BlockPos(26, 69, 20),
        BlockPos(26, 69, 14), BlockPos(26, 69, 12), BlockPos(26, 69, 6)
    )
}