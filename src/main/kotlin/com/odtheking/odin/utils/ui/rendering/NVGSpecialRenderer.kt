package com.odtheking.odin.utils.ui.rendering

import com.mojang.blaze3d.opengl.GlConst
import com.mojang.blaze3d.opengl.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import com.odtheking.odin.OdinMod.mc
import net.minecraft.client.gl.GlBackend
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.ScreenRect
import net.minecraft.client.gui.render.SpecialGuiElementRenderer
import net.minecraft.client.gui.render.state.special.SpecialGuiElementRenderState
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.texture.GlTexture
import net.minecraft.client.util.math.MatrixStack
import org.joml.Matrix3x2f

class NVGSpecialRenderer(vertexConsumers: VertexConsumerProvider.Immediate)
    : SpecialGuiElementRenderer<NVGSpecialRenderer.NVGRenderState>(vertexConsumers) {

    private var lastState: NVGRenderState? = null

    override fun render(state: NVGRenderState, matrices: MatrixStack) {
        lastState = state

        val colorTex = RenderSystem.outputColorTextureOverride

        val bufferManager = (RenderSystem.getDevice() as? GlBackend)?.bufferManager ?: return
        val glDepthTex = (RenderSystem.outputDepthTextureOverride?.texture() as? GlTexture) ?: return

        (colorTex?.texture() as? GlTexture)?.getOrCreateFramebuffer(bufferManager, glDepthTex)?.apply {
            GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, this)
            GlStateManager._viewport(0, 0, colorTex.getWidth(0), colorTex.getHeight(0))
        }

        NVGRenderer.beginFrame(mc.window.width.toFloat(), mc.window.height.toFloat())
        state.renderContent()
        NVGRenderer.endFrame()
        GlStateManager._disableDepthTest()
    }

    override fun getYOffset(height: Int, windowScaleFactor: Int): Float = height / 2f
    override fun getElementClass(): Class<NVGRenderState> = NVGRenderState::class.java
    override fun getName(): String = "nvg_renderer"

    data class NVGRenderState(
        private val x: Int,
        private val y: Int,
        private val width: Int,
        private val height: Int,
        private val poseMatrix: Matrix3x2f,
        private val scissor: ScreenRect?,
        private val bounds: ScreenRect?,
        val renderContent: () -> Unit
    ) : SpecialGuiElementRenderState {

        override fun scale(): Float = 1f
        override fun x1(): Int = x
        override fun y1(): Int = y
        override fun x2(): Int = x + width
        override fun y2(): Int = y + height
        override fun scissorArea(): ScreenRect? = scissor
        override fun bounds(): ScreenRect? = bounds
    }

    companion object {
        /**
         * Draw NVG content as a special GUI element.
         *
         * @param context The DrawContext to draw to
         * @param x The x position
         * @param y The y position
         * @param width The width of the rendering area
         * @param height The height of the rendering area
         * @param renderContent A lambda that draws the NVG content
         */
        fun draw(
            context: DrawContext,
            x: Int,
            y: Int,
            width: Int,
            height: Int,
            renderContent: () -> Unit
        ) {
            val scissor = context.scissorStack.peekLast()
            val pose = Matrix3x2f(context.matrices)
            val bounds = createBounds(x, y, x + width, y + height, pose, scissor)

            val state = NVGRenderState(
                x, y, width, height,
                pose, scissor, bounds,
                renderContent
            )
            context.state.addSpecialElement(state)
        }

        private fun createBounds(x0: Int, y0: Int, x1: Int, y1: Int, pose: Matrix3x2f, scissorArea: ScreenRect?): ScreenRect? {
            val screenRect = ScreenRect(x0, y0, x1 - x0, y1 - y0).transformEachVertex(pose)
            return if (scissorArea != null) scissorArea.intersection(screenRect) else screenRect
        }
    }
}

