package me.odinmod.odin.clickgui.settings.impl

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import me.odinmod.odin.clickgui.ClickGUI
import me.odinmod.odin.clickgui.ClickGUI.gray38
import me.odinmod.odin.clickgui.Panel
import me.odinmod.odin.clickgui.settings.RenderableSetting
import me.odinmod.odin.clickgui.settings.Saving
import me.odinmod.odin.features.impl.render.ClickGUIModule
import me.odinmod.odin.utils.Color
import me.odinmod.odin.utils.Color.Companion.darker
import me.odinmod.odin.utils.Color.Companion.hsbMax
import me.odinmod.odin.utils.Color.Companion.withAlpha
import me.odinmod.odin.utils.Colors
import me.odinmod.odin.utils.ui.MouseUtils.isAreaHovered
import me.odinmod.odin.utils.ui.TextInputHandler
import me.odinmod.odin.utils.ui.animations.EaseInOutAnimation
import me.odinmod.odin.utils.ui.animations.LinearAnimation
import me.odinmod.odin.utils.ui.rendering.Gradient
import me.odinmod.odin.utils.ui.rendering.NVGRenderer

class ColorSetting(
    name: String,
    override val default: Color,
    private var allowAlpha: Boolean = false,
    desc: String,
    hidden: Boolean = false
) : RenderableSetting<Color>(name, hidden, desc), Saving {

    override var value: Color = default.copy()

    private val expandAnim = EaseInOutAnimation(200)
    private val defaultHeight = Panel.HEIGHT
    private var extended = false

    private val mainSliderAnim = LinearAnimation<Float>(100)
    private var mainSliderPrevSat = 0f
    private var mainSliderPrevBright = 0f

    private val hueSliderAnim = LinearAnimation<Float>(100)
    private var hueSliderPrev = 0f

    private val alphaSliderAnim = LinearAnimation<Float>(100)
    private var alphaSliderPrev = 0f

    var section: Int? = null

    private var hexString = value.hex(allowAlpha)
        set(value) {
            if (value == field) return
            if (value.length > 8 && allowAlpha) return
            if (value.length > 6 && !allowAlpha) return
            field = value.filter { it in '0'..'9' || it in 'A'..'F' || it in 'a'..'f' }
            hexWidth = NVGRenderer.textWidth(field, 16f, NVGRenderer.defaultFont)
        }

    private var hexWidth = -1f

    private val textInputHandler = TextInputHandler(
        textProvider = { hexString },
        textSetter = { hexString = it }
    )

    private var previousMousePos = 0.0 to 0.0

    override fun render(x: Float, y: Float, mouseX: Double, mouseY: Double): Float {
        super.render(x, y, mouseX, mouseY)
        if (hexWidth < 0) {
            hexString = value.hex(allowAlpha)
            hexWidth = NVGRenderer.textWidth(hexString, 16f, NVGRenderer.defaultFont)
        }

        if (previousMousePos != mouseX to mouseY) textInputHandler.mouseDragged(mouseX)
        previousMousePos = mouseX to mouseY

        NVGRenderer.text(name, x + 6f, y + defaultHeight / 2f - 8f, 16f, Colors.WHITE.rgba, NVGRenderer.defaultFont)
        NVGRenderer.rect(x + width - 40f, y + defaultHeight / 2f - 10f, 34f, 20f, value.rgba, 5f)
        NVGRenderer.hollowRect(x + width - 40f, y + defaultHeight / 2f - 10f, 34f, 20f, 1.5f, value.withAlpha(1f).darker().rgba, 5f)

        if (!extended && !expandAnim.isAnimating()) return defaultHeight

        if (expandAnim.isAnimating()) NVGRenderer.pushScissor(x, y + defaultHeight, width, getHeight() - defaultHeight)
        // SATURATION AND BRIGHTNESS
        NVGRenderer.gradientRect(x + 10f, y + 38f, width - 20f, 170f, Colors.WHITE.rgba, value.hsbMax().rgba, Gradient.LeftToRight, 5f)
        NVGRenderer.gradientRect(x + 10f, y + 38f, width - 20f, 170f, Colors.TRANSPARENT.rgba, Colors.BLACK.rgba, Gradient.TopToBottom, 5f)

        val animatedSat = mainSliderAnim.get(mainSliderPrevSat, value.saturation, false)
        val animatedBright = mainSliderAnim.get(mainSliderPrevBright, value.brightness, false)
        val sbPointer = Pair((x + 10f + animatedSat * 220), (y + 38f + (1 - animatedBright) * 170))
        NVGRenderer.dropShadow(sbPointer.first - 8.5f, sbPointer.second - 8.5f, 17f, 17f, 2.5f, 2.5f, 9f)
        NVGRenderer.circle(sbPointer.first, sbPointer.second, 9f, value.darker(0.5f).rgba)
        NVGRenderer.circle(sbPointer.first, sbPointer.second, 7f, value.rgba)

        // HUE
        NVGRenderer.image(ClickGUI.hueImage, x + 10f, y + 214f, width - 20f, 15f, 5f)
        NVGRenderer.hollowRect(x + 10f, y + 214f, width - 20f, 15f, 1f, gray38.rgba, 5f)

        val huePos = x + 10f + hueSliderAnim.get(hueSliderPrev, value.hue, false) * 221f to y + 221f
        NVGRenderer.dropShadow(huePos.first - 8.5f, huePos.second - 8.5f, 17f, 17f, 2.5f, 2.5f, 9f)
        NVGRenderer.circle(huePos.first, huePos.second, 9f, value.hsbMax().darker(0.5f).rgba)
        NVGRenderer.circle(huePos.first, huePos.second, 7f, value.withAlpha(1f).hsbMax().rgba)

        // ALPHA
        if (allowAlpha) {
            NVGRenderer.gradientRect(x + 10f, y + 235f, width - 20f, 15f, Colors.TRANSPARENT.rgba, value.withAlpha(1f).rgba, Gradient.LeftToRight, 5f)

            val alphaPos = Pair((x + 10f + alphaSliderAnim.get(alphaSliderPrev, value.alphaFloat, false) * 220f), y + 243f)
            NVGRenderer.dropShadow(alphaPos.first - 8.5f, alphaPos.second - 8.5f, 17f, 17f, 2.5f, 2.5f, 9f)
            NVGRenderer.circle(alphaPos.first, alphaPos.second, 9f, Colors.WHITE.withAlpha(value.alphaFloat).darker(.5f).rgba)
            NVGRenderer.circle(alphaPos.first, alphaPos.second, 7f, Colors.WHITE.withAlpha(value.alphaFloat).rgba)
        }

        when (section) {
            0 -> {
                val newSaturation = (mouseX.toFloat() - (x + 10f)) / 220f
                val newBrightness = -((mouseY.toFloat() - (y + 38f)) - 170f) / 170f
                if (newSaturation != value.saturation || newBrightness != value.brightness) {
                    mainSliderPrevSat = mainSliderAnim.get(mainSliderPrevSat, value.saturation, false)
                    mainSliderPrevBright = mainSliderAnim.get(mainSliderPrevBright, value.brightness, false)
                    mainSliderAnim.start()
                    value.saturation = newSaturation.coerceIn(0f, 1f)
                    value.brightness = newBrightness.coerceIn(0f, 1f)
                }
            }
            1 -> {
                val newHue = (mouseX.toFloat() - (x + 10f)) / (width - 20f)
                if (newHue != value.hue) {
                    hueSliderPrev = hueSliderAnim.get(hueSliderPrev, value.hue, false)
                    hueSliderAnim.start()
                    value.hue = newHue.coerceIn(0f, 1f)
                }
            }
            2 -> {
                val newAlpha = (mouseX.toFloat() - (x + 10f)) / (width - 20f)
                if (newAlpha != value.alphaFloat) {
                    alphaSliderPrev = alphaSliderAnim.get(alphaSliderPrev, value.alphaFloat, false)
                    alphaSliderAnim.start()
                    value.alphaFloat = newAlpha.coerceIn(0f, 1f)
                }
            }
        }

        if (section != null) hexString = value.hex(allowAlpha)
        else {
            if (hexString.length == 8 && allowAlpha || hexString.length == 6 && !allowAlpha)
                value = Color(if (allowAlpha) hexString else hexString + "FF")
        }

        val rectX = x + (width - width / 2) / 2
        val actualHeight = defaultHeight + if (allowAlpha) 250f else 230f

        NVGRenderer.rect(rectX, y + actualHeight - 26f, width / 2, 24f, gray38.rgba, 4f)
        NVGRenderer.hollowRect(rectX, y + actualHeight - 26f, width / 2, 24f, 2f, ClickGUIModule.clickGUIColor.rgba, 4f)

        textInputHandler.x = rectX + (width / 4) - (hexWidth / 2)
        textInputHandler.y = y + actualHeight - 24f
        textInputHandler.width = width / 2
        textInputHandler.draw()

        if (expandAnim.isAnimating()) NVGRenderer.popScissor()
        return getHeight()
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, mouseButton: Int): Boolean {
        if (isHovered) {
            expandAnim.start()
            extended = !extended
            return true
        }

        if (!extended) return false
        textInputHandler.mouseClicked(mouseX, mouseY, mouseButton)

        section = when {
            isAreaHovered(lastX + 10f, lastY + 38f, width - 20f, 170f) -> 0 // sat & brightness
            isAreaHovered(lastX + 10f, lastY + 214f, width - 20f, 15f) -> 1 // hue
            isAreaHovered(lastX + 10f, lastY + 235f, width - 20f, 15f) && allowAlpha -> 2 // alpha
            else -> null
        }

        return section != null
    }

    override fun mouseReleased(state: Int) {
        if (state == 0) textInputHandler.mouseReleased()
        section = null
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        return if (extended) textInputHandler.keyPressed(keyCode, scanCode, modifiers)
        else false
    }

    override fun keyTyped(typedChar: Char, modifier: Int): Boolean {
        return if (extended) textInputHandler.keyTyped(typedChar)
        else false
    }

    override fun getHeight(): Float =
        expandAnim.get(defaultHeight, defaultHeight + if (allowAlpha) 250f else 230f, !extended)

    override val isHovered: Boolean get() = isAreaHovered(lastX + width - 40f, lastY + defaultHeight / 2f - 10f, 34f, 20f)

    override fun write(): JsonElement = JsonPrimitive("#${value.hex()}")

    override fun read(element: JsonElement?) {
        if (element?.asString?.startsWith("#") == true) value = Color(element.asString.drop(1))
    }
}