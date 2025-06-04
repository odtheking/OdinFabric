package me.odinmod.odin

import me.odinmod.odin.events.PacketEvent
import meteordevelopment.orbit.EventBus
import meteordevelopment.orbit.EventHandler
import net.fabricmc.api.ModInitializer
import java.lang.invoke.MethodHandles

class OdinMod : ModInitializer {

    override fun onInitialize() {
        EVENT_BUS.registerLambdaFactory("me.odinmod") { lookupInMethod, klass ->
            lookupInMethod.invoke(null, klass, MethodHandles.lookup()) as MethodHandles.Lookup
        }
        EVENT_BUS.subscribe(this)
    }

    @EventHandler
    fun onPacket(event: PacketEvent.Receive) {
        println(event.packet)
    }

    companion object {
        val EVENT_BUS = EventBus()
    }
}