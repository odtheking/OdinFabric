package com.odtheking.odin.features.impl.skyblock

import com.odtheking.mixin.accessors.AbstractContainerScreenAccessor
import com.odtheking.odin.clickgui.settings.impl.ColorSetting
import com.odtheking.odin.clickgui.settings.impl.KeybindSetting
import com.odtheking.odin.clickgui.settings.impl.MapSetting
import com.odtheking.odin.config.Config
import com.odtheking.odin.events.GuiEvent
import com.odtheking.odin.events.core.EventPriority
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.clickSlot
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.render.drawLine
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.InventoryScreen
import net.minecraft.world.inventory.ClickType
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

    init {
        on<GuiEvent.SlotClick> (EventPriority.HIGHEST) {
            if (!Screen.hasShiftDown() || screen !is InventoryScreen) return@on
            val clickedSlot = (screen as AbstractContainerScreenAccessor).hoveredSlot?.index?.takeIf { it in 5 until 45 } ?: return@on
            val boundSlot = slotBinds[clickedSlot] ?: return@on

            val (from, to) = when {
                clickedSlot in 36..44 -> boundSlot to clickedSlot
                boundSlot in 36..44 -> clickedSlot to boundSlot
                else -> return@on
            }

            mc.player?.clickSlot(screen.menu.containerId, from, to % 36, ClickType.SWAP)
            cancel()
        }

        on<GuiEvent.KeyPress> {
            if (screen !is InventoryScreen || keyCode != setNewSlotbind.value) return@on
            val clickedSlot = (screen as AbstractContainerScreenAccessor).hoveredSlot?.index?.takeIf { it in 5 until 45 } ?: return@on

            cancel()
            previousSlot?.let { slot ->
                if (slot == clickedSlot) return@on modMessage("§cYou can't bind a slot to itself.")
                if (slot !in 36..44 && clickedSlot !in 36..44) return@on modMessage("§cOne of the slots must be in the hotbar (36–44).")
                modMessage("§aAdded bind from slot §b$slot §ato §d${clickedSlot}.")

                slotBinds[slot] = clickedSlot
                Config.save()
                previousSlot = null
            } ?: run {
                slotBinds.entries.firstOrNull { it.key == clickedSlot }?.let {
                    slotBinds.remove(it.key)
                    Config.save()
                    return@on modMessage("§cRemoved bind from slot §b${it.key} §cto §d${it.value}.")
                }
                previousSlot = clickedSlot
            }
        }

        on<GuiEvent.DrawTooltip> {
            val screen = screen as? InventoryScreen ?: return@on
            val hoveredSlot = (screen as AbstractContainerScreenAccessor).hoveredSlot?.index?.takeIf { it in 5 until 45 } ?: return@on
            val boundSlot = slotBinds[hoveredSlot]

            val (startX, startY) = screen.menu.getSlot(previousSlot ?: hoveredSlot)?.let { slot ->
                slot.x + screen.x + 8 to slot.y + screen.y + 8
            } ?: return@on

            val (endX, endY) = previousSlot?.let { mouseX to mouseY } ?: boundSlot?.let { slot ->
                screen.menu.getSlot(slot)?.let { it.x + screen.x + 8 to it.y + screen.y + 8 }
            } ?: return@on

            if (previousSlot == null && !(Screen.hasShiftDown())) return@on

            guiGraphics.drawLine(startX.toFloat(), startY.toFloat(), endX.toFloat(), endY.toFloat(), lineColor, 1f)
        }

        on<GuiEvent.Close> {
            previousSlot = null
        }
    }
}