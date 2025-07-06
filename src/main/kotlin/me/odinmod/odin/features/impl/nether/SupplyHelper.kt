package me.odinmod.odin.features.impl.nether

import me.odinmod.odin.clickgui.settings.Setting.Companion.withDependency
import me.odinmod.odin.clickgui.settings.impl.BooleanSetting
import me.odinmod.odin.clickgui.settings.impl.ColorSetting
import me.odinmod.odin.events.PacketEvent
import me.odinmod.odin.events.RenderEvent
import me.odinmod.odin.features.Module
import me.odinmod.odin.utils.*
import me.odinmod.odin.utils.render.drawCustomBeacon
import me.odinmod.odin.utils.render.drawText
import me.odinmod.odin.utils.skyblock.KuudraUtils
import me.odinmod.odin.utils.skyblock.Supply
import meteordevelopment.orbit.EventHandler
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket
import net.minecraft.text.Text
import net.minecraft.util.math.Vec3d
import kotlin.math.cos
import kotlin.math.sin

object SupplyHelper : Module(
    name = "Supply Helper",
    description = "Provides visual aid for supply drops in Kuudra."
) {
    private val suppliesWaypoints by BooleanSetting("Supplies Waypoints", true, desc = "Renders the supply waypoints.")
    private val supplyWaypointColor by ColorSetting("Supply Waypoint Color", Colors.MINECRAFT_YELLOW, true, desc = "Color of the supply waypoints.").withDependency { suppliesWaypoints }
    private val supplyDropWaypoints by BooleanSetting("Supply Drop Waypoints", true, desc = "Renders the supply drop waypoints.")
    private val sendSupplyTime by BooleanSetting("Send Supply Time", true, desc = "Sends a message when a supply is collected.")
    private val renderArea by BooleanSetting("Render Area", true, desc = "Renders the area where supplies can be collected.").withDependency { supplyDropWaypoints }

    private val supplyPickUpRegex = Regex("(?:\\[[^]]*])? ?(\\w{1,16}) recovered one of Elle's supplies! \\((\\d)/(\\d)\\)") // https://regex101.com/r/xsDImP/1
    private val runStartRegex = Regex("^\\[NPC] Elle: Okay adventurers, I will go and fish up Kuudra!$")
    private var startRun = 0L

    @EventHandler
    fun onChat(event: PacketEvent.Receive) = with (event.packet) {
        if (this !is GameMessageS2CPacket || overlay || !KuudraUtils.inKuudra) return
        val message = content.string.noControlCodes

        when {
            runStartRegex.matches(message) -> startRun = System.currentTimeMillis()

            supplyPickUpRegex.matches(message) -> {
                if (!sendSupplyTime || KuudraUtils.phase != 1) return
                val (name, current, total) = supplyPickUpRegex.find(message)?.destructured ?: return
                modMessage("§6$name §a§lrecovered a supply in ${formatTime(System.currentTimeMillis() - startRun)}! §r§8($current/$total)", "")
                event.cancel()
            }
        }
    }

    @EventHandler
    fun onWorldRender(event: RenderEvent.Last) {
        if (!KuudraUtils.inKuudra || KuudraUtils.phase != 1) return

        if (supplyDropWaypoints) {
            Supply.entries.forEach { type ->
                if (type == Supply.Square || !type.isActive) return@forEach
                event.context.drawCustomBeacon("§ePlace Here!", type.dropOffSpot, if (NoPre.missing == type) Colors.MINECRAFT_GREEN else Colors.MINECRAFT_RED, increase = false)
            }
        }

        if (suppliesWaypoints) {
            KuudraUtils.giantZombies.forEach {
                event.context.drawCustomBeacon("Pick Up!", Vec3d(it.x + (3.7 * cos((it.yaw + 130) * (Math.PI / 180))), 73.0, it.z + (3.7 * sin((it.yaw + 130) * (Math.PI / 180)))).toBlockPos(), supplyWaypointColor, increase = false)
            }
        }

        if (renderArea) {
            Supply.entries.forEach { type ->
                event.context.drawText(Text.literal("§e${type.name}").asOrderedText(), type.pickUpSpot.toCenterPos(), 2f, true)
            }
        }
    }
}