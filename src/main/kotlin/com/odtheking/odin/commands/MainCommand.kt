package com.odtheking.odin.commands

import com.github.stivais.commodore.Commodore
import com.github.stivais.commodore.parsers.CommandParsable
import com.github.stivais.commodore.utils.GreedyString
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.ClickGUI
import com.odtheking.odin.clickgui.HudManager
import com.odtheking.odin.features.impl.render.ClickGUIModule
import com.odtheking.odin.utils.*
import com.odtheking.odin.utils.handlers.schedule
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils

val mainCommand = Commodore("odin", "od") {
    runs {
        schedule(0) { mc.setScreen(ClickGUI) }
    }

    literal("edithud").runs {
        schedule(0) { mc.setScreen(HudManager) }
    }

    literal("reset") {
        literal("clickgui").runs {
            ClickGUIModule.resetPositions()
            modMessage("Reset click gui positions.")
        }
        literal("hud").runs {
            HudManager.resetHUDS()
            modMessage("Reset HUD positions.")
        }
    }

    literal("copy").runs { greedyString: GreedyString ->
        setClipboardContent(greedyString.string)
    }

    literal("ep").runs {
        fillItemFromSack(16, "ENDER_PEARL", "ender_pearl", true)
    }

    literal("ij").runs {
        fillItemFromSack(64, "INFLATABLE_JERRY", "inflatable_jerry", true)
    }

    literal("sl").runs {
        fillItemFromSack(16, "SPIRIT_LEAP", "spirit_leap", true)
    }

    literal("sb").runs {
        fillItemFromSack(64, "SUPERBOOM_TNT", "superboom_tnt", true)
    }

    literal("sendcoords").runs { message: GreedyString? ->
        sendChatMessage(getPositionString() + if (message == null) "" else " ${message.string}")
    }

    literal("leaporder").runs { player1: String?, player2: String?, player3: String?, player4: String? ->
        val players = listOf(player1, player2, player3, player4).mapNotNull { it?.lowercase() }
        DungeonUtils.customLeapOrder = players
        modMessage("§aCustom leap order set to: §f${player1}, ${player2}, ${player3}, $player4")
    }

    runs { floor: Floors -> sendCommand("joininstance ${floor.instance()}") }
    runs { tier: KuudraTier -> sendCommand("joininstance ${tier.instance()}") }
}

@CommandParsable
private enum class Floors {
    F1, F2, F3, F4, F5, F6, F7, M1, M2, M3, M4, M5, M6, M7;

    private val floors = listOf("one", "two", "three", "four", "five", "six", "seven")
    fun instance() = "${if (ordinal > 6) "master_" else ""}catacombs_floor_${floors[(ordinal % 7)]}"
}

@CommandParsable
private enum class KuudraTier(private val test: String) {
    T1("normal"), T2("hot"), T3("burning"), T4("fiery"), T5("infernal");

    fun instance() = "kuudra_${test}"
}