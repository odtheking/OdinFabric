package me.odinmod.odin

import me.odinmod.odin.events.EventDispatcher
import me.odinmod.odin.events.PacketEvent
import me.odinmod.odin.utils.skyblock.LocationUtils
import me.odinmod.odin.utils.skyblock.SkyblockPlayer
import meteordevelopment.orbit.EventBus
import meteordevelopment.orbit.EventHandler
import net.fabricmc.api.ModInitializer
import net.minecraft.client.MinecraftClient
import java.lang.invoke.MethodHandles

class OdinMod : ModInitializer {

    override fun onInitialize() {
        EVENT_BUS.registerLambdaFactory("me.odinmod") { lookupInMethod, klass ->
            lookupInMethod.invoke(null, klass, MethodHandles.lookup()) as MethodHandles.Lookup
        }

        EventDispatcher

        listOf(
            this, LocationUtils, SkyblockPlayer
        ).forEach { EVENT_BUS.subscribe(it) }
    }

    @EventHandler
    fun onPacket(event: PacketEvent.Receive) {
    //    println(event.packet)
    }

    companion object {
        @JvmField
        val mc: MinecraftClient = MinecraftClient.getInstance()

        @JvmField
        val EVENT_BUS = EventBus()
    }
}