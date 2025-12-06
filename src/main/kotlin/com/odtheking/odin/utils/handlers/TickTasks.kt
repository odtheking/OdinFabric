package com.odtheking.odin.utils.handlers

import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.events.core.onReceive
import com.odtheking.odin.utils.logError
import net.minecraft.network.protocol.common.ClientboundPingPacket
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

fun schedule(ticks: Int, serverTick: Boolean = false, task: () -> Unit) {
    lateinit var tickTask: TickTask
    tickTask = object : TickTask(ticks, serverTick, {
        task()
        TickTasks.unregister(tickTask)
    }) {}
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

    init {
        on<TickEvent.End> {
            for (task in clientTickTasks) task.run()
        }

        onReceive<ClientboundPingPacket> {
            if (id != 0) for (task in serverTickTasks) task.run()
        }
    }
}
