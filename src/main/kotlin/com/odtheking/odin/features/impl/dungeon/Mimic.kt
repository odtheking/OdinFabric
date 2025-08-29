package com.odtheking.odin.features.impl.dungeon

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.ActionSetting
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.StringSetting
import com.odtheking.odin.events.PacketEvent
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.sendCommand
import com.odtheking.odin.utils.skyblock.dungeon.DungeonListener
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import meteordevelopment.orbit.EventHandler
import net.minecraft.entity.EntityStatuses
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.mob.ZombieEntity
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket

object Mimic : Module(
    name = "Mimic",
    description = "Highlights and announces mimic kills in dungeons."
) {
    private val mimicMessageToggle by BooleanSetting("Toggle Mimic Message", true, desc = "Toggles the mimic killed message.")
    val mimicMessage by StringSetting("Mimic Message", "Mimic Killed!", 128, desc = "Message sent when mimic is detected as killed.").withDependency { mimicMessageToggle }
    private val reset by ActionSetting("Mimic Killed", desc = "Sends Mimic killed message in party chat.") { mimicKilled() }

    private val princeMessageToggle by BooleanSetting("Toggle Prince Message", false, desc = "Toggles the prince killed message.")
    val princeMessage by StringSetting("Prince Message", "Prince Killed!", 128, desc = "Message sent when prince is detected as killed.").withDependency { princeMessageToggle }
    private val princeReset by ActionSetting("Prince Killed", desc = "Sends Prince killed message in party chat.") { princeKilled() }

    private val princeRegex = Regex("^A Prince falls\\. \\+1 Bonus Score$")

    @EventHandler
    fun onPacketReceived(event: PacketEvent.Receive) = with (event.packet) {
        if (this !is EntityStatusS2CPacket || status != EntityStatuses.PLAY_DEATH_SOUND_OR_ADD_PROJECTILE_HIT_PARTICLES ||
            !DungeonUtils.inDungeons || DungeonUtils.inBoss || DungeonUtils.mimicKilled) return@with
        val entity = getEntity(mc.world) ?: return@with
        if (entity is ZombieEntity && entity.isBaby && EquipmentSlot.entries.all { entity.getEquippedStack(it).isEmpty }) mimicKilled()
    }

    private fun mimicKilled() {
        if (DungeonUtils.mimicKilled || DungeonUtils.inBoss) return
        if (mimicMessageToggle) sendCommand("pc $mimicMessage")
        DungeonListener.dungeonStats.mimicKilled = true
    }

    private fun princeKilled() {
        if (DungeonUtils.princeKilled || DungeonUtils.inBoss) return
        if (princeMessageToggle) sendCommand("pc $princeMessage")
        DungeonListener.dungeonStats.princeKilled = true
    }

    @EventHandler
    fun onPacketReceive(event: PacketEvent.Receive) = with (event.packet) {
        if (this !is GameMessageS2CPacket || overlay || !DungeonUtils.inDungeons) return
        if (content.string.matches(princeRegex)) princeKilled()
    }
}