package io.github.odtheking.odin.utils

import io.github.odtheking.odin.OdinMod.mc
import net.minecraft.sound.SoundEvent
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos

fun playSoundAtPlayer(event: SoundEvent, volume: Float = 1f, pitch: Float = 1f) =
    mc.player?.playSound(event, volume, pitch)

fun setTitle(title: String) {
    mc.inGameHud.setTitleTicks(0, 20, 5)
    mc.inGameHud.setTitle(Text.of(title))
}

fun alert(title: String, playSound: Boolean = true) {
    setTitle(title)
    if (playSound) playSoundAtPlayer(SoundEvent.of(SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH.id))
}

fun getPositionString(): String {
    with(mc.player?.blockPos ?: BlockPos(0, 0, 0)) {
        return "x: $x, y: $y, z: $z"
    }
}