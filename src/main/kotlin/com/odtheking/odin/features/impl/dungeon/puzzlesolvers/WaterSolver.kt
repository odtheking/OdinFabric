package com.odtheking.odin.features.impl.dungeon.puzzlesolvers

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.render.drawLine
import com.odtheking.odin.utils.render.drawText
import com.odtheking.odin.utils.renderPos
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils.getRealCoords
import com.odtheking.odin.utils.toFixed
import net.minecraft.block.Blocks
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

object WaterSolver {

    private var waterSolutions: JsonObject

    init {
        val isr = WaterSolver::class.java.getResourceAsStream("/assets/odin/puzzles/waterSolutions.json")?.let { InputStreamReader(it, StandardCharsets.UTF_8) } ?: throw IllegalStateException("Water solutions file not found")
        waterSolutions = JsonParser.parseString(isr.readText()).asJsonObject
        isr.close()
    }

    private var solutions = HashMap<LeverBlock, Array<Double>>()
    private var patternIdentifier = -1
    private var openedWaterTicks = -1
    private var tickCounter = 0

    fun scan(optimized: Boolean) = with (DungeonUtils.currentRoom) {
        if (this?.data?.name != "Water Board" || patternIdentifier != -1) return@with
        val extendedSlots = WoolColor.entries.joinToString("") { if (it.isExtended) it.ordinal.toString() else "" }.takeIf { it.length == 3 } ?: return

        patternIdentifier = when {
            mc.world?.getBlockState(getRealCoords(BlockPos(14, 77, 27)))?.block == Blocks.TERRACOTTA -> 0 // right block == clay
            mc.world?.getBlockState(getRealCoords(BlockPos(16, 78, 27)))?.block == Blocks.EMERALD_BLOCK -> 1 // left block == emerald
            mc.world?.getBlockState(getRealCoords(BlockPos(14, 78, 27)))?.block == Blocks.DIAMOND_BLOCK -> 2 // right block == diamond
            mc.world?.getBlockState(getRealCoords(BlockPos(14, 78, 27)))?.block == Blocks.QUARTZ_BLOCK  -> 3 // right block == quartz
            else -> return@with modMessage("§cFailed to get Water Board pattern. Was the puzzle already started?")
        }

        modMessage("$patternIdentifier || ${WoolColor.entries.filter { it.isExtended }.joinToString(", ") { it.name.lowercase() }}")

        solutions.clear()
        waterSolutions[optimized.toString()].asJsonObject[patternIdentifier.toString()].asJsonObject[extendedSlots].asJsonObject.entrySet().forEach { entry ->
            solutions[
                when (entry.key) {
                    "diamond_block" -> LeverBlock.DIAMOND
                    "emerald_block" -> LeverBlock.EMERALD
                    "hardened_clay" -> LeverBlock.CLAY
                    "quartz_block"  -> LeverBlock.QUARTZ
                    "gold_block"    -> LeverBlock.GOLD
                    "coal_block"    -> LeverBlock.COAL
                    "water"         -> LeverBlock.WATER
                    else -> LeverBlock.NONE
                }
            ] = entry.value.asJsonArray.map { it.asDouble }.toTypedArray()
        }
    }

    fun onRenderWorld(event: RenderEvent, showTracer: Boolean, tracerColorFirst: Color, tracerColorSecond: Color) {
        if (patternIdentifier == -1 || solutions.isEmpty() || DungeonUtils.currentRoomName != "Water Board") return

        val solutionList = solutions
            .flatMap { (lever, times) -> times.drop(lever.i).map { Pair(lever, it) } }
            .sortedBy { (lever, time) -> time + if (lever == LeverBlock.WATER) 0.01 else 0.0 }

        if (showTracer) {
            val firstSolution = solutionList.firstOrNull()?.first ?: return
            mc.player?.let { event.drawLine(listOf(it.renderPos, Vec3d(firstSolution.leverPos).add(.5, .5, .5)), color = tracerColorFirst, depth = true) }

            if (solutionList.size > 1 && firstSolution.leverPos != solutionList[1].first.leverPos) {
                event.drawLine(
                    listOf(Vec3d(firstSolution.leverPos).add(.5, .5, .5), Vec3d(solutionList[1].first.leverPos).add(.5, .5, .5)),
                    color = tracerColorSecond, depth = true
                )
            }
        }

        solutions.forEach { (lever, times) ->
            times.drop(lever.i).forEachIndexed { index, time ->
                val timeInTicks = (time * 20).toInt()
                event.drawText(
                    Text.of(when (openedWaterTicks) {
                        -1 if timeInTicks == 0 -> "§a§lCLICK ME!"
                        -1 -> "§e${time}s"
                        else -> (openedWaterTicks + timeInTicks - tickCounter).takeIf { it > 0 }?.let { "§e${(it / 20f).toFixed()}s" } ?: "§a§lCLICK ME!"
                    }).asOrderedText(),
                    Vec3d(lever.leverPos).add(0.5, (index + lever.i) * 0.5 + 1.5, 0.5),
                    scale = 1f, true
                )
            }
        }
    }

    fun waterInteract(event: PlayerInteractBlockC2SPacket) {
        if (solutions.isEmpty()) return
        LeverBlock.entries.find { it.leverPos == event.blockHitResult.blockPos }?.let {
            if (it == LeverBlock.WATER && openedWaterTicks == -1) openedWaterTicks = tickCounter
            it.i++
        }
    }

    fun onServerTick() {
        tickCounter++
    }

    fun reset() {
        LeverBlock.entries.forEach { it.i = 0 }
        patternIdentifier = -1
        solutions.clear()
        openedWaterTicks = -1
        tickCounter = 0
    }

    private enum class WoolColor(val relativePosition: BlockPos) {
        PURPLE(BlockPos(15, 56, 19)),
        ORANGE(BlockPos(15, 56, 18)),
        BLUE(BlockPos(15, 56, 17)),
        GREEN(BlockPos(15, 56, 16)),
        RED(BlockPos(15, 56, 15));

        inline val isExtended: Boolean get() =
            DungeonUtils.currentRoom?.let { mc.world?.getBlockState(it.getRealCoords(relativePosition))?.block == Blocks.AIR } == false
    }

    private enum class LeverBlock(val relativePosition: BlockPos, var i: Int = 0) {
        QUARTZ(BlockPos(20, 61, 20)),
        GOLD(BlockPos(20, 61, 15)),
        COAL(BlockPos(20, 61, 10)),
        DIAMOND(BlockPos(10, 61, 20)),
        EMERALD(BlockPos(10, 61, 15)),
        CLAY(BlockPos(10, 61, 10)),
        WATER(BlockPos(15, 60, 5)),
        NONE(BlockPos(0, 0, 0));

        inline val leverPos: BlockPos
            get() = DungeonUtils.currentRoom?.getRealCoords(relativePosition) ?: BlockPos(0, 0, 0)
    }
}