package me.odinmod.odin.features.impl.nether

import me.odinmod.odin.clickgui.settings.impl.BooleanSetting
import me.odinmod.odin.events.GuiEvent
import me.odinmod.odin.features.Module
import me.odinmod.odin.utils.containsOneOf
import me.odinmod.odin.utils.equalsOneOf
import meteordevelopment.orbit.EventHandler
import net.minecraft.client.gui.screen.ingame.HandledScreen

object RemovePerks : Module(
    name = "Remove Perks",
    description = "Removes certain perks from the perk menu."
) {
    private val renderStun by BooleanSetting("Show Stun", false, desc = "Shows the stun role perks.")

    @EventHandler
    fun renderSlot(event: GuiEvent.DrawSlot) = with(event.screen) {
        if (this is HandledScreen<*> && title.string == "Perk Menu" && slotCheck(event.slot.stack?.name?.string ?: return)) event.cancel()
    }

    @EventHandler
    fun guiMouseClick(event: GuiEvent.MouseClick) = with(event.screen) {
        if (this is HandledScreen<*> && title.string == "Perk Menu" && slotCheck(screenHandler?.getSlot(event.slotId)?.stack?.name?.string ?: return))
            event.cancel()
    }

    private fun slotCheck(slot: String): Boolean {
        return slot.containsOneOf("Steady Hands", "Bomberman", "Mining Frenzy") || slot.equalsOneOf("Elle's Lava Rod", "Elle's Pickaxe", "Auto Revive") ||
                (!renderStun && slot.containsOneOf("Human Cannonball"))
    }
}