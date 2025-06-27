package me.odinmod.odin.features

import me.odinmod.odin.clickgui.settings.impl.KeybindSetting
import me.odinmod.odin.events.InputEvent
import me.odinmod.odin.features.impl.render.Camera
import me.odinmod.odin.features.impl.render.ClickGUIModule
import me.odinmod.odin.features.impl.render.ClickGUIModule.onKeybind
import me.odinmod.odin.features.impl.render.Etherwarp
import me.odinmod.odin.features.impl.skyblock.*
import me.odinmod.odin.utils.ui.rendering.NVGRenderer
import meteordevelopment.orbit.EventHandler

/**
 * # Module Manager
 *
 * This object stores all [Modules][Module] and provides functionality to [HUDs][Module.HUD]
 */
object ModuleManager {

    val modules: ArrayList<Module> = arrayListOf(
        // render
        ClickGUIModule, Camera, Etherwarp,

        //skyblock
        ChatCommands, NoCursorReset, RagnarockAxe, SpringBoots, WardrobeKeybinds

        // kuudra

    )

    init {
        for (module in modules) {
            module.key?.let {
                module.register(KeybindSetting("Keybind", it, "Toggles the module").apply { onPress = ::onKeybind })
            }
        }
    }

    @EventHandler
    fun activateModuleKeyBinds(event: InputEvent) {
        for (module in modules.toList()) {
            for (setting in module.settings.toList()) {
                if (setting is KeybindSetting && (setting.value == event.key.code || (setting.value < 0 && setting.value + 100 == event.key.code)))
                    setting.onPress?.invoke()
            }
        }
    }

    fun getModuleByName(name: String?): Module? = modules.firstOrNull { it.name.equals(name, true) }

    fun generateFeatureList(): String {
        val sortedCategories = modules.sortedByDescending { NVGRenderer.textWidth(it.name, 18f, NVGRenderer.defaultFont) }.groupBy { it.category }.entries
            .sortedBy { Category.entries.associateWith { it.ordinal }[it.key] }

        val featureList = StringBuilder()
        for ((category, modulesInCategory) in sortedCategories) {
            featureList.appendLine("Category: ${category.displayName}")
            for (module in modulesInCategory) featureList.appendLine("- ${module.name}: ${module.description}")
            featureList.appendLine()
        }
        return featureList.toString()
    }
}