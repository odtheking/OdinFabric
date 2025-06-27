package me.odinmod.odin.features

import me.odinmod.odin.OdinMod
import me.odinmod.odin.clickgui.settings.AlwaysActive
import me.odinmod.odin.clickgui.settings.Setting
import me.odinmod.odin.features.impl.render.ClickGUIModule
import me.odinmod.odin.utils.modMessage
import kotlin.reflect.full.hasAnnotation

/**
 * Class that represents a module. And handles all the settings.
 * @author Aton
 */
abstract class Module(
    val name: String,
    val key: Int? = 0,
    @Transient var description: String,
    toggled: Boolean = false,
) {

    /**
     * Category for this module.
     *
     * It is defined by the package of the module. (For example: me.odin.features.impl.render == [Category.RENDER]).
     * If it is in an invalid package, it will use [Category.RENDER] as a default
     */
    @Transient
    val category: Category = getCategory(this::class.java) ?: Category.RENDER

    /**
     * Reference for if the module is enabled
     *
     * When it is enabled, it is registered to the Forge Eventbus,
     * otherwise it's unregistered unless it has the annotation [@AlwaysActive][AlwaysActive]
     */
    var enabled: Boolean = toggled
        private set

    /**
     * List of settings for the module
     */
    val settings: ArrayList<Setting<*>> = ArrayList()

    protected inline val mc get() = OdinMod.mc

    /**
     * Indicates if the module has the annotation [AlwaysActive],
     * which keeps the module registered to the eventbus, even if disabled
     */
    @Transient
    val alwaysActive = this::class.hasAnnotation<AlwaysActive>()

    init {
        if (alwaysActive) {
            @Suppress("LeakingThis")
            OdinMod.EVENT_BUS.subscribe(this)
        }
    }

    /**
     * Gets toggled when module is enabled
     */
    open fun onEnable() {
        if (!alwaysActive) OdinMod.EVENT_BUS.subscribe(this)
    }

    /**
     * Gets toggled when module is disabled
     */
    open fun onDisable() {
        if (!alwaysActive) OdinMod.EVENT_BUS.unsubscribe(this)
    }

    open fun onKeybind() {
        toggle()
        if (ClickGUIModule.enableNotification) modMessage("$name ${if (enabled) "§aenabled" else "§cdisabled"}.")
    }

    fun toggle() {
        enabled = !enabled
        if (enabled) onEnable()
        else onDisable()
    }

    fun <K : Setting<*>> register(setting: K): K {
        settings.add(setting)
       // if (setting is HudSetting) {
       //     setting.value.init(this)
       // }
        return setting
    }

    fun register(vararg setting: Setting<*>) = setting.forEach(::register)

    operator fun <K : Setting<*>> K.unaryPlus(): K = register(this)

    fun getSettingByName(name: String?): Setting<*>? {
        for (setting in settings) {
            if (setting.name.equals(name, ignoreCase = true)) return setting
        }
        return null
    }

    private companion object {
        private fun getCategory(clazz: Class<out Module>): Category? =
            Category.entries.find { clazz.`package`.name.contains(it.name, true) }
    }
}