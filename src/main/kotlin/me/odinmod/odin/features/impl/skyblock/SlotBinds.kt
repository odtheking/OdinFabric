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
import me.odinmod.odin.utils.windowClick
import meteordevelopment.orbit.EventHandler
import meteordevelopment.orbit.EventPriority
import mixins.HandledScreenAccessor
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ingame.InventoryScreen
import net.minecraft.screen.slot.Slot
import net.minecraft.screen.slot.SlotActionType
import org.lwjgl.glfw.GLFW

object SlotBinds: Module(
    name = "Slot Binds",
    description = "Bind slots together for quick access.",
    key = null
) {
    private val setNewSlotbind by KeybindSetting(
        "Bind set key",
        GLFW.GLFW_KEY_UNKNOWN,
        desc = "Key to set new bindings."
    )
    private val lineColor by ColorSetting(
        "LineColor",
        Colors.MINECRAFT_GOLD,
        desc = "Color of the line drawn between slots."
    )
    private val slotBinds by MapSetting("slotBinds", mutableMapOf<Int, Int>())

    private var previousSlot: Int? = null

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onGuiClick(event: GuiEvent.MouseClick) {
        val screen = event.screen as? InventoryScreen ?: return
        if (!Screen.hasShiftDown()) return

        val hoveredSlot = (screen as HandledScreenAccessor).focusedSlot?.takeIf { it.index in 0 until 40 } ?: return
        val boundSlot = slotBinds[hoveredSlot.index] ?: return

        val (from, to) = when {
            hoveredSlot.index in 0..8 -> boundSlot to hoveredSlot.index
            boundSlot in 0..8 -> hoveredSlot.index to boundSlot
            else -> return
        }

        val fromSlot = screen.screenHandler.slots.indexOfFirst { it.inventory == mc.player?.inventory && it.index == from }.takeIf { it >= 0 } ?: return

        windowClick(fromSlot, to, SlotActionType.SWAP)
        event.cancel()
    }

    @EventHandler
    fun onGuiPress(event: GuiEvent.KeyPress) {
        val screen = event.screen as? InventoryScreen ?: return

        if (event.keyCode != setNewSlotbind.code) return

        val hoveredSlot = (screen as HandledScreenAccessor).focusedSlot?.takeIf { it.index in 0 until 40 } ?: return
        previousSlot?.let { slot ->
            if (slot == hoveredSlot.index) {
                modMessage("§cYou can't bind a slot to itself.")
                return
            }

            if (slot !in 0..8 && hoveredSlot.index !in 0..8) {
                modMessage("§cOne of the slots must be in the hotbar (0–8).")
                return
            }

            modMessage("§aAdded bind from slot §b$slot §ato §d${hoveredSlot.index}.")
            slotBinds[slot] = hoveredSlot.index
            Config.save()
            previousSlot = null
        } ?: run {
            val existing = slotBinds.entries.firstOrNull { it.key == hoveredSlot.index }
            if (existing != null) {
                slotBinds.remove(existing.key)
                Config.save()
                modMessage("§cRemoved bind from slot §b${existing.key} §cto §d${existing.value}.")
            } else {
                previousSlot = hoveredSlot.index
            }
        }
    }

    @EventHandler
    fun onRenderScreen(event: GuiEvent.RenderScreen) {
        val screen = event.screen as? InventoryScreen ?: return

        val hoveredSlot = (screen as HandledScreenAccessor).focusedSlot?.takeIf { it.index in 0 until 40 } ?: return
        val boundSlot = slotBinds[hoveredSlot.index]

        val slotA = screen.screenHandler.slots.firstOrNull {
            it.inventory == mc.player?.inventory && it.index == (previousSlot ?: hoveredSlot.index)
        } ?: return
        val startX = slotA.x + screen.x + 8
        val startY = slotA.y + screen.y + 8

        val (endX, endY) = previousSlot?.let {
            event.mouseX to event.mouseY
        } ?: boundSlot.let { index ->
            val slotB: Slot = screen.screenHandler.slots.firstOrNull { it.inventory == mc.player?.inventory && it.index == index } ?: return
            slotB.x + screen.x + 8 to slotB.y + screen.y + 8
        }

        if (previousSlot == null && !(Screen.hasShiftDown())) return

        event.drawContext.matrices.push()
        event.drawContext.matrices.translate(0f, 0f, 999f)
        drawLine(event.drawContext, startX, startY, endX, endY, lineColor, 2f)
        event.drawContext.matrices.pop()
    }

    @EventHandler
    fun onGuiClose(event: GuiEvent.Open) {
        if (event.screen == null) previousSlot = null;
    }
}