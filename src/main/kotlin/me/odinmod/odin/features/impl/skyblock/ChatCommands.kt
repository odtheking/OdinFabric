package me.odinmod.odin.features.impl.skyblock

import me.odinmod.odin.clickgui.settings.Setting.Companion.withDependency
import me.odinmod.odin.clickgui.settings.impl.BooleanSetting
import me.odinmod.odin.clickgui.settings.impl.DropdownSetting
import me.odinmod.odin.events.PacketEvent
import me.odinmod.odin.features.Module
import me.odinmod.odin.utils.getPositionString
import me.odinmod.odin.utils.noControlCodes
import me.odinmod.odin.utils.sendCommand
import me.odinmod.odin.utils.skyblock.LocationUtils
import meteordevelopment.orbit.EventHandler
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

object ChatCommands: Module(
    name = "Chat Commands",
    description = "Handles chat commands in Skyblock."
) {
    private val partyChatCommands by BooleanSetting("Party Commands", true, "Enables party chat commands.")
    private val guildChatCommands by BooleanSetting("Guild Commands", true, "Enables guild chat commands.")

    private val showSettings by DropdownSetting("Show Settings", false)
    private val partyWarp by BooleanSetting("Warp", true, desc = "Executes the /party warp commnad.").withDependency { showSettings }
    private val coords by BooleanSetting("Coords (coords)", true, desc = "Sends your current coordinates.").withDependency { showSettings }
    private val partyAllInvite by BooleanSetting("Allinvite", true, desc = "Executes the /party settings allinvite command.").withDependency { showSettings }
    private val odin by BooleanSetting("Odin", true, desc = "Sends the odin discord link.").withDependency { showSettings }
    private val boop by BooleanSetting("Boop", true, desc = "Executes the /boop command.").withDependency { showSettings }
    private val coinFlip by BooleanSetting("Coinflip (cf)", true, desc = "Sends the result of a coinflip..").withDependency { showSettings }
    private val eightBall by BooleanSetting("Eightball", true, desc = "Sends a random 8ball response.").withDependency { showSettings }
    private val dice by BooleanSetting("Dice", true, desc = "Rolls a dice.").withDependency { showSettings }
    private val partyTransfer by BooleanSetting("Party transfer (pt)", false, desc = "Executes the /party transfer command.").withDependency { showSettings }
    private val fps by BooleanSetting("FPS", true, desc = "Sends your current FPS.").withDependency { showSettings }
    private val racism by BooleanSetting("Racism", false, desc = "Sends a random racism percentage.").withDependency { showSettings }
    private val time by BooleanSetting("Time", false, desc = "Sends the current time.").withDependency { showSettings }
    private val partyDemote by BooleanSetting("Demote", false, desc = "Executes the /party demote command.").withDependency { showSettings }
    private val partyPromote by BooleanSetting("Promote", false, desc = "Executes the /party promote command.").withDependency { showSettings }
    private val location by BooleanSetting("Location", true, desc = "Sends your current location.").withDependency { showSettings }
    private val holding by BooleanSetting("Holding", true, desc = "Sends the item you are holding.").withDependency { showSettings }

    // https://regex101.com/r/joY7dm/1
    private val messageRegex = Regex("^(?:Party > (\\[[^]]*?])? ?(\\w{1,16})(?: [ቾ⚒])?: ?(.+)\$|Guild > (\\[[^]]*?])? ?(\\w{1,16})(?: \\[([^]]*?)])?: ?(.+)\$|From (\\[[^]]*?])? ?(\\w{1,16}): ?(.+)\$)")

    @EventHandler
    fun onPacketReceive(event: PacketEvent.Receive) = with (event.packet) {
        if (this !is GameMessageS2CPacket || overlay) return
        val result = messageRegex.find(content.string) ?: return
        val channel = when(result.value.split(" ")[0]) {
            //"From" -> if (!private) return@onMessage else*/ ChatChannel.PRIVATE
            "Party" -> if (!partyChatCommands)  return else ChatChannel.PARTY
            "Guild" -> if (!guildChatCommands)  return else ChatChannel.GUILD
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
            "odin", "od" -> if (odin) channelMessage("Odin! https://discord.gg/2nCbC9hkxT", name, channel)
            "coords", "co" -> if (coords) channelMessage(getPositionString(), name, channel)

            "boop" -> if (boop) words.getOrNull(1)?.let { sendCommand("boop $it") }
            "cf" -> if (coinFlip) channelMessage(if (Math.random() < 0.5) "heads" else "tails", name, channel)
            "8ball" -> if (eightBall) channelMessage(responses.random(), name, channel)
            "dice" -> if (dice) channelMessage((1..6).random(), name, channel)
            "racism" -> if (racism) channelMessage("$name is ${Random.nextInt(1, 101)}% racist. Racism is not allowed!", name, channel)
            "fps" -> if (fps) channelMessage("Current FPS: ${mc.currentFps}", name, channel)
            "time" -> if (time) channelMessage("Current Time: ${ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"))}", name, channel)
            "location" -> if (location) channelMessage("Current Location: ${LocationUtils.currentArea.displayName}", name, channel)
            "holding" -> if (holding) channelMessage("Holding: ${mc.player?.mainHandStack?.name?.string?.noControlCodes ?: "Nothing :("}", name, channel)

            // party commands

            "warp", "w" ->
                if (channel == ChatChannel.PARTY && partyWarp) sendCommand("/party warp")

            "allinvite", "allinv" ->
                if (channel == ChatChannel.PARTY && partyAllInvite) sendCommand("/party settings allinvite")

            "pt", "ptme", "transfer" ->
                if (channel == ChatChannel.PARTY && partyTransfer) sendCommand("/party transfer $name")

            "promote" ->
                if (channel == ChatChannel.PARTY && partyPromote) sendCommand("/party promote $name")

            "demote" ->
                if (channel == ChatChannel.PARTY && partyDemote) sendCommand("/party demote $name")
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