package com.odtheking.odin.utils.render

import net.minecraft.client.renderer.rendertype.LayeringTransform
import net.minecraft.client.renderer.rendertype.RenderSetup
import net.minecraft.client.renderer.rendertype.RenderType

object CustomRenderLayer {

    val LINE_LIST: RenderType = RenderType.create(
        "line-list",
        RenderSetup.builder(CustomRenderPipelines.LINE_LIST)
            .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
            .createRenderSetup()
    )

    val LINE_LIST_ESP: RenderType = RenderType.create(
        "line-list-esp",
        RenderSetup.builder(CustomRenderPipelines.LINE_LIST_ESP)
            .createRenderSetup()
    )

    val TRIANGLE_STRIP: RenderType = RenderType.create(
        "triangle_strip",
        RenderSetup.builder(CustomRenderPipelines.TRIANGLE_STRIP)
            .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
            .createRenderSetup()
    )

    val TRIANGLE_STRIP_ESP: RenderType = RenderType.create(
        "triangle_strip_esp",
        RenderSetup.builder(CustomRenderPipelines.TRIANGLE_STRIP_ESP)
            .createRenderSetup()
    )

    // New layers for Gizmos-style rendering with proper depth support
    val DEBUG_FILLED_BOX_DEPTH: RenderType = RenderType.create(
        "debug_filled_box_depth",
        RenderSetup.builder(CustomRenderPipelines.DEBUG_FILLED_BOX_DEPTH)
            .sortOnUpload()
            .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
            .createRenderSetup()
    )

    val DEBUG_FILLED_BOX_NO_DEPTH: RenderType = RenderType.create(
        "debug_filled_box_no_depth",
        RenderSetup.builder(CustomRenderPipelines.DEBUG_FILLED_BOX_NO_DEPTH)
            .sortOnUpload()
            .createRenderSetup()
    )
}