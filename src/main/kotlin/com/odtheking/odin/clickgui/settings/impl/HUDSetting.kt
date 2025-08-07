package com.odtheking.odin.clickgui.settings.impl

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.ClickGUI
import com.odtheking.odin.clickgui.ClickGUI.gray38
import com.odtheking.odin.clickgui.HudManager
import com.odtheking.odin.clickgui.settings.RenderableSetting
import com.odtheking.odin.clickgui.settings.Saving
import com.odtheking.odin.features.Module
import com.odtheking.odin.features.impl.render.ClickGUIModule
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.ui.HoverHandler
import com.odtheking.odin.utils.ui.animations.LinearAnimation
import com.odtheking.odin.utils.ui.isAreaHovered
import com.odtheking.odin.utils.ui.rendering.NVGRenderer
import net.minecraft.client.gui.DrawContext

class HUDSetting(
    name: String,
    hud: HudElement,
    private val toggleable: Boolean = false,
    description: String,
    val module: Module,
) : RenderableSetting<HudElement>(name, description), Saving {

    constructor(
        name: String,
        x: Float,
        y: Float,
        scale: Float,
        toggleable: Boolean,
        description: String,
        module: Module,
        draw: DrawContext.(Boolean) -> Pair<Number, Number>
    )
            : this(name, HudElement(x, y, scale, toggleable, draw), toggleable, description, module)

    override val default: HudElement = hud
    override var value: HudElement = default

    val isEnabled: Boolean get() = module.enabled && value.enabled

    private val toggleAnimation = LinearAnimation<Float>(200)
    private val hoverHandler = HoverHandler(150)

    override fun render(x: Float, y: Float, mouseX: Float, mouseY: Float): Float {
        super.render(x, y, mouseX, mouseY)
        val height = getHeight()
        NVGRenderer.text(name, x + 6f, y + height / 2f - 8f, 16f, Colors.WHITE.rgba, NVGRenderer.defaultFont)

        val iconX = x + width - 30f
        val iconY = y + height / 2f - 12f
        hoverHandler.handle(iconX, iconY, 24f, 24f)

        val imageSize = 24f + (6f * hoverHandler.percent() / 100f)
        val offset = (imageSize - 24f) / 2f

        NVGRenderer.image(ClickGUI.movementImage, iconX - offset, iconY - offset, imageSize, imageSize)

        if (toggleable) {
            NVGRenderer.rect(x + width - 70f, y + height / 2f - 10f, 34f, 20f, gray38.rgba, 9f)

            if (value.enabled || toggleAnimation.isAnimating())
                NVGRenderer.rect(
                    x + width - 70f,
                    y + height / 2f - 10f,
                    toggleAnimation.get(34f, 9f, value.enabled),
                    20f,
                    ClickGUIModule.clickGUIColor.rgba,
                    9f
                )

            NVGRenderer.hollowRect(
                x + width - 70f,
                y + height / 2f - 10f,
                34f,
                20f,
                2f,
                ClickGUIModule.clickGUIColor.rgba,
                9f
            )
            NVGRenderer.circle(
                x + width - toggleAnimation.get(30f, 14f, !value.enabled) - 30f,
                y + height / 2f,
                6f,
                Colors.WHITE.rgba
            )
        }
        return height
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, mouseButton: Int): Boolean {
        if (mouseButton != 0) return false
        return if (isHovered) {
            mc.setScreen(HudManager)
            true
        } else if (toggleable && isAreaHovered(lastX + width - 70f, lastY + getHeight() / 2f - 10f, 34f, 20f)) {
            toggleAnimation.start()
            value.enabled = !value.enabled
            true

        } else false
    }

    override val isHovered: Boolean get() = isAreaHovered(lastX + width - 30F, lastY + getHeight() / 2f - 12f, 24f, 24f)

    override fun write(): JsonElement = with(JsonObject()) {
        addProperty("x", value.x)
        addProperty("y", value.y)
        addProperty("scale", value.scale)
        addProperty("enabled", value.enabled)
        return this
    }

    override fun read(element: JsonElement?) {
        if (element !is JsonObject) return
        value.x = element.get("x")?.asFloat ?: value.x
        value.y = element.get("y")?.asFloat ?: value.y
        value.scale = element.get("scale")?.asFloat ?: value.scale
        value.enabled = if (toggleable) element.get("enabled")?.asBoolean ?: value.enabled else true
    }
}