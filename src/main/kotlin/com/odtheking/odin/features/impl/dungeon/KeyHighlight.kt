package com.odtheking.odin.features.impl.dungeon

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.ColorSetting
import com.odtheking.odin.events.PacketEvent
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.WorldLoadEvent
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Color.Companion.withAlpha
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.alert
import com.odtheking.odin.utils.render.drawWireFrameBox
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import meteordevelopment.orbit.EventHandler
import net.minecraft.entity.Entity
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket

object KeyHighlight : Module(
    name = "Key Highlight",
    description = "Highlights wither and blood keys in dungeons."
) {
    private val announceKeySpawn by BooleanSetting(
        "Announce Key Spawn",
        true,
        desc = "Announces when a key is spawned."
    )
    private val witherColor by ColorSetting(
        "Wither Color",
        Colors.BLACK.withAlpha(0.8f),
        allowAlpha = true,
        desc = "The color of the box."
    )
    private val bloodColor by ColorSetting("Blood Color", Colors.MINECRAFT_RED.withAlpha(0.8f), allowAlpha = true, desc = "The color of the box.")
    private data class KeyInfo(val entity: Entity, val color: Color)
    private var currentKey: KeyInfo? = null

    @EventHandler
    fun onWorldLoad(event: WorldLoadEvent) {
        currentKey = null
    }

    @EventHandler
    fun onPacketReceive(event: PacketEvent.Receive) = with(event.packet) {
        if (this !is EntityTrackerUpdateS2CPacket || !DungeonUtils.inDungeons || DungeonUtils.inBoss) return@with
        val entity = mc.world?.getEntityById(id) as? ArmorStandEntity ?: return
        if (currentKey?.entity == entity) return

        currentKey = when (entity.name.string) {
            "Wither Key" -> KeyInfo(entity, witherColor)
            "Blood Key" -> KeyInfo(entity, bloodColor)
            else -> return
        }
        if (announceKeySpawn) alert("${entity.name}§7 spawned!")
    }

    @EventHandler
    fun onRenderWorld(event: RenderEvent.Last) {
        currentKey?.let { (entity, color) ->
            if (!entity.isAlive) {
                currentKey = null
                return
            }
            event.context.drawWireFrameBox(entity.boundingBox, color)
        }
    }
}