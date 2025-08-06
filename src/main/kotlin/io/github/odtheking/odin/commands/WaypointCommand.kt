package io.github.odtheking.odin.commands

import com.github.stivais.commodore.Commodore
import io.github.odtheking.odin.OdinMod.mc
import io.github.odtheking.odin.features.impl.render.Waypoints
import io.github.odtheking.odin.utils.*

val waypointCommand = Commodore("odwaypoint") {

    literal("help").runs {
        modMessage(
            """
                 Waypoint command help:
                 §3- /waypoint » §8Main command.
                 §3- /waypoint share » §8Used to send your location in party chat.
                 §3- /waypoint share <x, y, z> » §8Used to send a specific location in party chat.
                 §3- /waypoint addtemp » §8Used to add temporary waypoints.
                 §3- /waypoint addtemp <x, y, z> » §8Used to add temporary waypoints.
                 §3- /waypoint addtemp <name, x?, y?, z?> » §8Used to add temporary waypoints.
            """.trimIndent()
        )
    }

    literal("share") {
        runs {
            sendChatMessage(getPositionString())
        }
        runs { x: Int, y: Int, z: Int ->
            sendChatMessage("x: $x, y: $y, z: $z")
        }
    }

    literal("addtemp") {
        runs { x: Int, y: Int, z: Int ->
            Waypoints.addTempWaypoint("Waypoint", x, y, z)
        }

        runs { name: String, x: Int?, y: Int?, z: Int? ->
            val (posX, posY, posZ) = mc.player?.blockPos ?: return@runs
            Waypoints.addTempWaypoint(name, x ?: posX, y ?: posY, z ?: posZ)
        }

        runs {
            val (posX, posY, posZ) = mc.player?.blockPos ?: return@runs
            Waypoints.addTempWaypoint("", posX, posY, posZ)
        }
    }
}