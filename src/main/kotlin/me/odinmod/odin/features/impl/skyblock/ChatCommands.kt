package me.odinmod.odin.features.impl.skyblock

import me.odinmod.odin.clickgui.settings.Setting.Companion.withDependency
import me.odinmod.odin.clickgui.settings.impl.BooleanSetting
import me.odinmod.odin.clickgui.settings.impl.DropdownSetting
import me.odinmod.odin.events.MessageSentEvent
import me.odinmod.odin.events.PacketEvent
import me.odinmod.odin.features.Module
import me.odinmod.odin.utils.*
import me.odinmod.odin.utils.skyblock.LocationUtils
import meteordevelopment.orbit.EventHandler
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket
import net.minecraft.sound.SoundEvents
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

object ChatCommands : Module(
    name = "Chat Commands",
    description = "Adds various chat commands (boop, kick, coinFlip, 8ball, etc..)."
) {
    private val chatEmotes by BooleanSetting("Chat Emotes", true, desc = "Replaces chat emotes with their corresponding emojis.")
    private val partyChatCommands by BooleanSetting("Party Commands", true, "Enables party chat commands.")
    private val guildChatCommands by BooleanSetting("Guild Commands", true, "Enables guild chat commands.")
    private val privateChatCommands by BooleanSetting("Private Commands", true, "Enables private chat commands.")

    private val showSettings by DropdownSetting("Show Settings", false)
    private val partyWarp by BooleanSetting("Warp", true, desc = "Executes the /party warp command.").withDependency { showSettings }
    private val coords by BooleanSetting("Coords (coords)", true, desc = "Sends your current coordinates.").withDependency { showSettings }
    private val partyAllInvite by BooleanSetting("Allinvite", true, desc = "Executes the /party settings allinvite command.").withDependency { showSettings }
    private val odin by BooleanSetting("Odin", true, desc = "Sends the odin discord link.").withDependency { showSettings }
    private val boop by BooleanSetting("Boop", true, desc = "Executes the /boop command.").withDependency { showSettings }
    private val kick by BooleanSetting("Kick", true, desc = "Executes the /p kick command.").withDependency { showSettings }
    private val coinFlip by BooleanSetting("Coinflip (cf)", true, desc = "Sends the result of a coinflip..").withDependency { showSettings }
    private val eightBall by BooleanSetting("Eightball", true, desc = "Sends a random 8ball response.").withDependency { showSettings }
    private val dice by BooleanSetting("Dice", true, desc = "Rolls a dice.").withDependency { showSettings }
    private val partyTransfer by BooleanSetting("Party transfer (pt)", false, desc = "Executes the /party transfer command.").withDependency { showSettings }
    private val ping by BooleanSetting("Ping", true, desc = "Sends your current Ping.").withDependency { showSettings }
    private val tps by BooleanSetting("Tps", true, desc = "Sends your server's current TPS.").withDependency { showSettings }
    private val fps by BooleanSetting("FPS", true, desc = "Sends your current FPS.").withDependency { showSettings }
    private val invite by BooleanSetting("Invite", true, desc = "Invites the player to your party.").withDependency { showSettings }
    private val autoConfirm by BooleanSetting("Auto Confirm", false, desc = "Removes the need to confirm a party invite with the !invite command.").withDependency { showSettings && invite }
    private val racism by BooleanSetting("Racism", false, desc = "Sends a random racism percentage.").withDependency { showSettings }
    private val queInstance by BooleanSetting("Queue instance cmds", true, desc = "Queue dungeons commands.").withDependency { showSettings }
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

        val result = messageRegex.find(content.string.noControlCodes) ?: return
        val channel = when(result.value.split(" ")[0]) {
            "From" -> if (!privateChatCommands) return else ChatChannel.PRIVATE
            "Party" -> if (!partyChatCommands)  return else ChatChannel.PARTY
            "Guild" -> if (!guildChatCommands)  return else ChatChannel.GUILD
            else -> return
        }

        val ign = result.groups[2]?.value ?: result.groups[5]?.value ?: result.groups[9]?.value ?: return
        val msg = result.groups[3]?.value ?: result.groups[7]?.value ?: result.groups[10]?.value ?: return
        if (!msg.startsWith("!")) return

        handleChatCommands(msg, ign, channel)
    }

    @EventHandler
    fun onMessageSent(event: MessageSentEvent) {
        if (!chatEmotes || (event.message.startsWith("/") && !listOf("/pc", "/ac", "/gc", "/msg", "/w", "/r").any { event.message.startsWith(it) })) return

        var replaced = false
        val words = event.message.split(" ").toMutableList()

        for (i in words.indices) {
            replacements[words[i]]?.let {
                replaced = true
                words[i] = it
            }
        }

        if (!replaced) return

        event.cancel()
        sendChatMessage(words.joinToString(" "))
    }

    private fun handleChatCommands(message: String, name: String, channel: ChatChannel) {
        val commandsMap = when (channel) {
            ChatChannel.PARTY -> mapOf (
                "coords" to coords, "odin" to odin, "boop" to boop, "kick" to kick, "cf" to coinFlip, "8ball" to eightBall, "dice" to dice, "racism" to racism, "tps" to tps, "warp" to partyWarp,
                "allinvite" to partyAllInvite, "pt" to partyTransfer, "m?" to queInstance, "f?" to queInstance, "t?" to queInstance, "time" to time,
                "demote" to partyDemote, "promote" to partyPromote
            )
            ChatChannel.GUILD -> mapOf ("coords" to coords, "odin" to odin, "boop" to boop, "cf" to coinFlip, "8ball" to eightBall, "dice" to dice, "racism" to racism, "ping" to ping, "tps" to tps, "time" to time)
            ChatChannel.PRIVATE -> mapOf ("coords" to coords, "odin" to odin, "boop" to boop, "cf" to coinFlip, "8ball" to eightBall, "dice" to dice, "racism" to racism, "ping" to ping, "tps" to tps, "invite" to invite, "time" to time)
        }

        val words = message.drop(1).split(" ").map { it.lowercase() }

        when (words[0]) {
            "help", "h" -> channelMessage("Commands: ${commandsMap.filterValues { it }.keys.joinToString(", ")}", name, channel)
            "odin", "od" -> if (odin) channelMessage("Odin! https://discord.gg/2nCbC9hkxT", name, channel)
            "coords", "co" -> if (coords) channelMessage(getPositionString(), name, channel)

            "boop" -> if (boop) words.getOrNull(1)?.let { sendCommand("boop $it") }
            "cf" -> if (coinFlip) channelMessage(if (Math.random() < 0.5) "heads" else "tails", name, channel)
            "8ball" -> if (eightBall) channelMessage(responses.random(), name, channel)
            "dice" -> if (dice) channelMessage((1..6).random(), name, channel)
            "racism" -> if (racism) channelMessage("$name is ${Random.nextInt(1, 101)}% racist. Racism is not allowed!", name, channel)
            "ping" -> if (ping) channelMessage("Current Ping: ${ServerUtils.currentPing}ms", name, channel)
            "tps" -> if (tps) channelMessage("Current TPS: ${ServerUtils.averageTps.toInt()}", name, channel)
            "fps" -> if (fps) channelMessage("Current FPS: ${mc.currentFps}", name, channel)
            "time" -> if (time) channelMessage("Current Time: ${ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"))}", name, channel)
            "location" -> if (location) channelMessage("Current Location: ${LocationUtils.currentArea.displayName}", name, channel)
            "holding" -> if (holding) channelMessage("Holding: ${mc.player?.mainHandStack?.name?.string?.noControlCodes ?: "Nothing :("}", name, channel)

            // party commands

            "warp", "w" ->
                if (channel == ChatChannel.PARTY && partyWarp) sendCommand("party warp")

            "allinvite", "allinv" ->
                if (channel == ChatChannel.PARTY && partyAllInvite) sendCommand("party settings allinvite")

            "pt", "ptme", "transfer" ->
                if (channel == ChatChannel.PARTY && partyTransfer) sendCommand("party transfer $name")

            "promote" ->
                if (channel == ChatChannel.PARTY && partyPromote) sendCommand("party promote $name")

            "demote" ->
                if (channel == ChatChannel.PARTY && partyDemote) sendCommand("party demote $name")

            "kick", "k" ->
                if (channel == ChatChannel.PARTY && kick) words.getOrNull(1)?.let { sendCommand("p kick $it") }

            "f1", "f2", "f3", "f4", "f5", "f6", "f7", "m1", "m2", "m3", "m4", "m5", "m6", "m7", "t1", "t2", "t3", "t4", "t5" -> {
                if (!queInstance || channel != ChatChannel.PARTY) return
                modMessage("§8Entering -> §e${words[0].capitalizeFirst()}")
                sendCommand("odin ${words[0].lowercase()}")
            }

            // private commands

            "invite", "inv" -> if (invite && channel == ChatChannel.PRIVATE) {
                if (autoConfirm) return sendCommand("p invite $name")
                modMessage("§aClick on this message to invite $name to your party!", chatStyle = createClickStyle("/party invite $name"))
                playSoundAtPlayer(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value())
            }
        }
    }

    private fun channelMessage(message: Any, name: String, channel: ChatChannel) {
        when (channel) {
            ChatChannel.GUILD -> sendCommand("g chat $message")
            ChatChannel.PARTY -> sendCommand("p chat $message")
            ChatChannel.PRIVATE -> sendCommand("msg $name $message")
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

    private val replacements = mapOf(
        "<3" to "❤",
        "o/" to "( ﾟ◡ﾟ)/",
        ":star:" to "✮",
        ":yes:" to "✔",
        ":no:" to "✖",
        ":java:" to "☕",
        ":arrow:" to "➜",
        ":shrug:" to "¯\\_(\u30c4)_/¯",
        ":tableflip:" to "(╯°□°）╯︵ ┻━┻",
        ":totem:" to "☉_☉",
        ":typing:" to "✎...",
        ":maths:" to "√(π+x)=L",
        ":snail:" to "@'-'",
        "ez" to "ｅｚ",
        ":thinking:" to "(0.o?)",
        ":gimme:" to "༼つ◕_◕༽つ",
        ":wizard:" to "('-')⊃━☆ﾟ.*･｡ﾟ",
        ":pvp:" to "⚔",
        ":peace:" to "✌",
        ":puffer:" to "<('O')>",
        "h/" to "ヽ(^◇^*)/",
        ":sloth:" to "(・⊝・)",
        ":dog:" to "(ᵔᴥᵔ)",
        ":dj:" to "ヽ(⌐■_■)ノ♬",
        ":yey:" to "ヽ (◕◡◕) ﾉ",
        ":snow:" to "☃",
        ":dab:" to "<o/",
        ":cat:" to "= ＾● ⋏ ●＾ =",
        ":cute:" to "(✿◠‿◠)",
        ":skull:" to "☠",
        ":bum:" to "♿"
    )

    private enum class ChatChannel {
        PARTY, GUILD, PRIVATE
    }
}