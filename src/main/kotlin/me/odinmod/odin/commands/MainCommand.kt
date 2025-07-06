package me.odinmod.odin.commands

import com.github.stivais.commodore.Commodore
import com.github.stivais.commodore.parsers.CommandParsable
import com.github.stivais.commodore.utils.GreedyString
import me.odinmod.odin.OdinMod.mc
import me.odinmod.odin.clickgui.ClickGUI
import me.odinmod.odin.clickgui.HudManager
import me.odinmod.odin.events.PacketEvent
import me.odinmod.odin.features.impl.render.ClickGUIModule
import me.odinmod.odin.utils.*
import me.odinmod.odin.utils.handlers.LimitedTickTask
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket
import net.minecraft.text.Text

val mainCommand = Commodore("odin") {
    runs {
        LimitedTickTask(0, 1) { mc.setScreen(ClickGUI) }
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

    literal("simulate").runs { greedyString: GreedyString ->
        PacketEvent.Receive(GameMessageS2CPacket(Text.literal(greedyString.string), false)).postAndCatch()
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