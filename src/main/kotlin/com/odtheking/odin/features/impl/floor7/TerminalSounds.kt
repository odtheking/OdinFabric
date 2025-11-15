package com.odtheking.odin.features.impl.floor7

import com.odtheking.mixin.accessors.AbstractContainerScreenAccessor
import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.ActionSetting
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.StringSetting
import com.odtheking.odin.events.ChatPacketEvent
import com.odtheking.odin.events.GuiEvent
import com.odtheking.odin.events.TerminalEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.events.core.onReceive
import com.odtheking.odin.features.Module
import com.odtheking.odin.features.impl.floor7.terminalhandler.TerminalTypes
import com.odtheking.odin.utils.playSoundAtPlayer
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.network.protocol.game.ClientboundSoundPacket
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents

object TerminalSounds : Module(
    name = "Terminal Sounds",
    description = "Plays a sound whenever you click a correct item in a terminal."
){
    val clickSounds by BooleanSetting("Click Sounds", true, desc = "Replaces the click sounds in terminals.")
    private val clickSound by StringSetting("Custom Click Sound", "entity.blaze.hurt", desc = "Name of a custom sound to play. This is used when Custom is selected in the Sound setting.", length = 32).withDependency { clickSounds }
    private val reset by ActionSetting("Play click sound", desc = "Plays the sound with the current settings.") { playSoundAtPlayer(SoundEvent.createVariableRangeEvent(ResourceLocation.withDefaultNamespace(clickSound))) }
    private val completeSounds by BooleanSetting("Complete Sounds", false, desc = "Plays a sound when you complete a terminal.")
    private val cancelLastClick by BooleanSetting("Cancel Last Click", false, desc = "Cancels the last click sound instead of playing both click and completion sound.").withDependency { clickSounds && completeSounds }
    private val customCompleteSound by StringSetting("Custom Completion Sound", "entity.blaze.hurt", desc = "Name of a custom sound to play. This is used when Custom is selected in the Sound setting.", length = 32).withDependency { completeSounds }
    private val playCompleteSound by ActionSetting("Play complete sound", desc = "Plays the sound with the current settings.") {
        playSoundAtPlayer(SoundEvent.createVariableRangeEvent(ResourceLocation.withDefaultNamespace(customCompleteSound)))
    }

    private val coreRegex = Regex("^The Core entrance is opening!$")
    private val gateRegex = Regex("^The gate has been destroyed!$")
    private var lastPlayed = System.currentTimeMillis()

    init {
        on<TerminalEvent.Solved> {
            if (shouldReplaceSounds && (!completeSounds && !clickSounds)) mc.player?.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 8f, 4f)
            else if (shouldReplaceSounds && completeSounds && !clickSounds) playCompleteSound()
        }

        on<GuiEvent.MouseClick> {
            if (shouldReplaceSounds) playSoundForSlot((screen as AbstractContainerScreenAccessor).hoveredSlot?.index ?: return@on, click.button())
        }

        on<GuiEvent.CustomTermGuiClick> {
            if (shouldReplaceSounds) playSoundForSlot(slot, button)
        }

        onReceive<ClientboundSoundPacket> {
            if (sound.value() == SoundEvents.NOTE_BLOCK_PLING.value() && volume == 8f && pitch == 4.047619f && shouldReplaceSounds)
                it.cancel()
        }

        on<ChatPacketEvent> {
            if (!DungeonUtils.inDungeons || !shouldReplaceSounds) return@on
            when {
                value.matches(gateRegex) -> playSoundAtPlayer(SoundEvents.NOTE_BLOCK_PLING.value())
                value.matches(coreRegex) -> playSoundAtPlayer(SoundEvents.NOTE_BLOCK_PLING.value())
            }
        }
    }

    private fun playSoundForSlot(slot: Int, button: Int) {
        if (TerminalSolver.currentTerm?.isClicked == true || TerminalSolver.currentTerm?.canClick(slot, button) == false) return
        if ((TerminalSolver.currentTerm?.solution?.size == 1 || (TerminalSolver.currentTerm?.type == TerminalTypes.MELODY && slot == 43)) && completeSounds) {
            if (!cancelLastClick) playTerminalSound()
            playSoundAtPlayer(SoundEvent.createVariableRangeEvent(ResourceLocation.withDefaultNamespace(customCompleteSound)))
        } else playTerminalSound()
    }

    private fun playTerminalSound() {
        if (System.currentTimeMillis() - lastPlayed <= 2) return
        playSoundAtPlayer(SoundEvent.createVariableRangeEvent(ResourceLocation.withDefaultNamespace(clickSound)))
        lastPlayed = System.currentTimeMillis()
    }

    private inline val shouldReplaceSounds get() = (TerminalSolver.currentTerm != null && clickSounds)
}