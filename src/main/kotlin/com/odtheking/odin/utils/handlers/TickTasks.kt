package com.odtheking.odin.utils.handlers

import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.utils.logError
import com.odtheking.odin.utils.modMessage

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
        if (ticks == tickDelay) {
            runCatching(task).onFailure { logError(it, this) }
            ticks = 0
        } else ticks++
    }
}

class OneShotTickTask(ticks: Int, serverTick: Boolean = false, task: () -> Unit) : TickTask(ticks, serverTick, task)

fun schedule(ticks: Int, serverTick: Boolean = false, task: () -> Unit) {
    OneShotTickTask(ticks, serverTick, task)
}

object TickTasks {
    private val clientTickTasks = mutableListOf<TickTask>()
    private val serverTickTasks = mutableListOf<TickTask>()

    fun registerClientTask(task: TickTask) = clientTickTasks.add(task)
    fun registerServerTask(task: TickTask) = serverTickTasks.add(task)

    private fun MutableList<TickTask>.runTasks() {
        forEach { task ->
            task.run()
            removeIf { task is OneShotTickTask && task.ticks == 0 }
        }
    }

    init {
        on<TickEvent.End> { clientTickTasks.runTasks() }
        on<TickEvent.Server> { serverTickTasks.runTasks() }
    }
}
