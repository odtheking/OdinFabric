package me.odinmod.odin.commands

import com.github.stivais.commodore.Commodore
import com.github.stivais.commodore.utils.GreedyString
import me.odinmod.odin.config.Config
import me.odinmod.odin.features.impl.render.CustomHighlight
import me.odinmod.odin.utils.Color
import me.odinmod.odin.utils.Colors
import me.odinmod.odin.utils.modMessage

@OptIn(ExperimentalStdlibApi::class)
val highlightCommand = Commodore("highlight") {
    val colorRegex = Regex("^(.*?)(?:\\s+#?([0-9a-fA-F]{6}|[0-9a-fA-F]{8}))?$")

    literal("add").runs { input: GreedyString ->
        val inputString = input.string.trim()
        val matchResult = colorRegex.matchEntire(inputString) ?: return@runs modMessage("Invalid format. Use: /highlight add <mob name> [#hexcolor]")

        val (mobName, colorCode) = matchResult.destructured
        val mobNameTrimmed = mobName.trim()
        val lowercase = mobNameTrimmed.lowercase()

        if (mobNameTrimmed.isEmpty()) return@runs modMessage("Mob name cannot be empty.")

        if (CustomHighlight.highlightMap.any { it.key == lowercase }) return@runs modMessage("$mobNameTrimmed is already in the highlight list.")

        if (colorCode.isNotEmpty() && !Regex("^[0-9a-fA-F]{6}|[0-9a-fA-F]{8}$").matches(colorCode)) return@runs modMessage("Invalid color format. Use #RRGGBB or #RRGGBBAA.")

        val color = if (colorCode.isNotEmpty()) {
            try {
                Color(colorCode.padEnd(8, 'f'))
            } catch (e: Exception) {
                modMessage("Invalid color format. Use #RRGGBB or #RRGGBBAA.")
                Colors.TRANSPARENT
            }
        } else Colors.TRANSPARENT

        CustomHighlight.highlightMap[lowercase] = color
        modMessage("Added $mobNameTrimmed to the highlight list${if (colorCode.isNotEmpty()) " with color #$colorCode" else ""}.")
        Config.save()
    }

    literal("remove").runs { mob: GreedyString ->
        val lowercase = mob.string.lowercase()
        if (CustomHighlight.highlightMap.none { it.key == lowercase }) return@runs modMessage("$mob isn't in the highlight list.")

        modMessage("Removed $mob from the highlight list.")
        CustomHighlight.highlightMap.remove(lowercase)
        CustomHighlight.entities.clear()
        Config.save()
    }

    literal("clear").runs {
        modMessage("Highlight list cleared.")
        CustomHighlight.highlightMap.clear()
        CustomHighlight.entities.clear()
        Config.save()
    }

    literal("list").runs {
        if (CustomHighlight.highlightMap.isEmpty()) return@runs modMessage("Highlight list is empty")
        modMessage("Highlight list:\n${CustomHighlight.highlightMap.entries.joinToString("\n") {
            "${it.key} - ${if (it.value.isTransparent) "default" else it.value.rgba.toHexString()}"
        }}")
    }
}