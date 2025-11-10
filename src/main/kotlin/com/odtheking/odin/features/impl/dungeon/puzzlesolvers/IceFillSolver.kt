package com.odtheking.odin.features.impl.dungeon.puzzlesolvers

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.odtheking.odin.OdinMod.logger
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.RoomEnterEvent
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.render.drawLine
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils.getRealCoords
import com.odtheking.odin.utils.skyblock.dungeon.tiles.Rotations
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.minecraft.world.level.block.Blocks
import net.minecraft.core.BlockPos
import net.minecraft.core.Vec3i
import net.minecraft.world.phys.Vec3
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

object IceFillSolver {
    private var currentPatterns: ArrayList<Vec3> = ArrayList()

    private var representativeFloors: List<List<List<Int>>>
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val isr = this::class.java.getResourceAsStream("/assets/odin/puzzles/icefillFloors.json")
        ?.let { InputStreamReader(it, StandardCharsets.UTF_8) }

    init {
        try {
            val text = isr?.readText()
            representativeFloors = gson.fromJson(text, object : TypeToken<List<List<List<Int>>>>() {}.type)
            isr?.close()
        } catch (e: Exception) {
            logger.error("Error loading ice fill floors", e)
            representativeFloors = emptyList()
        }
    }

    fun onRenderWorld(context: WorldRenderContext, color: Color) {
        if (currentPatterns.isEmpty() || DungeonUtils.currentRoomName != "Ice Fill") return

        context.drawLine(currentPatterns, color = color, depth = true)
    }

    fun onRoomEnter(event: RoomEnterEvent, optimizePatterns: Boolean) = with (event.room) {
        if (this?.data?.name != "Ice Fill" || currentPatterns.isNotEmpty()) return

        scanAllFloors(getRealCoords(BlockPos(15, 70, 7)), rotation, optimizePatterns)
    }

    private fun scanAllFloors(pos: BlockPos, rotation: Rotations, optimizePatterns: Boolean) {
        listOf(pos, pos.offset(transformTo(
            Vec3i(
                5,
                1,
                0
            ), rotation)), pos.offset(transformTo(Vec3i(12, 2, 0), rotation))).forEachIndexed { floorIndex, startPosition ->
            val floorHeight = representativeFloors[floorIndex]
            val startTime = System.nanoTime()

            for (patternIndex in floorHeight.indices) {
                if (
                    mc.level?.getBlockState(startPosition.offset(transform(floorHeight[patternIndex][0], floorHeight[patternIndex][1], rotation)))?.block == Blocks.AIR &&
                    mc.level?.getBlockState(startPosition.offset(transform(floorHeight[patternIndex][2], floorHeight[patternIndex][3], rotation)))?.block != Blocks.AIR
                ) {
                    modMessage("Section $floorIndex scan took ${(System.nanoTime() - startTime) / 1000000.0}ms pattern: $patternIndex")

                    (if (optimizePatterns) IceFillFloors.advanced[floorIndex][patternIndex] else IceFillFloors.IceFillFloors[floorIndex][patternIndex]).toMutableList().let {
                        currentPatterns.addAll(it.map { Vec3(startPosition.offset(transformTo(it, rotation))).add(0.5, 0.1, 0.5) })
                    }
                    return@forEachIndexed
                }
            }
            modMessage("§cFailed to scan floor ${floorIndex + 1}")
        }
    }

    private fun transform(x: Int, z: Int, rotation: Rotations): Vec3i {
        return when (rotation) {
            Rotations.NORTH -> Vec3i(z, 0, -x) // east
            Rotations.WEST -> Vec3i(-x, 0, -z) // north
            Rotations.SOUTH -> Vec3i(-z, 0, x) // west
            Rotations.EAST -> Vec3i(x, 0, z) // south
            else -> Vec3i(x, 0, z)
        }
    }

    private fun transformTo(vec: Vec3i, rotation: Rotations): Vec3i = with(transform(vec.x, vec.z, rotation)) {
        Vec3i(x, vec.y, z)
    }

    fun reset() {
        currentPatterns.clear()
    }
}
