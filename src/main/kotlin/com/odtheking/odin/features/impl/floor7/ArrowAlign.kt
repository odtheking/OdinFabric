package com.odtheking.odin.features.impl.floor7

import com.odtheking.mixin.accessors.PlayerInteractEntityC2SPacketAccessor
import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.events.PacketEvent
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.addVec
import com.odtheking.odin.utils.component1
import com.odtheking.odin.utils.component2
import com.odtheking.odin.utils.component3
import com.odtheking.odin.utils.handlers.TickTask
import com.odtheking.odin.utils.render.drawText
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.M7Phases
import meteordevelopment.orbit.EventHandler
import net.minecraft.entity.decoration.ItemFrameEntity
import net.minecraft.item.Items
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

object ArrowAlign : Module(
    name = "Arrow Align",
    description = "Shows the solution for the Arrow Align device."
) {
    private val blockWrong by BooleanSetting("Block Wrong Clicks", false, desc = "Blocks wrong clicks, shift will override this.")
    private val invertSneak by BooleanSetting("Invert Sneak", false, desc = "Only block wrong clicks whilst sneaking, instead of whilst standing").withDependency { blockWrong }

    private val frameGridCorner = BlockPos(-2, 120, 75)
    private val recentClickTimestamps = mutableMapOf<Int, Long>()
    private val clicksRemaining = mutableMapOf<Int, Int>()
    private var currentFrameRotations: List<Int>? = null
    private var targetSolution: List<Int>? = null

    init {
        TickTask(1) {
            if (DungeonUtils.getF7Phase() != M7Phases.P3) return@TickTask
            clicksRemaining.clear()
            if ((mc.player?.pos?.distanceTo(Vec3d(0.0, 120.0, 77.0)) ?: return@TickTask) > 200) {
                currentFrameRotations = null
                targetSolution = null
                return@TickTask
            }

            currentFrameRotations = getFrames()

            possibleSolutions.forEach { arr ->
                for (i in arr.indices) {
                    if ((arr[i] == -1 || currentFrameRotations?.get(i) == -1) && arr[i] != currentFrameRotations?.get(i)) return@forEach
                }

                targetSolution = arr

                for (i in arr.indices) {
                    clicksRemaining[i] = calculateClicksNeeded(currentFrameRotations?.get(i) ?: return@forEach, arr[i]).takeIf { it != 0 } ?: continue
                }
            }
        }
    }

    @EventHandler
    fun onPacket(event: PacketEvent.Send) {
        val packet = event.packet as? PlayerInteractEntityC2SPacket ?: return
        if (DungeonUtils.getF7Phase() != M7Phases.P3) return

        val entity = mc.world?.getEntityById((packet as PlayerInteractEntityC2SPacketAccessor).entityId) as? ItemFrameEntity ?: return
        if (entity.heldItemStack?.item != Items.ARROW) return
        val (x, y, z) = entity.blockPos

        val frameIndex = ((y - frameGridCorner.y) + (z - frameGridCorner.z) * 5)
        if (x != frameGridCorner.x || currentFrameRotations?.get(frameIndex) == -1 || frameIndex !in 0..24) return

        if (!clicksRemaining.containsKey(frameIndex) && mc.player?.isSneaking == invertSneak && blockWrong) {
            event.cancel()
            return
        }

        recentClickTimestamps[frameIndex] = System.currentTimeMillis()
        currentFrameRotations = currentFrameRotations?.toMutableList()?.apply { this[frameIndex] = (this[frameIndex] + 1) % 8 }

        if (calculateClicksNeeded(currentFrameRotations?.get(frameIndex) ?: return, targetSolution?.get(frameIndex) ?: return) == 0) clicksRemaining.remove(frameIndex)
    }

    @EventHandler
    fun onRenderWorld(event: RenderEvent.Last) {
        if (clicksRemaining.isEmpty() || DungeonUtils.getF7Phase() != M7Phases.P3) return
        clicksRemaining.forEach { (index, clickNeeded) ->
            val colorCode = when {
                clickNeeded == 0 -> return@forEach
                clickNeeded < 3 -> 'a'
                clickNeeded < 5 -> '6'
                else -> 'c'
            }
            event.context.drawText(
                Text.of("§$colorCode$clickNeeded").asOrderedText(),
                getFramePositionFromIndex(index).toCenterPos().addVec(y = 0.1, x = -0.3),
                1f,
                false
            )
        }
    }

    private fun getFrames(): List<Int> {
        val itemFrames = mc.world?.entities?.mapNotNull {
            if (it is ItemFrameEntity && it.heldItemStack?.item?.asItem() == Items.ARROW) it else null
        }?.takeIf { it.isNotEmpty() } ?: return List(25) { -1 }

        return (0..24).map { index ->
            if (recentClickTimestamps[index]?.let { System.currentTimeMillis() - it < 1000 } == true && currentFrameRotations != null)
                currentFrameRotations?.get(index) ?: -1
            else
                itemFrames.find { it.blockPos == getFramePositionFromIndex(index) }?.rotation ?: -1
        }
    }

    private fun getFramePositionFromIndex(index: Int): BlockPos =
        frameGridCorner.add(0, index % 5, index / 5)

    private fun calculateClicksNeeded(currentRotation: Int, targetRotation: Int): Int =
        (8 - currentRotation + targetRotation) % 8

    private val possibleSolutions = listOf(
        listOf(7, 7, -1, -1, -1, 1, -1, -1, -1, -1, 1, 3, 3, 3, 3, -1, -1, -1, -1, 1, -1, -1, -1, 7, 1),
        listOf(-1, -1, 7, 7, 5, -1, 7, 1, -1, 5, -1, -1, -1, -1, -1, -1, 7, 5, -1, 1, -1, -1, 7, 7, 1),
        listOf(7, 7, -1, -1, -1, 1, -1, -1, -1, -1, 1, 3, -1, 7, 5, -1, -1, -1, -1, 5, -1, -1, -1, 3, 3),
        listOf(5, 3, 3, 3, -1, 5, -1, -1, -1, -1, 7, 7, -1, -1, -1, 1, -1, -1, -1, -1, 1, 3, 3, 3, -1),
        listOf(5, 3, 3, 3, 3, 5, -1, -1, -1, 1, 7, 7, -1, -1, 1, -1, -1, -1, -1, 1, -1, 7, 7, 7, 1),
        listOf(7, 7, 7, 7, -1, 1, -1, -1, -1, -1, 1, 3, 3, 3, 3, -1, -1, -1, -1, 1, -1, 7, 7, 7, 1),
        listOf(-1, -1, -1, -1, -1, 1, -1, 1, -1, 1, 1, -1, 1, -1, 1, 1, -1, 1, -1, 1, -1, -1, -1, -1, -1),
        listOf(-1, -1, -1, -1, -1, 1, 3, 3, 3, 3, -1, -1, -1, -1, 1, 7, 7, 7, 7, 1, -1, -1, -1, -1, -1),
        listOf(-1, -1, -1, -1, -1, -1, 1, -1, 1, -1, 7, 1, 7, 1, 3, 1, -1, 1, -1, 1, -1, -1, -1, -1, -1)
    )
}