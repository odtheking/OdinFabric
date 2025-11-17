package com.odtheking.odin.utils

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.features.impl.render.ClickGUIModule
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style

fun sendChatMessage(message: Any) {
    mc.player?.connection?.sendChat(message.toString())
}

fun sendCommand(command: String) {
    mc.player?.connection?.sendCommand(command)
}

fun modMessage(message: Any?, prefix: String = "§3Odin §8»§r ", chatStyle: Style? = null) {
    val text = Component.literal("$prefix$message")
    chatStyle?.let { text.setStyle(chatStyle) }
    mc.gui?.chat?.addMessage(text)
}

fun modMessage(message: Component, prefix: String = "§3Odin §8»§r ", chatStyle: Style? = null) {
    val text = Component.literal(prefix).append(message)
    chatStyle?.let { text.setStyle(chatStyle) }
    mc.gui?.chat?.addMessage(text)
}

fun devMessage(message: Any?) {
    if (!ClickGUIModule.devMessage) return
    modMessage(message, "§3Odin§bDev §8»§r ")
}