package com.odtheking.odin

import com.odtheking.odin.commands.*
import com.odtheking.odin.config.Config
import com.odtheking.odin.events.EventDispatcher
import com.odtheking.odin.events.core.EventBus
import com.odtheking.odin.features.ModuleManager
import com.odtheking.odin.utils.ServerUtils
import com.odtheking.odin.utils.handlers.MobCaches
import com.odtheking.odin.utils.handlers.TickTasks
import com.odtheking.odin.utils.network.WebUtils.createClient
import com.odtheking.odin.utils.network.WebUtils.postData
import com.odtheking.odin.utils.render.ItemStateRenderer
import com.odtheking.odin.utils.skyblock.KuudraUtils
import com.odtheking.odin.utils.skyblock.LocationUtils
import com.odtheking.odin.utils.skyblock.PartyUtils
import com.odtheking.odin.utils.skyblock.SkyblockPlayer
import com.odtheking.odin.utils.skyblock.SplitsManager
import com.odtheking.odin.utils.skyblock.dungeon.DungeonListener
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.ScanUtils
import com.odtheking.odin.utils.ui.rendering.NVGSpecialRenderer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.rendering.v1.SpecialGuiElementRegistry
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.Version
import net.fabricmc.loader.api.metadata.ModMetadata
import net.minecraft.client.Minecraft
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import kotlin.coroutines.EmptyCoroutineContext

object OdinMod : ClientModInitializer {
    val mc: Minecraft
        get() = Minecraft.getInstance()

    const val MOD_ID = "odin-fabric"

    private val metadata: ModMetadata by lazy {
        FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow().metadata
    }
    val version: Version by lazy { metadata.version }
    val logger: Logger = LogManager.getLogger("Odin")

    val okClient = createClient()
    val scope = CoroutineScope(SupervisorJob() + EmptyCoroutineContext)

    override fun onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            arrayOf(
                mainCommand, petCommand, devCommand, waypointCommand,
                highlightCommand, termSimCommand, posMsgCommand, dungeonWaypointsCommand
            ).forEach { commodore -> commodore.register(dispatcher) }
        }

        listOf(
            this, LocationUtils, TickTasks, KuudraUtils,
            SkyblockPlayer, MobCaches, ServerUtils,
            EventDispatcher, ModuleManager, DungeonListener,
            ScanUtils, DungeonUtils, SplitsManager, PartyUtils
        ).forEach { EventBus.subscribe(it) }

        SpecialGuiElementRegistry.register { context ->
            NVGSpecialRenderer(context.vertexConsumers())
        }

        SpecialGuiElementRegistry.register { context ->
            ItemStateRenderer(context.vertexConsumers())
        }

        Config.load()

        val name = mc.user?.name ?: return
        scope.launch {
            postData("https://api.odtheking.com/tele/", """{"username": "$name", "version": "Fabric $version"}""")
        }
    }
}
