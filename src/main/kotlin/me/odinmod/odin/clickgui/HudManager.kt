package me.odinmod.odin.clickgui

import me.odinmod.odin.OdinMod.mc
import me.odinmod.odin.clickgui.settings.impl.HudElement
import me.odinmod.odin.config.Config
import me.odinmod.odin.features.ModuleManager.hudSettingsCache
import me.odinmod.odin.utils.ui.heightResFactor
import me.odinmod.odin.utils.ui.scaledMouseX
import me.odinmod.odin.utils.ui.scaledMouseY
import me.odinmod.odin.utils.ui.widthResFactor
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import kotlin.math.sign

object HudManager : Screen(Text.of("HUD Manager")) {

    private var dragging: HudElement? = null

    private var startX = 0f
    private var startY = 0f

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, deltaTicks: Float) {
        dragging?.let {
            it.x = (scaledMouseX - startX).coerceIn(0f, mc.window.width / mc.window.widthResFactor - (it.width * it.scale))
            it.y = (scaledMouseY - startY).coerceIn(0f, mc.window.height / mc.window.heightResFactor - (it.height * it.scale))
        }

        context?.matrices?.push()
        val sf = mc.window.scaleFactor.toFloat()
        context?.matrices?.scale(1f / sf, 1f / sf, 1f)

        for (hud in hudSettingsCache) {
            if (hud.isEnabled) hud.value.draw(context!!, true)
        }
        context?.matrices?.pop()
        super.render(context, mouseX, mouseY, deltaTicks)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        val actualAmount = verticalAmount.sign.toFloat() * 0.2f
        for (hud in hudSettingsCache) {
            if (hud.isEnabled && hud.value.isHovered()) {
                hud.value.scale = (hud.value.scale + actualAmount).coerceIn(2f, 10f)
                return true
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        for (hud in hudSettingsCache) {
            if (hud.isEnabled && hud.value.isHovered()) {
                dragging = hud.value

                startX = (scaledMouseX - hud.value.x)
                startY = (scaledMouseY - hud.value.y)
                return true
            }
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, state: Int): Boolean {
        dragging = null
        return super.mouseReleased(mouseX, mouseY, state)
    }

    override fun close()  {
        Config.save()
        super.close()
    }

    fun resetHUDS() {
        hudSettingsCache.forEach {
            it.value.x = 10f
            it.value.y = 10f
            it.value.scale = 2f
        }
    }

    override fun shouldPause(): Boolean = false
}