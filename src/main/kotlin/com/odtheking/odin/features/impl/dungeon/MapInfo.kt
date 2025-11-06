package com.odtheking.odin.features.impl.dungeon

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.*
import com.odtheking.odin.events.WorldLoadEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Color.Companion.withAlpha
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.alert
import com.odtheking.odin.utils.handlers.TickTask
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.render.drawString
import com.odtheking.odin.utils.render.drawStringWidth
import com.odtheking.odin.utils.render.getStringWidth
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils

object MapInfo : Module(
    name = "Map Info",
    description = "Displays stats about the dungeon such as score, secrets, and deaths."
) {
    private val disableInBoss by BooleanSetting("Disable in boss", true, desc = "Disables the information display when you're in boss.")
    private val scoreTitle by BooleanSetting("300 Score Title", true, desc = "Displays a title on 300 score.")
    private val scoreText by StringSetting("Title Text", "&c300 Score!", desc = "Text to be displayed on 300 score.").withDependency { scoreTitle }
    private val printWhenScore by BooleanSetting("Print Score Time", true, desc = "Sends elapsed time in chat when 300 score is reached.")
    val togglePaul by SelectorSetting("Paul Settings", "Automatic", arrayListOf("Automatic", "Force Disable", "Force Enable"), desc = "Toggle Paul's settings.")

    private val fullHud: HudElement by HUD("Full Hud", "Displays a full hud with score, secrets, crypts, and mimic info.") {
        if ((!DungeonUtils.inDungeons || (disableInBoss && DungeonUtils.inBoss)) && !it) return@HUD 0 to 0

        val scoreText = "§7Score: ${colorizeScore(DungeonUtils.score)}"
        val secretText = "§7Secrets: §b${DungeonUtils.secretCount}" +
                (if (fullAddRemaining && alternate) "§7-§d${(DungeonUtils.neededSecretsAmount - DungeonUtils.secretCount).coerceAtLeast(0)}" else "") +
                "§7-§e${if (fullRemaining != 0 || (fullAddRemaining && alternate)) DungeonUtils.neededSecretsAmount else (DungeonUtils.neededSecretsAmount - DungeonUtils.secretCount).coerceAtLeast(0)}"+
                "§7-§c${DungeonUtils.totalSecrets}"
        val unknownSecretsText = if (unknown == 0) "§7Deaths: §c${colorizeDeaths(DungeonUtils.deathCount)}" else "§7Unfound: §e${(DungeonUtils.totalSecrets - DungeonUtils.knownSecrets).coerceAtLeast(0)}"
        val mimicText = "§7M: ${if (DungeonUtils.mimicKilled) "§a✔" else "§c✘"} §8| §7P: ${if (DungeonUtils.princeKilled) "§a✔" else "§c✘"}"
        val cryptText = "§7Crypts: ${colorizeCrypts(DungeonUtils.cryptCount.coerceAtMost(5))}"

        val trText = if (alternate) cryptText else scoreText
        val brText = if (alternate) scoreText else cryptText

        if (fullBackground) fill((-fullMargin).toInt(), 0, (fullWidth + (fullMargin * 2)).toInt(), 19, fullColor.rgba)
        val brWidth = getStringWidth(brText)

        drawString(secretText, 1, 1, Colors.WHITE.rgba)
        drawString(trText, fullWidth - 1 - getStringWidth(trText), 1, Colors.WHITE.rgba)
        val unknownWidth = drawStringWidth(unknownSecretsText, 1, 10, Colors.WHITE)
        val centerX = (unknownWidth + 1 + (fullWidth - 1 - unknownWidth - brWidth) / 2) - getStringWidth(mimicText) / 2
        drawString(mimicText, centerX, 10, Colors.WHITE.rgba)
        drawString(brText, fullWidth - 1 - brWidth, 10, Colors.WHITE.rgba)
        fullWidth to 19
    }

    private val alternate by BooleanSetting("Flip Crypts and Score", false, desc = "Flips crypts and score.").withDependency { fullHud.enabled }
    private val fullAddRemaining by BooleanSetting("Include Remaining", false, desc = "Adds remaining to the secrets display.").withDependency { alternate && fullHud.enabled }
    private val fullRemaining by SelectorSetting("Remaining Secrets", "Minimum", options = arrayListOf("Minimum", "Remaining"), desc = "Display minimum secrets or secrets until s+.").withDependency { !(fullAddRemaining && alternate) && fullHud.enabled }
    private val fullWidth by NumberSetting("Width", 160, 160, 200, 1, desc = "The width of the hud.").withDependency { fullHud.enabled }
    private val unknown by SelectorSetting("Deaths", "Deaths", arrayListOf("Deaths", "Unfound"), desc = "Display deaths or unfound secrets. (Unknown secrets are secrets in rooms that haven't been discovered yet. May not be helpful in full party runs.)").withDependency { fullHud.enabled }
    private val fullBackground by BooleanSetting("Hud Background", false, desc = "Render a background behind the score info.").withDependency { fullHud.enabled }
    private val fullMargin by NumberSetting("Hud Margin", 0f, 0f, 5f, 1f, desc = "The margin around the hud.").withDependency { fullBackground && fullHud.enabled }
    private val fullColor by ColorSetting("Hud Background Color", Colors.MINECRAFT_DARK_GRAY.withAlpha(0.5f), true, desc = "The color of the background.").withDependency { fullBackground && fullHud.enabled }

    private val compactSecrets: HudElement by HUD("Compact Secrets", "Displays a compact secrets hud with score and secrets.") {
        if ((!DungeonUtils.inDungeons || (disableInBoss && DungeonUtils.inBoss)) && !it) return@HUD 0 to 0
        val secretText = "§7Secrets: §b${DungeonUtils.secretCount}" +
                (if (compactAddRemaining) "§7-§d${(DungeonUtils.neededSecretsAmount - DungeonUtils.secretCount).coerceAtLeast(0)}" else "") +
                "§7-§e${if (compactRemaining == 0 || fullAddRemaining) DungeonUtils.neededSecretsAmount else (DungeonUtils.neededSecretsAmount - DungeonUtils.secretCount).coerceAtLeast(0)}"+
                "§7-§c${DungeonUtils.totalSecrets}"
        val width = getStringWidth(secretText)

        if (compactSecretBackground) fill((-compactSecretMargin).toInt(), 0, (width + 2 + (compactSecretMargin * 2)).toInt(), 9, compactSecretColor.rgba)
        drawString(secretText, 1, 1, Colors.WHITE.rgba)
        width + 2 to 10
    }

    private val compactAddRemaining by BooleanSetting("Compact Include remaining", false, desc = "Adds remaining to the secrets display.").withDependency { compactSecrets.enabled }
    private val compactRemaining by SelectorSetting("Min Secrets", "Minimum", options = arrayListOf("Minimum", "Remaining"), desc = "Display minimum secrets or secrets until s+.").withDependency { !compactAddRemaining && compactSecrets.enabled }
    private val compactSecretBackground by BooleanSetting("Secret Background", false, desc = "Render a background behind the score info.").withDependency { compactSecrets.enabled }
    private val compactSecretMargin by NumberSetting("Secret Margin", 0f, 0f, 5f, 1f, desc = "The margin around the hud.").withDependency { compactSecretBackground && compactSecrets.enabled }
    private val compactSecretColor by ColorSetting("Secret Background Color", Colors.MINECRAFT_DARK_GRAY.withAlpha(0.5f), true, desc = "The color of the background.").withDependency { compactSecretBackground && compactSecrets.enabled }

    private val compactScore: HudElement by HUD("Compact Score", "Displays a compact score hud with score info.") {
        if ((!DungeonUtils.inDungeons || (disableInBoss && DungeonUtils.inBoss)) && !it) return@HUD 0 to 0
        val missing = (if (DungeonUtils.mimicKilled) 0 else 2) + (if (DungeonUtils.princeKilled) 0 else 1)
        val scoreText = "§7Score: ${colorizeScore(DungeonUtils.score)}" + if (missing > 0) " §7(§6+${missing}?§7)" else ""
        val width = getStringWidth(scoreText)
        if (compactScoreBackground) fill((-compactScoreMargin).toInt(), 0, (width + 2 + (compactScoreMargin * 2)).toInt(), 9, compactScoreColor.rgba)
        drawString(scoreText, 1, 1, Colors.WHITE.rgba)
        width + 2 to 10
    }

    private val compactScoreBackground by BooleanSetting("Score Background", false, desc = "Render a background behind the score info.").withDependency { compactScore.enabled }
    private val compactScoreMargin by NumberSetting("Score Margin", 0f, 0f, 5f, 1f, desc = "The margin around the hud.").withDependency { compactScoreBackground && compactScore.enabled }
    private val compactScoreColor by ColorSetting("Score Background Color", Colors.MINECRAFT_DARK_GRAY.withAlpha(0.5f), true, desc = "The color of the background.").withDependency { compactScoreBackground && compactScore.enabled }

    var shownTitle = false

    init {
        TickTask(10) {
            if (!DungeonUtils.inDungeons || shownTitle || (!scoreTitle && !printWhenScore) || DungeonUtils.score < 300) return@TickTask
            if (scoreTitle) alert(scoreText.replace("&", "§"))
            if (printWhenScore) modMessage("§b${DungeonUtils.score} §ascore reached in §6${DungeonUtils.dungeonTime} || ${DungeonUtils.floor?.name}.")
            shownTitle = true
        }

        on<WorldLoadEvent> {
            shownTitle = false
        }
    }

    private fun colorizeCrypts(count: Int): String {
        return when {
            count < 3 -> "§c${count}"
            count < 5 -> "§e${count}"
            else -> "§a${count}"
        }
    }

    private fun colorizeScore(score: Int): String {
        return when {
            score < 270 -> "§c${score}"
            score < 300 -> "§e${score}"
            else -> "§a${score}"
        }
    }

    private fun colorizeDeaths(count: Int): String {
        val floor = DungeonUtils.floor?.floorNumber ?: 0
        return when {
            count == 0 -> "§a0"
            count <= if (floor < 6) 2 else 3 -> "§e${count}"
            count == if (floor < 6) 3 else 4 -> "§c${count}"
            else -> "§4${count}"
        }
    }
}