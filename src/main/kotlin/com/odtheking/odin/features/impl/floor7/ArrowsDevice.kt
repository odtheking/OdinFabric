package com.odtheking.odin.features.impl.floor7

import com.odtheking.odin.clickgui.settings.impl.ActionSetting
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.ColorSetting
import com.odtheking.odin.events.BlockUpdateEvent
import com.odtheking.odin.events.ChatPacketEvent
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.WorldLoadEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.events.core.onReceive
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Color.Companion.withAlpha
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.alert
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.render.drawFilledBox
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.M7Phases
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.AABB

object ArrowsDevice : Module(
    name = "Arrows Device",
    description = "Shows a solution for the Sharp Shooter puzzle in floor 7."
) {
    private val markedPositionColor by ColorSetting("Marked Position", Colors.MINECRAFT_AQUA.withAlpha(0.5f), true, desc = "Color of the marked position.")
    private val targetPositionColor by ColorSetting("Target Position", Colors.MINECRAFT_LIGHT_PURPLE.withAlpha(0.5f), true, desc = "Color of the target position.")
    private val depthCheck by BooleanSetting("Depth check", true, desc = "Marked positions show through walls.")
    private val alertOnDeviceComplete by BooleanSetting("Device complete alert", true, desc = "Send an alert when device is complete.")
    private val reset by ActionSetting("Reset", desc = "Resets the solver.") {
        markedPositions.clear()
        targetPosition = null
    }

    private val deviceCompleteRegex = Regex("^(.{1,16}) completed a device! \\((\\d)/(\\d)\\)$")
    private val roomBoundingBox = AABB(20.0, 100.0, 30.0, 89.0, 151.0, 51.0)
    private val markedPositions = mutableSetOf<AABB>()
    private var targetPosition: AABB? = null
    private var isDeviceComplete = false

    init {
        on<BlockUpdateEvent> {
            if (DungeonUtils.getF7Phase() != M7Phases.P3 || !devicePositions.contains(pos)) return@on

            if (old.block == Blocks.EMERALD_BLOCK && updated.block == Blocks.BLUE_TERRACOTTA) {
                markedPositions.add(AABB(pos))
                if (targetPosition == AABB(pos)) targetPosition = null
            } else if (old.block == Blocks.BLUE_TERRACOTTA && updated.block == Blocks.EMERALD_BLOCK) {
                markedPositions.remove(AABB(pos))
                targetPosition = AABB(pos)
            }
        }

        on<RenderEvent.Last> {
            if (DungeonUtils.getF7Phase() != M7Phases.P3) return@on

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
            isDeviceComplete = false
        }

        on<ChatPacketEvent> {
            if (DungeonUtils.getF7Phase() != M7Phases.P3 || !isPlayerInRoom || isDeviceComplete) return@on
            if (deviceCompleteRegex.find(value)?.groupValues?.get(1) == mc.player?.name?.string) onComplete("Chat")
        }

        onReceive<ClientboundSetEntityDataPacket> {
            if (DungeonUtils.getF7Phase() != M7Phases.P3 || !isPlayerInRoom || isDeviceComplete) return@onReceive
            if (mc.level?.getEntity(id)?.name?.string == "Active") onComplete("Entity")
        }
    }

    private fun onComplete(method: String) {
        isDeviceComplete = true

        if (alertOnDeviceComplete) {
            modMessage("§aSharp shooter device complete §7($method)")
            alert("§aDevice Complete")
        }
        reset()
    }

    private val isPlayerInRoom: Boolean
        get() = mc.player?.let { roomBoundingBox.contains(it.position()) } == true

    private val devicePositions = listOf(
        BlockPos(68, 130, 50), BlockPos(66, 130, 50), BlockPos(64, 130, 50),
        BlockPos(68, 128, 50), BlockPos(66, 128, 50), BlockPos(64, 128, 50),
        BlockPos(68, 126, 50), BlockPos(66, 126, 50), BlockPos(64, 126, 50)
    )
}