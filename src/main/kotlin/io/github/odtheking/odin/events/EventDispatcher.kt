package io.github.odtheking.odin.events

import io.github.odtheking.odin.OdinMod.mc
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents

object EventDispatcher {

    init {
        ClientPlayConnectionEvents.JOIN.register { handler, _, _ ->
            ServerEvent.Connect(handler.serverInfo?.address ?: "SinglePlayer").postAndCatch()
        }

        ClientPlayConnectionEvents.DISCONNECT.register { handler, _ ->
            ServerEvent.Disconnect(handler.serverInfo?.address ?: "SinglePlayer").postAndCatch()
        }

        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register { _, _ ->
            WorldLoadEvent().postAndCatch()
        }

        ClientTickEvents.START_CLIENT_TICK.register { _ ->
            mc.world?.let { TickEvent.Start().postAndCatch() }
        }

        ClientTickEvents.END_CLIENT_TICK.register { _ ->
            mc.world?.let { TickEvent.End().postAndCatch() }
        }

        WorldRenderEvents.AFTER_TRANSLUCENT.register { context ->
            mc.world?.let { RenderEvent.Last(context).postAndCatch() }
        }
    }
}