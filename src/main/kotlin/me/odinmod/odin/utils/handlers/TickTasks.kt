package me.odinmod.odin.utils.handlers

import me.odinmod.odin.events.PacketEvent
import me.odinmod.odin.events.TickEvent
import meteordevelopment.orbit.EventHandler
import net.minecraft.network.packet.s2c.common.CommonPingS2CPacket

class TickTask(
    private val ticksPerCycle: Int,
    private val serverTick: Boolean = false,
    private val task: () -> Unit = {}
) {
    init {
        if (serverTick) TickTasks.registerServerTask(this)
        else TickTasks.registerClientTask(this)
    }

    private var ticks = 0

    fun run() {
        if (++ticks < ticksPerCycle) return
        task()
        ticks = 0
    }
}

object TickTasks {
    private val clientTickTasks = ArrayList<TickTask>()
    private val serverTickTasks = ArrayList<TickTask>()

    fun registerClientTask(task: TickTask) {
        clientTickTasks.add(task)
    }

    fun registerServerTask(task: TickTask) {
        serverTickTasks.add(task)
    }

    @EventHandler
    fun onTick(event: TickEvent.Start) {
        clientTickTasks.forEach { it.run() }
    }

    @EventHandler
    fun onServerTick(event: PacketEvent.Receive) {
        if (event.packet !is CommonPingS2CPacket) return
        serverTickTasks.forEach { it.run() }
    }
}