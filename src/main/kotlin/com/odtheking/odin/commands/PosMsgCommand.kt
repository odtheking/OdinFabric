package com.odtheking.odin.commands

import com.github.stivais.commodore.Commodore
import com.github.stivais.commodore.utils.GreedyString
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.config.Config
import com.odtheking.odin.features.impl.dungeon.PositionalMessages
import com.odtheking.odin.features.impl.dungeon.PositionalMessages.posMessageStrings
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.modMessage
import kotlin.math.floor

val posMsgCommand = Commodore("posmsg") {
    literal("add") {
        literal("at").runs { x: Double, y: Double, z: Double, delay: Long, distance: Double, color: String, message: GreedyString ->
            val color = getColorFromString(color) ?: return@runs modMessage("Unknown color $color")
            posMessageStrings.add(PositionalMessages.PosMessage(x, y, z, null, null, null, delay, distance, color, message.string
                ).takeUnless { it in posMessageStrings } ?: return@runs modMessage("This message already exists!"))
            modMessage("Message \"${message}\" added at $x, $y, $z, with ${delay}ms delay, triggered up to $distance blocks away.")
            Config.save()
        }
        literal("in").runs { x: Double, y: Double, z: Double, x2: Double, y2: Double, z2: Double, delay: Long, color: String, message: GreedyString ->
            val color = getColorFromString(color) ?: return@runs modMessage("Unknown color $color")
            posMessageStrings.add(
                PositionalMessages.PosMessage(x, y, z, x2, y2, z2, delay, null, color, message.string).takeUnless { it in posMessageStrings } ?: return@runs modMessage("This message already exists!"))
            modMessage("Message \"${message}\" added in $x, $y, $z, $x2, $y2, $z2, with ${delay}ms delay.")
            Config.save()
        }
        literal("atself").runs { delay: Long, distance: Double, color: String, message: GreedyString ->
            val color = getColorFromString(color) ?: return@runs modMessage("Unknown color $color")
            val x = floor(mc.player?.x ?: return@runs)
            val y = floor(mc.player?.y ?: return@runs)
            val z = floor(mc.player?.z ?: return@runs)
            posMessageStrings.add(PositionalMessages.PosMessage(x, y, z, null, null, null, delay, distance, color, message.string)
                .takeUnless { it in posMessageStrings } ?: return@runs modMessage("This message already exists!"))
            modMessage("Message \"${message}\" added at ${x}, ${y}, ${z}, with ${delay}ms delay, triggered up to $distance blocks away.")
            Config.save()
        }
    }

    literal("remove").runs { index: Int ->
        if (posMessageStrings.size < index) return@runs modMessage("Theres no message in position #$index")
        modMessage("Removed Positional Message #$index")
        posMessageStrings.removeAt(index-1)
        Config.save()
    }

    literal("clear").runs {
        modMessage("Cleared List")
        posMessageStrings.clear()
        Config.save()
    }

    literal("list").runs {
        val output = posMessageStrings.joinToString(separator = "\n") {
            "${posMessageStrings.indexOf(it) + 1}: ${it.x}, ${it.y}, ${it.z}, ${it.x2}, ${it.y2}, ${it.z2}, ${it.delay}, ${it.distance}, ${it.color.hex()}, \"${it.message}\""
        }
        modMessage(if (posMessageStrings.isEmpty()) "Positional Message list is empty!" else "Positonal Message list:\n$output")
    }

    literal("colors").runs {
        modMessage("""
            Available Colors:
            §1DARKBLUE, §2DARKGREEN, §3DARKAQUA, §4DARKRED, §5DARKPURPLE
            §6GOLD, §7GRAY, §8DARKGRAY, §9BLUE, §aGREEN, §bAQUA, §cRED
            §dLIGHTPURPLE, §eYELLOW, §fWHITE, §0BLACK
            §rAlso supports hex colors, like #FF0000 or #FF000000
        """.trimIndent())
    }
}

val hexRegex = Regex("^#?([A-Fa-f0-9]{6}|[A-Fa-f0-9]{8})$")
fun getColorFromString(color: String): Color? {
    hexRegex.find(color)?.let {
        val hex = it.value.replace("#", "")
        return if (hex.length == 6) Color(hex + "FF")
        else Color(hex)
    }
    return when (color.uppercase()) {
        "DARKBLUE" -> Colors.MINECRAFT_DARK_BLUE
        "DARKGREEN" -> Colors.MINECRAFT_DARK_GREEN
        "DARKAQUA" -> Colors.MINECRAFT_DARK_AQUA
        "DARKRED" -> Colors.MINECRAFT_DARK_RED
        "DARKPURPLE" -> Colors.MINECRAFT_DARK_PURPLE
        "GOLD" -> Colors.MINECRAFT_GOLD
        "GRAY" -> Colors.MINECRAFT_GRAY
        "DARKGRAY" -> Colors.MINECRAFT_DARK_GRAY
        "BLUE" -> Colors.MINECRAFT_BLUE
        "GREEN" -> Colors.MINECRAFT_GREEN
        "AQUA" -> Colors.MINECRAFT_AQUA
        "RED" -> Colors.MINECRAFT_RED
        "LIGHTPURPLE" -> Colors.MINECRAFT_LIGHT_PURPLE
        "YELLOW" -> Colors.MINECRAFT_YELLOW
        "WHITE" -> Colors.WHITE
        "BLACK" -> Colors.BLACK
        else -> null
    }
}
