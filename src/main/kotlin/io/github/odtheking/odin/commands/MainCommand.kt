package io.github.odtheking.odin.commands

import com.github.stivais.commodore.Commodore
import com.github.stivais.commodore.parsers.CommandParsable
import com.github.stivais.commodore.utils.GreedyString
import io.github.odtheking.odin.OdinMod.mc
import io.github.odtheking.odin.clickgui.ClickGUI
import io.github.odtheking.odin.clickgui.HudManager
import io.github.odtheking.odin.events.PacketEvent
import io.github.odtheking.odin.features.impl.render.ClickGUIModule
import io.github.odtheking.odin.utils.*
import io.github.odtheking.odin.utils.handlers.LimitedTickTask
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket
import net.minecraft.text.Text

val mainCommand = Commodore("odin", "od") {
    runs {
        LimitedTickTask(0, 1) { mc.setScreen(ClickGUI) }
    }

    literal("edithud").runs {
        LimitedTickTask(0, 1) { mc.setScreen(HudManager) }
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

    literal("simulate").runs { greedyString: GreedyString ->
        PacketEvent.Receive(GameMessageS2CPacket(Text.of(greedyString.string), false)).postAndCatch()
        modMessage("§8Simulated message: ${greedyString.string}")
    }

    literal("sendcoords").runs { message: GreedyString? ->
        sendChatMessage(getPositionString() + if (message == null) "" else " ${message.string}")
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