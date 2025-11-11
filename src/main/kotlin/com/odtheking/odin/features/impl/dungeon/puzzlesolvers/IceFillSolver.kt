package com.odtheking.odin.features.impl.dungeon.puzzlesolvers

import com.google.gson.GsonBuilder
import com.odtheking.odin.OdinMod.logger
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.RoomEnterEvent
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.render.drawLine
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils.getRealCoords
import com.odtheking.odin.utils.skyblock.dungeon.tiles.Room
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.Vec3
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

object IceFillSolver {
    private var currentPatterns: ArrayList<Vec3> = ArrayList()

    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val isr = this::class.java.getResourceAsStream("/assets/odin/puzzles/IceFillFloors.json")
        ?.let { InputStreamReader(it, StandardCharsets.UTF_8) }
    private var iceFillFloors = IceFillData(emptyList(), emptyList(), emptyList())

    init {
        try {
            val text = isr?.readText()
            iceFillFloors = gson.fromJson(text, IceFillData::class.java)
            isr?.close()
        } catch (e: Exception) {
            logger.error("Error loading ice fill floors", e)
        }
    }

    fun onRenderWorld(event: RenderEvent, color: Color) {
        if (currentPatterns.isEmpty() || DungeonUtils.currentRoomName != "Ice Fill") return

        event.drawLine(currentPatterns, color, true)
    }

    fun onRoomEnter(event: RoomEnterEvent, optimizePatterns: Boolean) = with (event.room) {
        if (this?.data?.name != "Ice Fill" || currentPatterns.isNotEmpty()) return

        (0..2).forEach { index ->
            val floorIdentifiers = iceFillFloors.identifier[index]

            for (patternIndex in floorIdentifiers.indices) {
                if (isRealAir(floorIdentifiers[patternIndex][0]) && !isRealAir(floorIdentifiers[patternIndex][1])) {
                    val patterns = if (optimizePatterns) iceFillFloors.hard else iceFillFloors.easy

                    patterns[index][patternIndex].toMutableList().let { pattern ->
                        currentPatterns.addAll(pattern.map { Vec3(getRealCoords(it)).add(0.5, 0.1, 0.5) })
                    }

                    return@forEach
                }
            }
            modMessage("§cFailed to scan floor $index")
        }
    }

    private fun Room.isRealAir(pos: BlockPos): Boolean =
        mc.level?.getBlockState(getRealCoords(pos))?.block == Blocks.AIR

    fun reset() {
        currentPatterns.clear()
    }

    private data class IceFillData(
        val identifier: List<List<List<BlockPos>>>,
        val easy: List<List<List<BlockPos>>>,
        val hard: List<List<List<BlockPos>>>
    )
}