package com.odtheking.odin.utils

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.features.impl.render.ClickGUIModule
import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting

fun sendChatMessage(message: Any) {
    mc.player?.networkHandler?.sendChatMessage(message.toString())
}

fun sendCommand(command: String) {
    mc.player?.networkHandler?.sendChatCommand(command)
}

fun modMessage(message: Any?, prefix: String = "§3Odin §8»§r ", chatStyle: Style? = null) {
    val text = Text.literal("$prefix$message")
    chatStyle?.let { text.setStyle(chatStyle) }
    if (mc.isOnThread) mc.inGameHud?.chatHud?.addMessage(text)
    else mc.execute { mc.inGameHud?.chatHud?.addMessage(text) }
}

fun modMessage(message: Text, prefix: String = "§3Odin §8»§r ", chatStyle: Style? = null) {
    val text = Text.literal(prefix).append(message)
    chatStyle?.let { text.setStyle(chatStyle) }
    if (mc.isOnThread) mc.inGameHud?.chatHud?.addMessage(text)
    else mc.execute { mc.inGameHud?.chatHud?.addMessage(text) }
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
            HoverEvent.ShowText(Text.literal(value).styled { it.withColor(Formatting.YELLOW) })
        )
}