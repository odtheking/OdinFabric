package me.odinmod.odin.features.impl.skyblock

import me.odinmod.odin.clickgui.settings.impl.BooleanSetting
import me.odinmod.odin.clickgui.settings.impl.NumberSetting
import me.odinmod.odin.clickgui.settings.impl.StringSetting
import me.odinmod.odin.events.PacketEvent
import me.odinmod.odin.features.Module
import me.odinmod.odin.utils.Clock
import me.odinmod.odin.utils.alert
import me.odinmod.odin.utils.getPositionString
import me.odinmod.odin.utils.modMessage
import me.odinmod.odin.utils.sendChatMessage
import me.odinmod.odin.utils.sendCommand
import meteordevelopment.orbit.EventHandler
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket
import net.minecraft.text.Text

object MobSpawn: Module(
    name = "Mob Spawn",
    description = "Sends a message whenever a mob spawns."
) {
    private val mobName by StringSetting("Mob Name", "MobName", 40, desc = "Message sent when mob is detected as spawned.")
    private val soundOnly by BooleanSetting("Sound Only", false, desc = "Only plays sound when mob spawns.")
    private val delay by NumberSetting("Time between alerts", 3000L, 10, 10000, 10, desc = "Time between alerts.", unit = "ms"
    )
    private val ac by BooleanSetting("All Chat", false , desc = "Send message in all chat.")
    private val pc by BooleanSetting("Party Chat", false, desc = "Send message in party chat.")

    private val time = Clock(delay)

    @EventHandler
    fun postMeta(event: PacketEvent.Receive) {
        if (event.packet !is EntityTrackerUpdateS2CPacket) return
        val entity = mc.world?.getEntityById(event.packet.id) ?: return
        if (!entity.name.contains(Text.literal(mobName)) || !time.hasTimePassed(delay)) return
        time.update()

        modMessage("§5$mobName has spawned!")
        alert("§5$mobName has spawned!", playSound = !soundOnly)
        if (ac) sendChatMessage("$mobName spawned at: ${getPositionString()}")
        if (pc) sendCommand("p chat $mobName spawned at: ${getPositionString()}")
    }
}