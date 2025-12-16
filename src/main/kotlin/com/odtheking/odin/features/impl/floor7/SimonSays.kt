package com.odtheking.odin.features.impl.floor7

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.ColorSetting
import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.clickgui.settings.impl.SelectorSetting
import com.odtheking.odin.events.*
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Color.Companion.withAlpha
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.devMessage
import com.odtheking.odin.utils.render.drawStyledBox
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.M7Phases
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.phys.AABB

object SimonSays : Module(
    name = "Simon Says",
    description = "Shows a solution for the Simon Says device."
) {
    private val firstColor by ColorSetting("First Color", Colors.MINECRAFT_GREEN.withAlpha(0.5f), true, desc = "The color of the first button.")
    private val secondColor by ColorSetting("Second Color", Colors.MINECRAFT_GOLD.withAlpha(0.5f), true, desc = "The color of the second button.")
    private val thirdColor by ColorSetting("Third Color", Colors.MINECRAFT_RED.withAlpha(0.5f), true, desc = "The color of the buttons after the second.")
    private val style by SelectorSetting("Style", "Filled Outline", arrayListOf("Filled", "Outline", "Filled Outline"), desc = "The style of the box rendering.")
    private val blockWrong by BooleanSetting("Block Wrong Clicks", false, desc = "Blocks wrong clicks, shift will override this.")
    private val adjustTicks by NumberSetting("Reload Ticks", 12, 0, 30, 1, desc = "Adjust the timing of the solver to wait until the device is done highlighting.")

    private val startButton = BlockPos(110, 121, 91)
    private val clickInOrder = ArrayList<BlockPos>()
    private var lastLanternTick = -1
    private var clickNeeded = 0
    private var firstPhase = true

    private fun resetSolution() {
        clickInOrder.clear()
        clickNeeded = 0
        lastLanternTick = -1
        firstPhase = true
    }

    init {
        on<WorldLoadEvent> {
            resetSolution()
        }

        on<BlockUpdateEvent> {
            if (DungeonUtils.getF7Phase() != M7Phases.P3) return@on

            if (pos == startButton && updated.block == Blocks.STONE_BUTTON && updated.getValue(BlockStateProperties.POWERED)) {
                resetSolution()
                return@on
            }

            if (pos.y !in 120..123 || pos.z !in 92..95) return@on

            when (pos.x) {
                111 ->
                    if (updated.block == Blocks.OBSIDIAN && old.block == Blocks.SEA_LANTERN && pos !in clickInOrder) {
                        clickInOrder.add(pos.immutable())
                        if (lastLanternTick != -1) devMessage("§eLantern spawned after §a${lastLanternTick} §eserver ticks")
                        lastLanternTick = 0
                    }

                110 ->
                    if (updated.block == Blocks.AIR) resetSolution()
                    else if (old.block == Blocks.STONE_BUTTON && updated.getValue(BlockStateProperties.POWERED)) {
                        clickNeeded = clickInOrder.indexOf(pos.east()) + 1
                        if (clickNeeded >= clickInOrder.size) {
                            clickNeeded = 0
                            firstPhase = false
                        }
                    }
            }
        }

        on<TickEvent.Server> {
            if (DungeonUtils.getF7Phase() != M7Phases.P3 || !firstPhase) return@on

            if (lastLanternTick != -1) {
                lastLanternTick++

                if (lastLanternTick > adjustTicks && grid.all { mc.level?.getBlockState(it)?.block != Blocks.STONE_BUTTON }) {
                    devMessage("§aSkip should be over?")
                    when {
                        clickInOrder.size >= 3 -> clickInOrder.removeFirst()
                        clickInOrder.size == 2 -> clickInOrder.reverse()
                    }
                    firstPhase = false
                }
            }
        }

        on<BlockInteractEvent> {
            if (DungeonUtils.getF7Phase() != M7Phases.P3) return@on

            if (
                blockWrong && mc.player?.isShiftKeyDown == false &&
                pos.x == 110 && pos.y in 120..123 && pos.z in 92..95 &&
                pos.east() != clickInOrder.getOrNull(clickNeeded)
            ) cancel()
        }

        on<RenderEvent.Last> {
            if (DungeonUtils.getF7Phase() != M7Phases.P3 || clickNeeded >= clickInOrder.size) return@on

            for (index in clickNeeded until clickInOrder.size) {
                with(clickInOrder[index]) {
                    val color = when (index) {
                        clickNeeded -> firstColor
                        clickNeeded + 1 -> secondColor
                        else -> thirdColor
                    }

                    context.drawStyledBox(AABB(x + 0.05, y + 0.37, z + 0.3, x - 0.15, y + 0.63, z + 0.7), color, style, true)
                }
            }
        }
    }

    private val grid = setOf(
        BlockPos(110, 123, 92), BlockPos(110, 123, 93), BlockPos(110, 123, 94), BlockPos(110, 123, 95),
        BlockPos(110, 122, 92), BlockPos(110, 122, 93), BlockPos(110, 122, 94), BlockPos(110, 122, 95),
        BlockPos(110, 121, 92), BlockPos(110, 121, 93), BlockPos(110, 121, 94), BlockPos(110, 121, 95),
        BlockPos(110, 120, 92), BlockPos(110, 120, 93), BlockPos(110, 120, 94), BlockPos(110, 120, 95),
    )
}