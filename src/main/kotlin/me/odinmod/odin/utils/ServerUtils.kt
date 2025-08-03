package me.odinmod.odin.utils

import me.odinmod.odin.OdinMod.mc
import me.odinmod.odin.events.PacketEvent
import meteordevelopment.orbit.EventHandler
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket
import net.minecraft.network.packet.s2c.query.PingResultS2CPacket
import net.minecraft.util.Util

object ServerUtils {
    private var prevTime = 0L
    var averageTps = 20f
        private set

    var currentPing: Int = 0
        private set

    var averagePing: Int = 0
        private set


    @EventHandler
    fun onPacketReceive(event: PacketEvent.Receive) = with (event.packet) {
        when (this) {
            is WorldTimeUpdateS2CPacket -> {
                if (prevTime != 0L)
                    averageTps = (20000f / (System.currentTimeMillis() - prevTime + 1)).coerceIn(0f, 20f)

                prevTime = System.currentTimeMillis()
            }

            is PingResultS2CPacket -> {
                currentPing = (Util.getMeasuringTimeMs() - startTime()).toInt().coerceAtLeast(0)

                val log = ArrayList<Long>()
                for (i in 0 until minOf(mc.debugHud.pingLog.dimension, 5)) log.add(mc.debugHud.pingLog.get(i))

                averagePing = log.average().toInt().coerceAtLeast(0)
            }
        }
    }
}