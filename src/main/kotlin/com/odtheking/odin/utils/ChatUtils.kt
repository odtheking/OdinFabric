package com.odtheking.odin.utils

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.features.impl.render.ClickGUIModule
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
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
    if (mc.isOnThread) mc.gui?.chat?.addMessage(text)
    else mc.execute { mc.gui?.chat?.addMessage(text) }
}

fun modMessage(message: Component, prefix: String = "§3Odin §8»§r ", chatStyle: Style? = null) {
    val text = Component.literal(prefix).append(message)
    chatStyle?.let { text.setStyle(chatStyle) }
    if (mc.isOnThread) mc.gui?.chat?.addMessage(text)
    else mc.execute { mc.gui?.chat?.addMessage(text) }
}

fun devMessage(message: Any?) {
    if (!ClickGUIModule.devMessage) return
    modMessage(message, "§3Odin§bDev §8»§r ")
}

/**
 * Creates a `ChatStyle` with click and hover events for making a message clickable.
 *
 * @param value Text to show up when hovered.
 * @return A `ChatStyle` with click and hover events.
 */
fun createClickStyle(value: String): Style {
    return Style.EMPTY
        .withClickEvent(ClickEvent.RunCommand(value))
        .withHoverEvent(
            HoverEvent.ShowText(Component.literal(value).withStyle { it.withColor(ChatFormatting.YELLOW) })
        )
}