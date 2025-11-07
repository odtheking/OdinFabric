package com.odtheking.odin.events.core

import com.odtheking.odin.events.PacketEvent
import net.minecraft.network.packet.Packet
import net.minecraft.util.profiler.Profilers
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference
import kotlin.reflect.KClass

object EventBus {


    @JvmField
    internal val listenerArrays = ConcurrentHashMap<KClass<out Event>, AtomicReference<Array<ListenerEntry<*>>>>()
    @JvmField
    internal val invokers = ConcurrentHashMap<KClass<out Event>, Invoker>()
    @JvmField
    internal val activeSubscribers = ConcurrentHashMap.newKeySet<Any>()
    @JvmField
    internal val subscriberClasses = ConcurrentHashMap<Any, KClass<*>>()

    private val profiler = Profilers.get()

    fun subscribe(subscriber: Any) {
        if (activeSubscribers.add(subscriber)) {
            subscriberClasses[subscriber] = subscriber::class
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
        profiler.push(event::class.simpleName ?: "Event")
        try {
            invokers[event::class]?.invoke(event)
        } finally {
            profiler.pop()
        }
    }

    fun <T : Event> registerListener(
        subscriber: KClass<*>,
        eventClass: KClass<T>,
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
        val subscriberClass = subscriber::class
        for ((eventClass, ref) in listenerArrays) {
            val listeners = ref.get()
            if (listeners.any { it.subscriber == subscriberClass }) {
                rebuildInvoker(eventClass, listeners)
            }
        }
    }

    private fun rebuildInvoker(eventClass: KClass<out Event>, allListeners: Array<ListenerEntry<*>>) {
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
        val subscriber: KClass<*>,
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
        fun invoke(event: Event)
    }

    private object EmptyInvoker : Invoker {
        override fun invoke(event: Event) {}
    }

    private object InvokerFactory {
        fun build(listeners: Array<EventListener<Event>>): Invoker {
            if (listeners.isEmpty()) return EmptyInvoker

            return object : Invoker {
                override fun invoke(event: Event) {
                    for (listener in listeners) {
                        profiler.push("Odin: ${listener.subscriberName}")
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
) = EventBus.registerListener(this::class, T::class, priority, ignoreCancelled) { it.handler() }

inline fun <reified P : Packet<*>> Any.onReceive(
    priority: Int = 0,
    ignoreCancelled: Boolean = false,
    noinline handler: P.(PacketEvent.Receive) -> Unit
) = EventBus.registerListener(this::class, PacketEvent.Receive::class, priority, ignoreCancelled) {
    (it.packet as? P)?.handler(it)
}

inline fun <reified P : Packet<*>> Any.onSend(
    priority: Int = 0,
    ignoreCancelled: Boolean = false,
    noinline handler: P.(PacketEvent.Send) -> Unit
) = EventBus.registerListener(this::class, PacketEvent.Send::class, priority, ignoreCancelled) {
    (it.packet as? P)?.handler(it)
}