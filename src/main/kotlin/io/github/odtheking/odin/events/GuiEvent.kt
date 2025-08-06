package io.github.odtheking.odin.events

import io.github.odtheking.odin.clickgui.ClickGUI
import io.github.odtheking.odin.events.core.CancellableEvent
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.screen.slot.Slot

abstract class GuiEvent(val screen: Screen) : CancellableEvent() {

    class Open(screen: Screen) : GuiEvent(screen)

    class Close(screen: Screen) : GuiEvent(screen)

    class SlotClick(screen: Screen, val slotId: Int, val button: Int) : GuiEvent(screen)

    class MouseClick(screen: Screen, val mouseX: Int, val mouseY: Int, val button: Int) : GuiEvent(screen)

    class KeyPress(screen: Screen, val keyCode: Int, val scanCode: Int, val modifiers: Int) : GuiEvent(screen)

    class Render(screen: Screen, val drawContext: DrawContext, val mouseX: Int, val mouseY: Int) : GuiEvent(screen)

    class DrawSlot(screen: Screen, val drawContext: DrawContext, val slot: Slot) : GuiEvent(screen)

    class NVGRender(clickGui: ClickGUI) : GuiEvent(clickGui)
}