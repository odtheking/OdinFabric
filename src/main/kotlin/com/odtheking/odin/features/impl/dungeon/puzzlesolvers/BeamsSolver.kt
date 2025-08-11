package com.odtheking.odin.features.impl.dungeon.puzzlesolvers

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.odtheking.odin.OdinMod.logger
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.BlockUpdateEvent
import com.odtheking.odin.events.RoomEnterEvent
import com.odtheking.odin.features.impl.dungeon.puzzlesolvers.PuzzleSolvers.onPuzzleComplete
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Color.Companion.withAlpha
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.equalsOneOf
import com.odtheking.odin.utils.render.drawLine
import com.odtheking.odin.utils.render.drawStyledBox
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils.getRealCoords
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.minecraft.block.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap

object BeamsSolver {
    private var scanned = false
    private var lanternPairs: List<List<Int>>
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val isr = this::class.java.getResourceAsStream("/assets/odin/puzzles/creeperBeamsSolutions.json")?.let { InputStreamReader(it, StandardCharsets.UTF_8) }

    init {
        try {
            val text = isr?.readText()
            lanternPairs = gson.fromJson(text, object : TypeToken<List<List<Int>>>() {}.type)
            isr?.close()
        } catch (e: Exception) {
            logger.error("Error loading creeper beams solutions", e)
            lanternPairs = emptyList()
        }
    }

    private var currentLanternPairs = ConcurrentHashMap<BlockPos, Pair<BlockPos, Color>>()

    fun onRoomEnter(event: RoomEnterEvent) = with(event.room) {
        if (this?.data?.name != "Creeper Beams") return reset()

        currentLanternPairs.clear()
        lanternPairs.forEach { list ->
            val pos = getRealCoords(BlockPos(list[0], list[1], list[2])).takeIf { mc.world?.getBlockState(it)?.block == Blocks.SEA_LANTERN } ?: return@forEach
            val pos2 = getRealCoords(BlockPos(list[3], list[4], list[5])).takeIf { mc.world?.getBlockState(it)?.block == Blocks.SEA_LANTERN } ?: return@forEach

            currentLanternPairs[pos] = pos2 to colors[currentLanternPairs.size]
        }
    }

    fun onRenderWorld(context: WorldRenderContext, beamStyle: Int, beamsTracer: Boolean, beamsAlpha: Float) {
        if (DungeonUtils.currentRoomName != "Creeper Beams" || currentLanternPairs.isEmpty()) return

        currentLanternPairs.entries.forEach { positions ->
            val color = positions.value.second.withAlpha(beamsAlpha)

            context.drawStyledBox(Box(positions.key), color, depth = true, style = beamStyle)
            context.drawStyledBox(Box(positions.value.first), color, depth = true, style = beamStyle)

            if (beamsTracer)
                context.drawLine(listOf(positions.key.toCenterPos(), positions.value.first.toCenterPos()), color = color, depth = false)
        }
    }

    fun onBlockChange(event: BlockUpdateEvent) {
        if (DungeonUtils.currentRoomName != "Creeper Beams") return
        if (event.pos == DungeonUtils.currentRoom?.getRealCoords(BlockPos(15, 69, 15)) && event.old.block == Blocks.AIR && event.updated.block == Blocks.CHEST) onPuzzleComplete("Creeper Beams")
        currentLanternPairs.forEach { (key, value) ->
            if (event.pos.equalsOneOf(key, value.first) &&
                event.updated.block != Blocks.SEA_LANTERN &&
                event.old.block == Blocks.SEA_LANTERN) currentLanternPairs.remove(key)
        }
    }

    fun reset() {
        scanned = false
        currentLanternPairs.clear()
    }

    private val colors = listOf(Colors.MINECRAFT_GOLD, Colors.MINECRAFT_GREEN, Colors.MINECRAFT_LIGHT_PURPLE, Colors.MINECRAFT_DARK_AQUA, Colors.MINECRAFT_YELLOW, Colors.MINECRAFT_DARK_RED, Colors.WHITE, Colors.MINECRAFT_DARK_PURPLE)
}

