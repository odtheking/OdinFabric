package me.odinmod.odin.clickgui

import me.odinmod.odin.OdinMod.mc
import me.odinmod.odin.clickgui.settings.impl.HudElement
import me.odinmod.odin.config.Config
import me.odinmod.odin.features.ModuleManager.hudSettingsCache
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import kotlin.math.sign

object HudManager : Screen(Text.literal("HUD Manager")) {

    private var dragging: HudElement? = null

    private var startX = 0f
    private var startY = 0f

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, deltaTicks: Float) {
        dragging?.let {
            it.x = (mc.mouse.x - startX).toFloat().coerceIn(0f, mc.window.width - (it.width * it.scale).coerceAtLeast(50f))
            it.y = (mc.mouse.y - startY).toFloat().coerceIn(0f, mc.window.height - (it.height * it.scale).coerceAtLeast(20f))
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
            if (hud.value.isHovered()) {
                hud.value.scale = (hud.value.scale + actualAmount).coerceIn(1f, 5f)
                return true
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        for (hud in hudSettingsCache) {
            if (hud.value.isHovered()) {
                dragging = hud.value

                startX = (mc.mouse.x - hud.value.x).toFloat()
                startY = (mc.mouse.y - hud.value.y).toFloat()
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