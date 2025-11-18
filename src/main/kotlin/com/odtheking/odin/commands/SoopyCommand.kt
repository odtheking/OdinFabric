package com.odtheking.odin.commands

import com.github.stivais.commodore.Commodore
import com.github.stivais.commodore.utils.SyntaxException
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.OdinMod.scope
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.network.WebUtils.fetchString
import kotlinx.coroutines.launch

val soopyCommand = Commodore("soopycmd", "spcmd", "spc") {
    val commands = listOf(
        "auctions", "bestiary", "bank", "classaverage", "currdungeon", "dojo", "dungeon", "essence", "faction",
        "guildof", "kuudra", "nucleus", "nw", "overflowskillaverage", "overflowskills", "pet", "rtca", "sblvl",
        "secrets", "skillaverage", "skills"
    )

    literal("help").runs {
        modMessage("Available commands for /spcmd:\n ${commands.joinToString()}")
    }

    executable {
        param("command") {
            parser { string: String ->
                if (!commands.contains(string)) throw SyntaxException("Invalid command.")
                string
            }
            suggests { commands }
        }

        runs { command: String, user: String? ->
            val player = user ?: mc.player?.name
            modMessage("Running command...")
            scope.launch {
                fetchString("https://soopy.dev/api/soopyv2/botcommand?m=$command&u=$player").fold(
                    { modMessage(it) },
                    { e -> modMessage("Failed to fetch data: ${e.message}") }
                )
            }
        }
    }
}