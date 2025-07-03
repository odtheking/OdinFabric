package me.odinmod.odin.features.impl.skyblock

import me.odinmod.odin.clickgui.settings.impl.ColorSetting
import me.odinmod.odin.clickgui.settings.impl.KeybindSetting
import me.odinmod.odin.clickgui.settings.impl.MapSetting
import me.odinmod.odin.config.Config
import me.odinmod.odin.events.GuiEvent
import me.odinmod.odin.features.Module
import me.odinmod.odin.utils.Colors
import me.odinmod.odin.utils.drawLine
import me.odinmod.odin.utils.modMessage
import meteordevelopment.orbit.EventHandler
import meteordevelopment.orbit.EventPriority
import mixins.HandledScreenAccessor
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ingame.InventoryScreen
import net.minecraft.screen.slot.SlotActionType
import org.lwjgl.glfw.GLFW

object SlotBinds: Module(
    name = "Slot Binds",
    description = "Bind slots together for quick access.",
    key = null
) {
    private val setNewSlotbind by KeybindSetting("Bind set key", GLFW.GLFW_KEY_UNKNOWN, desc = "Key to set new bindings.")
    private val lineColor by ColorSetting("LineColor", Colors.MINECRAFT_GOLD, desc = "Color of the line drawn between slots.")
    private val slotBinds by MapSetting("slotBinds", mutableMapOf<Int, Int>())

    private var previousSlot: Int? = null

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onGuiClick(event: GuiEvent.MouseClick) {
        if (!Screen.hasShiftDown() || event.screen !is InventoryScreen) return

        val hoveredSlot = (event.screen as HandledScreenAccessor).focusedSlot?.takeIf { it.index in 0 until 40 } ?: return
        val boundSlot = slotBinds[hoveredSlot.index] ?: return

        val (from, to) = when {
            hoveredSlot.index in 0..8 -> boundSlot to hoveredSlot.index
            boundSlot in 0..8 -> hoveredSlot.index to boundSlot
            else -> return
        }

        mc.interactionManager?.clickSlot(event.screen.screenHandler.syncId, from, to, SlotActionType.SWAP, mc.player)
        event.cancel()
    }

    @EventHandler
    fun onGuiPress(event: GuiEvent.KeyPress) {
        if (event.screen !is InventoryScreen || event.keyCode != setNewSlotbind.code) return
        val hoveredSlot = (event.screen as HandledScreenAccessor).focusedSlot?.takeIf { it.index in 0 until 40 } ?: return

        previousSlot?.let { slot ->
            if (slot == hoveredSlot.index) return modMessage("§cYou can't bind a slot to itself.")
            if (slot !in 0..8 && hoveredSlot.index !in 0..8) return modMessage("§cOne of the slots must be in the hotbar (0–8).")

            modMessage("§aAdded bind from slot §b$slot §ato §d${hoveredSlot.index}.")
            slotBinds[slot] = hoveredSlot.index
            Config.save()
            previousSlot = null
        } ?: run {
            slotBinds.entries.firstOrNull { it.key == hoveredSlot.index }?.let {
                slotBinds.remove(it.key)
                Config.save()
                return modMessage("§cRemoved bind from slot §b${it.key} §cto §d${it.value}.")
            }
            previousSlot = hoveredSlot.index
        }
    }

    @EventHandler
    fun onRenderScreen(event: GuiEvent.Render) {
        val screen = event.screen as? InventoryScreen ?: return
        val hoveredSlot = (screen as HandledScreenAccessor).focusedSlot?.takeIf { it.index in 0 until 40 } ?: return
        val boundSlot = slotBinds[hoveredSlot.index]

        val (startX, startY) = screen.screenHandler.getSlot(previousSlot ?: hoveredSlot.id)?.let { slot ->
            slot.x + screen.x + 8 to slot.y + screen.y + 8 } ?: return

        val (endX, endY) = previousSlot?.let { event.mouseX to event.mouseY } ?: boundSlot?.let { slot ->
            screen.screenHandler.getSlot(slot)?.let { it.x + screen.x + 8 to it.y + screen.y + 8 } } ?: return

        if (previousSlot == null && !(Screen.hasShiftDown() && boundSlot != null)) return

        event.drawContext.matrices.push()
        event.drawContext.matrices.translate(0f, 0f, 999f)
        drawLine(event.drawContext, startX, startY, endX, endY, lineColor, 1)
        event.drawContext.matrices.pop()
    }

    @EventHandler
    fun onGuiClose(event: GuiEvent.Close) {
        previousSlot = null;
    }
}