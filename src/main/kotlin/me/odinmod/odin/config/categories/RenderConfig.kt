package me.odinmod.odin.config.categories

import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import me.odinmod.odin.config.ConfigUtils.autoRefresh
import me.odinmod.odin.utils.RenderStyle

object RenderConfig: CategoryKt("Render") {

    override val description: TranslatableValue = TranslatableValue("Settings related to rendering and visual effects in Odin Mod.")

    init {
        separator {
            this.title = "Render Settings"
        }
    }

    val etherwarpHelper by autoRefresh(boolean(false) {
        name = TranslatableValue("Etherwarp Helper")
        description = TranslatableValue("Enable the Etherwarp helper to assist with Etherwarp mechanics.")
    })

    val etherwarpHelperColor by color(0x00bbff) {
        name = TranslatableValue("Etherwarp Helper Color")
        description = TranslatableValue("Set the color for the Etherwarp helper.")
        condition = { etherwarpHelper }
    }

    val showFailed by autoRefresh(boolean(false) {
        name = TranslatableValue("Show Failed Etherwarp")
        description = TranslatableValue("Display failed Etherwarp attempts in the Etherwarp helper.")
        condition = { etherwarpHelper }
    })

    val showFailedColor by color(0xFF0000) {
        name = TranslatableValue("Failed Etherwarp Color")
        description = TranslatableValue("Set the color for failed Etherwarp attempts.")
        condition = { etherwarpHelper && showFailed }
    }

    val renderStyle by enum("Render Style", RenderStyle.OULINE) {
        name = TranslatableValue("Render Style")
        description = TranslatableValue("Choose the style of rendering for visual effects.")
        condition = { etherwarpHelper }
    }

    init {
        separator {
            this.title = "Camera"
        }
    }

    val disableFrontCam by boolean(false) {
        name = TranslatableValue("Disable Front Camera")
        description = TranslatableValue("Disable the front camera in the game.")
    }
}