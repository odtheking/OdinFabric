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
import me.odinmod.odin.utils.skyblock.Supply
import meteordevelopment.orbit.EventHandler
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket
import net.minecraft.util.math.Vec3d

object NoPre : Module(
    name = "Pre-Spot Alert",
    description = "Alerts the party if a pre spot is missing."
) {
    private val showCratePriority by BooleanSetting("Show Crate Priority", false, desc = "Shows the crate priority.")
    private val advanced by BooleanSetting("Advanced Mode", false, desc = "Provides harder crate priority in certain situations.").withDependency { showCratePriority }

    private val partyRegex = Regex("^Party > (\\[[^]]*?])? ?(\\w{1,16}): No ?(Triangle|X|Equals|Slash|xCannon|Square|Shop)!$")
    private val preRegex = Regex("^\\[NPC] Elle: Head over to the main platform, I will join you when I get a bite!$")
    private val startRegex = Regex("^\\[NPC] Elle: Not again!$")

    private var preSpot = Supply.None
    var missing = Supply.None

    @EventHandler
    fun onChat(event: PacketEvent.Receive) = with (event.packet) {
        if (this !is GameMessageS2CPacket || overlay || !KuudraUtils.inKuudra) return
        val message = content.string.noControlCodes

        when {
            preRegex.matches(message) -> {
                val playerLocation = mc.player?.blockPos ?: return
                preSpot = when {
                    Supply.Triangle.pickUpSpot.isWithinDistance(playerLocation, 15.0) -> Supply.Triangle
                    Supply.X.pickUpSpot.isWithinDistance(playerLocation, 30.0) -> Supply.X
                    Supply.Equals.pickUpSpot.isWithinDistance(playerLocation, 15.0) -> Supply.Equals
                    Supply.Slash.pickUpSpot.isWithinDistance(playerLocation, 15.0) -> Supply.Slash
                    else -> Supply.None
                }
                modMessage(if (preSpot == Supply.None) "§cDidn't register your pre-spot because you didn't get there in time." else "Pre-spot: ${preSpot.name}")
            }

            startRegex.matches(message) -> {
                if (preSpot == Supply.None) return
                var second = false
                var pre = false
                var msg = ""
                KuudraUtils.giantZombies.forEach { supply ->
                    val supplyLoc = Vec3d(supply.x, 76.0, supply.z)
                    when {
                        preSpot.pickUpSpot.isWithinDistance(supplyLoc, 18.0) -> pre = true
                        preSpot == Supply.Triangle && Supply.Shop.pickUpSpot.isWithinDistance(supplyLoc, 18.0) -> second = true
                        preSpot == Supply.X && Supply.xCannon.pickUpSpot.isWithinDistance(supplyLoc, 16.0) -> second = true
                        preSpot == Supply.Slash && Supply.Square.pickUpSpot.isWithinDistance(supplyLoc, 20.0) -> second = true
                    }
                }
                if (second && pre) return
                if (!pre && preSpot != Supply.None) msg = "No ${preSpot.name}!"
                else if (!second) {
                    msg = when (preSpot) {
                        Supply.Triangle -> "No Shop!"
                        Supply.X -> "No xCannon!"
                        Supply.Slash -> "No Square!"
                        else -> return
                    }
                }
                if (msg.isNotEmpty()) sendCommand("pc $msg")
            }

            partyRegex.matches(message) -> {
                val match = partyRegex.find(message)?.groupValues ?: return
                missing = Supply.valueOf(match.lastOrNull() ?: return)
                if (!showCratePriority) return
                val cratePriority = cratePriority(missing).ifEmpty { return }
                alert(cratePriority)
                modMessage("Crate Priority: $cratePriority")
            }
        }
        Unit
    }

    @EventHandler
    fun onWorldLoad(event: WorldLoadEvent) {
        preSpot = Supply.None
        missing = Supply.None
    }

    private fun cratePriority(missing: Supply): String {
        return when (missing) {
            // Shop Missing
            Supply.Shop -> when (preSpot) {
                Supply.Triangle, Supply.X -> "Go X Cannon"
                Supply.Equals, Supply.Slash -> "Go Square, place on Shop"
                else -> ""
            }

            // Triangle Missing
            Supply.Triangle -> when (preSpot) {
                Supply.Triangle -> if (advanced) "Pull Square and X Cannon. Next: collect Shop" else "Pull Square. Next: collect Shop"
                Supply.X -> "Go X Cannon"
                Supply.Equals -> if (advanced) "Go Shop" else "Go X Cannon"
                Supply.Slash -> "Go Square, place on Triangle"
                else -> ""
            }

            // Equals Missing
            Supply.Equals -> when (preSpot) {
                Supply.Triangle -> if (advanced) "Go Shop" else "Go X Cannon"
                Supply.X -> "Go X Cannon"
                Supply.Equals -> if (advanced) "Pull Square and X Cannon. Next: collect Shop" else "Pull Square. Next: collect Shop"
                Supply.Slash -> "Go Square, place on Equals"
                else -> ""
            }

            // Slash Missing
            Supply.Slash -> when (preSpot) {
                Supply.Triangle -> "Go Square, place on Slash"
                Supply.X -> "Go X Cannon"
                Supply.Equals -> if (advanced) "Go Shop" else "Go X Cannon"
                Supply.Slash -> if (advanced) "Pull Square and X Cannon. Next: collect Shop" else "Pull Square. Next: collect Shop"
                else -> ""
            }

            // Square Missing
            Supply.Square -> when (preSpot) {
                Supply.Triangle, Supply.Equals -> "Go Shop"
                Supply.X, Supply.Slash -> "Go X Cannon"
                else -> ""
            }

            // X Cannon Missing
            Supply.xCannon -> when (preSpot) {
                Supply.Triangle, Supply.Equals -> "Go Shop"
                Supply.Slash, Supply.X -> "Go Square, place on X Cannon"
                else -> ""
            }

            // X Missing
            Supply.X -> when (preSpot) {
                Supply.Triangle -> "Go X Cannon"
                Supply.X -> if (advanced) "Pull Square and X Cannon. Next: collect Shop" else "Pull Square. Next: collect Shop"
                Supply.Equals -> if (advanced) "Go Shop" else "Go X Cannon"
                Supply.Slash -> "Go Square, place on X"
                else -> ""
            }

            else -> ""
        }
    }
}