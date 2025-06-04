package me.odinmod.odin

import me.odinmod.odin.events.EventDispatcher
import me.odinmod.odin.events.PacketEvent
import me.odinmod.odin.utils.handlers.TickTasks
import me.odinmod.odin.utils.skyblock.LocationUtils
import me.odinmod.odin.utils.skyblock.SkyblockPlayer
import meteordevelopment.orbit.EventBus
import meteordevelopment.orbit.EventHandler
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.Version
import net.fabricmc.loader.api.metadata.ModMetadata
import net.minecraft.client.MinecraftClient
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.lang.invoke.MethodHandles

class OdinMod : ModInitializer {

    override fun onInitialize() {
        EVENT_BUS.registerLambdaFactory("me.odinmod") { lookupInMethod, klass ->
            lookupInMethod.invoke(null, klass, MethodHandles.lookup()) as MethodHandles.Lookup
        }

        EventDispatcher

        listOf(
            this, LocationUtils, TickTasks, SkyblockPlayer
        ).forEach { EVENT_BUS.subscribe(it) }
    }

    @EventHandler
    fun onPacket(event: PacketEvent.Receive) {
//        modMessage(event.packet.toString())
    }


    companion object {
        @JvmField
        val mc: MinecraftClient = MinecraftClient.getInstance()

        @JvmField
        val EVENT_BUS = EventBus()

        private const val MOD_ID = "odining"
        private val metadata: ModMetadata by lazy {
            FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow().metadata
        }
        val version: Version by lazy { metadata.version }
        val logger: Logger = LogManager.getLogger("Odin")
    }
}