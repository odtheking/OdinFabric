package com.odtheking.odin.commands

import com.github.stivais.commodore.Commodore
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.OdinMod.scope
import com.odtheking.odin.config.DungeonWaypointConfig
import com.odtheking.odin.config.DungeonWaypointConfig.encodeWaypoints
import com.odtheking.odin.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints
import com.odtheking.odin.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints.setWaypoints
import com.odtheking.odin.features.impl.dungeon.dungeonwaypoints.SecretWaypoints
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.setClipboardContent
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import kotlinx.coroutines.launch

val dungeonWaypointsCommand = Commodore("dwp", "dungeonwaypoints") {
    runs {
        DungeonWaypoints.onKeybind()
    }

    literal("fill").runs {
        DungeonWaypoints.filled = !DungeonWaypoints.filled
        modMessage("Fill status changed to: ${DungeonWaypoints.filled}")
    }

    literal("size").runs { size: Double ->
        if (size !in 0.1..1.0) return@runs modMessage("§cSize must be between 0.1 and 1.0!")
        DungeonWaypoints.size = size
        modMessage("Size changed to: ${DungeonWaypoints.size}")
    }

    literal("resetsecrets").runs {
        SecretWaypoints.resetSecrets()
        modMessage("§aSecrets have been reset!")
    }

    literal("type").runs { type: String ->
        DungeonWaypoints.WaypointType.getByName(type)?.let {
            DungeonWaypoints.waypointType = it.ordinal
            modMessage("Waypoint type changed to: ${it.displayName}")
        } ?: modMessage("§cInvalid waypoint type!")
    }

    literal("useblocksize").runs {
        DungeonWaypoints.useBlockSize = !DungeonWaypoints.useBlockSize
        modMessage("Use block size status changed to: ${DungeonWaypoints.useBlockSize}")
    }

    literal("depth").runs {
        DungeonWaypoints.depthCheck = !DungeonWaypoints.depthCheck
        modMessage("Next waypoint will be added with depth check: ${DungeonWaypoints.depthCheck}")
    }

    literal("color").runs { hex: String ->
        if (!hex.matches(Regex("[0-9A-Fa-f]{8}"))) return@runs modMessage("Color hex not properly formatted! Use format RRGGBBAA")
        DungeonWaypoints.color = Color(hex)
        modMessage("Color changed to: $hex")
    }

    literal("export").runs {
        scope.launch {
            setClipboardContent(encodeWaypoints() ?: return@launch modMessage("Failed to write waypoint config to clipboard."))
            modMessage("Wrote waypoint config to clipboard.")
        }
    }

    literal("import").runs {
        scope.launch {
            val base64Data = mc.keyboardHandler?.clipboard?.trimEnd { it == '\n' } ?: return@launch modMessage("§cFailed to read a string from clipboard. §fDid you copy it correctly?")
            if (base64Data.startsWith("{")) return@launch modMessage("§eIt looks like you copied json data instead of base64. §f§lEnsure you copied the correct text!")
            val waypoints = DungeonWaypointConfig.decodeWaypoints(base64Data) ?: return@launch
            DungeonWaypointConfig.waypoints = waypoints
            DungeonWaypointConfig.saveConfig()

            DungeonUtils.currentRoom?.setWaypoints()
            modMessage("Imported waypoints from clipboard!")
        }
    }
}