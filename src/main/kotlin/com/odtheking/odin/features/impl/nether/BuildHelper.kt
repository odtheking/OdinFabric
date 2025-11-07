package com.odtheking.odin.features.impl.nether

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.alert
import com.odtheking.odin.utils.render.drawCustomBeacon
import com.odtheking.odin.utils.render.drawString
import com.odtheking.odin.utils.render.drawText
import com.odtheking.odin.utils.render.getStringWidth
import com.odtheking.odin.utils.skyblock.KuudraUtils
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
        if (!example && (!KuudraUtils.inKuudra || KuudraUtils.phase != 2)) return@HUD 0 to 0
        drawString("§bFreshers: ${colorBuilders(KuudraUtils.freshers.size)}", 1, 1)
        drawString("§bBuilders: ${colorBuilders(KuudraUtils.playersBuildingAmount)}", 1, 10)
        drawString("§bBuild: ${colorBuild(KuudraUtils.buildDonePercentage)}%", 1, 19)
        getStringWidth("Freshers: 0") + 2 to mc.textRenderer.fontHeight * 3
    }

    private val stunNotificationNumber by NumberSetting("Stun Percent", 93f, 0, 100, desc = "The build % to notify at (set to 0 to disable).", unit = "%")

    init {
        on<RenderEvent.Last> {
            if (!KuudraUtils.inKuudra || KuudraUtils.phase != 2) return@on
            if (stunNotificationNumber != 0f && KuudraUtils.buildDonePercentage >= stunNotificationNumber) alert("§l§3Go to stun", false)
            if (buildHelperDraw)
                context.drawText(
                    Text.of("§bBuild §c${colorBuild(KuudraUtils.buildDonePercentage)}%").asOrderedText(),
                    Vec3d(-101.5, 82.0, -105.5),
                    3f,
                    false
                )

            if (buildHelperDraw)
                context.drawText(
                    Text.of("§bBuilders ${colorBuilders(KuudraUtils.playersBuildingAmount)}").asOrderedText(),
                    Vec3d(-101.5, 81.0, -105.5),
                    3f,
                    false
                )

            if (unfinishedWaypoints)
                KuudraUtils.buildingPiles.forEach {
                    context.drawCustomBeacon(
                        it.name.asOrderedText(),
                        it.blockPos,
                        Colors.MINECRAFT_DARK_RED,
                        increase = false,
                        distance = false
                    )
                    if (hideDefaultTag) it.isCustomNameVisible = false
                }
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