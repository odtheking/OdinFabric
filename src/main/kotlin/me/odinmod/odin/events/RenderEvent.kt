package me.odinmod.odin.events

import me.odinmod.odin.events.core.Event
import net.minecraft.network.packet.Packet
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext

abstract class RenderEvent(val context: WorldRenderContext): Event() {

    class Last(context: WorldRenderContext) : RenderEvent(context)

}