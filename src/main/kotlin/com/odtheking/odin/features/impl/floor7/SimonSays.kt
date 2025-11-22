package com.odtheking.odin.features.impl.floor7

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.ColorSetting
import com.odtheking.odin.clickgui.settings.impl.SelectorSetting
import com.odtheking.odin.events.BlockUpdateEvent
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.WorldLoadEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.events.core.onReceive
import com.odtheking.odin.events.core.onSend
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Color.Companion.withAlpha
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.drawStyledBox
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.M7Phases
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.Items
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
    private val optimizeSolution by BooleanSetting("Optimized Solution", true, desc = "Use optimized solution, might fix ss-skip")

    private val startButton = BlockPos(110, 121, 91)
    private val clickInOrder = ArrayList<BlockPos>()
    private var clickNeeded = 0

    private fun resetSolution() {
        clickInOrder.clear()
        clickNeeded = 0
    }

    init {
        on<WorldLoadEvent> {
            clickInOrder.clear()
            clickNeeded = 0
        }

        on<BlockUpdateEvent> {
            if (DungeonUtils.getF7Phase() != M7Phases.P3) return@on

            if (pos == startButton && updated.block == Blocks.STONE_BUTTON && updated.getValue(BlockStateProperties.POWERED)) {
                if (!optimizeSolution) resetSolution()
                return@on
            }

            if (pos.y !in 120..123 || pos.z !in 92..95) return@on

            when (pos.x) {
                111 ->
                    if (optimizeSolution) {
                        if (updated.block == Blocks.SEA_LANTERN && old.block == Blocks.OBSIDIAN && (clickInOrder.isEmpty() || pos !in clickInOrder))
                            clickInOrder.add(pos)
                    } else if (updated.block == Blocks.OBSIDIAN && old.block == Blocks.SEA_LANTERN && pos !in clickInOrder) clickInOrder.add(pos)

                110 ->
                    if (updated.block == Blocks.AIR) {
                        if (!optimizeSolution) resetSolution()
                    } else if (old.block == Blocks.STONE_BUTTON && updated.getValue(BlockStateProperties.POWERED)) {
                        clickNeeded = clickInOrder.indexOf(pos.east()) + 1
                        if (clickNeeded >= clickInOrder.size) if (optimizeSolution) resetSolution() else clickNeeded = 0
                    }
            }
        }

        onReceive<ClientboundSetEntityDataPacket> {
            if (DungeonUtils.getF7Phase() != M7Phases.P3) return@onReceive
            val entity = mc.level?.getEntity(id) as? ItemEntity ?: return@onReceive
            if (entity.item?.item != Items.STONE_BUTTON) return@onReceive

            val index = clickInOrder.indexOf(entity.blockPosition().east())
            if (index == 2 && clickInOrder.size == 3) clickInOrder.removeFirst()
            else if (index == 0 && clickInOrder.size == 2) clickInOrder.reverse()
        }

        onSend<ServerboundUseItemOnPacket> {
            if (DungeonUtils.getF7Phase() != M7Phases.P3) return@onSend

            if (hitResult.blockPos == startButton) {
                if (optimizeSolution) resetSolution()
                return@onSend
            }

            if (
                blockWrong && mc.player?.isShiftKeyDown == false &&
                hitResult.blockPos.x == 110 && hitResult.blockPos.y in 120..123 && hitResult.blockPos.z in 92..95 &&
                hitResult.blockPos.east() != clickInOrder.getOrNull(clickNeeded)
            ) it.cancel()
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
}