package com.odtheking.odin.features.impl.render

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.DropdownSetting
import com.odtheking.odin.clickgui.settings.impl.KeybindSetting
import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.events.PacketEvent
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.WorldLoadEvent
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.render.drawCustomBeacon
import com.odtheking.odin.utils.sendCommand
import meteordevelopment.orbit.EventHandler
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import org.lwjgl.glfw.GLFW
import kotlin.math.abs

object Waypoints : Module(
    name = "Waypoints",
    description = "Allows to render waypoints based on coordinates in chat."
) {
    private val fromParty by BooleanSetting("From Party Chat", true, desc = "Adds waypoints from party chat.")
    private val fromAll by BooleanSetting("From All Chat", false, desc = "Adds waypoints from all chat.")

    private val pingLocationDropDown by DropdownSetting("Ping Location Dropdown", false)
    private val pingLocationToggle by BooleanSetting(
        "Ping Location",
        false,
        desc = "Adds a waypoint at the location you are looking at."
    ).withDependency { pingLocationDropDown }
    private val pingLocation by KeybindSetting(
        "Ping Location",
        GLFW.GLFW_KEY_UNKNOWN,
        desc = "Sends the location you are looking at as coords in chat for waypoints."
    ).onPress {
        if (!pingLocationToggle) return@onPress
        Etherwarp.getEtherPos(mc.player?.pos, pingDistance).pos?.let { pos ->
            addTempWaypoint("§fWaypoint", pos.x, pos.y, pos.z, pingWaypointTime)
            if (sendPingedLocation) sendCommand("odinwaypoint share ${pos.x} ${pos.y} ${pos.z}")
        }
    }.withDependency { pingLocationToggle && pingLocationDropDown }
    private val sendPingedLocation by BooleanSetting(
        "Send Pinged Location",
        false,
        desc = "Sends the location you are looking at as coords in chat for waypoints."
    ).withDependency { pingLocationToggle && pingLocationDropDown }
    private val pingWaypointTime by NumberSetting(
        "Ping Waypoint Time",
        15000L,
        0L,
        128000L,
        1000L,
        unit = "ms",
        desc = "Time to wait before sending the waypoint command."
    ).withDependency { pingLocationToggle && pingLocationDropDown }
    private val pingDistance by NumberSetting(
        "Ping Distance",
        64.0,
        1,
        128,
        1,
        desc = "Distance to ping location."
    ).withDependency { pingLocationToggle && pingLocationDropDown }

    private val partyRegex =
        Regex("^Party > (?:\\[[^]]*?])? ?(\\w{1,16})(?: [ቾ⚒])?: x: (-?\\d+), y: (-?\\d+), z: (-?\\d+).*") // https://regex101.com/r/8K26A1/1
    private val allRegex =
        Regex("^(?!Party >).*\\s(?:\\[[^]]*?])? ?(\\w{1,16})(?: [ቾ⚒])?: x: (-?\\d+),? y: (-?\\d+),? z: (-?\\d+).*") // https://regex101.com/r/A3aoyL/1

    private val temporaryWaypoints = mutableListOf<Waypoint>()

    @EventHandler
    fun onPacketReceive(event: PacketEvent.Receive) = with(event.packet) {
        if (this !is GameMessageS2CPacket || overlay) return

        val (name, x, y, z) = when {
            fromParty && partyRegex.matches(content.string) -> partyRegex.find(content.string)?.destructured
            fromAll && allRegex.matches(content.string) -> allRegex.find(content.string)?.destructured
            else -> null
        } ?: return

        addTempWaypoint("§6$name", x.toIntOrNull() ?: return, y.toIntOrNull() ?: return, z.toIntOrNull() ?: return)
    }

    @EventHandler
    fun onRenderWorld(event: RenderEvent.Last) {
        temporaryWaypoints.removeAll {
            event.context.drawCustomBeacon(Text.of(it.name).asOrderedText(), it.blockPos, it.color)
            System.currentTimeMillis() > it.timeAdded + it.duration
        }
    }

    @EventHandler
    fun onWorldLoad(event: WorldLoadEvent) {
        temporaryWaypoints.clear()
    }

    fun addTempWaypoint(name: String = "Waypoint", x: Int, y: Int, z: Int, duration: Long = 60_000) {
        if (!Waypoints.enabled) return
        if (listOf(x, y, z).any { abs(it) > 5000 }) return modMessage("§cWaypoint out of bounds.")
        if (temporaryWaypoints.any { it.blockPos.x == x && it.blockPos.y == y && it.blockPos.z == z }) return modMessage(
            "§cWaypoint already exists at $x, $y, $z."
        )
        modMessage("§aAdded temporary waypoint at §6$x§r, §3$y§r, §d$z§r.")
        temporaryWaypoints.add(Waypoint(name, BlockPos(x, y, z), colors.random(), duration))
    }

    private val colors = listOf(
        Colors.MINECRAFT_GOLD,
        Colors.MINECRAFT_GREEN,
        Colors.MINECRAFT_LIGHT_PURPLE,
        Colors.MINECRAFT_DARK_AQUA,
        Colors.MINECRAFT_YELLOW,
        Colors.MINECRAFT_DARK_RED,
        Colors.WHITE,
        Colors.MINECRAFT_DARK_PURPLE,
        Colors.MINECRAFT_YELLOW,
        Colors.MINECRAFT_RED,
        Colors.MINECRAFT_LIGHT_PURPLE,
        Colors.MINECRAFT_DARK_GREEN,
        Colors.MINECRAFT_BLUE
    )

    data class Waypoint(
        val name: String,
        val blockPos: BlockPos,
        val color: Color,
        val duration: Long,
        val timeAdded: Long = System.currentTimeMillis()
    )

}