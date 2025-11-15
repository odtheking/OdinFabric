@file:JvmName("Utils")

package com.odtheking.odin.utils

import com.odtheking.odin.OdinMod
import com.odtheking.odin.OdinMod.mc
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import java.util.*

val FORMATTING_CODE_PATTERN = Regex("§[0-9a-fk-or]", RegexOption.IGNORE_CASE)

/**
 * Returns the string without any minecraft formatting codes.
 */
inline val String?.noControlCodes: String
    get() = this?.replace(FORMATTING_CODE_PATTERN, "") ?: ""

/**
 * Checks if the current string contains at least one of the specified strings.
 *
 * @param options List of strings to check.
 * @param ignoreCase If comparison should be case-sensitive or not.
 * @return `true` if the string contains at least one of the specified options, otherwise `false`.
 */
fun String.containsOneOf(vararg options: String, ignoreCase: Boolean = false): Boolean =
    containsOneOf(options.toList(), ignoreCase)

/**
 * Checks if the current string contains at least one of the specified strings.
 *
 * @param options List of strings to check.
 * @param ignoreCase If comparison should be case-sensitive or not.
 * @return `true` if the string contains at least one of the specified options, otherwise `false`.
 */
fun String.containsOneOf(options: Collection<String>, ignoreCase: Boolean = false): Boolean =
    options.any { this.contains(it, ignoreCase) }

fun Number.toFixed(decimals: Int = 2): String =
    "%.${decimals}f".format(Locale.US, this)

fun String.startsWithOneOf(vararg options: String, ignoreCase: Boolean = false): Boolean =
    options.any { this.startsWith(it, ignoreCase) }

/**
 * Checks if the current object is equal to at least one of the specified objects.
 *
 * @param options List of other objects to check.
 * @return `true` if the object is equal to one of the specified objects.
 */
fun Any?.equalsOneOf(vararg options: Any?): Boolean =
    options.any { this == it }

fun String.matchesOneOf(vararg options: Regex): Boolean =
    options.any { this.matches(it) }

fun logError(throwable: Throwable, context: Any) {
    val message =
        "${OdinMod.version} Caught an ${throwable::class.simpleName ?: "error"} at ${context::class.simpleName}."
    OdinMod.logger.error(message, throwable)

    modMessage(Component.literal("$message §cPlease click this message to copy and send it in the Odin discord!").withStyle {
        it
            .withClickEvent(
                ClickEvent.RunCommand(
                    "odin copy $message \\n``` ${throwable.message} \\n${
                        throwable.stackTraceToString().lineSequence().take(10).joinToString("\n")
                    }```"
                )
            )
            .withHoverEvent(HoverEvent.ShowText(Component.literal("§6Click to copy the error to your clipboard.")))
    })
}

fun setClipboardContent(string: String) {
    try {
        mc.keyboardHandler?.clipboard = string.ifEmpty { " " }
    } catch (e: Exception) {
        OdinMod.logger.error("Failed to set Clipboard Content", e)
    }
}

fun String.capitalizeFirst(): String =
    if (isNotEmpty() && this[0] in 'a'..'z') this[0].uppercaseChar() + substring(1) else this

fun formatTime(time: Long, decimalPlaces: Int = 2): String {
    if (time == 0L) return "0s"
    var remaining = time
    val hours = (remaining / 3600000).toInt().let {
        remaining -= it * 3600000
        if (it > 0) "${it}h " else ""
    }
    val minutes = (remaining / 60000).toInt().let {
        remaining -= it * 60000
        if (it > 0) "${it}m " else ""
    }
    return "$hours$minutes${(remaining / 1000f).toFixed(decimalPlaces)}s"
}

inline val Entity.renderX: Double
    get() =
        xo + (x - xo) * mc.deltaTracker.getGameTimeDeltaPartialTick(true)

inline val Entity.renderY: Double
    get() =
        yo + (y - yo) * mc.deltaTracker.getGameTimeDeltaPartialTick(true)

inline val Entity.renderZ: Double
    get() =
        zo + (z - zo) * mc.deltaTracker.getGameTimeDeltaPartialTick(true)

inline val Entity.renderPos: Vec3
    get() =
        Vec3(renderX, renderY, renderZ)

inline val Entity.renderBoundingBox: AABB
    get() =
        boundingBox.move(renderX - x, renderY - y, renderZ - z)

infix fun EquipmentSlot.isItem(itemId: String): Boolean =
    mc.player?.getItemBySlot(this)?.itemId == itemId

fun fillItemFromSack(amount: Int, itemId: String, sackName: String, sendMessage: Boolean) {
    val needed = mc.player?.inventory?.find { it?.itemId == itemId }?.count ?: 0
    if (needed != amount) sendCommand("gfs $sackName ${amount - needed}") else if (sendMessage) modMessage("§cAlready at max stack size.")
}

private val romanMap = mapOf('I' to 1, 'V' to 5, 'X' to 10, 'L' to 50, 'C' to 100, 'D' to 500, 'M' to 1000)
private val numberRegex = Regex("^[0-9]+$")
fun romanToInt(s: String): Int {
    return if (s.matches(numberRegex)) s.toInt()
    else {
        var result = 0
        for (i in 0 until s.length - 1) {
            val current = romanMap[s[i]] ?: 0
            val next = romanMap[s[i + 1]] ?: 0
            result += if (current < next) -current else current
        }
        result + (romanMap[s.last()] ?: 0)
    }
}

fun BlockPos.getBlockBounds() =
    mc.level?.let { level ->
        level.getBlockState(this)?.getShape(level, this)?.singleEncompassing()
            ?.takeIf { !it.isEmpty }?.bounds()
    }


fun Player.clickSlot(containerId: Int, slotIndex: Int, button: Int = 0, clickType: ClickType = ClickType.PICKUP) {
    mc.gameMode?.handleInventoryMouseClick(containerId, slotIndex, button, clickType, this)
}

fun getCenteredText(text: String): String {
    val strippedText = text.noControlCodes
    if (strippedText.isEmpty()) return text
    val textWidth = mc.font.width(strippedText)
    val chatWidth = mc.gui.chat.width

    if (textWidth >= chatWidth) return text

    val spacesNeeded = ((chatWidth - textWidth) / 2 / 4).coerceAtLeast(0)
    return " ".repeat(spacesNeeded) + text
}

fun getChatBreak(): String =
    mc.gui?.chat?.width?.let {
        "§9§m" + "-".repeat(it / mc.font.width("-"))
    } ?: ""

fun formatNumber(numStr: String): String {
    val num = numStr.replace(",", "").toDoubleOrNull() ?: return numStr
    return when {
        num >= 1_000_000_000 -> "%.2fB".format(num / 1_000_000_000)
        num >= 1_000_000 -> "%.2fM".format(num / 1_000_000)
        num >= 1_000 -> "%.2fK".format(num / 1_000)
        else -> "%.0f".format(num)
    }
}