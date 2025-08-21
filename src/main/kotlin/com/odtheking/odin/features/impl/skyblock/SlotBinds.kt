package com.odtheking.odin.features.impl.skyblock

import com.odtheking.mixin.accessors.HandledScreenAccessor
import com.odtheking.odin.clickgui.settings.impl.ColorSetting
import com.odtheking.odin.clickgui.settings.impl.KeybindSetting
import com.odtheking.odin.clickgui.settings.impl.MapSetting
import com.odtheking.odin.config.Config
import com.odtheking.odin.events.GuiEvent
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.render.drawLine
import meteordevelopment.orbit.EventHandler
import meteordevelopment.orbit.EventPriority
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ingame.InventoryScreen
import net.minecraft.screen.slot.SlotActionType
import org.lwjgl.glfw.GLFW

object SlotBinds : Module(
    name = "Slot Binds",
    description = "Bind slots together for quick access.",
    key = null
) {
    private val setNewSlotbind by KeybindSetting("Bind set key", GLFW.GLFW_KEY_UNKNOWN, desc = "Key to set new bindings.")
    private val lineColor by ColorSetting("Line Color", Colors.MINECRAFT_GOLD, desc = "Color of the line drawn between slots.")
    private val slotBinds by MapSetting("SlotBinds", mutableMapOf<Int, Int>())

    private var previousSlot: Int? = null

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onGuiClick(event: GuiEvent.SlotClick) {
        if (!Screen.hasShiftDown() || event.screen !is InventoryScreen) return
        val clickedSlot = (event.screen as HandledScreenAccessor).focusedSlot?.id?.takeIf { it in 5 until 45 }
            ?: return modMessage("§cYou must be hovering over a valid slot (5–44).")
        val boundSlot = slotBinds[clickedSlot] ?: return

        val (from, to) = when {
            clickedSlot in 36..44 -> boundSlot to clickedSlot
            boundSlot in 36..44 -> clickedSlot to boundSlot
            else -> return
        }

        mc.interactionManager?.clickSlot(
            event.screen.screenHandler.syncId,
            from,
            to % 36,
            SlotActionType.SWAP,
            mc.player
        )
        event.cancel()
    }

    @EventHandler
    fun onGuiPress(event: GuiEvent.KeyPress) {
        if (event.screen !is InventoryScreen || event.keyCode != setNewSlotbind.code) return
        val clickedSlot = (event.screen as HandledScreenAccessor).focusedSlot?.id?.takeIf { it in 5 until 45 } ?: return

        event.cancel()
        previousSlot?.let { slot ->
            if (slot == clickedSlot) return modMessage("§cYou can't bind a slot to itself.")
            if (slot !in 36..44 && clickedSlot !in 36..44) return modMessage("§cOne of the slots must be in the hotbar (36–44).")
            modMessage("§aAdded bind from slot §b$slot §ato §d${clickedSlot}.")

            slotBinds[slot] = clickedSlot
            Config.save()
            previousSlot = null
        } ?: run {
            slotBinds.entries.firstOrNull { it.key == clickedSlot }?.let {
                slotBinds.remove(it.key)
                Config.save()
                return modMessage("§cRemoved bind from slot §b${it.key} §cto §d${it.value}.")
            }
            previousSlot = clickedSlot
        }
    }

    @EventHandler
    fun onRenderScreen(event: GuiEvent.Draw) {
        val screen = event.screen as? InventoryScreen ?: return
        val hoveredSlot = (screen as HandledScreenAccessor).focusedSlot?.id?.takeIf { it in 5 until 45 } ?: return
        val boundSlot = slotBinds[hoveredSlot]

        val (startX, startY) = screen.screenHandler.getSlot(previousSlot ?: hoveredSlot)?.let { slot ->
            slot.x + screen.x + 8 to slot.y + screen.y + 8
        } ?: return

        val (endX, endY) = previousSlot?.let { event.mouseX to event.mouseY } ?: boundSlot?.let { slot ->
            screen.screenHandler.getSlot(slot)?.let { it.x + screen.x + 8 to it.y + screen.y + 8 }
        } ?: return

        if (previousSlot == null && !(Screen.hasShiftDown())) return

        event.drawContext.matrices.push()
        event.drawContext.matrices.translate(0f, 0f, 999f)
        event.drawContext.drawLine(startX.toFloat(), startY.toFloat(), endX.toFloat(), endY.toFloat(), lineColor, 1f)
        event.drawContext.matrices.pop()
    }

    @EventHandler
    fun onGuiClose(event: GuiEvent.Close) {
        previousSlot = null;
    }
}