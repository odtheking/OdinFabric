package me.odinmod.odin.utils

import me.odinmod.odin.OdinMod.mc
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.random.LocalRandom

fun playSoundAtPlayer(event: SoundEvent) =
    PositionedSoundInstance(event, SoundCategory.MASTER, 1f, 1f, LocalRandom(0L), mc.player?.blockPos ?: BlockPos(0, 0, 0))
        .also { mc.soundManager?.play(it) }

fun setTitle(title: String) {
    mc.inGameHud.setTitleTicks(5, 20, 5)
    mc.inGameHud.setTitle(Text.literal(title))
}

fun alert(title: String) {
    setTitle(title)
    playSoundAtPlayer(SoundEvent.of(SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH.id))
}

fun getPositionString(): String {
    with (mc.player?.blockPos ?: BlockPos(0, 0, 0)) {
        return "x: $x, y: $y, z: $z"
    }
}