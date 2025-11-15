package com.odtheking.odin.features

import com.odtheking.odin.OdinMod
import com.odtheking.odin.clickgui.settings.AlwaysActive
import com.odtheking.odin.clickgui.settings.DevModule
import com.odtheking.odin.clickgui.settings.Setting
import com.odtheking.odin.clickgui.settings.impl.HUDSetting
import com.odtheking.odin.events.core.EventBus
import com.odtheking.odin.features.impl.render.ClickGUIModule
import com.odtheking.odin.utils.modMessage
import net.minecraft.client.gui.GuiGraphics
import org.lwjgl.glfw.GLFW
import kotlin.reflect.full.hasAnnotation

/**
 * Class that represents a module. And handles all the settings.
 * @author Aton
 */
abstract class Module(
    val name: String,
    val key: Int? = GLFW.GLFW_KEY_UNKNOWN,
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

    @Transient
    val isDevModule = this::class.hasAnnotation<DevModule>()

    init {
        if (alwaysActive) {
            @Suppress("LeakingThis")
            EventBus.subscribe(this)
        }
    }

    /**
     * Gets toggled when module is enabled
     */
    open fun onEnable() {
        if (!alwaysActive) EventBus.subscribe(this)
    }

    /**
     * Gets toggled when module is disabled
     */
    open fun onDisable() {
        if (!alwaysActive) EventBus.unsubscribe(this)
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

    fun <K : Setting<*>> register(setting: K): K = setting.also { settings.add(it) }

    operator fun <K : Setting<*>> K.unaryPlus(): K = register(this)

    fun getSettingByName(name: String?): Setting<*>? =
        settings.find { it.name.equals(name, ignoreCase = true) }

    @Suppress("FunctionName")
    fun HUD(
        name: String,
        desc: String,
        toggleable: Boolean = true,
        x: Int = 10,
        y: Int = 10,
        scale: Float = 2f,
        block: GuiGraphics.(example: Boolean) -> Pair<Int, Int>
    ): HUDSetting = HUDSetting(name, x, y, scale, toggleable, desc, this, block)

    private companion object {
        private fun getCategory(clazz: Class<out Module>): Category? =
            Category.entries.find { clazz.`package`.name.contains(it.name, true) }
    }
}