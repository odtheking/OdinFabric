package me.odinmod.odin.features

import me.odinmod.odin.OdinMod
import me.odinmod.odin.OdinMod.mc
import me.odinmod.odin.clickgui.settings.impl.HUDSetting
import me.odinmod.odin.clickgui.settings.impl.KeybindSetting
import me.odinmod.odin.events.InputEvent
import me.odinmod.odin.features.impl.render.Camera
import me.odinmod.odin.features.impl.render.ClickGUIModule
import me.odinmod.odin.features.impl.render.Etherwarp
import me.odinmod.odin.features.impl.render.PlayerSize
import me.odinmod.odin.features.impl.skyblock.*
import me.odinmod.odin.utils.ui.rendering.NVGRenderer
import meteordevelopment.orbit.EventHandler
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer
import net.fabricmc.fabric.api.client.rendering.v1.LayeredDrawerWrapper
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderTickCounter
import net.minecraft.util.Identifier

/**
 * # Module Manager
 *
 * This object stores all [Modules][Module] and provides functionality to [HUDs][Module.HUD]
 */
object ModuleManager {

    private val HUD_LAYER: Identifier = Identifier.of(OdinMod.MOD_ID, "odin_hud")
    private val keybindSettingsCache = mutableListOf<KeybindSetting>()
    val hudSettingsCache = mutableListOf<HUDSetting>()

    val modules: ArrayList<Module> = arrayListOf(
        // render
        ClickGUIModule, Camera, Etherwarp, PlayerSize,

        //skyblock
        ChatCommands, NoCursorReset, RagnarockAxe, SpringBoots, WardrobeKeybinds, PetKeybinds,

        // kuudra

    )

    init {
        for (module in modules) {
            module.key?.let {
                module.register(KeybindSetting("Keybind", it, "Toggles the module").apply { onPress = { module.onKeybind() } })
            }
            for (setting in module.settings) {
                if (setting is KeybindSetting) keybindSettingsCache.add(setting)
                if (setting is HUDSetting) hudSettingsCache.add(setting)
            }
        }

        HudLayerRegistrationCallback.EVENT.register(HudLayerRegistrationCallback { drawer: LayeredDrawerWrapper ->
            drawer.attachLayerBefore(IdentifiedLayer.DEBUG, HUD_LAYER, ModuleManager::render)
        })
    }

    fun render(context: DrawContext, tickCounter: RenderTickCounter) {
        if (mc.world == null || mc.player == null) return
        context.matrices.push()
        val sf = mc.window.scaleFactor.toFloat()
        context.matrices.scale(1f / sf, 1f / sf, 1f)
        for (hudSettings in hudSettingsCache) {
             if (hudSettings.isEnabled) hudSettings.value.draw(context, false)
        }
        context.matrices?.pop()
    }

    @EventHandler
    fun activateModuleKeyBinds(event: InputEvent) {
        for (setting in keybindSettingsCache) {
            if (setting.value.code == event.key.code) setting.onPress?.invoke()
        }
    }

    fun getModuleByName(name: String?): Module? = modules.firstOrNull { it.name.equals(name, true) }

    fun generateFeatureList(): String {
        val featureList = StringBuilder()

        for ((category, modulesInCategory) in modules.groupBy { it.category }.entries) {
            featureList.appendLine("Category: ${category.displayName}")
            for (module in modulesInCategory.sortedByDescending { NVGRenderer.textWidth(it.name, 16f, NVGRenderer.defaultFont) }) {
                featureList.appendLine("- ${module.name}: ${module.description}")
            }
            featureList.appendLine()
        }
        return featureList.toString()
    }
}