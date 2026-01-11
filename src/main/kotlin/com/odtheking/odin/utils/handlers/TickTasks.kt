package com.odtheking.odin.utils.handlers

import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.utils.logError
import it.unimi.dsi.fastutil.objects.ObjectArrayList

open class TickTask(
    private val tickDelay: Int,
    serverTick: Boolean = false,
    private val task: () -> Unit
) {
    internal var ticks = 0

    init {
        if (serverTick) TickTasks.registerServerTask(this)
        else TickTasks.registerClientTask(this)
    }

    fun run() {
        if (++ticks < tickDelay) return
        runCatching(task).onFailure { logError(it, this) }
        ticks = 0
    }
}

class OneShotTickTask(ticks: Int, serverTick: Boolean = false, task: () -> Unit) : TickTask(ticks, serverTick, task)

fun schedule(ticks: Int, serverTick: Boolean = false, task: () -> Unit) {
    OneShotTickTask(ticks, serverTick, task)
}

object TickTasks {
    private val clientTickTasks = ObjectArrayList<TickTask>()
    private val serverTickTasks = ObjectArrayList<TickTask>()

    fun registerClientTask(task: TickTask) = clientTickTasks.add(task)
    fun registerServerTask(task: TickTask) = serverTickTasks.add(task)

    private fun ObjectArrayList<TickTask>.runTasks() {
        for (i in indices.reversed()) {
            val task = this[i]
            task.run()
            if (task is OneShotTickTask && task.ticks == 0) removeAt(i)
        }
    }

    init {
        on<TickEvent.End> { clientTickTasks.runTasks() }
        on<TickEvent.Server> { serverTickTasks.runTasks() }
    }
}
