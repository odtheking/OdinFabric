package me.odinmod.odin

import com.teamresourceful.resourcefulconfig.api.loader.Configurator
import me.odinmod.odin.commands.mainCommand
import me.odinmod.odin.config.Config
import me.odinmod.odin.events.EventDispatcher
import me.odinmod.odin.features.Box
import me.odinmod.odin.features.foraging.TreeHud
import me.odinmod.odin.features.render.Camera
import me.odinmod.odin.features.render.Etherwarp
import me.odinmod.odin.features.skyblock.*
import me.odinmod.odin.utils.handlers.MobCaches
import me.odinmod.odin.utils.handlers.TickTask
import me.odinmod.odin.utils.handlers.TickTasks
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

object OdinMod : ModInitializer {

    @JvmField
    val mc: MinecraftClient = MinecraftClient.getInstance()

    @JvmField
    val EVENT_BUS = EventBus()

    private const val MOD_ID = "odining"
    private val metadata: ModMetadata by lazy { FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow().metadata }
    val version: Version by lazy { metadata.version }
    val logger: Logger = LogManager.getLogger("Odin")
    private val configurator = Configurator("odining")
    val config = Config.register(configurator)

    override fun onInitialize() {
        EVENT_BUS.registerLambdaFactory("me.odinmod") { lookupInMethod, klass ->
            lookupInMethod.invoke(null, klass, MethodHandles.lookup()) as MethodHandles.Lookup
        }

        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            arrayOf(mainCommand).forEach { commodore ->
                commodore.register(dispatcher)
            }
        }

        EventDispatcher
        Box

        listOf(
            this, LocationUtils, TickTasks, SkyblockPlayer, MobCaches, Box, TreeHud, Etherwarp, ChatCommands, WardrobeKeybinds, NoCursorReset, Camera,
            SpringBoots, RagnarockAxe
        ).forEach { EVENT_BUS.subscribe(it) }
    }

    init {
        TickTask(50) {
         /*   val player = mc.player ?: return@TickTask
            getEtherPos(player.yaw, player.pitch, 60.0, true).also { etherPos ->
                if (etherPos.succeeded) {
                    modMessage("Ether position: ${etherPos.vec}")
                } else {
                    modMessage("Ether position not found")
                }
            }*/
//            val item = mc.player?.mainHandStack ?: return@TickTask
//            modMessage(getItemId(item))
//            modMessage(getItemUUID(item))
//            modMessage(getLoreText(item).joinToString("\n"))
//            modMessage(getCustomData(item))
        }
    }
}