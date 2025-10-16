package com.odtheking.odin.features.impl.dungeon.puzzlesolvers

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.RoomEnterEvent
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Color.Companion.withAlpha
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.isXZInterceptable
import com.odtheking.odin.utils.render.drawFilledBox
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils.getRealCoords
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import java.util.concurrent.CopyOnWriteArraySet

object TPMazeSolver {
    private var tpPads = setOf<BlockPos>()
    private var correctPortals = listOf<BlockPos>()
    private var visited = CopyOnWriteArraySet<BlockPos>()

    fun onRoomEnter(event: RoomEnterEvent) = with(event.room) {
        if (this?.data?.name == "Teleport Maze") tpPads = endPortalFrameLocations.map { getRealCoords(BlockPos(it.x, it.y, it.z)) }.toSet()
    }

    fun tpPacket(event: PlayerPositionLookS2CPacket) = with (event.change.position) {
        if (DungeonUtils.currentRoomName != "Teleport Maze" || x % 0.5 != 0.0 || y != 69.5 || z % 0.5 != 0.0 || tpPads.isEmpty()) return
        visited.addAll(tpPads.filter { Box.from(Vec3d(x, y, z)).expand(1.0, 0.0, 1.0).intersects(Box(it)) ||
                mc.player?.boundingBox?.expand(1.0, 0.0, 1.0)?.intersects(Box(it)) == true })
        getCorrectPortals(Vec3d(x, y, z), event.change.yaw, event.change.pitch)
    }

    private fun getCorrectPortals(pos: Vec3d, yaw: Float, pitch: Float) {
        if (correctPortals.isEmpty()) correctPortals = correctPortals.plus(tpPads)

        correctPortals = correctPortals.filter {
            it !in visited &&
            isXZInterceptable(
                Box(it.x.toDouble(), it.y.toDouble(), it.z.toDouble(), it.x + 1.0, it.y + 4.0, it.z + 1.0).expand(0.75, 0.0, 0.75),
                32.0, pos, yaw, pitch
            ) && !Box(it).expand(.5, .0, .5).intersects(mc.player?.boundingBox)
        }
    }

    fun onRenderWorld(event: RenderEvent, mazeColorOne: Color, mazeColorMultiple: Color, mazeColorVisited: Color) {
        if (DungeonUtils.currentRoomName != "Teleport Maze") return
        tpPads.forEach {
            when (it) {
                in correctPortals -> event.drawFilledBox(Box(it), if (correctPortals.size == 1) mazeColorOne else mazeColorMultiple, depth = false)
                in visited -> event.drawFilledBox(Box(it), mazeColorVisited, depth = true)
                else -> event.drawFilledBox(Box(it), Colors.WHITE.withAlpha(0.5f), depth = true)
            }
        }
    }

    fun reset() {
        correctPortals = listOf()
        visited = CopyOnWriteArraySet<BlockPos>()
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