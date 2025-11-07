package com.odtheking.odin.features.impl.floor7

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.StringSetting
import com.odtheking.odin.events.PacketEvent
import com.odtheking.odin.events.TerminalEvent
import com.odtheking.odin.events.WorldLoadEvent
import com.odtheking.odin.features.Module
import com.odtheking.odin.features.impl.floor7.terminalhandler.TerminalTypes
import com.odtheking.odin.features.impl.floor7.termsim.TermSimGUI
import com.odtheking.odin.features.impl.render.ClickGUIModule
import com.odtheking.odin.utils.equalsOneOf
import com.odtheking.odin.utils.network.WebUtils.gson
import com.odtheking.odin.utils.network.webSocket
import com.odtheking.odin.utils.render.drawString
import com.odtheking.odin.utils.render.getStringWidth
import com.odtheking.odin.utils.sendCommand
import com.odtheking.odin.utils.skyblock.LocationUtils
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.M7Phases
import meteordevelopment.orbit.EventHandler
import net.minecraft.client.gui.DrawContext
import net.minecraft.item.Items
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket
import java.util.concurrent.ConcurrentHashMap

object MelodyMessage : Module(
    name = "Melody Message",
    description = "Helpful messages for the melody terminal in floor 7."
) {
    private val sendMelodyMessage by BooleanSetting("Send Melody Message", true, desc = "Sends a message when the melody terminal opens.")
    private val melodyMessage by StringSetting("Melody Message", "Melody Terminal start!", 128, desc = "Message sent when the melody terminal opens.").withDependency { sendMelodyMessage }
    private val melodyProgress by BooleanSetting("Melody Progress", false, desc = "Tells the party about melody terminal progress.")
    private val melodySendCoords by BooleanSetting("Melody Send Coords", false, desc = "Sends the coordinates of the melody terminal.").withDependency { melodyProgress }

    private val broadcast by BooleanSetting("Broadcast Progress", true, desc = "Broadcasts melody progress to all other odin users in the party.")
    private val melodyGui by HUD("Progress GUI", "Shows a gui with the progress of broadcasting odin users in melody.", true) {
        if (it) {
            drawMelody(MelodyData(3, 1, 2), 0)
        }

        if (!broadcast || !melodyWebSocket.connected) return@HUD 0 to 0
        melodies.entries.forEachIndexed { i, (name, data) ->
            if (!showOwn && name == mc.session.username) return@forEachIndexed
            drawMelody(data, i)
        }
        45 to 25
    }.withDependency { broadcast }

    private val showOwn: Boolean by BooleanSetting("Show Own", true, desc = "Shows your own progress in the melody GUI.").withDependency { broadcast && melodyGui.enabled }

    val melodyWebSocket = webSocket {
        onMessage {
            val (user, type, slot) = try { gson.fromJson(it, UpdateMessage::class.java) } catch (_: Exception) { return@onMessage }
            val entry = melodies.getOrPut(user) { MelodyData(null, null, null) }
            when (type) {
                0 -> melodies.remove(user)
                1 -> entry.clay = slot
                2 -> entry.purple = slot
                5 -> entry.pane = slot
            }
        }
    }

    private val melodies = ConcurrentHashMap<String, MelodyData>()
    private val lastSent = MelodyData(null, null, null)

    @EventHandler
    fun onTermLoad(event: TerminalEvent.Opened) {
        if (DungeonUtils.getF7Phase() != M7Phases.P3 || event.terminal.type != TerminalTypes.MELODY || mc.currentScreen is TermSimGUI) return
        if (sendMelodyMessage) sendCommand("pc $melodyMessage")
        if (melodySendCoords) sendCommand("od sendcoords")
    }

    private val coreRegex = Regex("^The Core entrance is opening!$")
    private val p3StartRegex = Regex("^\\[BOSS] Goldor: Who dares trespass into myh domain\\?$")

    @EventHandler
    fun onPacketReceive(event: PacketEvent.Receive) {
        if (!broadcast && !melodyProgress) return
        when (val packet = event.packet) {
            is GameMessageS2CPacket if (broadcast) -> onChatMessage(packet)
            is ScreenHandlerSlotUpdateS2CPacket -> onSlotUpdate(packet)
            else -> {}
        }
    }

    @EventHandler
    fun onWorldLoad(event: WorldLoadEvent) {
        melodyWebSocket.shutdown()
        melodies.clear()
    }

    @EventHandler
    fun onTermClose(event: TerminalEvent.Closed) {
        if (event.terminal.type != TerminalTypes.MELODY) return
        melodyWebSocket.send(update(0, 0))
    }

    private fun onChatMessage(packet: GameMessageS2CPacket) {
        val text = packet.content?.string ?: return

        if (coreRegex.matches(text)) {
            melodyWebSocket.shutdown()
            melodies.clear()
        }

        if (p3StartRegex.matches(text)) {
            melodyWebSocket.connect("${ClickGUIModule.webSocketUrl}${LocationUtils.lobbyId}")
        }
    }

    private fun onSlotUpdate(packet: ScreenHandlerSlotUpdateS2CPacket) {
        val term = TerminalSolver.currentTerm ?: return
        if (DungeonUtils.getF7Phase() != M7Phases.P3 || term.type != TerminalTypes.MELODY || mc.currentScreen is TermSimGUI) return

        val item = packet.stack?.item ?: return
        if (item == Items.LIME_TERRACOTTA) {
            val position = packet.slot / 9
            if (lastSent.clay == position) return
            if (broadcast) melodyWebSocket.send(update(1, position))
            if (melodyProgress) clayProgress[position]?.let {  }
            lastSent.clay = position
            return
        }
        if (!broadcast || !item.equalsOneOf(Items.MAGENTA_STAINED_GLASS_PANE, Items.LIME_STAINED_GLASS_PANE)) return
        val index = mapToRange(packet.slot) ?: return
        val meta = when (item) {
            Items.MAGENTA_STAINED_GLASS_PANE -> {
                if (lastSent.purple == index) return
                lastSent.purple = index
                2
            }
            Items.LIME_STAINED_GLASS_PANE -> {
                if (lastSent.pane == index) return
                lastSent.pane = index
                5
            }
            else -> return
        }

        melodyWebSocket.send(update(meta, index))
    }

    private fun update(type: Int, slot: Int): String = gson.toJson(UpdateMessage(mc.session.username, type, slot))

    private val clayProgress = hashMapOf(2 to "Melody 25%", 3 to "Melody 50%", 4 to "Melody 75%")
    private val ranges = listOf(1..5, 10..14, 19..23, 28..32, 37..41)

    private fun mapToRange(value: Int): Int? {
        for (r in ranges) {
            if (value in r) return (value - r.first) % 5
        }
        return null
    }

    private val width by lazy { getStringWidth("§d■") }

    private fun DrawContext.drawMelody(data: MelodyData, index: Int) {
        val y = width* 2 * index

        repeat(5) {
            if (data.purple == it) drawString("§d■", width * it, y)
            val color = if (data.pane == it) "§a" else "§f"
            drawString("${color}■", width * it, y + width)
        }
        data.clay?.let { drawString(it.toString(), 40, y + width / 2) }
    }

    private data class UpdateMessage(val user: String, val type: Int, val slot: Int)
    private data class MelodyData(var purple: Int?, var pane: Int?, var clay: Int?)
}