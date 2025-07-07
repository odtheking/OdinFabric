package me.odinmod.odin.features.impl.skyblock

import me.odinmod.odin.clickgui.settings.Setting.Companion.withDependency
import me.odinmod.odin.clickgui.settings.impl.BooleanSetting
import me.odinmod.odin.clickgui.settings.impl.ColorSetting
import me.odinmod.odin.clickgui.settings.impl.DropdownSetting
import me.odinmod.odin.features.Module
import me.odinmod.odin.utils.Colors
import me.odinmod.odin.utils.render.drawStringWidth
import me.odinmod.odin.utils.skyblock.LocationUtils
import me.odinmod.odin.utils.skyblock.SkyblockPlayer
import net.minecraft.text.Text
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

object PlayerDisplay : Module(
    name = "Player Display",
    description = "Allows to customize the player stat displays (health, strength and more)."
) {
    private val hideElements by DropdownSetting("Hide Elements")
    private val hideArmor by BooleanSetting("Hide Armor", false, desc = "Hides the armor bar.").withDependency { hideElements }
    private val hideFood by BooleanSetting("Hide Food", false, desc = "Hides the food bar.").withDependency { hideElements }
    private val hideHearts by BooleanSetting("Hide Hearts", false, desc = "Hides the hearts.").withDependency { hideElements }
    private val hideXP by BooleanSetting("Hide XP Level", false, desc = "Hides the XP level.").withDependency { hideElements }
    private val hideActionBar by DropdownSetting("Hide Action Bar Elements")
    private val hideHealth by BooleanSetting("Hide Health", true, desc = "Hides the health bar.").withDependency { hideActionBar }
    private val hideMana by BooleanSetting("Hide Mana", true, desc = "Hides the mana bar.").withDependency { hideActionBar }
    private val hideOverflow by BooleanSetting("Hide Overflow Mana", true, desc = "Hides the overflow mana bar.").withDependency { hideActionBar }
    private val hideDefense by BooleanSetting("Hide Defense", true, desc = "Hides the defense bar.").withDependency { hideActionBar }
    private val overflow by DropdownSetting("Overflow Mana")
    private val separateOverflow by BooleanSetting("Separate Overflow Mana", true, desc = "Separates the overflow mana from the mana bar.").withDependency { overflow }
    private val hideZeroSF by BooleanSetting("Hide 0 Overflow", true, desc = "Hides the overflow mana when it's 0.").withDependency { overflow && separateOverflow }

    private val showIcons by BooleanSetting("Show Icons", true, desc = "Shows icons indicating what the number means.")

    private val healthHud by HUD("Health HUD", "Displays the player's health.") { example ->
        val text = when {
            example -> 3000 to 4000
            !LocationUtils.isInSkyblock -> return@HUD 0f to 0f
            SkyblockPlayer.currentHealth != 0 && SkyblockPlayer.maxHealth != 0 -> SkyblockPlayer.currentHealth to SkyblockPlayer.maxHealth
            else -> return@HUD 0f to 0f
        }
        return@HUD drawStringWidth(generateText(text.first, text.second, "❤"), 1f, 1f, healthColor) + 2f to mc.textRenderer.fontHeight
    }
    private val healthColor by ColorSetting("Health Color", Colors.MINECRAFT_RED, true, "The color of the health text.")

    private val manaHud by HUD("Mana HUD", "Displays the player's mana.") { example ->
        val text = when {
            example -> generateText(2000, 20000, "✎") + (if(!separateOverflow) " ${generateText(SkyblockPlayer.overflowMana, "ʬ", hideZeroSF)}" else "")
            !LocationUtils.isInSkyblock -> return@HUD 0f to 0f
            SkyblockPlayer.maxMana != 0 -> when {
                SkyblockPlayer.currentMana == 0 && separateOverflow -> return@HUD 0f to 0f
                else -> generateText(SkyblockPlayer.currentMana, SkyblockPlayer.maxMana, "✎") +
                        (if(!separateOverflow && overflowManaHud.enabled) " ${generateText(SkyblockPlayer.overflowMana, "ʬ", hideZeroSF)}" else "")
            }
            else -> return@HUD 0f to 0f
        }
        return@HUD drawStringWidth(text, 1f, 1f, manaColor) + 2f to mc.textRenderer.fontHeight
    }
    private val manaColor by ColorSetting("Mana Color", Colors.MINECRAFT_AQUA, true, "The color of the mana text.")

    private val overflowManaHud by HUD("Overflow Mana HUD", "Displays the player's overflow mana.") { example ->
        val text = when {
            example -> 333
            !LocationUtils.isInSkyblock -> return@HUD 0f to 0f
            separateOverflow -> SkyblockPlayer.overflowMana
            else -> return@HUD 0f to 0f
        }
        return@HUD drawStringWidth(generateText(text, "ʬ", hideZeroSF), 1f, 1f, overflowManaColor) + 2f to mc.textRenderer.fontHeight
    }
    private val overflowManaColor by ColorSetting("Overflow Mana Color", Colors.MINECRAFT_DARK_AQUA, true, desc = "The color of the overflow mana text.")

    private val defenseHud by HUD("Defense HUD", "Displays the player's defense.") { example ->
        val text = when {
            example -> 1000
            !LocationUtils.isInSkyblock -> return@HUD 0f to 0f
            SkyblockPlayer.currentDefense != 0 -> SkyblockPlayer.currentDefense
            else -> return@HUD 0f to 0f
        }
        return@HUD drawStringWidth(generateText(text, "❈", true), 1f, 1f, defenseColor) + 2f to mc.textRenderer.fontHeight
    }
    private val defenseColor by ColorSetting("Defense Color", Colors.MINECRAFT_GREEN, true, desc = "The color of the defense text.")

    private val eHPHud by HUD("EHP HUD", "Displays the player's effective health (EHP).") { example ->
        val text = when {
            example -> 1000000
            !LocationUtils.isInSkyblock -> return@HUD 0f to 0f
            SkyblockPlayer.effectiveHP != 0 -> SkyblockPlayer.effectiveHP
            else -> return@HUD 0f to 0f
        }
        return@HUD drawStringWidth(generateText(text, "", true), 1f, 1f, ehpColor) + 2f to mc.textRenderer.fontHeight
    }
    private val ehpColor by ColorSetting("EHP", Colors.MINECRAFT_DARK_GREEN, true, "The color of the effective health text.")

    private val HEALTH_REGEX = Regex("[\\d|,]+/[\\d|,]+❤")
    private val MANA_REGEX = Regex("[\\d|,]+/[\\d|,]+✎( Mana)?")
    private val OVERFLOW_MANA_REGEX = Regex("§?[\\d|,]+ʬ")
    private val DEFENSE_REGEX = Regex("[\\d|,]+§a❈ Defense")

    @JvmStatic
    fun modifyText(text: Text): Text {
        if (!enabled) return text
        var toReturn = text.string
        toReturn = if (hideHealth) toReturn.replace(HEALTH_REGEX, "") else toReturn
        toReturn = if (hideMana) toReturn.replace(MANA_REGEX, "") else toReturn
        toReturn = if (hideOverflow) toReturn.replace(OVERFLOW_MANA_REGEX, "") else toReturn
        toReturn = if (hideDefense) toReturn.replace(DEFENSE_REGEX, "") else toReturn
        return Text.of(toReturn.trim())
    }

    private fun generateText(current: Int, max: Int, icon: String): String =
        "${formatNumberWithCustomSeparator(current)}/${formatNumberWithCustomSeparator(max)}${if (showIcons) icon else ""}"

    private fun generateText(current: Int, icon: String, hideZero: Boolean): String =
        if (!hideZero || current != 0) "${formatNumberWithCustomSeparator(current)}${if (showIcons) icon else ""}" else ""

    private fun formatNumberWithCustomSeparator(number: Int): String =
        DecimalFormat("#,###", DecimalFormatSymbols(Locale.US)).format(number)

    @JvmStatic
    fun shouldCancelOverlay(type: String): Boolean {
        if (!enabled || !LocationUtils.isInSkyblock) return false
        return when (type) {
            "armor" -> hideArmor
            "hearts" -> hideHearts
            "food" -> hideFood
            "xp" -> hideXP
            else -> false
        }
    }
}