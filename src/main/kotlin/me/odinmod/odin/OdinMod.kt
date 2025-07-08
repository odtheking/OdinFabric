package me.odinmod.odin

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import me.odinmod.odin.commands.*
import me.odinmod.odin.config.Config
import me.odinmod.odin.events.EventDispatcher
import me.odinmod.odin.features.ModuleManager
import me.odinmod.odin.features.impl.render.RenderTest
import me.odinmod.odin.utils.ServerUtils
import me.odinmod.odin.utils.handlers.MobCaches
import me.odinmod.odin.utils.handlers.TickTasks
import me.odinmod.odin.utils.sendDataToServer
import me.odinmod.odin.utils.skyblock.KuudraUtils
import me.odinmod.odin.utils.skyblock.LocationUtils
import me.odinmod.odin.utils.skyblock.SkyblockPlayer
import meteordevelopment.orbit.EventBus
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.Version
import net.fabricmc.loader.api.metadata.ModMetadata
import net.minecraft.client.MinecraftClient
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.lang.invoke.MethodHandles
import kotlin.coroutines.EmptyCoroutineContext

object OdinMod : ModInitializer {

    @JvmField
    val mc: MinecraftClient = MinecraftClient.getInstance()

    @JvmField
    val EVENT_BUS = EventBus()

    const val MOD_ID = "odining"
    private val metadata: ModMetadata by lazy { FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow().metadata }
    val version: Version by lazy { metadata.version }
    val logger: Logger = LogManager.getLogger("Odin")

    val scope = CoroutineScope(SupervisorJob() + EmptyCoroutineContext)

    override fun onInitialize() {
        EVENT_BUS.registerLambdaFactory("me.odinmod") { lookupInMethod, klass ->
            lookupInMethod.invoke(null, klass, MethodHandles.lookup()) as MethodHandles.Lookup
        }

        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            arrayOf(mainCommand, petCommand, devCommand, waypointCommand, highlightCommand).forEach { commodore ->
                commodore.register(dispatcher)
            }
        }

        listOf(
            this, LocationUtils, TickTasks, KuudraUtils,
            SkyblockPlayer, MobCaches, RenderTest,
            ServerUtils, EventDispatcher, ModuleManager
        ).forEach { EVENT_BUS.subscribe(it) }

        Config.load()

        val name = mc.session?.username?.takeIf { !it.matches(Regex("Player\\d{2,3}")) } ?: return
        sendDataToServer(body = """{"username": "$name", "version": "Fabric $version"}""")
    }
}