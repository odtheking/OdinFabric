package com.odtheking.odin.features.impl.floor7

import com.odtheking.odin.clickgui.settings.impl.ActionSetting
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.ColorSetting
import com.odtheking.odin.events.BlockUpdateEvent
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.WorldLoadEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Color.Companion.withAlpha
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.drawFilledBox
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.M7Phases
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.AABB

object ArrowsDevice : Module(
    name = "Arrows Device",
    description = "Shows a solution for the Sharp Shooter puzzle in floor 7."
) {
    private val markedPositionColor by ColorSetting("Marked Position", Colors.MINECRAFT_AQUA.withAlpha(0.5f), true, desc = "Color of the marked position.")
    private val targetPositionColor by ColorSetting("Target Position", Colors.MINECRAFT_LIGHT_PURPLE.withAlpha(0.5f), true, desc = "Color of the target position.")
    private val depthCheck by BooleanSetting("Depth check", true, desc = "Marked positions show through walls.")
    private val reset by ActionSetting("Reset", desc = "Resets the solver.") {
        markedPositions.clear()
        targetPosition = null
    }

    private val markedPositions = mutableSetOf<AABB>()
    private var targetPosition: AABB? = null

    init {
        on<BlockUpdateEvent> {
            if (!DungeonUtils.inBoss || DungeonUtils.getF7Phase() != M7Phases.P3 || !devicePositions.contains(pos)) return@on
            val (yaw, pitch) = mc.player?.let { it.xRot to it.yRot } ?: return@on

            if (old.block == Blocks.EMERALD_BLOCK && updated.block == Blocks.BLUE_TERRACOTTA) {
                markedPositions.add(AABB(pos))
                if (targetPosition == AABB(pos)) targetPosition = null
            } else if (old.block == Blocks.BLUE_TERRACOTTA && updated.block == Blocks.EMERALD_BLOCK) {
                markedPositions.remove(AABB(pos))
                targetPosition = AABB(pos)
            }
        }

        on<RenderEvent.Last> {
            if (!DungeonUtils.inBoss || DungeonUtils.getF7Phase() != M7Phases.P3) return@on

            markedPositions.forEach { position ->
                context.drawFilledBox(position, markedPositionColor, depth = depthCheck)
            }

            targetPosition?.let { position ->
                context.drawFilledBox(position, targetPositionColor, depth = depthCheck)
            }
        }

        on<WorldLoadEvent> {
            markedPositions.clear()
            targetPosition = null
        }
    }

    private val devicePositions = listOf(
        BlockPos(68, 130, 50), BlockPos(66, 130, 50), BlockPos(64, 130, 50),
        BlockPos(68, 128, 50), BlockPos(66, 128, 50), BlockPos(64, 128, 50),
        BlockPos(68, 126, 50), BlockPos(66, 126, 50), BlockPos(64, 126, 50)
    )
}