package me.odinmod.odin.features.impl.nether

import me.odinmod.odin.clickgui.settings.Setting.Companion.withDependency
import me.odinmod.odin.clickgui.settings.impl.BooleanSetting
import me.odinmod.odin.events.PacketEvent
import me.odinmod.odin.events.WorldLoadEvent
import me.odinmod.odin.features.Module
import me.odinmod.odin.utils.alert
import me.odinmod.odin.utils.modMessage
import me.odinmod.odin.utils.noControlCodes
import me.odinmod.odin.utils.sendCommand
import me.odinmod.odin.utils.skyblock.KuudraUtils
import me.odinmod.odin.utils.skyblock.SupplyPickUpSpot
import meteordevelopment.orbit.EventHandler
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket
import net.minecraft.util.math.Vec3d

object NoPre : Module(
    name = "Pre-Spot Alert",
    description = "Alerts the party if a pre spot is missing."
) {
    private val showCratePriority by BooleanSetting("Show Crate Priority", false, desc = "Shows the crate priority alert.")
    private val advanced by BooleanSetting("Advanced Mode", false, desc = "Enables pro mode for the crate priority alert.").withDependency { showCratePriority }

    private val partyRegex = Regex("^Party > (\\[[^]]*?])? ?(\\w{1,16}): No ?(Triangle|X|Equals|Slash|xCannon|Square|Shop)!$")
    private val preRegex = Regex("^\\[NPC] Elle: Head over to the main platform, I will join you when I get a bite!$")
    private val startRegex = Regex("^\\[NPC] Elle: Not again!$")

    private var preSpot = SupplyPickUpSpot.None
    var missing = SupplyPickUpSpot.None

    @EventHandler
    fun onChat(event: PacketEvent.Receive) = with (event.packet) {
        if (!KuudraUtils.inKuudra || this !is GameMessageS2CPacket || overlay) return
        val message = content.string.noControlCodes

        when {
            preRegex.matches(message) -> {
                val playerLocation = mc.player?.blockPos ?: return
                preSpot = when {
                    SupplyPickUpSpot.Triangle.location.isWithinDistance(playerLocation, 15.0) -> SupplyPickUpSpot.Triangle
                    SupplyPickUpSpot.X.location.isWithinDistance(playerLocation, 30.0) -> SupplyPickUpSpot.X
                    SupplyPickUpSpot.Equals.location.isWithinDistance(playerLocation, 15.0) -> SupplyPickUpSpot.Equals
                    SupplyPickUpSpot.Slash.location.isWithinDistance(playerLocation, 15.0) -> SupplyPickUpSpot.Slash
                    else -> SupplyPickUpSpot.None
                }
                modMessage(if (preSpot == SupplyPickUpSpot.None) "§cDidn't register your pre-spot because you didn't get there in time." else "Pre-spot: ${preSpot.name}")
            }

            startRegex.matches(message) -> {
                if (preSpot == SupplyPickUpSpot.None) return
                var second = false
                var pre = false
                var msg = ""
                KuudraUtils.giantZombies.forEach { supply ->
                    val supplyLoc = Vec3d(supply.x, 76.0, supply.z)
                    when {
                        preSpot.location.isWithinDistance(supplyLoc, 18.0) -> pre = true
                        preSpot == SupplyPickUpSpot.Triangle && SupplyPickUpSpot.Shop.location.isWithinDistance(supplyLoc, 18.0) -> second = true
                        preSpot == SupplyPickUpSpot.X && SupplyPickUpSpot.xCannon.location.isWithinDistance(supplyLoc, 16.0) -> second = true
                        preSpot == SupplyPickUpSpot.Slash && SupplyPickUpSpot.Square.location.isWithinDistance(supplyLoc, 20.0) -> second = true
                    }
                }
                if (second && pre) return
                if (!pre && preSpot != SupplyPickUpSpot.None) msg = "No ${preSpot.name}!"
                else if (!second) {
                    msg = when (preSpot) {
                        SupplyPickUpSpot.Triangle -> "No Shop!"
                        SupplyPickUpSpot.X -> "No xCannon!"
                        SupplyPickUpSpot.Slash -> "No Square!"
                        else -> return
                    }
                }
                if (msg.isNotEmpty()) sendCommand("/pc $msg")
            }

            partyRegex.matches(message) -> {
                val match = partyRegex.find(message)?.groupValues ?: return
                missing = SupplyPickUpSpot.valueOf(match.lastOrNull() ?: return)
                if (!showCratePriority) return
                val cratePriority = cratePriority(missing).ifEmpty { return }
                alert(cratePriority)
                modMessage("Crate Priority: $cratePriority")
            }
        }
        Unit
    }

    fun onWorldLoad(event: WorldLoadEvent) {
        preSpot = SupplyPickUpSpot.None
        missing = SupplyPickUpSpot.None
    }

    private fun cratePriority(missing: SupplyPickUpSpot): String {
        return when (missing) {
            // Shop Missing
            SupplyPickUpSpot.Shop -> when (preSpot) {
                SupplyPickUpSpot.Triangle, SupplyPickUpSpot.X -> "Go X Cannon"
                SupplyPickUpSpot.Equals, SupplyPickUpSpot.Slash -> "Go Square, place on Shop"
                else -> ""
            }

            // Triangle Missing
            SupplyPickUpSpot.Triangle -> when (preSpot) {
                SupplyPickUpSpot.Triangle -> if (advanced) "Pull Square and X Cannon. Next: collect Shop" else "Pull Square. Next: collect Shop"
                SupplyPickUpSpot.X -> "Go X Cannon"
                SupplyPickUpSpot.Equals -> if (advanced) "Go Shop" else "Go X Cannon"
                SupplyPickUpSpot.Slash -> "Go Square, place on Triangle"
                else -> ""
            }

            // Equals Missing
            SupplyPickUpSpot.Equals -> when (preSpot) {
                SupplyPickUpSpot.Triangle -> if (advanced) "Go Shop" else "Go X Cannon"
                SupplyPickUpSpot.X -> "Go X Cannon"
                SupplyPickUpSpot.Equals -> if (advanced) "Pull Square and X Cannon. Next: collect Shop" else "Pull Square. Next: collect Shop"
                SupplyPickUpSpot.Slash -> "Go Square, place on Equals"
                else -> ""
            }

            // Slash Missing
            SupplyPickUpSpot.Slash -> when (preSpot) {
                SupplyPickUpSpot.Triangle -> "Go Square, place on Slash"
                SupplyPickUpSpot.X -> "Go X Cannon"
                SupplyPickUpSpot.Equals -> if (advanced) "Go Shop" else "Go X Cannon"
                SupplyPickUpSpot.Slash -> if (advanced) "Pull Square and X Cannon. Next: collect Shop" else "Pull Square. Next: collect Shop"
                else -> ""
            }

            // Square Missing
            SupplyPickUpSpot.Square -> when (preSpot) {
                SupplyPickUpSpot.Triangle, SupplyPickUpSpot.Equals -> "Go Shop"
                SupplyPickUpSpot.X, SupplyPickUpSpot.Slash -> "Go X Cannon"
                else -> ""
            }

            // X Cannon Missing
            SupplyPickUpSpot.xCannon -> when (preSpot) {
                SupplyPickUpSpot.Triangle, SupplyPickUpSpot.Equals -> "Go Shop"
                SupplyPickUpSpot.Slash, SupplyPickUpSpot.X -> "Go Square, place on X Cannon"
                else -> ""
            }

            // X Missing
            SupplyPickUpSpot.X -> when (preSpot) {
                SupplyPickUpSpot.Triangle -> "Go X Cannon"
                SupplyPickUpSpot.X -> if (advanced) "Pull Square and X Cannon. Next: collect Shop" else "Pull Square. Next: collect Shop"
                SupplyPickUpSpot.Equals -> if (advanced) "Go Shop" else "Go X Cannon"
                SupplyPickUpSpot.Slash -> "Go Square, place on X"
                else -> ""
            }

            else -> ""
        }
    }
}