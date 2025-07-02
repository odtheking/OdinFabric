package me.odinmod.odin.events

import me.odinmod.odin.events.core.CancellableEvent
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen

abstract class GuiEvent(val screen: Screen): CancellableEvent() {

    class Open(screen: Screen) : GuiEvent(screen)

    class Close(screen: Screen) : GuiEvent(screen)

    class MouseClick(screen: Screen, val slotId: Int, val button: Int) : GuiEvent(screen)

    class KeyPress(screen: Screen, val keyCode: Int, val scanCode: Int, val modifiers: Int) : GuiEvent(screen)

    class Render(screen: Screen, val drawContext: DrawContext, val mouseX: Int, val mouseY: Int) : GuiEvent(screen)

    class NVGRender(screen: Screen) : GuiEvent(screen)
}