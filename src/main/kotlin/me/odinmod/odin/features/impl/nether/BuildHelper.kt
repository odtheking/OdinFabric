package me.odinmod.odin.features.impl.nether

import me.odinmod.odin.clickgui.settings.Setting.Companion.withDependency
import me.odinmod.odin.clickgui.settings.impl.BooleanSetting
import me.odinmod.odin.clickgui.settings.impl.NumberSetting
import me.odinmod.odin.events.RenderEvent
import me.odinmod.odin.features.Module
import me.odinmod.odin.utils.Colors
import me.odinmod.odin.utils.alert
import me.odinmod.odin.utils.render.drawCustomBeacon
import me.odinmod.odin.utils.render.drawString
import me.odinmod.odin.utils.render.drawText
import me.odinmod.odin.utils.skyblock.KuudraUtils
import meteordevelopment.orbit.EventHandler
import net.minecraft.text.Text
import net.minecraft.util.math.Vec3d

object BuildHelper : Module(
    name = "Build Helper",
    description = "Displays various information about the current state of the ballista build."
) {
    private val buildHelperDraw by BooleanSetting("Render on Ballista", false, desc = "Draws the build information on the ballista.")
    private val unfinishedWaypoints by BooleanSetting("Unfinished Waypoints", true, desc = "Draws waypoints over the unfinished piles.")
    private val hideDefaultTag by BooleanSetting("Hide Default Tag", true, desc = "Hides the default tag for unfinished piles.").withDependency { unfinishedWaypoints }
    private val hud by HUD("Build helper", "Shows information about the build progress.") { example ->
        if (!example && (!KuudraUtils.inKuudra || KuudraUtils.phase != 2)) return@HUD 0f to 0f
        drawString("§bFreshers: ${colorBuilders(KuudraUtils.freshers.size)}", 1f, 1f)
        drawString("§bBuilders: ${colorBuilders(KuudraUtils.playersBuildingAmount)}", 1f, 10f)
        drawString("§bBuild: ${colorBuild(KuudraUtils.buildDonePercentage)}%", 1f, 19f)
        mc.textRenderer.getWidth("Freshers: 0") + 2f to mc.textRenderer.fontHeight * 3
    }

    private val stunNotificationNumber by NumberSetting("Stun Percent", 93, 0.0, 100.0, desc = "The build % to notify at (set to 0 to disable).", unit = "%")

    @EventHandler
    fun renderWorldEvent(event: RenderEvent.Last) {
        if (!KuudraUtils.inKuudra || KuudraUtils.phase != 2) return
        if (stunNotificationNumber != 0 && KuudraUtils.buildDonePercentage >= stunNotificationNumber) alert("§l§3Go to stun", false)
        if (buildHelperDraw)
            event.context.drawText(Text.of("§bBuild §c${colorBuild(KuudraUtils.buildDonePercentage)}%").asOrderedText(), Vec3d(-101.5, 82.0, -105.5), 3f, false)

        if (buildHelperDraw)
            event.context.drawText(Text.of("§bBuilders ${colorBuilders(KuudraUtils.playersBuildingAmount)}").asOrderedText(), Vec3d(-101.5, 81.0, -105.5), 3f, false)

        if (unfinishedWaypoints)
            KuudraUtils.buildingPiles.forEach {
                event.context.drawCustomBeacon(it.name.asOrderedText(), it.blockPos, Colors.MINECRAFT_DARK_RED, false)
                if (hideDefaultTag) it.isCustomNameVisible = false
            }
    }

    private fun colorBuild(build: Int): String {
        return when {
            build >= 75 -> "§a$build"
            build >= 50 -> "§e$build"
            build >= 25 -> "§6$build"
            else -> "§c$build"
        }
    }

    private fun colorBuilders(builders: Int): String {
        return when {
            builders >= 3 -> "§a$builders"
            builders >= 2 -> "§e$builders"
            else -> "§c$builders"
        }
    }
}