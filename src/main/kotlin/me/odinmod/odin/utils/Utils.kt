@file:JvmName("Utils")

package me.odinmod.odin.utils

import me.odinmod.odin.OdinMod
import me.odinmod.odin.OdinMod.mc
import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.text.Text
import java.util.*
import kotlin.math.max
import kotlin.math.min

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

fun logError(throwable: Throwable, context: Any) {
    val message = "${OdinMod.version} Caught an ${throwable::class.simpleName ?: "error"} at ${context::class.simpleName}."
    OdinMod.logger.error(message, throwable)

    modMessage(Text.literal("$message §cPlease click this message to copy and send it in the Odin discord!").styled { it
        .withClickEvent(ClickEvent.RunCommand("odin copy $message \\n``` ${throwable.message} \\n${throwable.stackTraceToString().lineSequence().take(10).joinToString("\n")}```"))
        .withHoverEvent(HoverEvent.ShowText(Text.literal("§6Click to copy the error to your clipboard.")))
    })
}

fun setClipboardContent(string: String) {
    try {
        mc.keyboard?.clipboard = string.ifEmpty { " " }
    } catch (e: Exception) {
        OdinMod.logger.error("Failed to set Clipboard Content", e)
    }
}

fun String.substringSafe(from: Int, to: Int): String {
    val f = min(from, to).coerceAtLeast(0)
    val t = max(to, from)
    if (t > length) return substring(f)
    return substring(f, t)
}

fun String.removeRangeSafe(from: Int, to: Int): String {
    val f = min(from, to)
    val t = max(to, from)
    return removeRange(f, t)
}

fun String.dropAt(at: Int, amount: Int): String {
    return removeRangeSafe(at, at + amount)
}

/**
 * Returns the String with the first letter capitalized
 *
 * @return The String with the first letter capitalized
 */
fun String.capitalizeFirst(): String = replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }