package com.odtheking.odin.features.impl.dungeon.puzzlesolvers

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.odtheking.odin.OdinMod.logger
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.RoomEnterEvent
import com.odtheking.odin.features.impl.dungeon.puzzlesolvers.PuzzleSolvers.onPuzzleComplete
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.render.drawBeaconBeam
import com.odtheking.odin.utils.render.drawFilledBox
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils.getRealCoords
import com.odtheking.odin.utils.startsWithOneOf
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

object QuizSolver {
    private var answers: MutableMap<String, List<String>>
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val isr = this::class.java.getResourceAsStream("/assets/odin/puzzles/quizAnswers.json")?.let { InputStreamReader(it, StandardCharsets.UTF_8) }
    private var triviaAnswers: List<String>? = null

    private var triviaOptions: MutableList<TriviaAnswer> = MutableList(3) { TriviaAnswer(null, false) }
    private data class TriviaAnswer(var blockPos: BlockPos?, var isCorrect: Boolean)

    init {
        try {
            val text = isr?.readText()
            answers = gson.fromJson(text, object : TypeToken<MutableMap<String, List<String>>>() {}.type)
            isr?.close()
        } catch (e: Exception) {
            logger.error("Error loading quiz answers", e)
            answers = mutableMapOf()
        }
    }

    fun onMessage(msg: String) {
        if (msg.startsWith("[STATUE] Oruo the Omniscient: ") && msg.endsWith("correctly!")) {
            if (msg.contains("answered the final question")) {
                onPuzzleComplete("Quiz")
                reset()
                return
            }
            if (msg.contains("answered Question #")) triviaOptions.forEach { it.isCorrect = false }
        }

        if (msg.trim().startsWithOneOf("ⓐ", "ⓑ", "ⓒ", ignoreCase = true) && triviaAnswers?.any { msg.endsWith(it) } == true) {
            when (msg.trim()[0]) {
                'ⓐ' -> triviaOptions[0].isCorrect = true
                'ⓑ' -> triviaOptions[1].isCorrect = true
                'ⓒ' -> triviaOptions[2].isCorrect = true
            }
        }

        triviaAnswers = when {
            msg.trim() == "What SkyBlock year is it?" -> listOf("Year ${(((System.currentTimeMillis() / 1000) - 1560276000) / 446400).toInt() + 1}")
            else -> answers.entries.find { msg.contains(it.key) }?.value ?: return
        }
    }

    fun onRoomEnter(event: RoomEnterEvent) = with(event.room) {
        if (this?.data?.name != "Quiz") return

        triviaOptions[0].blockPos = getRealCoords(BlockPos(20, 70, 6))
        triviaOptions[1].blockPos = getRealCoords(BlockPos(15, 70, 9))
        triviaOptions[2].blockPos = getRealCoords(BlockPos(10, 70, 6))
    }

    fun onRenderWorld(event: RenderEvent, quizColor: Color, quizDepth: Boolean) {
        if (triviaAnswers == null || triviaOptions.isEmpty()) return
        triviaOptions.forEach { answer ->
            if (!answer.isCorrect) return@forEach
            answer.blockPos?.add(0, -1, 0)?.let {
                event.drawFilledBox(Box(it), quizColor, depth = quizDepth)
                event.drawBeaconBeam(it, quizColor)
            }
        }
    }

    fun reset() {
        triviaOptions = MutableList(3) { TriviaAnswer(null, false) }
        triviaAnswers = null
    }
}