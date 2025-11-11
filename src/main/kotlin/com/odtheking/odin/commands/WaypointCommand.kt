package com.odtheking.odin.commands

import com.github.stivais.commodore.Commodore
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.features.impl.render.Waypoints
import com.odtheking.odin.utils.*

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
            val (posX, posY, posZ)= mc.player?.blockPosition() ?: return@runs
            Waypoints.addTempWaypoint(name, x ?: posX, y ?: posY, z ?: posZ)
        }

        runs {
            val pos = mc.player?.blockPosition() ?: return@runs
            Waypoints.addTempWaypoint("", pos.x, pos.y, pos.z)
        }
    }
}