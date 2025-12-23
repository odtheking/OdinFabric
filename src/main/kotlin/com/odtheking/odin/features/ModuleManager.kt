package com.odtheking.odin.features

import com.odtheking.odin.OdinMod
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.HudManager
import com.odtheking.odin.clickgui.settings.impl.HUDSetting
import com.odtheking.odin.clickgui.settings.impl.KeybindSetting
import com.odtheking.odin.events.InputEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.impl.dungeon.*
import com.odtheking.odin.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints
import com.odtheking.odin.features.impl.dungeon.puzzlesolvers.PuzzleSolvers
import com.odtheking.odin.features.impl.floor7.*
import com.odtheking.odin.features.impl.nether.*
import com.odtheking.odin.features.impl.render.*
import com.odtheking.odin.features.impl.skyblock.*
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.resources.ResourceLocation
import net.minecraft.resources.ResourceLocation.fromNamespaceAndPath

/**
 * # Module Manager
 *
 * This object stores all [Modules][Module] and provides functionality to [HUDs][Module.HUD]
 */
object ModuleManager {

    /**
     * Map containing all modules in Odin,
     * where the key is the modules name in lowercase.
     */
    val modules: HashMap<String, Module> = linkedMapOf()

    /**
     * Map containing all modules under their category.
     */
    val modulesByCategory: HashMap<Category, ArrayList<Module>> = hashMapOf()

    val keybindSettingsCache: ArrayList<KeybindSetting> = arrayListOf<KeybindSetting>()
    val hudSettingsCache: ArrayList<HUDSetting> = arrayListOf<HUDSetting>()

    private val HUD_LAYER: ResourceLocation = fromNamespaceAndPath(OdinMod.MOD_ID, "odin_hud")

    init {
        registerModules(
            // dungeon
            PuzzleSolvers, BlessingDisplay, LeapMenu, SecretClicked, MapInfo, Mimic, DungeonQueue,
            KeyHighlight, BloodCamp, PositionalMessages, TerracottaTimer, BreakerDisplay, LividSolver,
            InvincibilityTimer, SpiritBear, DungeonWaypoints, ExtraStats, BetterPartyFinder, Croesus, MageBeam,

            // floor 7
            TerminalSimulator, TerminalSolver, TerminalTimes, TerminalSounds, TickTimers, ArrowAlign,
            InactiveWaypoints, MelodyMessage, WitherDragons, SimonSays, KingRelics, ArrowsDevice,

            // render
            ClickGUIModule, Camera, Etherwarp, PlayerSize, PerformanceHUD, RenderOptimizer,
            PlayerDisplay, Waypoints, HidePlayers, Highlight, GyroWand,

            //skyblock
            ChatCommands, NoCursorReset, Ragnarock, SpringBoots, WardrobeKeybinds, PetKeybinds, AutoSprint,
            CommandKeybinds, SlotBinds, Splits,

            // nether
            SupplyHelper, BuildHelper, RemovePerks, NoPre, PearlWaypoints, FreshTools, KuudraInfo, Misc
        )

        // hashmap, but would need to keep track when setting values change
        on<InputEvent> {
            for (setting in keybindSettingsCache) {
                if (setting.value.value == key.value) setting.onPress?.invoke()
            }
        }

        HudElementRegistry.attachElementBefore(VanillaHudElements.SLEEP, HUD_LAYER, ModuleManager::render)
    }

    private fun registerModules(vararg modules: Module) {
        for (module in modules) {
            // dev module shouldn't be registered while not in dev env
            if (module.isDevModule && !FabricLoader.getInstance().isDevelopmentEnvironment) {
                continue
            }

            this.modules[module.name.lowercase()] = module
            this.modulesByCategory.getOrPut(module.category) { arrayListOf() }.add(module)

            module.key?.let { keybind ->
                val setting = KeybindSetting("Keybind", keybind, "Toggles this module.")
                setting.onPress = module::onKeybind
                module.registerSetting(setting)
            }

            for ((_, setting) in module.settings) {
                when (setting) {
                    is KeybindSetting -> keybindSettingsCache.add(setting)
                    is HUDSetting -> hudSettingsCache.add(setting)
                }
            }
        }
    }

    fun render(context: GuiGraphics, tickCounter: DeltaTracker) {
        if (mc.level == null || mc.player == null || mc.screen == HudManager || mc.options.hideGui) return
        context.pose().pushMatrix()
        val sf = mc.window.guiScale
        context.pose().scale(1f / sf, 1f / sf)
        for (hudSettings in hudSettingsCache) {
            if (hudSettings.isEnabled) hudSettings.value.draw(context, false)
        }
        context.pose().popMatrix()
    }
}