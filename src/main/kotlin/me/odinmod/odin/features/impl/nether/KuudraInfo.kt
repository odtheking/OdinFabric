package me.odinmod.odin.features.impl.nether

import me.odinmod.odin.clickgui.settings.Setting.Companion.withDependency
import me.odinmod.odin.clickgui.settings.impl.BooleanSetting
import me.odinmod.odin.clickgui.settings.impl.ColorSetting
import me.odinmod.odin.clickgui.settings.impl.NumberSetting
import me.odinmod.odin.events.RenderEvent
import me.odinmod.odin.features.Module
import me.odinmod.odin.utils.Colors
import me.odinmod.odin.utils.addVec
import me.odinmod.odin.utils.render.drawString
import me.odinmod.odin.utils.render.drawText
import me.odinmod.odin.utils.render.drawWireFrameBox
import me.odinmod.odin.utils.skyblock.KuudraUtils
import me.odinmod.odin.utils.toFixed
import meteordevelopment.orbit.EventHandler
import net.minecraft.text.Text

object KuudraInfo : Module(
    name = "Kuudra Info",
    description = "Displays information about Kuudra entity itself."
) {
    private val highlightKuudra by BooleanSetting("Highlight Kuudra", true, desc = "Highlights the kuudra entity.")
    private val kuudraColor by ColorSetting("Kuudra Color", Colors.MINECRAFT_RED, true, desc = "Color of the kuudra highlight.").withDependency { highlightKuudra }
    private val kuudraHPDisplay by BooleanSetting("Kuudra HP", true, desc = "Renders Kuudra's HP infront of it.")
    private val healthSize by NumberSetting("Health Size", 4f, 3f, 8.0f, 0.1, desc = "Size of the health display.").withDependency { kuudraHPDisplay }
    private val scaledHealth by BooleanSetting("Use Scaled", true, desc = "Use scaled health for the display meaning the health will update in tier 5 when below 25,000.").withDependency { kuudraHPDisplay }
    private val hud by HUD("Health Display", "Displays the current health of Kuudra.") { example ->
        if (!example && !KuudraUtils.inKuudra) return@HUD 0f to 0f
        val string = if (example) "§a99.975M/240M§c❤" else getCurrentHealthDisplay(KuudraUtils.kuudraEntity?.health ?: return@HUD 0 to 0)

        drawString(string, 1f, 1f)

        mc.textRenderer.getWidth(string) + 2f to mc.textRenderer.fontHeight
    }

    @EventHandler
    fun renderWorldEvent(event: RenderEvent.Last) {
        if (!KuudraUtils.inKuudra) return

        KuudraUtils.kuudraEntity?.let {
            if (highlightKuudra)
                event.context.drawWireFrameBox(it.boundingBox, kuudraColor, depth = true)

            if (kuudraHPDisplay) {
                event.context.drawText(
                    Text.of(getCurrentHealthDisplay(it.health)).asOrderedText(),
                    it.pos.add(it.rotationVector.multiply(13.0).addVec(y = 10.0)), healthSize, depth = true
                )
            }
        }
    }

    private fun getCurrentHealthDisplay(kuudraHP: Float): String {
        val color = when {
            kuudraHP > 99000 -> "§a"
            kuudraHP > 75000 -> "§2"
            kuudraHP > 50000 -> "§e"
            kuudraHP > 25000 -> "§6"
            kuudraHP > 10000 -> "§c"
            else -> "§4"
        }
        val health = kuudraHP / 1000

        return when {
            // Scaled
            kuudraHP <= 25000 && scaledHealth && KuudraUtils.kuudraTier == 5 -> "$color${(health * 9.6).toFixed()}M§7/§a240M§c❤"
            // Absolute
            else -> "$color${health}K§7/§a100k§c❤"
        }
    }
}