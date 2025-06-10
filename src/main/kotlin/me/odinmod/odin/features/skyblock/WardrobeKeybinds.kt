package me.odinmod.odin.features.skyblock

import me.odinmod.odin.OdinMod.mc
import me.odinmod.odin.config.categories.SkyblockConfig
import me.odinmod.odin.events.GuiEvent
import meteordevelopment.orbit.EventHandler
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.screen.slot.SlotActionType
import org.lwjgl.glfw.GLFW

object WardrobeKeybinds {

    private val wardrobeRegex = Regex("Wardrobe \\((\\d)/(\\d)\\)")
    private val equippedRegex = Regex("Slot (\\d): Equipped")

    @EventHandler
    fun onGuiEvent(event: GuiEvent.MouseClick) {
        if (onClick((event.screen as? HandledScreen<*>) ?: return, event.button)) event.cancel()
    }

    @EventHandler
    fun onGuiEvent(event: GuiEvent.KeyPress) {
        if (onClick((event.screen as? HandledScreen<*>) ?: return, event.keyCode)) event.cancel()
    }

    private fun onClick(screen: HandledScreen<*>, keyCode: Int): Boolean {
        if (!SkyblockConfig.wardrobeKeybinds) return false
        val title = screen.title?.string ?: return false
        val (current, total) = wardrobeRegex.find(title)?.destructured?.let { it.component1().toIntOrNull() to it.component2().toIntOrNull() } ?: return false
        if (current == null || total == null) return false

        val equippedIndex = screen.screenHandler.slots.find { equippedRegex.matches(it.stack.itemName.string) }?.index

        val index = when (keyCode) {
            SkyblockConfig.nextPageKeybind -> if (current < total) 53 else return false
            SkyblockConfig.previousPageKeybind -> if (current > total) 53 else return false
            SkyblockConfig.unequipKeybind -> equippedIndex ?: return false
            else -> {
                val keyIndex = with (SkyblockConfig) { arrayOf(wardrobe1, wardrobe2, wardrobe3, wardrobe4, wardrobe5, wardrobe6, wardrobe7, wardrobe8, wardrobe9) }
                    .indexOfFirst { it == keyCode }.takeIf { it != -1 } ?: return false
                if (equippedIndex == keyIndex + 36 && SkyblockConfig.disallowUnequipKeybind) return false
                keyIndex + 36
            }
        }

        mc.interactionManager?.clickSlot(screen.screenHandler.syncId, index, GLFW.GLFW_MOUSE_BUTTON_1, SlotActionType.PICKUP, mc.player)
        return true
    }
}