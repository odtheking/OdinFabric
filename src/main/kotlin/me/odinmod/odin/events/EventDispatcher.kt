package me.odinmod.odin.events

import me.odinmod.odin.OdinMod.mc
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents

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

        ScreenEvents.BEFORE_INIT.register { _, screen, _, _ ->
            GuiEvent.Open(screen).postAndCatch()

//            ScreenMouseEvents.beforeMouseClick(screen).register { _, mouseX, mouseY, button ->
//                GuiEvent.MouseClick(screen, mouseX.toInt(), mouseY.toInt(), button).postAndCatch()
//            }

            ScreenKeyboardEvents.beforeKeyPress(screen).register { _, keyCode, scanCode, modifiers ->
                GuiEvent.KeyPress(screen, keyCode, scanCode, modifiers).postAndCatch()
            }

            ScreenEvents.afterRender(screen).register { _, drawContext, _, _, _ ->
                GuiEvent.Render(screen, drawContext).postAndCatch()
            }
        }
    }
}