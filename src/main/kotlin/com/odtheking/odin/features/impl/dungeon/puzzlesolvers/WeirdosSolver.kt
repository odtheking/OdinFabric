package com.odtheking.odin.features.impl.dungeon.puzzlesolvers

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.addRotationCoords
import com.odtheking.odin.utils.playSoundAtPlayer
import com.odtheking.odin.utils.render.drawStyledBox
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import java.util.concurrent.CopyOnWriteArraySet

object WeirdosSolver {
    private var correctPos: BlockPos? = null
    private var wrongPositions = CopyOnWriteArraySet<BlockPos>()

    fun onNPCMessage(npc: String, msg: String) {
        if (solutions.none { it.matches(msg) } && wrong.none { it.matches(msg) }) return
        val correctNPC = mc.world?.entities?.find { it is ArmorStandEntity && it.name.string == npc } ?: return
        val room = DungeonUtils.currentRoom ?: return
        val pos = BlockPos(correctNPC.x.toInt() - 1, 69, correctNPC.z.toInt() -1).addRotationCoords(room.rotation, -1, 0)

        if (solutions.any { it.matches(msg) }) {
            correctPos = pos
            playSoundAtPlayer(SoundEvents.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 2f, 1f)
        } else wrongPositions.add(pos)
    }

    fun onRenderWorld(event: RenderEvent, weirdosColor: Color, weirdosWrongColor: Color, weirdosStyle: Int) {
        if (DungeonUtils.currentRoomName != "Three Weirdos") return
        correctPos?.let { event.drawStyledBox(Box(it), weirdosColor, weirdosStyle) }
        wrongPositions.forEach {
            event.drawStyledBox(Box(it), weirdosWrongColor, weirdosStyle)
        }
    }

    fun reset() {
        correctPos = null
        wrongPositions.clear()
    }

    private val solutions = listOf(
        Regex("The reward is not in my chest!"),
        Regex("At least one of them is lying, and the reward is not in .+'s chest.?"),
        Regex("My chest doesn't have the reward. We are all telling the truth.?"),
        Regex("My chest has the reward and I'm telling the truth!"),
        Regex("The reward isn't in any of our chests.?"),
        Regex("Both of them are telling the truth. Also, .+ has the reward in their chest.?"),
    )

    private val wrong = listOf(
        Regex("One of us is telling the truth!"),
        Regex("They are both telling the truth. The reward isn't in .+'s chest."),
        Regex("We are all telling the truth!"),
        Regex(".+ is telling the truth and the reward is in his chest."),
        Regex("My chest doesn't have the reward. At least one of the others is telling the truth!"),
        Regex("One of the others is lying."),
        Regex("They are both telling the truth, the reward is in .+'s chest."),
        Regex("They are both lying, the reward is in my chest!"),
        Regex("The reward is in my chest."),
        Regex("The reward is not in my chest. They are both lying."),
        Regex(".+ is telling the truth."),
        Regex("My chest has the reward.")
    )
}