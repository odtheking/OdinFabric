package com.odtheking.odin.features.impl.dungeon

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.ActionSetting
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.StringSetting
import com.odtheking.odin.events.ChatPacketEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.events.core.onReceive
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.sendCommand
import com.odtheking.odin.utils.skyblock.dungeon.DungeonListener
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.world.entity.monster.Zombie

object Mimic : Module(
    name = "Mimic",
    description = "Highlights and announces mimic kills in dungeons."
) {
    private val mimicMessageToggle by BooleanSetting("Toggle Mimic Message", true, desc = "Toggles the mimic killed message.")
    val mimicMessage by StringSetting("Mimic Message", "Mimic Killed!", 128, desc = "Message sent when mimic is detected as killed.").withDependency { mimicMessageToggle }
    private val reset by ActionSetting("Mimic Killed", desc = "Sends Mimic killed message in party chat.") { mimicKilled() }

    private val princeMessageToggle by BooleanSetting("Send Prince Message", true, desc = "Toggles the prince killed message.")
    val princeMessage by StringSetting("Prince Message", "Prince Killed!", 128, desc = "Message sent when prince is detected as killed.").withDependency { princeMessageToggle }
    private val princeReset by ActionSetting("Prince Killed", desc = "Sends Prince killed message in party chat.") { princeKilled() }

    private val princeRegex = Regex("^A Prince falls\\. \\+1 Bonus Score$")

    init {
        onReceive<ClientboundRemoveEntitiesPacket> {
            if (!DungeonUtils.isFloor(6, 7) || DungeonUtils.inBoss || DungeonUtils.mimicKilled) return@onReceive
            entityIds.forEach { id ->
                val entity = mc.level?.getEntity(id) ?: return@forEach
                if (entity is Zombie && entity.isBaby) mimicKilled()
            }
        }

        onReceive<ClientboundSetEntityDataPacket> {
            if (!DungeonUtils.isFloor(6, 7) || DungeonUtils.inBoss || DungeonUtils.mimicKilled) return@onReceive
            val entity = runCatching { mc.level?.getEntity(id) }.getOrNull() ?: return@onReceive
            val health = (packedItems.find { it.id == 9 }?.value as? Float) ?: return@onReceive
            if (entity is Zombie && entity.isBaby && health <= 0f) mimicKilled()
        }

        on<ChatPacketEvent> {
            if (value.matches(princeRegex)) princeKilled()
        }
    }

    private fun mimicKilled() {
        if (DungeonUtils.mimicKilled || DungeonUtils.inBoss) return
        if (mimicMessageToggle) sendCommand("pc $mimicMessage")
        DungeonListener.dungeonStats.mimicKilled = true
    }

    private fun princeKilled() {
        if (DungeonUtils.princeKilled || DungeonUtils.inBoss || !DungeonUtils.inDungeons) return
        if (princeMessageToggle) sendCommand("pc $princeMessage")
        DungeonListener.dungeonStats.princeKilled = true
    }
}