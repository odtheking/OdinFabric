package com.odtheking.odin.utils.render

import net.minecraft.client.renderer.RenderStateShard
import net.minecraft.client.renderer.RenderType
import java.util.*

object CustomRenderLayer {

    val LINE_LIST: RenderType.CompositeRenderType = RenderType.create(
        "line-list",
        RenderType.BIG_BUFFER_SIZE,
        CustomRenderPipelines.LINE_LIST,
        RenderType.CompositeState.builder()
            .setLayeringState(RenderType.VIEW_OFFSET_Z_LAYERING)
            .setLineState(RenderStateShard.LineStateShard(OptionalDouble.of(3.0)))
            .createCompositeState(false)
    )

    val LINE_LIST_ESP: RenderType.CompositeRenderType = RenderType.create(
        "line-list-esp",
        RenderType.BIG_BUFFER_SIZE,
        CustomRenderPipelines.LINE_LIST_ESP,
        RenderType.CompositeState
            .builder()
            .setLineState(RenderStateShard.LineStateShard(OptionalDouble.of(3.0)))
            .createCompositeState(false)
    )

    val TRIANGLE_STRIP: RenderType.CompositeRenderType = RenderType.create(
        "triangle_strip",
        RenderType.BIG_BUFFER_SIZE,
        false,
        true,
        CustomRenderPipelines.TRIANGLE_STRIP,
        RenderType.CompositeState.builder()
            .setLayeringState(RenderType.VIEW_OFFSET_Z_LAYERING)
            .createCompositeState(false)
    )

    val TRIANGLE_STRIP_ESP: RenderType.CompositeRenderType = RenderType.create(
        "triangle_strip_esp",
        RenderType.BIG_BUFFER_SIZE,
        false,
        true,
        CustomRenderPipelines.TRIANGLE_STRIP_ESP,
        RenderType.CompositeState.builder().createCompositeState(false)
    )
}