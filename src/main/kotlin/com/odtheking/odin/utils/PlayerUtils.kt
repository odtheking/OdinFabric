package com.odtheking.odin.utils

import com.odtheking.odin.OdinMod.mc
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents

fun playSoundAtPlayer(event: SoundEvent, volume: Float = 1f, pitch: Float = 1f) =
    mc.execute { mc.player?.playSound(event, volume, pitch) }

fun setTitle(title: String) {
    mc.gui.setTimes(0, 20, 5)
    mc.gui.setTitle(Component.literal(title))
}

fun alert(title: String, playSound: Boolean = true) {
    setTitle(title)
    if (playSound) playSoundAtPlayer(SoundEvent.createVariableRangeEvent(SoundEvents.FIREWORK_ROCKET_LAUNCH.location))
}

fun getPositionString(): String {
    with(mc.player?.blockPosition() ?: BlockPos(0, 0, 0)) {
        return "x: $x, y: $y, z: $z"
    }
}