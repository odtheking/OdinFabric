package com.odtheking.odin.features.impl.dungeon.puzzlesolvers

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.RoomEnterEvent
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.addRotationCoords
import com.odtheking.odin.utils.equalsOneOf
import com.odtheking.odin.utils.render.drawStyledBox
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.minecraft.block.Blocks
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

object BoulderSolver {
    private data class BoxPosition(val render: BlockPos, val click: BlockPos)
    private var currentPositions = mutableListOf<BoxPosition>()
    private var solutions: Map<String, List<List<Int>>>
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val isr = this::class.java.getResourceAsStream("/assets/odin/puzzles/boulderSolutions.json")?.let { InputStreamReader(it, StandardCharsets.UTF_8) }

    init {
        try {
            val text = isr?.readText()
            solutions = gson.fromJson(text, object : TypeToken<Map<String, List<List<Int>>>>() {}.type)
            isr?.close()
        } catch (e: Exception) {
            e.printStackTrace()
            solutions = emptyMap()
        }
    }

    fun onRoomEnter(event: RoomEnterEvent) {
        val room = event.room ?: return reset()
        if (room.data.name != "Boulder") return reset()
        val roomComponent = room.roomComponents.firstOrNull() ?: return reset()
        var str = ""
        for (z in -3..2) {
            for (x in -3..3) {
                roomComponent.blockPos.addRotationCoords(room.rotation, x * 3, z * 3).let { str += if (mc.world?.getBlockState(it.down(4))?.block == Blocks.AIR) "0" else "1" }
            }
        }
        currentPositions = solutions[str]?.map { sol ->
            val render = roomComponent.blockPos.addRotationCoords(room.rotation, sol[0], sol[1]).down(5)
            val click = roomComponent.blockPos.addRotationCoords(room.rotation, sol[2], sol[3]).down(5)
            BoxPosition(render, click)
        }?.toMutableList() ?: return
    }

    fun onRenderWorld(context: WorldRenderContext, showAllBoulderClicks: Boolean, boulderStyle: Int, boulderColor: Color) {
        if (DungeonUtils.currentRoomName != "Boulder" || currentPositions.isEmpty()) return
        if (showAllBoulderClicks) currentPositions.forEach {
            context.drawStyledBox(Box(it.render), boulderColor, boulderStyle)
        } else currentPositions.firstOrNull()?.let {
            context.drawStyledBox(Box(it.render), boulderColor, boulderStyle)
        }
    }

    fun playerInteract(event: PlayerInteractBlockC2SPacket) {
        if (mc.world?.getBlockState(event.blockHitResult.blockPos).equalsOneOf(Blocks.OAK_SIGN, Blocks.STONE_BUTTON))
            currentPositions.remove(currentPositions.firstOrNull { it.click == event.blockHitResult.blockPos })
    }

    fun reset() {
        currentPositions = mutableListOf()
    }
}