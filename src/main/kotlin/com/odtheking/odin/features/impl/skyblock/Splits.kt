package com.odtheking.odin.features.impl.skyblock

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.MapSetting
import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.PersonalBest
import com.odtheking.odin.utils.formatTime
import com.odtheking.odin.utils.render.drawString
import com.odtheking.odin.utils.render.getStringWidth
import com.odtheking.odin.utils.skyblock.SplitsManager.currentSplits
import com.odtheking.odin.utils.skyblock.SplitsManager.getAndUpdateSplitsTimes
import com.odtheking.odin.utils.toFixed

object Splits : Module(
    name = "Splits",
    description = "Provides visual timers for Kuudra and Dungeons."
) {
    private val hud by HUD("Splits Display HUD", "Shows timers for each split.") { example ->
        if (example) {
            repeat(5) { i ->
                drawString("Split $i: 0h 00m 00s" + if (showTickTime) " §7(§80s§7)" else "", 1, 1 + i * 9, Colors.WHITE.rgba)
            }
            return@HUD getStringWidth("Split 0: 0h 00m 00s" + if (showTickTime) " (0s)" else "") + 2 to 9 * 5
        }

        val (times, tickTimes, current) = getAndUpdateSplitsTimes(currentSplits)
        if (currentSplits.splits.isEmpty()) return@HUD 0 to 0

        val maxWidth = currentSplits.splits.dropLast(1).maxOf { getStringWidth(it.name) }

        currentSplits.splits.dropLast(1).forEachIndexed { index, split ->
            val time = formatTime(if (index >= times.size) 0 else times[index], numbersAfterDecimal)
            drawString(split.name, 1, 1 + index * 9, Colors.WHITE.rgba)

            val displayText = if (showTickTime && index < tickTimes.size) "$time §7(§8${(tickTimes[index] / 20f).toFixed()}§7)" else time

            drawString(displayText, maxWidth + 5, 1 + index * 9, Colors.WHITE.rgba)
        }

        if (bossEntrySplit && currentSplits.splits.size > 3) {
            drawString("§9Boss Entry", 1, 1 + (currentSplits.splits.size - 1) * 9, Colors.WHITE.rgba)

            val totalTime = formatTime(times.take(3).sum(), numbersAfterDecimal)
            val displayText = if (showTickTime) "$totalTime §7(§8${(tickTimes.take(3).sum() / 20f).toFixed()}§7)" else totalTime

            drawString(displayText, maxWidth + 5, 1 + (currentSplits.splits.size - 1) * 9, Colors.WHITE.rgba)
        }

        getStringWidth("Split 0: 0h 00m 00s" + if (showTickTime) " (0h 00m 00s)" else "") + 2 to 9 * (currentSplits.splits.size + (if (bossEntrySplit) 1 else 0))
    }

    private val bossEntrySplit by BooleanSetting("Boss Entry Split", true, desc = "Split for boss entry.")
    val sendSplits by BooleanSetting("Send Splits", true, desc = "Send splits to chat.")
    val sendOnlyPB by BooleanSetting("Send Only PB", false, desc = "Send only personal bests.")
    private val numbersAfterDecimal by NumberSetting("Numbers After Decimal", 2, 0, 5, 1, desc = "Numbers after decimal in time.")
    val showTickTime by BooleanSetting("Show Tick Time", false, desc = "Show tick-based time alongside real time.")

    val kuudraT5PBs = PersonalBest(+MapSetting("KuudraT5", mutableMapOf<Int, Float>()))
    val kuudraT4PBs = PersonalBest(+MapSetting("KuudraT4", mutableMapOf<Int, Float>()))
    val kuudraT3PBs = PersonalBest(+MapSetting("KuudraT3", mutableMapOf<Int, Float>()))
    val kuudraT2PBs = PersonalBest(+MapSetting("KuudraT2", mutableMapOf<Int, Float>()))
    val kuudraT1PBs = PersonalBest(+MapSetting("KuudraT1", mutableMapOf<Int, Float>()))

    private val dungeonEPBs = PersonalBest(+MapSetting("DungeonE", mutableMapOf<Int, Float>()))
    private val dungeonF1PBs = PersonalBest(+MapSetting("DungeonF1", mutableMapOf<Int, Float>()))
    private val dungeonF2PBs = PersonalBest(+MapSetting("DungeonF2", mutableMapOf<Int, Float>()))
    private val dungeonF3PBs = PersonalBest(+MapSetting("DungeonF3", mutableMapOf<Int, Float>()))
    private val dungeonF4PBs = PersonalBest(+MapSetting("DungeonF4", mutableMapOf<Int, Float>()))
    private val dungeonF5PBs = PersonalBest(+MapSetting("DungeonF5", mutableMapOf<Int, Float>()))
    private val dungeonF6PBs = PersonalBest(+MapSetting("DungeonF6", mutableMapOf<Int, Float>()))
    private val dungeonF7PBs = PersonalBest(+MapSetting("DungeonF7", mutableMapOf<Int, Float>()))

    private val dungeonM1PBs = PersonalBest(+MapSetting("DungeonM1", mutableMapOf<Int, Float>()))
    private val dungeonM2PBs = PersonalBest(+MapSetting("DungeonM2", mutableMapOf<Int, Float>()))
    private val dungeonM3PBs = PersonalBest(+MapSetting("DungeonM3", mutableMapOf<Int, Float>()))
    private val dungeonM4PBs = PersonalBest(+MapSetting("DungeonM4", mutableMapOf<Int, Float>()))
    private val dungeonM5PBs = PersonalBest(+MapSetting("DungeonM5", mutableMapOf<Int, Float>()))
    private val dungeonM6PBs = PersonalBest(+MapSetting("DungeonM6", mutableMapOf<Int, Float>()))
    private val dungeonM7PBs = PersonalBest(+MapSetting("DungeonM7", mutableMapOf<Int, Float>()))

    val dungeonPBsList = listOf(dungeonEPBs, dungeonF1PBs, dungeonF2PBs, dungeonF3PBs, dungeonF4PBs, dungeonF5PBs, dungeonF6PBs, dungeonF7PBs,
        dungeonM1PBs, dungeonM2PBs, dungeonM3PBs, dungeonM4PBs, dungeonM5PBs, dungeonM6PBs, dungeonM7PBs)
}