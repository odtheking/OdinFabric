package com.odtheking.odin.events

import com.odtheking.odin.events.core.Event
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext

abstract class RenderEvent(val context: WorldRenderContext) : Event() {

    class Last(context: WorldRenderContext) : RenderEvent(context)
}