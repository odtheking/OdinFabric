package io.github.odtheking.odin.utils.handlers

import io.github.odtheking.odin.events.PacketEvent
import io.github.odtheking.odin.events.TickEvent
import io.github.odtheking.odin.utils.logError
import meteordevelopment.orbit.EventHandler
import net.minecraft.network.packet.s2c.common.CommonPingS2CPacket
import java.util.concurrent.CopyOnWriteArrayList

open class TickTask(
    private val ticksPerCycle: Int,
    serverTick: Boolean = false,
    private val task: () -> Unit
) {
    init {
        if (serverTick) TickTasks.registerServerTask(this)
        else TickTasks.registerClientTask(this)
    }

    var ticks = 0

    open fun run() {
        if (++ticks < ticksPerCycle) return
        runCatching(task).onFailure { logError(it, this) }
        ticks = 0
    }
}

class LimitedTickTask(
    ticksPerCycle: Int,
    private val maxCycles: Int,
    serverTick: Boolean = false,
    task: () -> Unit
) : TickTask(ticksPerCycle, serverTick, task) {

    private var cycleCount = 0

    override fun run() {
        if (cycleCount >= maxCycles) return
        super.run()

        if ((ticks == 0)) cycleCount++
        if (cycleCount >= maxCycles) TickTasks.unregister(this)
    }
}

object TickTasks {
    private val clientTickTasks = CopyOnWriteArrayList<TickTask>()
    private val serverTickTasks = CopyOnWriteArrayList<TickTask>()

    fun registerClientTask(task: TickTask) {
        clientTickTasks.add(task)
    }

    fun registerServerTask(task: TickTask) {
        serverTickTasks.add(task)
    }

    fun unregister(task: TickTask) {
        clientTickTasks.remove(task)
        serverTickTasks.remove(task)
    }

    @EventHandler
    fun onTick(event: TickEvent.End) {
        for (task in clientTickTasks) task.run()
    }

    @EventHandler
    fun onServerTick(event: PacketEvent.Receive) {
        if (event.packet is CommonPingS2CPacket)
            for (task in serverTickTasks) task.run()
    }
}
