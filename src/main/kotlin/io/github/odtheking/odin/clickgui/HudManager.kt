package io.github.odtheking.odin.clickgui

import io.github.odtheking.odin.OdinMod.mc
import io.github.odtheking.odin.clickgui.settings.impl.HudElement
import io.github.odtheking.odin.config.Config
import io.github.odtheking.odin.features.ModuleManager.hudSettingsCache
import io.github.odtheking.odin.utils.Colors
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW
import kotlin.math.floor
import kotlin.math.sign
import io.github.odtheking.odin.utils.ui.mouseX as odinMouseX
import io.github.odtheking.odin.utils.ui.mouseY as odinMouseY

object HudManager : Screen(Text.of("HUD Manager")) {

    private var dragging: HudElement? = null

    private var deltaX = 0f
    private var deltaY = 0f

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, deltaTicks: Float) {
        super.render(context, mouseX, mouseY, deltaTicks)

        dragging?.let {
            it.x = floor((odinMouseX + deltaX).coerceIn(0f, mc.window.width - (it.width * it.scale)))
            it.y = floor((odinMouseY + deltaY).coerceIn(0f, mc.window.height - (it.height * it.scale)))
        }

        context?.matrices?.push()
        val sf = mc.window.scaleFactor.toFloat()
        context?.matrices?.scale(1f / sf, 1f / sf, 1f)

        for (hud in hudSettingsCache) {
            if (hud.isEnabled) hud.value.draw(context!!, true)
            if (!hud.value.isHovered()) continue
            context?.matrices?.push()
            context?.matrices?.translate(
                hud.value.x + hud.value.width * hud.value.scale + 10.0,
                hud.value.y.toDouble(),
                1.0
            )
            context?.matrices?.scale(2f, 2f, 1f)
            context?.drawTextWithShadow(mc.textRenderer, Text.of(hud.name), 0, 0, Colors.WHITE.rgba)
            context?.drawWrappedTextWithShadow(mc.textRenderer, Text.of(hud.description), 0, 10, 150, Colors.WHITE.rgba)
            context?.matrices?.pop()
        }
        context?.matrices?.pop()
    }

    override fun mouseScrolled(
        mouseX: Double,
        mouseY: Double,
        horizontalAmount: Double,
        verticalAmount: Double
    ): Boolean {
        val actualAmount = verticalAmount.sign.toFloat() * 0.2f
        for (hud in hudSettingsCache) {
            if (hud.isEnabled && hud.value.isHovered()) {
                hud.value.scale = (hud.value.scale + actualAmount).coerceIn(1f, 10f)
                return true
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        for (hud in hudSettingsCache) {
            if (hud.isEnabled && hud.value.isHovered()) {
                dragging = hud.value

                deltaX = (hud.value.x - odinMouseX)
                deltaY = (hud.value.y - odinMouseY)
                return true
            }
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, state: Int): Boolean {
        dragging = null
        return super.mouseReleased(mouseX, mouseY, state)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        when (keyCode) {
            GLFW.GLFW_KEY_EQUAL -> {
                for (hud in hudSettingsCache) {
                    if (hud.isEnabled && hud.value.isHovered()) {
                        hud.value.scale = (hud.value.scale + 0.1f).coerceIn(1f, 10f)
                        return true
                    }
                }
            }

            GLFW.GLFW_KEY_MINUS -> {
                for (hud in hudSettingsCache) {
                    if (hud.isEnabled && hud.value.isHovered()) {
                        hud.value.scale = (hud.value.scale - 0.1f).coerceIn(1f, 10f)
                        return true
                    }
                }
            }

            GLFW.GLFW_KEY_RIGHT -> {
                for (hud in hudSettingsCache) {
                    if (hud.isEnabled && hud.value.isHovered()) {
                        hud.value.x += 10f
                        return true
                    }
                }
            }

            GLFW.GLFW_KEY_LEFT -> {
                for (hud in hudSettingsCache) {
                    if (hud.isEnabled && hud.value.isHovered()) {
                        hud.value.x -= 10f
                        return true
                    }
                }
            }

            GLFW.GLFW_KEY_UP -> {
                for (hud in hudSettingsCache) {
                    if (hud.isEnabled && hud.value.isHovered()) {
                        hud.value.y -= 10f
                        return true
                    }
                }
            }

            GLFW.GLFW_KEY_DOWN -> {
                for (hud in hudSettingsCache) {
                    if (hud.isEnabled && hud.value.isHovered()) {
                        hud.value.y += 10f
                        return true
                    }
                }
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun close() {
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