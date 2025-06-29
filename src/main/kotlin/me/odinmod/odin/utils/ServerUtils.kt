package me.odinmod.odin.utils

import me.odinmod.odin.OdinMod.mc
import me.odinmod.odin.events.PacketEvent
import me.odinmod.odin.utils.handlers.TickTask
import meteordevelopment.orbit.EventHandler
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket
import net.minecraft.network.packet.s2c.play.StatisticsS2CPacket
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket

object ServerUtils {
    private var prevTime = 0L
    var averageTps = 20.0
    var averagePing = 0.0
    private var isPinging = false
    private var pingStartTime = 0L

    private val packets = ArrayList<Packet<*>>()

    @JvmStatic
    fun handleSendPacket(packet: Packet<*>): Boolean {
        return packets.remove(packet)
    }

    private fun sendPacketNoEvent(packet: Packet<*>) {
        packets.add(packet)
        mc.networkHandler?.sendPacket(packet)
    }

    init {
        TickTask(2) {
            sendPing()
        }
    }

    @EventHandler
    fun onPacketReceive(event: PacketEvent.Receive) {
        when (event.packet) {
            is StatisticsS2CPacket -> averagePing = (System.nanoTime() - pingStartTime) / 1e6
            is GameJoinS2CPacket -> averagePing = 0.0
            is WorldTimeUpdateS2CPacket -> {
                if (prevTime != 0L)
                    averageTps = (20_000.0 / (System.currentTimeMillis() - prevTime + 1)).coerceIn(0.0, 20.0)

                prevTime = System.currentTimeMillis()
            }
            else -> return
        }
        isPinging = false
    }

    private fun sendPing() {
        if (isPinging || mc.player == null) return
        if (pingStartTime - System.nanoTime() > 10e6) reset()
        pingStartTime = System.nanoTime()
        isPinging = true
        sendPacketNoEvent(ClientStatusC2SPacket(ClientStatusC2SPacket.Mode.REQUEST_STATS))
    }

    private fun reset() {
        prevTime = 0L
        averageTps = 20.0
        averagePing = 0.0
    }
}