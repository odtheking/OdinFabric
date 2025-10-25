package com.odtheking.odin.utils.render.item

import com.mojang.blaze3d.textures.FilterMode
import com.odtheking.mixin.accessors.SpecialGuiElementRendererAccessor
import com.odtheking.odin.OdinMod.mc
import net.minecraft.client.gl.RenderPipelines
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.ScreenRect
import net.minecraft.client.gui.render.SpecialGuiElementRenderer
import net.minecraft.client.gui.render.state.GuiRenderState
import net.minecraft.client.gui.render.state.ItemGuiElementRenderState
import net.minecraft.client.gui.render.state.TexturedQuadGuiElementRenderState
import net.minecraft.client.gui.render.state.special.SpecialGuiElementRenderState
import net.minecraft.client.render.DiffuseLighting
import net.minecraft.client.render.LightmapTextureManager
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.item.KeyedItemRenderState
import net.minecraft.client.texture.TextureSetup
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.ItemDisplayContext
import net.minecraft.item.ItemStack
import org.joml.Matrix3x2f
import java.util.*

class ItemStateRenderer(vertexConsumers: VertexConsumerProvider.Immediate)
    : SpecialGuiElementRenderer<ItemStateRenderer.State>(vertexConsumers) {

    private var lastState: State? = null

    override fun render(state: State, matrices: MatrixStack) {
        lastState = state
        matrices.scale(1f, -1f, -1f)

        if (state.state.state().isSideLit) mc.gameRenderer.diffuseLighting.setShaderLights(DiffuseLighting.Type.ITEMS_3D)
        else mc.gameRenderer.diffuseLighting.setShaderLights(DiffuseLighting.Type.ITEMS_FLAT)

        state.state.state().render(matrices, vertexConsumers, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV)
    }

    private val textureView by lazy { TextureSetup.of((this as SpecialGuiElementRendererAccessor).getTextureView()?.apply { texture().setTextureFilter(FilterMode.NEAREST, false) }) }

    override fun renderElement(element: State, state: GuiRenderState?) {
        state?.addSimpleElementToCurrentLayer(
            TexturedQuadGuiElementRenderState(
                RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA, textureView,
                element.pose(), element.x1(), element.y1(), element.x1() + 16, element.y1() + 16,
                0.0f, 1.0f, 1.0f, 0.0f, -1, element.scissorArea(), null
            )
        )
    }

    override fun shouldBypassScaling(state: State): Boolean = lastState != null && lastState == state
    override fun getYOffset(height: Int, windowScaleFactor: Int): Float = height / 2f
    override fun getElementClass(): Class<State> = State::class.java
    override fun getName(): String = "item_state"

    data class State(val state: ItemGuiElementRenderState) : SpecialGuiElementRenderState {
        override fun scale(): Float = maxOf(state.pose().m00(), state.pose().m11()) * 16f
        override fun x1(): Int = state.x()
        override fun y1(): Int = state.y()
        override fun x2(): Int = state.x() + scale().toInt()
        override fun y2(): Int = state.y() + scale().toInt()
        override fun scissorArea(): ScreenRect? = state.scissorArea()
        override fun bounds(): ScreenRect? = state.bounds()
        override fun pose(): Matrix3x2f = state.pose()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is State) return false
            if (other.state.state().modelKey != state.state().modelKey) return false
            if (other.state.pose().m00() != state.pose().m00()) return false
            if (other.state.pose().m11() != state.pose().m11()) return false
            return true
        }

        override fun hashCode(): Int {
            return Objects.hash(state.state().modelKey, state.pose().m00(), state.pose().m11())
        }
    }

    companion object {
        fun draw(context: DrawContext, item: ItemStack, x: Int, y: Int) {
            if (item.isEmpty) return

            val keyed = KeyedItemRenderState()
            mc.itemModelManager.clearAndUpdate(keyed, item, ItemDisplayContext.GUI, mc.world, mc.player, 0)

            val state = State(
                ItemGuiElementRenderState(
                    item.item.name.string,
                    Matrix3x2f(context.matrices),
                    keyed, x, y,
                    context.scissorStack.peekLast()
                )
            )
            context.state.addSpecialElement(state)
        }
    }
}
