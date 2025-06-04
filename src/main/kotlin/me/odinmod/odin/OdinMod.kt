package me.odinmod.odin

import me.odinmod.odin.events.EventDispatcher
import me.odinmod.odin.events.PacketEvent
import me.odinmod.odin.events.TickEvent
import me.odinmod.odin.utils.handlers.TickTask
import me.odinmod.odin.utils.handlers.TickTasks
import me.odinmod.odin.utils.modMessage
import me.odinmod.odin.utils.skyblock.LocationUtils
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
            this, LocationUtils, TickTasks
        ).forEach { EVENT_BUS.subscribe(it) }
    }

    @EventHandler
    fun onPacket(event: PacketEvent.Receive) {
//        modMessage(event.packet.toString())
    }

    @EventHandler
    fun onTick(event: TickEvent.Start) {
//        modMessage(getBlockAtPos(mc.player?.blockPos?.add(Vec3i(0, -1, 0))))
    }

//    init {
//        var ticks = 0
//        var serverTicks = 0
//        var firstServer = true
//
//        TickTask(0, serverTick = true) {
//            if (firstServer) {
//                firstServer = false
//                TickTask(0) { ticks++ }
//            }
//
//            serverTicks++
//
//            modMessage("$ticks $serverTicks")
//        }
//    }

    companion object {
        @JvmField
        val mc: MinecraftClient = MinecraftClient.getInstance()

        @JvmField
        val EVENT_BUS = EventBus()
    }
}