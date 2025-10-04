package com.odtheking.odin.features.impl.floor7

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.ColorSetting
import com.odtheking.odin.events.PacketEvent
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.WorldLoadEvent
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Color.Companion.withAlpha
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.addVec
import com.odtheking.odin.utils.containsOneOf
import com.odtheking.odin.utils.handlers.TickTask
import com.odtheking.odin.utils.render.*
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.M7Phases
import meteordevelopment.orbit.EventHandler
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket
import net.minecraft.text.Text
import net.minecraft.util.math.Box

object InactiveWaypoints : Module(
    name = "Inactive Waypoints",
    description = "Shows inactive terminals, devices and levers."
) {
    private val showTerminals by BooleanSetting("Show Terminals", true, desc = "Shows inactive terminals.")
    private val showDevices by BooleanSetting("Show Devices", true, desc = "Shows inactive devices.")
    private val showLevers by BooleanSetting("Show Levers", true, desc = "Shows inactive levers.")
    private val renderText by BooleanSetting("Render Text", true, desc = "Renders the name of the inactive waypoint.")
    private val renderBeacon by BooleanSetting("Render Beacon", true, desc = "Renders a beacon beam on the inactive waypoint.")
    private val renderBox by BooleanSetting("Render Box", true, desc = "Renders a box around the inactive waypoint.")
    private val hideDefault by BooleanSetting("Hide Default", true, desc = "Hide the Hypixel names of Inactive Terminals.")
    private val color by ColorSetting("Color", Colors.MINECRAFT_YELLOW.withAlpha(.4f), true, desc = "The color of the box.")
    private val depthCheck by BooleanSetting("Depth check", false, desc = "Boxes show through walls.")

    private val hud by HUD("Term Info", "Shows information about the terminals, levers and devices in the dungeon.") {
        if (!(DungeonUtils.inBoss && shouldRender) && !it) return@HUD 0 to 0
        val y = 1
        val width = drawStringWidth("§6Levers ${if (levers == 2) "§a" else "§c"}${levers}§8/§a2", 1, y, Colors.WHITE)
        drawString("§6Terms ${if ((section == 2 && terminals == 5) || (section != 2 && terminals == 4)) "§a" else "§c"}${terminals}§8/§a${if (section == 2) 5 else 4}", 1, y + 10, Colors.WHITE.rgba)
        drawString("§6Device ${if (device) "§a✔" else "§c✘"}", 1, y + 20, Colors.WHITE.rgba)
        drawString("§6Gate ${if (gate) "§a✔" else "§c✘"}", 1, y + 30, Colors.WHITE.rgba)

        width + 1 to 40
    }

    private var inactiveList = setOf<ArmorStandEntity>()
    private var firstInSection = false
    private var shouldRender = false
    private var isComplete = false
    private var lastCompleted = 0
    private var device = false
    private var terminals = 0
    private var gate = false
    private var section = 1
    private var levers = 0

    private val completedRegex = Regex("^(.{1,16}) (activated|completed) a (terminal|lever|device)! \\((\\d)/(\\d)\\)$")
    private val goldorRegex = Regex("^\\[BOSS] Goldor: Who dares trespass into my domain\\?$")
    private val coreOpeningRegex = Regex("^The Core entrance is opening!$")
    private val gateRegex = Regex("^The gate has been destroyed!$")

    init {
        TickTask(10) {
            if (DungeonUtils.getF7Phase() != M7Phases.P3) return@TickTask
            inactiveList = mc.world?.entities?.filterIsInstance<ArmorStandEntity>()?.filter {
                it.name.string.containsOneOf("Inactive", "Not Activated", "CLICK HERE", ignoreCase = true)
            }?.toSet().orEmpty()
        }
    }

    @EventHandler
    fun onPacketReceive(event: PacketEvent.Receive) = with (event.packet) {
        if (this !is GameMessageS2CPacket || overlay || !DungeonUtils.inBoss) return
        val text = content?.string ?: return

        when {
            completedRegex.matches(text) -> {
                val it = completedRegex.find(text) ?: return
                val completed = (it.groupValues[4].toIntOrNull() ?: 0).apply { if (this == 1) firstInSection = true }

                if (completed == (it.groupValues[5].toIntOrNull() ?: 0)) {
                    if (gate) newSection() else isComplete = true
                    return
                }

                when (it.groupValues[3]) {
                    "lever" -> levers++

                    "terminal" -> terminals++

                    "device" -> if (!firstInSection || lastCompleted != completed) device = true
                }
                lastCompleted = completed
            }

            gateRegex.matches(text) -> {
                gate = true
                if (isComplete) newSection()
            }

            goldorRegex.matches(text) -> {
                shouldRender = true
                resetState()
                section = 1
            }

            coreOpeningRegex.matches(text) -> {
                shouldRender = false
                resetState()
            }
        }
    }

    @EventHandler
    fun onWorldLoad(event: WorldLoadEvent) {
        shouldRender = false
        resetState()
    }

    private fun resetState() {
        inactiveList = emptySet()
        firstInSection = false
        lastCompleted = 0
        isComplete = false
        device = false
        terminals = 0
        gate = false
        section = 1
        levers = 0
    }

    private fun newSection() {
        firstInSection = false
        isComplete = false
        device = false
        terminals = 0
        gate = false
        levers = 0
        section++
    }

    @EventHandler
    fun onRenderWorld(event: RenderEvent.Last) {
        if (inactiveList.isEmpty() || DungeonUtils.getF7Phase() != M7Phases.P3) return
        inactiveList.forEach {
            val name = it.name.string
            if ((name == "Inactive Terminal" && showTerminals) || (name == "Inactive" && showDevices) || (name == "Not Activated" && showLevers)) {
                val customName = Text.of(if (name == "Inactive Terminal") "Terminal" else if (name == "Inactive") "Device" else "Lever").asOrderedText()
                if (renderBox)
                    event.context.drawWireFrameBox(Box.from(it.pos.addVec(-0.5, z = -0.5)), color, depth = depthCheck)
                if (renderText)
                    event.context.drawText(customName, it.pos.addVec(y = 2.0), 1.5f, true)
                if (renderBeacon)
                    event.context.drawBeaconBeam(it.blockPos, color)
            }
            it.isCustomNameVisible = !hideDefault
        }
    }
}