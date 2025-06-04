package me.odinmod.odin.utils

import me.odinmod.odin.OdinMod.Companion.mc
import net.minecraft.text.Text
import net.minecraft.util.Formatting

fun sendChatMessage(message: Any) {
    mc.player?.networkHandler?.sendChatMessage(message.toString())
}

fun modMessage(message: Any?, prefix: String = "§3Odin §8»§r ", formatting: Formatting? = null) {
    val text = Text.literal("$prefix$message")
    formatting?.let { text.formatted(it) }

    mc.inGameHud.chatHud.addMessage(text)
}