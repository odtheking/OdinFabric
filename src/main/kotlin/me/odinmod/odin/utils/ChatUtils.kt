package me.odinmod.odin.utils

import me.odinmod.odin.OdinMod.Companion.mc
import net.minecraft.text.Text

fun sendChatMessage(message: Any) {
    mc.player?.networkHandler?.sendChatMessage(message.toString())
}

fun modMessage(message: Any?, prefix: String = "§3Odin §8»§r ") {
    mc.inGameHud?.chatHud?.addMessage(Text.literal("$prefix$message"))
}

fun modMessage(message: Text, prefix: String = "§3Odin §8»§r ") {
    mc.inGameHud?.chatHud?.addMessage(Text.literal(prefix).append(message))
}