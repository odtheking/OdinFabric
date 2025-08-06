package io.github.odtheking.odin.features.impl.render

import io.github.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import io.github.odtheking.odin.clickgui.settings.impl.BooleanSetting
import io.github.odtheking.odin.clickgui.settings.impl.NumberSetting
import io.github.odtheking.odin.features.Module
import io.github.odtheking.odin.utils.skyblock.Island
import io.github.odtheking.odin.utils.skyblock.LocationUtils
import net.minecraft.entity.Entity

object HidePlayers : Module(
    name = "Hide Players",
    description = "Hides players in your vicinity."
) {
    private val hideAll by BooleanSetting("Hide all", false, desc = "Hides all players, regardless of distance.")
    private val distance by NumberSetting(
        "Distance",
        3f,
        0.0,
        32.0,
        .5,
        "The number of blocks away to hide players.",
        unit = " blocks"
    ).withDependency { !hideAll }

    @JvmStatic
    fun shouldRenderPlayer(entity: Entity): Boolean {
        if (!enabled || entity.uuid.version() != 4 || entity == mc.player || LocationUtils.currentArea.isArea(Island.SinglePlayer)) return true
        if (hideAll) return false
        return entity.squaredDistanceTo(mc.player) > (distance * distance)
    }
}