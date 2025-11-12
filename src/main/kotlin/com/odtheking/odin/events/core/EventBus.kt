package com.odtheking.odin.events.core

import com.odtheking.odin.events.PacketEvent
import net.minecraft.network.protocol.Packet
import net.minecraft.util.profiling.Profiler
import net.minecraft.util.profiling.ProfilerFiller
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

object EventBus {

    @JvmField
    internal val listenerArrays = ConcurrentHashMap<Class<out Event>, AtomicReference<Array<ListenerEntry<*>>>>()
    @JvmField
    internal val invokers = ConcurrentHashMap<Class<out Event>, Invoker>()
    @JvmField
    internal val activeSubscribers = ConcurrentHashMap.newKeySet<Any>()
    @JvmField
    internal val subscriberClasses = ConcurrentHashMap<Any, Class<*>>()

    fun subscribe(subscriber: Any) {
        if (activeSubscribers.add(subscriber)) {
            subscriberClasses[subscriber] = subscriber::class.java
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
        profiler.push("Odin: ${event::class.simpleName}")
        try {
            invokers[event::class.java]?.invoke(event, profiler)
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
        val ref = listenerArrays.computeIfAbsent(eventClass) { AtomicReference(emptyArray()) }
        val current = ref.get()
        val newArray = (current + entry).sortedByDescending { it.listener.priority }.toTypedArray()
        ref.set(newArray)
        rebuildInvoker(eventClass, newArray)
    }

    private fun rebuildAffectedCaches(subscriber: Any) {
        val subscriberClass = subscriber::class.java
        for ((eventClass, ref) in listenerArrays) {
            val listeners = ref.get()
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
) = EventBus.registerListener(this::class.java, T::class.java, priority, ignoreCancelled) { it.handler() }

inline fun <reified P : Packet<*>> Any.onReceive(
    priority: Int = 0,
    ignoreCancelled: Boolean = false,
    noinline handler: P.(PacketEvent.Receive) -> Unit
) = EventBus.registerListener(this::class.java, PacketEvent.Receive::class.java, priority, ignoreCancelled) {
    (it.packet as? P)?.handler(it)
}

inline fun <reified P : Packet<*>> Any.onSend(
    priority: Int = 0,
    ignoreCancelled: Boolean = false,
    noinline handler: P.(PacketEvent.Send) -> Unit
) = EventBus.registerListener(this::class.java, PacketEvent.Send::class.java, priority, ignoreCancelled) {
    (it.packet as? P)?.handler(it)
}