package me.odinmod.odin.events

import me.odinmod.odin.OdinMod
import me.odinmod.odin.OdinMod.Companion.mc
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents

object EventDispatcher {

    init {
        ClientPlayConnectionEvents.JOIN.register { handler, _, _ ->
            OdinMod.EVENT_BUS.post(handler.serverInfo?.address?.let { ServerEvent.Connect(it) })
        }

        ClientPlayConnectionEvents.DISCONNECT.register { handler, _ ->
            OdinMod.EVENT_BUS.post(handler.serverInfo?.address?.let { ServerEvent.Disconnect(it) })
        }

        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register { _, _ ->
            OdinMod.EVENT_BUS.post(WorldLoadEvent())
        }

        ClientTickEvents.START_CLIENT_TICK.register { _ ->
            mc.world?.let { OdinMod.EVENT_BUS.post(TickEvent.Start()) }
        }

        ClientTickEvents.END_CLIENT_TICK.register { _ ->
            mc.world?.let { OdinMod.EVENT_BUS.post(TickEvent.End()) }
        }
    }
}