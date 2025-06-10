package me.odinmod.odin.features.skyblock

import me.odinmod.odin.OdinMod.mc
import me.odinmod.odin.config.categories.SkyblockConfig
import me.odinmod.odin.events.PacketEvent
import me.odinmod.odin.utils.getPositionString
import me.odinmod.odin.utils.noControlCodes
import me.odinmod.odin.utils.sendCommand
import me.odinmod.odin.utils.skyblock.LocationUtils
import meteordevelopment.orbit.EventHandler
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

object ChatCommands {

    // https://regex101.com/r/joY7dm/1
    private val messageRegex = Regex("^(?:Party > (\\[[^]]*?])? ?(\\w{1,16})(?: [ቾ⚒])?: ?(.+)\$|Guild > (\\[[^]]*?])? ?(\\w{1,16})(?: \\[([^]]*?)])?: ?(.+)\$|From (\\[[^]]*?])? ?(\\w{1,16}): ?(.+)\$)")

    @EventHandler
    fun onPacketReceive(event: PacketEvent.Receive) = with (event.packet) {
        if (!SkyblockConfig.enableChatCommands || this !is GameMessageS2CPacket || overlay) return
        val result = messageRegex.find(content.string) ?: return
        val channel = when(result.value.split(" ")[0]) {
            //"From" -> if (!private) return@onMessage else*/ ChatChannel.PRIVATE
            "Party" -> if (!SkyblockConfig.partyChatCommands)  return else ChatChannel.PARTY
            "Guild" -> if (!SkyblockConfig.guildChatCommands)  return else ChatChannel.GUILD
            else -> return
        }

        val ign = result.groups[2]?.value ?: result.groups[5]?.value ?: result.groups[9]?.value ?: return
        val msg = result.groups[3]?.value ?: result.groups[7]?.value ?: result.groups[10]?.value ?: return

        handleChatCommands(msg, ign, channel)
    }

    private fun handleChatCommands(message: String, name: String, channel: ChatChannel) {
        val words = message.drop(1).split(" ").map { it.lowercase() }

        when (words[0]) {
            "h", "help" -> channelMessage("Commands: !help, !odin", name, channel)
            "odin", "od" -> if (SkyblockConfig.odin) channelMessage("Odin! https://discord.gg/2nCbC9hkxT", name, channel)
            "coords", "co" -> if (SkyblockConfig.coords) channelMessage(getPositionString(), name, channel)

            "boop" -> if (SkyblockConfig.boop) words.getOrNull(1)?.let { sendCommand("boop $it") }
            "cf" -> if (SkyblockConfig.coinFlip) channelMessage(if (Math.random() < 0.5) "heads" else "tails", name, channel)
            "8ball" -> if (SkyblockConfig.eightBall) channelMessage(responses.random(), name, channel)
            "dice" -> if (SkyblockConfig.dice) channelMessage((1..6).random(), name, channel)
            "racism" -> if (SkyblockConfig.racism) channelMessage("$name is ${Random.nextInt(1, 101)}% racist. Racism is not allowed!", name, channel)
            "fps" -> if (SkyblockConfig.fps) channelMessage("Current FPS: ${mc.currentFps}", name, channel)
            "time" -> if (SkyblockConfig.time) channelMessage("Current Time: ${ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"))}", name, channel)
            "location" -> if (SkyblockConfig.location) channelMessage("Current Location: ${LocationUtils.currentArea.displayName}", name, channel)
            "holding" -> if (SkyblockConfig.holding) channelMessage("Holding: ${mc.player?.mainHandStack?.name?.string?.noControlCodes ?: "Nothing :("}", name, channel)

            // party commands

            "warp", "w" ->
                if (channel == ChatChannel.PARTY && SkyblockConfig.partyWarp) sendCommand("/party warp")

            "allinvite", "allinv" ->
                if (channel == ChatChannel.PARTY && SkyblockConfig.partyAllInvite) sendCommand("/party settings allinvite")

            "pt", "ptme", "transfer" ->
                if (channel == ChatChannel.PARTY && SkyblockConfig.partyTransfer) sendCommand("/party transfer $name")

            "promote" ->
                if (channel == ChatChannel.PARTY && SkyblockConfig.partyPromote) sendCommand("/party promote $name")

            "demote" ->
                if (channel == ChatChannel.PARTY && SkyblockConfig.partyDemote) sendCommand("/party demote $name")
        }
    }

    private fun channelMessage(message: Any, name: String, channel: ChatChannel) {
        when (channel) {
            ChatChannel.GUILD -> sendCommand("/g chat $message")
            ChatChannel.PARTY -> sendCommand("/p chat $message")
            ChatChannel.PRIVATE -> sendCommand("/msg $name $message")
        }
    }

    private val responses = arrayOf(
        "It is certain", "It is decidedly so", "Without a doubt",
        "Yes definitely", "You may rely on it", "As I see it, yes",
        "Most likely", "Outlook good", "Yes", "Signs point to yes",
        "Reply hazy try again", "Ask again later", "Better not tell you now",
        "Cannot predict now", "Concentrate and ask again", "Don't count on it",
        "My reply is no", "My sources say no", "Outlook not so good", "Very doubtful"
    )

    enum class ChatChannel {
        PARTY, GUILD, PRIVATE
    }
}