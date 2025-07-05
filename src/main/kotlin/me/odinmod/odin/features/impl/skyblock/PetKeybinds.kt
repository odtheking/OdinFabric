package me.odinmod.odin.features.impl.skyblock

import me.odinmod.odin.clickgui.settings.Setting.Companion.withDependency
import me.odinmod.odin.clickgui.settings.impl.BooleanSetting
import me.odinmod.odin.clickgui.settings.impl.DropdownSetting
import me.odinmod.odin.clickgui.settings.impl.KeybindSetting
import me.odinmod.odin.clickgui.settings.impl.ListSetting
import me.odinmod.odin.events.GuiEvent
import me.odinmod.odin.features.Module
import me.odinmod.odin.utils.getItemUUID
import me.odinmod.odin.utils.getLoreString
import me.odinmod.odin.utils.modMessage
import meteordevelopment.orbit.EventHandler
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.screen.slot.SlotActionType
import org.lwjgl.glfw.GLFW

object PetKeybinds: Module (
    name = "Pet Keybinds",
    description = "Keybinds for the pets menu. (/petkeys)"
){
    private val unequipKeybind by KeybindSetting("Unequip", GLFW.GLFW_KEY_UNKNOWN, "Unequips the current Pet.")
    private val nextPageKeybind by KeybindSetting("Next Page", GLFW.GLFW_KEY_UNKNOWN, "Goes to the next page.")
    private val previousPageKeybind by KeybindSetting("Previous Page", GLFW.GLFW_KEY_UNKNOWN, "Goes to the previous page.")
    private val nounequip by BooleanSetting("Disable Unequip", false, desc = "Prevents using a pets keybind to unequip a pet. Does not prevent unequip keybind or normal clicking.")
    private val advanced by DropdownSetting("Show Settings", false)

    private val pet1 by KeybindSetting("Pet 1", GLFW.GLFW_KEY_1, "Pet 1 on the list.").withDependency { advanced }
    private val pet2 by KeybindSetting("Pet 2", GLFW.GLFW_KEY_2, "Pet 2 on the list.").withDependency { advanced }
    private val pet3 by KeybindSetting("Pet 3", GLFW.GLFW_KEY_3, "Pet 3 on the list.").withDependency { advanced }
    private val pet4 by KeybindSetting("Pet 4", GLFW.GLFW_KEY_4, "Pet 4 on the list.").withDependency { advanced }
    private val pet5 by KeybindSetting("Pet 5", GLFW.GLFW_KEY_5, "Pet 5 on the list.").withDependency { advanced }
    private val pet6 by KeybindSetting("Pet 6", GLFW.GLFW_KEY_6, "Pet 6 on the list.").withDependency { advanced }
    private val pet7 by KeybindSetting("Pet 7", GLFW.GLFW_KEY_7, "Pet 7 on the list.").withDependency { advanced }
    private val pet8 by KeybindSetting("Pet 8", GLFW.GLFW_KEY_8, "Pet 8 on the list.").withDependency { advanced }
    private val pet9 by KeybindSetting("Pet 9", GLFW.GLFW_KEY_9, "Pet 9 on the list.").withDependency { advanced }

    private val petsRegex = Regex("Pets(?: \\((\\d)/(\\d)\\))?")

    val petList by ListSetting("PetKeys List", mutableListOf<String>())

    @EventHandler
    fun onGuiEvent(event: GuiEvent.MouseClick) {
        if (onClick((event.screen as? HandledScreen<*>) ?: return, event.button)) event.cancel()
    }

    @EventHandler
    fun onGuiEvent(event: GuiEvent.KeyPress) {
        if (onClick((event.screen as? HandledScreen<*>) ?: return, event.keyCode)) event.cancel()
    }

    private fun onClick(screen: HandledScreen<*>, keyCode: Int): Boolean {
        val (current, total) = petsRegex.find(screen.title?.string ?: "")?.destructured?.let { (it.component1().toIntOrNull() ?: 1) to (it.component2().toIntOrNull() ?: 1) } ?: return false

        val index = when (keyCode) {
            nextPageKeybind.code -> if (current < total) 53 else return modMessage("§cYou are already on the last page.").let { false }
            previousPageKeybind.code -> if (current > 1) 45 else return modMessage("§cYou are already on the first page.").let { false }
            unequipKeybind.code ->
                screen.screenHandler.slots.subList(10, 43).indexOfFirst { it.stack?.getLoreString()?.contains("§7§cClick to despawn!") == true }.takeIf { it != -1 }?.plus(10) ?: return modMessage("§cCouldn't find equipped pet").let { false }

            else -> {
                val petIndex = arrayOf(pet1, pet2, pet3, pet4, pet5, pet6, pet7, pet8, pet9).indexOfFirst { it.code == keyCode }.takeIf { it != -1 } ?: return false
                petList.getOrNull(petIndex)?.let { uuid -> screen.screenHandler.slots.subList(10, 43).indexOfFirst { it?.stack?.getItemUUID() == uuid } }?.takeIf { it != -1 }?.plus(10) ?: return modMessage("§cCouldn't find matching pet or there is no pet in that position.").let { false }
            }
        }

        if (nounequip && screen.screenHandler.slots.subList(10, 43).indexOfFirst { it.stack?.getLoreString()?.contains("§7§cClick to despawn!") == true } == index && unequipKeybind.code != keyCode) return modMessage("§cThat pet is already equipped!").let { false }
        mc.interactionManager?.clickSlot(screen.screenHandler.syncId, index, GLFW.GLFW_MOUSE_BUTTON_1, SlotActionType.PICKUP, mc.player)
        return true
    }
}