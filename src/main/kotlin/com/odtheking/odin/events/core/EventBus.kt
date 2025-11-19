package com.odtheking.odin.events.core

import com.odtheking.odin.events.PacketEvent
import net.minecraft.network.protocol.Packet
import net.minecraft.util.profiling.Profiler
import net.minecraft.util.profiling.ProfilerFiller
import java.util.concurrent.ConcurrentHashMap

object EventBus {

    @JvmField
    internal val listenerArrays = mutableMapOf<Class<out Event>, Array<ListenerEntry<*>>>()
    @JvmField
    internal val activeSubscribers = mutableSetOf<Any>()
    @JvmField
    internal val subscriberClasses = mutableMapOf<Any, Class<*>>()
    @JvmField
    internal val invokers = ConcurrentHashMap<Class<out Event>, Invoker>()

    fun subscribe(subscriber: Any) {
        if (activeSubscribers.add(subscriber)) {
            subscriberClasses[subscriber] = subscriber.javaClass
            rebuildAffectedCaches(subscriber)
        }
    }

    fun unsubscribe(subscriber: Any) {
        if (activeSubscribers.remove(subscriber)) {
            subscriberClasses.remove(subscriber)
            rebuildAffectedCaches(subscriber)
        }
    }

    fun <T : Event> post(event: T) {
        val profiler = Profiler.get()
        profiler.push("Odin: ${event.javaClass.simpleName}")
        try {
            invokers[event.javaClass]?.invoke(event, profiler)
        } finally {
            profiler.pop()
        }
    }

    fun <T : Event> registerListener(
        subscriber: Class<*>,
        eventClass: Class<T>,
        priority: Int,
        ignoreCancelled: Boolean,
        handler: (T) -> Unit
    ) {
        val subscriberName = subscriber.simpleName ?: "Unknown"
        val entry = ListenerEntry(subscriber, EventListener(priority, ignoreCancelled, subscriberName, handler))

        val existing = listenerArrays[eventClass] ?: emptyArray()
        val newArray = (existing + entry).sortedByDescending { it.listener.priority }.toTypedArray()
        listenerArrays[eventClass] = newArray

        rebuildInvoker(eventClass, newArray)
    }

    private fun rebuildAffectedCaches(subscriber: Any) {
        val subscriberClass = subscriber::class.java
        for ((eventClass, listeners) in listenerArrays) {
            if (listeners.any { it.subscriber == subscriberClass }) rebuildInvoker(eventClass, listeners)
        }
    }

    private fun rebuildInvoker(eventClass: Class<out Event>, allListeners: Array<ListenerEntry<*>>) {
        val activeSubscriberClasses = activeSubscribers.mapNotNull { subscriberClasses[it] }.toSet()
        @Suppress("UNCHECKED_CAST")
        val activeListeners = allListeners
            .filter { it.subscriber in activeSubscriberClasses }
            .map { it.listener as EventListener<Event> }
            .toTypedArray()

        if (activeListeners.isEmpty()) {
            invokers[eventClass] = EmptyInvoker
            return
        }

        invokers[eventClass] = InvokerFactory.build(activeListeners)
    }

    data class ListenerEntry<T : Event>(
        val subscriber: Class<*>,
        val listener: EventListener<T>
    )

    class EventListener<T : Event>(
        val priority: Int,
        val ignoreCancelled: Boolean,
        val subscriberName: String,
        val handler: (T) -> Unit
    ) {
        fun invoke(event: T) {
            if (!ignoreCancelled || event !is CancellableEvent || !event.isCancelled)
                handler(event)
        }
    }

    interface Invoker {
        fun invoke(event: Event, profiler: ProfilerFiller)
    }

    private object EmptyInvoker : Invoker {
        override fun invoke(event: Event, profiler: ProfilerFiller) {}
    }

    private object InvokerFactory {
        fun build(listeners: Array<EventListener<Event>>): Invoker {
            if (listeners.isEmpty()) return EmptyInvoker

            return object : Invoker {
                override fun invoke(event: Event, profiler: ProfilerFiller) {
                    for (listener in listeners) {
                        profiler.push(listener.subscriberName)
                        try {
                            listener.invoke(event)
                        } finally {
                            profiler.pop()
                        }
                    }
                }
            }
        }
    }
}

inline fun <reified T : Event> Any.on(
    priority: Int = 0,
    ignoreCancelled: Boolean = false,
    noinline handler: T.() -> Unit
) = EventBus.registerListener(this.javaClass, T::class.java, priority, ignoreCancelled) { it.handler() }

inline fun <reified P : Packet<*>> Any.onReceive(
    priority: Int = 0,
    ignoreCancelled: Boolean = false,
    noinline handler: P.(PacketEvent.Receive) -> Unit
) = EventBus.registerListener(this.javaClass, PacketEvent.Receive::class.java, priority, ignoreCancelled) {
    (it.packet as? P)?.handler(it)
}

inline fun <reified P : Packet<*>> Any.onSend(
    priority: Int = 0,
    ignoreCancelled: Boolean = false,
    noinline handler: P.(PacketEvent.Send) -> Unit
) = EventBus.registerListener(this.javaClass, PacketEvent.Send::class.java, priority, ignoreCancelled) {
    (it.packet as? P)?.handler(it)
}