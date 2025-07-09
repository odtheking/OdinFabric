package me.odinmod.odin.utils

import me.odinmod.odin.events.PacketEvent
import meteordevelopment.orbit.EventHandler
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket
import net.minecraft.network.packet.s2c.query.PingResultS2CPacket
import net.minecraft.util.Util
import java.util.*

object ServerUtils {
    private var prevTime = 0L
    var averageTps = 20f
        private set

    var currentPing: Int = 0
        private set

    var averagePing: Int = 0
        private set

    private val pingHistory = LinkedList<Int>()
    private const val PING_HISTORY_SIZE = 10

    @EventHandler
    fun onPacketReceive(event: PacketEvent.Receive) = with (event.packet) {
        when (this) {
            is WorldTimeUpdateS2CPacket -> {
                if (prevTime != 0L)
                    averageTps = (20000f / (System.currentTimeMillis() - prevTime + 1)).coerceIn(0f, 20f)

                prevTime = System.currentTimeMillis()
            }

            is PingResultS2CPacket -> {
                val newPing = (Util.getMeasuringTimeMs() - startTime()).toInt().coerceAtLeast(0)
                currentPing = newPing

                pingHistory.add(newPing)
                if (pingHistory.size > PING_HISTORY_SIZE) pingHistory.removeFirst()

                averagePing = if (pingHistory.isNotEmpty()) ArrayList(pingHistory).sum() / pingHistory.size else newPing
            }
        }
    }
}