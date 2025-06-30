package me.odinmod.odin.utils.ui.rendering

import com.mojang.blaze3d.opengl.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import me.odinmod.odin.OdinMod.mc
import me.odinmod.odin.utils.Color.Companion.alpha
import me.odinmod.odin.utils.Color.Companion.blue
import me.odinmod.odin.utils.Color.Companion.green
import me.odinmod.odin.utils.Color.Companion.red
import net.minecraft.client.gl.GlBackend
import net.minecraft.client.texture.GlTexture
import org.lwjgl.nanovg.NVGColor
import org.lwjgl.nanovg.NVGPaint
import org.lwjgl.nanovg.NanoSVG.*
import org.lwjgl.nanovg.NanoVG.*
import org.lwjgl.nanovg.NanoVGGL3.*
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30
import org.lwjgl.stb.STBImage.stbi_load_from_memory
import org.lwjgl.system.MemoryUtil.memAlloc
import org.lwjgl.system.MemoryUtil.memFree
import java.nio.ByteBuffer
import kotlin.math.max
import kotlin.math.min

object NVGRenderer {

    private val nvgPaint = NVGPaint.malloc()
    private val nvgColor = NVGColor.malloc()
    private val nvgColor2: NVGColor = NVGColor.malloc()

    val defaultFont = Font("Inter", "/assets/odin/Inter.otf")

    private val fontMap = HashMap<Font, NVGFont>()
    private val fontBounds = FloatArray(4)

    private val images = HashMap<Image, NVGImage>()

    private var scissor: Scissor? = null

    private var previousProgram = -1
    private var previousTexture = -1
    private var textureBinding = -1
    private var vg: Long = -1

    private var drawing: Boolean = false

    init {
        vg = nvgCreate(NVG_ANTIALIAS or NVG_DEBUG)
        require(vg != -1L) { "Failed to initialize NanoVG" }
    }

    fun beginFrame(width: Float, height: Float) {
        if (drawing) throw IllegalStateException("[NVGRenderer] Already drawing, but called beginFrame")

        previousProgram = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM)
        previousTexture = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE)
        textureBinding = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D)

        val framebuffer = mc.framebuffer
        val glFramebuffer = (framebuffer.colorAttachment as GlTexture).getOrCreateFramebuffer((RenderSystem.getDevice() as GlBackend).framebufferManager, null)
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, glFramebuffer)
        GL11.glViewport(0, 0, framebuffer.viewportWidth, framebuffer.viewportHeight)

        nvgBeginFrame(vg, width, height, 1f)
        nvgTextAlign(vg, NVG_ALIGN_LEFT or NVG_ALIGN_TOP)
        drawing = true
    }

    fun endFrame() {
        if (!drawing) throw IllegalStateException("[NVGRenderer] Not drawing, but called endFrame")
        nvgEndFrame(vg)
        GlStateManager._disableCull()
        GlStateManager._disableDepthTest()
        GlStateManager._enableBlend()
        GlStateManager._blendFuncSeparate(770, 771, 1, 0)

        if (previousProgram != -1) GL20.glUseProgram(previousProgram)
        else GL20.glUseProgram(0)

        if (previousTexture != -1) {
            GlStateManager._activeTexture(previousTexture)
            if (textureBinding != -1) GlStateManager._bindTexture(textureBinding)
        }

        drawing = false
    }

    fun push() = nvgSave(vg)

    fun pop() = nvgRestore(vg)

    fun scale(x: Float, y: Float) = nvgScale(vg, x, y)

    fun translate(x: Float, y: Float) = nvgTranslate(vg, x, y)

    fun rotate(amount: Float) = nvgRotate(vg, amount)

    fun globalAlpha(amount: Float) = nvgGlobalAlpha(vg, amount.coerceIn(0f, 1f))

    fun pushScissor(x: Float, y: Float, w: Float, h: Float) {
        scissor = Scissor(scissor, x, y, w + x, h + y)
        scissor?.applyScissor()
    }

    fun popScissor() {
        nvgResetScissor(vg)
        scissor = scissor?.previous
        scissor?.applyScissor()
    }

    fun line(x1: Float, y1: Float, x2: Float, y2: Float, thickness: Float, color: Int) {
        nvgBeginPath(vg)
        nvgMoveTo(vg, x1, y1)
        nvgLineTo(vg, x2, y2)
        nvgStrokeWidth(vg, thickness)
        color(color)
        nvgStrokeColor(vg, nvgColor)
        nvgStroke(vg)
    }

    fun drawHalfRoundedRect(x: Float, y: Float, w: Float, h: Float, color: Int, radius: Float, roundTop: Boolean) {
        nvgBeginPath(vg)

        if (roundTop) {
            nvgMoveTo(vg, x, y + h)
            nvgLineTo(vg, x + w, y + h)
            nvgLineTo(vg, x + w, y + radius)
            nvgArcTo(vg, x + w, y, x + w - radius, y, radius)
            nvgLineTo(vg, x + radius, y)
            nvgArcTo(vg, x, y, x, y + radius, radius)
            nvgLineTo(vg, x, y + h)
        } else {
            nvgMoveTo(vg, x, y)
            nvgLineTo(vg, x + w, y)
            nvgLineTo(vg, x + w, y + h - radius)
            nvgArcTo(vg, x + w, y + h, x + w - radius, y + h, radius)
            nvgLineTo(vg, x + radius, y + h)
            nvgArcTo(vg, x, y + h, x, y + h - radius, radius)
            nvgLineTo(vg, x, y)
        }

        nvgClosePath(vg)
        color(color)
        nvgFillColor(vg, nvgColor)
        nvgFill(vg)
    }

    fun rect(x: Float, y: Float, w: Float, h: Float, color: Int, radius: Float) {
        nvgBeginPath(vg)
        nvgRoundedRect(vg, x, y, w, h + .5f, radius)
        color(color)
        nvgFillColor(vg, nvgColor)
        nvgFill(vg)
    }

    fun rect(x: Float, y: Float, w: Float, h: Float, color: Int) {
        nvgBeginPath(vg)
        nvgRect(vg, x, y, w, h + .5f)
        color(color)
        nvgFillColor(vg, nvgColor)
        nvgFill(vg)
    }

    fun hollowRect(x: Float, y: Float, w: Float, h: Float, thickness: Float, color: Int, radius: Float) {
        nvgBeginPath(vg)
        nvgRoundedRect(vg, x, y, w, h, radius)
        nvgStrokeWidth(vg, thickness)
        nvgPathWinding(vg, NVG_HOLE)
        color(color)
        nvgStrokeColor(vg, nvgColor)
        nvgStroke(vg)
    }

    fun gradientRect(x: Float, y: Float, w: Float, h: Float, color1: Int, color2: Int, gradient: Gradient, radius: Float) {
        nvgBeginPath(vg)
        nvgRoundedRect(vg, x, y, w, h, radius)
        gradient(color1, color2, x, y, w, h, gradient)
        nvgFillPaint(vg, nvgPaint)
        nvgFill(vg)
    }

    fun dropShadow(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        blur: Float,
        spread: Float,
        radius: Float
    ) {
        nvgRGBA(0, 0, 0, 125, nvgColor)
        nvgRGBA(0, 0, 0, 0, nvgColor2)

        nvgBoxGradient(vg, x - spread, y - spread, width + 2 * spread, height + 2 * spread, radius + spread, blur, nvgColor, nvgColor2, nvgPaint)
        nvgBeginPath(vg)
        nvgRoundedRect(vg, x - spread - blur, y - spread - blur, width + 2 * spread + 2 * blur, height + 2 * spread + 2 * blur, radius + spread)
        nvgRoundedRect(vg, x, y, width, height, radius)
        nvgPathWinding(vg, NVG_HOLE)
        nvgFillPaint(vg, nvgPaint)
        nvgFill(vg)
    }

    fun circle(x: Float, y: Float, radius: Float, color: Int) {
        nvgBeginPath(vg)
        nvgCircle(vg, x, y, radius)
        color(color)
        nvgFillColor(vg, nvgColor)
        nvgFill(vg)
    }

    fun text(text: String, x: Float, y: Float, size: Float, color: Int, font: Font) {
        nvgFontSize(vg, size)
        nvgFontFaceId(vg, getFontID(font))
        color(color)
        nvgFillColor(vg, nvgColor)
        nvgText(vg, x, y + .5f, text)
    }

    fun textWidth(text: String, size: Float, font: Font): Float {
        nvgFontSize(vg, size)
        nvgFontFaceId(vg, getFontID(font))
        return nvgTextBounds(vg, 0f, 0f, text, fontBounds)
    }

    fun drawWrappedString(
        text: String,
        x: Float,
        y: Float,
        w: Float,
        size: Float,
        color: Int,
        font: Font,
        lineHeight: Float = 1f
    ) {
        nvgFontSize(vg, size)
        nvgFontFaceId(vg, getFontID(font))
        nvgTextLineHeight(vg, lineHeight)
        color(color)
        nvgFillColor(vg, nvgColor)
        nvgTextBox(vg, x, y, w, text)
    }

    fun wrappedTextBounds(
        text: String,
        w: Float,
        size: Float,
        font: Font,
        lineHeight: Float = 1f
    ): FloatArray {
        val bounds = FloatArray(4)
        nvgFontSize(vg, size)
        nvgFontFaceId(vg, getFontID(font))
        nvgTextLineHeight(vg, lineHeight)
        nvgTextBoxBounds(vg, 0f, 0f, w, text, bounds)
        return bounds // [minX, minY, maxX, maxY]
    }

    fun image(
        image: Image,
        x: Float,
        y: Float,
        w: Float,
        h: Float
    ) {
        nvgImagePattern(vg, x, y, w, h, 0f, getImage(image), 1f, nvgPaint)
        nvgBeginPath(vg)
        nvgRect(vg, x, y, w, h + .5f)
        nvgFillPaint(vg, nvgPaint)
        nvgFill(vg)
    }

    fun image(
        image: Image,
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        radius: Float
    ) {
        nvgImagePattern(vg, x, y, w, h, 0f, getImage(image), 1f, nvgPaint)
        nvgBeginPath(vg)
        nvgRoundedRect(vg, x, y, w, h + .5f, radius)
        nvgFillPaint(vg, nvgPaint)
        nvgFill(vg)
    }

    fun image(
        resourcePath: String,
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        radius: Float
    ) {
        val image = images.keys.find { it.identifier == resourcePath } ?: Image(resourcePath)
        if (image.isSVG) images.getOrPut(image) { NVGImage(0, loadSVG(image)) }.count++
        else images.getOrPut(image) { NVGImage(0, loadImage(image)) }.count++
        if (radius == 0f) image(image, x, y, w, h)
        else image(image, x, y, w, h, radius)
    }

    // lowers reference count by 1, if it reaches 0 it gets deleted from mem
    fun deleteImage(image: Image) {
        val nvgImage = images[image] ?: return
        nvgImage.count--
        if (nvgImage.count == 0) {
            nvgDeleteImage(vg, nvgImage.nvg)
            images.remove(image)
        }
    }

    private fun getImage(image: Image): Int {
        return images[image]?.nvg ?: throw IllegalStateException("Image (${image.identifier}) doesn't exist")
    }

    private fun loadImage(image: Image): Int {
        val w = IntArray(1)
        val h = IntArray(1)
        val channels = IntArray(1)
        val buffer = stbi_load_from_memory(
            image.buffer(),
            w,
            h,
            channels,
            4
        ) ?: throw NullPointerException("Failed to load image: ${image.identifier}")
        return nvgCreateImageRGBA(vg, w[0], h[0], 0, buffer)
    }

    private fun loadSVG(image: Image): Int {
        val vec = image.stream.use { it.bufferedReader().readText() }
        val svg = nsvgParse(vec, "px", 96f) ?: throw IllegalStateException("Failed to parse ${image.identifier}")

        val width = svg.width().toInt()
        val height = svg.height().toInt()
        val buffer = memAlloc(width * height * 4)

        try {
            val rasterizer = nsvgCreateRasterizer()
            nsvgRasterize(rasterizer, svg, 0f, 0f, 1f, buffer, width, height, width * 4)
            val nvgImage = nvgCreateImageRGBA(vg, width, height, 0, buffer)
            nsvgDeleteRasterizer(rasterizer)
            return nvgImage
        } finally {
            nsvgDelete(svg)
            memFree(buffer)
        }
    }


    private fun color(color: Int) {
        nvgRGBA(color.red.toByte(), color.green.toByte(), color.blue.toByte(), color.alpha.toByte(), nvgColor)
    }

    private fun color(color1: Int, color2: Int) {
        nvgRGBA(color1.red.toByte(), color1.green.toByte(), color1.blue.toByte(), color1.alpha.toByte(), nvgColor)
        nvgRGBA(color2.red.toByte(), color2.green.toByte(), color2.blue.toByte(), color2.alpha.toByte(), nvgColor2)
    }

    private fun gradient(color1: Int, color2: Int, x: Float, y: Float, w: Float, h: Float, direction: Gradient) {
        color(color1, color2)
        when (direction) {
            Gradient.LeftToRight -> nvgLinearGradient(vg, x, y, x + w, y, nvgColor, nvgColor2, nvgPaint)
            Gradient.TopToBottom -> nvgLinearGradient(vg, x, y, x, y + h, nvgColor, nvgColor2, nvgPaint)
        }
    }

    private fun getFontID(font: Font): Int {
        return fontMap.getOrPut(font) {
            val buffer = font.buffer()
            NVGFont(nvgCreateFontMem(vg, font.name, buffer, false), buffer)
        }.id
    }

    private class Scissor(val previous: Scissor?, val x: Float, val y: Float, val maxX: Float, val maxY: Float) {
        fun applyScissor() {
            if (previous == null) nvgScissor(vg, x, y, maxX - x, maxY - y)
            else {
                val x = max(x, previous.x)
                val y = max(y, previous.y)
                val width = max(0f, (min(maxX, previous.maxX) - x))
                val height = max(0f, (min(maxY, previous.maxY) - y))
                nvgScissor(vg, x, y, width, height)
            }
        }
    }

    private data class NVGImage(var count: Int, val nvg: Int)
    private data class NVGFont(val id: Int, val buffer: ByteBuffer)
}