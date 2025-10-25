package com.odtheking.odin

import com.odtheking.odin.commands.*
import com.odtheking.odin.config.Config
import com.odtheking.odin.events.EventDispatcher
import com.odtheking.odin.features.ModuleManager
import com.odtheking.odin.utils.ServerUtils
import com.odtheking.odin.utils.handlers.MobCaches
import com.odtheking.odin.utils.handlers.TickTasks
import com.odtheking.odin.utils.render.item.ItemStateRenderer
import com.odtheking.odin.utils.sendDataToServer
import com.odtheking.odin.utils.skyblock.KuudraUtils
import com.odtheking.odin.utils.skyblock.LocationUtils
import com.odtheking.odin.utils.skyblock.SkyblockPlayer
import com.odtheking.odin.utils.skyblock.SplitsManager
import com.odtheking.odin.utils.skyblock.dungeon.DungeonListener
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.ScanUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import meteordevelopment.orbit.EventBus
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.rendering.v1.SpecialGuiElementRegistry
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.Version
import net.fabricmc.loader.api.metadata.ModMetadata
import net.minecraft.client.MinecraftClient
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.lang.invoke.MethodHandles
import kotlin.coroutines.EmptyCoroutineContext

object OdinMod : ModInitializer {
    val mc: MinecraftClient
        get() = MinecraftClient.getInstance()

    val EVENT_BUS = EventBus()

    const val MOD_ID = "odin-fabric"

    private val metadata: ModMetadata by lazy {
        FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow().metadata
    }
    val version: Version by lazy { metadata.version }
    val logger: Logger = LogManager.getLogger("Odin")

    val scope = CoroutineScope(SupervisorJob() + EmptyCoroutineContext)

    override fun onInitialize() {
        EVENT_BUS.registerLambdaFactory("com.odtheking") { lookupInMethod, klass ->
            lookupInMethod.invoke(null, klass, MethodHandles.lookup()) as MethodHandles.Lookup
        }

        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            arrayOf(
                mainCommand, petCommand, devCommand, waypointCommand,
                highlightCommand, termSimCommand, PosMsgCommand
            ).forEach { commodore -> commodore.register(dispatcher) }
        }

        listOf(
            this, LocationUtils, TickTasks, KuudraUtils,
            SkyblockPlayer, MobCaches, ServerUtils,
            EventDispatcher, ModuleManager, DungeonListener,
            ScanUtils, DungeonUtils, SplitsManager
        ).forEach { EVENT_BUS.subscribe(it) }

        SpecialGuiElementRegistry.register { context ->
            ItemStateRenderer(context.vertexConsumers())
        }

        Config.load()

        val name = mc.session?.username?.takeIf { !it.matches(Regex("Player\\d{2,3}")) } ?: return
        sendDataToServer(body = """{"username": "$name", "version": "Fabric $version"}""")
    }
}