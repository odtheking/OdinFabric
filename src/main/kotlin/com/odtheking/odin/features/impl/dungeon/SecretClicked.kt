package com.odtheking.odin.features.impl.dungeon

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.*
import com.odtheking.odin.events.PacketEvent
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.SecretPickupEvent
import com.odtheking.odin.events.WorldLoadEvent
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Color.Companion.withAlpha
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.handlers.LimitedTickTask
import com.odtheking.odin.utils.playSoundAtPlayer
import com.odtheking.odin.utils.render.drawStyledBox
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import meteordevelopment.orbit.EventHandler
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket
import net.minecraft.sound.SoundEvent
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import java.util.concurrent.CopyOnWriteArrayList

object SecretClicked : Module(
    name = "Secret Clicked",
    description = "Provides both audio and visual feedback when a secret is clicked."
) {
    private val boxesDropdown by DropdownSetting("Secret Boxes Dropdown")
    private val boxes by BooleanSetting("Secret Boxes", true, desc = "Whether or not to render boxes around clicked secrets.").withDependency { boxesDropdown }
    private val style by SelectorSetting("Style", "Outline", arrayListOf("Filled", "Outline", "Filled Outline"), desc = "The style of the box.").withDependency { boxesDropdown && boxes }
    private val color by ColorSetting("Color", Colors.MINECRAFT_GOLD.withAlpha(.4f), true, desc = "The color of the box.").withDependency { boxesDropdown && boxes }
    private val depthCheck by BooleanSetting("Depth check", false, desc = "Boxes show through walls.").withDependency { boxesDropdown && boxes }
    private val lockedColor by ColorSetting("Locked Color", Colors.MINECRAFT_RED.withAlpha(.4f), true, desc = "The color of the box when the chest is locked.").withDependency { boxesDropdown && boxes }
    private val timeToStay by NumberSetting("Time To Stay", 7, 1, 20, 0.2, desc = "The time the chests should remain highlighted.", unit = "s").withDependency { boxesDropdown && boxes }
    private val boxInBoss by BooleanSetting("Box In Boss", false, desc = "Highlight clicks in boss.").withDependency { boxesDropdown && boxes }
    private val toggleItems by BooleanSetting("Item Boxes", true, desc = "Render boxes for collected items.").withDependency { boxesDropdown && boxes }

    private val chimeDropdownSetting by DropdownSetting("Secret Chime Dropdown")
    private val chime by BooleanSetting("Secret Chime", true, desc = "Whether or not to play a sound when a secret is clicked.").withDependency { chimeDropdownSetting }
    private val customSound by StringSetting("Custom Sound", "entity.blaze.hurt", desc = "Name of a custom sound to play. Do not use the bat death sound or your game will freeze!", length = 64).withDependency { chimeDropdownSetting && chime }
    private val reset by ActionSetting("Play Sound", desc = "Plays the sound with the current settings.") { playSoundAtPlayer(SoundEvent.of(Identifier.of(customSound))) }.withDependency { chimeDropdownSetting && chime }

    private val chimeInBoss by BooleanSetting("Chime In Boss", false, desc = "Prevent playing the sound if in boss room.").withDependency { chimeDropdownSetting && chime }

    private data class Secret(val pos: BlockPos, var locked: Boolean = false)
    private val clickedSecretsList = CopyOnWriteArrayList<Secret>()
    private var lastPlayed = System.currentTimeMillis()

    @EventHandler
    fun onRenderWorld(event: RenderEvent.Last) {
        if (!boxes || !DungeonUtils.inDungeons || (DungeonUtils.inBoss && !boxInBoss) || clickedSecretsList.isEmpty()) return

        clickedSecretsList.forEach { secret ->
            val currentColor = if (secret.locked) lockedColor else color
            val box = mc.world?.getBlockState(secret.pos)?.getOutlineShape(mc.world, secret.pos)?.asCuboid()
                ?.takeIf { !it.isEmpty }?.boundingBox?.offset(secret.pos) ?: Box(secret.pos)
            event.context.drawStyledBox(box, currentColor, style, depthCheck)
        }
    }

    @EventHandler
    fun onSecretInteract(event: SecretPickupEvent.Interact) {
        secretBox(event.blockPos)
        secretChime()
    }

    @EventHandler
    fun onSecretBat(event: SecretPickupEvent.Bat) {
        secretBox(BlockPos(event.packet.x.toInt(), event.packet.y.toInt(), event.packet.z.toInt()))
        secretChime()
    }

    @EventHandler
    fun onSecretItem(event: SecretPickupEvent.Item) {
        if (toggleItems) secretBox(event.entity.blockPos)
        secretChime()
    }

    private fun secretChime() {
        if (!chime || (DungeonUtils.inBoss && !chimeInBoss) || System.currentTimeMillis() - lastPlayed <= 10) return
        playSoundAtPlayer(SoundEvent.of(Identifier.of(customSound)))
        lastPlayed = System.currentTimeMillis()
    }

    private fun secretBox(pos: BlockPos) {
        if (!boxes || (DungeonUtils.inBoss && !boxInBoss) || clickedSecretsList.any { it.pos == pos }) return
        clickedSecretsList.add(Secret(pos))
        LimitedTickTask(timeToStay * 20, 1) { clickedSecretsList.removeFirstOrNull() }
    }

    @EventHandler
    fun onWorldLoad(event: WorldLoadEvent) {
        clickedSecretsList.clear()
    }

    @EventHandler
    fun onPacketReceive(event: PacketEvent.Receive) = with (event.packet) {
        if (this !is GameMessageS2CPacket || overlay) return
        if (content.string == "That chest is locked!") clickedSecretsList.lastOrNull()?.locked = true
    }
}