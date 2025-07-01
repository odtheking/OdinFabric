package me.odinmod.odin.utils

import me.odinmod.odin.OdinMod
import me.odinmod.odin.events.PacketEvent
import me.odinmod.odin.utils.handlers.TickTask
import meteordevelopment.orbit.EventHandler
import mixins.ClientPlayNetworkHandlerAccessor
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket

object ServerUtils {
    private var prevTime = 0L
    var averageTps = 20.0

    @JvmField
    var currentPing: Int = 0

    init {
        TickTask(2) {
            (OdinMod.mc.networkHandler as ClientPlayNetworkHandlerAccessor).pingMeasurer.ping()
        }
    }

    @EventHandler
    fun onPacketReceive(event: PacketEvent.Receive) {
        when (event.packet) {
            is WorldTimeUpdateS2CPacket -> {
                if (prevTime != 0L)
                    averageTps = (20_000.0 / (System.currentTimeMillis() - prevTime + 1)).coerceIn(0.0, 20.0)

                prevTime = System.currentTimeMillis()
            }
            else -> return
        }
    }
}