package com.odtheking.odin.features.impl.floor7

import com.odtheking.mixin.accessors.HandledScreenAccessor
import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.ActionSetting
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.StringSetting
import com.odtheking.odin.events.GuiEvent
import com.odtheking.odin.events.PacketEvent
import com.odtheking.odin.events.TerminalEvent
import com.odtheking.odin.features.Module
import com.odtheking.odin.features.impl.floor7.terminalhandler.TerminalTypes
import com.odtheking.odin.utils.playSoundAtPlayer
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import meteordevelopment.orbit.EventHandler
import meteordevelopment.orbit.EventPriority
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket
import net.minecraft.sound.SoundEvent
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Identifier

object TerminalSounds : Module(
    name = "Terminal Sounds",
    description = "Plays a sound whenever you click a correct item in a terminal."
){
    val clickSounds by BooleanSetting("Click Sounds", true, desc = "Replaces the click sounds in terminals.")
    private val clickSound by StringSetting("Custom Click Sound", "entity.blaze.hurt", desc = "Name of a custom sound to play. This is used when Custom is selected in the Sound setting.", length = 32).withDependency { clickSounds }
    private val reset by ActionSetting("Play click sound", desc = "Plays the sound with the current settings.") { playSoundAtPlayer(SoundEvent.of(Identifier.of(clickSound))) }
    private val completeSounds by BooleanSetting("Complete Sounds", false, desc = "Plays a sound when you complete a terminal.")
    private val cancelLastClick by BooleanSetting("Cancel Last Click", false, desc = "Cancels the last click sound instead of playing both click and completion sound.").withDependency { clickSounds && completeSounds }
    private val customCompleteSound by StringSetting("Custom Completion Sound", "entity.blaze.hurt", desc = "Name of a custom sound to play. This is used when Custom is selected in the Sound setting.", length = 32).withDependency { completeSounds }
    private val playCompleteSound by ActionSetting("Play complete sound", desc = "Plays the sound with the current settings.") {
        playSoundAtPlayer(SoundEvent.of(Identifier.of(customCompleteSound)))
    }

    private val coreRegex = Regex("^The Core entrance is opening!$")
    private val gateRegex = Regex("^The gate has been destroyed!$")
    private var lastPlayed = System.currentTimeMillis()

    @EventHandler
    fun onTermComplete(event: TerminalEvent.Solved) {
        if (shouldReplaceSounds && (!completeSounds && !clickSounds)) mc.player?.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 8f, 4f)
        else if (shouldReplaceSounds && completeSounds && !clickSounds) playCompleteSound()
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onSlotClick(event: GuiEvent.MouseClick) {
        val id = (event.screen as HandledScreenAccessor).focusedSlot?.id ?: return
        if (shouldReplaceSounds) playSoundForSlot(id, event.button)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onCustomSlotClick(event: GuiEvent.CustomTermGuiClick) {
        if (shouldReplaceSounds) playSoundForSlot(event.slot, event.button)
    }

    @EventHandler
    fun onPacketReceive(event: PacketEvent.Receive) = with (event.packet) {
        if (this is PlaySoundS2CPacket && sound.value() == SoundEvents.BLOCK_NOTE_BLOCK_PLING.value() && volume == 8f && pitch == 4.047619f && shouldReplaceSounds)
            event.cancel()
        if (this !is GameMessageS2CPacket || overlay || !DungeonUtils.inDungeons || !shouldReplaceSounds) return
        when {
            content.string.matches(gateRegex) -> playSoundAtPlayer(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value())
            content.string.matches(coreRegex) -> playSoundAtPlayer(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value())
        }
    }

    private fun playSoundForSlot(slot: Int, button: Int) {
        if (TerminalSolver.currentTerm?.isClicked == true || TerminalSolver.currentTerm?.canClick(slot, button) == false) return
        if ((TerminalSolver.currentTerm?.solution?.size == 1 || (TerminalSolver.currentTerm?.type == TerminalTypes.MELODY && slot == 43)) && completeSounds) {
            if (!cancelLastClick) playTerminalSound()
            playSoundAtPlayer(SoundEvent.of(Identifier.of(customCompleteSound)))
        } else playTerminalSound()
    }

    private fun playTerminalSound() {
        if (System.currentTimeMillis() - lastPlayed <= 2) return
        playSoundAtPlayer(SoundEvent.of(Identifier.of(clickSound)))
        lastPlayed = System.currentTimeMillis()
    }

    private inline val shouldReplaceSounds get() = (TerminalSolver.currentTerm != null && clickSounds)
}