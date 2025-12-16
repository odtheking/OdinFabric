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
import com.odtheking.odin.utils.ui.rendering.NVGRenderer
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.resources.ResourceLocation

/**
 * # Module Manager
 *
 * This object stores all [Modules][Module] and provides functionality to [HUDs][Module.HUD]
 */
object ModuleManager {

    private val HUD_LAYER: ResourceLocation = ResourceLocation.fromNamespaceAndPath(OdinMod.MOD_ID, "odin_hud")
    val keybindSettingsCache = mutableListOf<KeybindSetting>()
    val hudSettingsCache = mutableListOf<HUDSetting>()

    val modules: ArrayList<Module> = arrayListOf(
        // dungeon
        PuzzleSolvers, BlessingDisplay, LeapMenu, SecretClicked, MapInfo, Mimic, DungeonQueue, KeyHighlight, BloodCamp,
        PositionalMessages, TerracottaTimer, BreakerDisplay, LividSolver, InvincibilityTimer, SpiritBear,
        DungeonWaypoints, ExtraStats, BetterPartyFinder, Croesus, MageBeam,

        // floor 7
        TerminalSimulator, TerminalSolver, TerminalTimes, TerminalSounds, TickTimers, ArrowAlign, InactiveWaypoints,
        MelodyMessage, WitherDragons, SimonSays, KingRelics, ArrowsDevice,

        // render
        ClickGUIModule, Camera, Etherwarp, PlayerSize, PerformanceHUD, RenderOptimizer,
        PlayerDisplay, Waypoints, HidePlayers, Highlight, GyroWand,

        //skyblock
        ChatCommands, NoCursorReset, Ragnarock, SpringBoots, WardrobeKeybinds, PetKeybinds, AutoSprint,
        CommandKeybinds, SlotBinds, Splits,

        // nether
        SupplyHelper, BuildHelper, RemovePerks, NoPre, PearlWaypoints, FreshTools, KuudraInfo, Misc
    )

    init {
        for (module in modules) {
            module.key?.let {
                module.register(KeybindSetting("Keybind", it, "Toggles the module").apply {
                    onPress = { module.onKeybind() }
                })
            }
            for (setting in module.settings) {
                if (setting is KeybindSetting) keybindSettingsCache.add(setting)
                if (setting is HUDSetting) hudSettingsCache.add(setting)
            }
        }

        on<InputEvent> {
            for (setting in keybindSettingsCache) {
                if (setting.value.value == key.value) setting.onPress?.invoke()
            }
        }

        HudElementRegistry.attachElementBefore(VanillaHudElements.SLEEP, HUD_LAYER, ModuleManager::render)
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

    fun getModuleByName(name: String?): Module? = modules.firstOrNull { it.name.equals(name, true) }

    fun generateFeatureList(): String {
        val featureList = StringBuilder()

        for ((category, modulesInCategory) in modules.groupBy { it.category }.entries) {
            featureList.appendLine("Category: ${category.displayName}")
            for (module in modulesInCategory.sortedByDescending {
                NVGRenderer.textWidth(it.name, 16f, NVGRenderer.defaultFont)
            }) featureList.appendLine("- ${module.name}: ${module.description}")
            featureList.appendLine()
        }
        return featureList.toString()
    }
}