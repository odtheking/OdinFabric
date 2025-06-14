package me.odinmod.odin.config.categories

import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import me.odinmod.odin.config.ConfigUtils.autoRefresh
import org.lwjgl.glfw.GLFW

object SkyblockConfig: CategoryKt("Skyblock") {

    override val description: TranslatableValue = TranslatableValue("Skyblock specific settings for Odin Mod.")

    init {
        separator {
            this.title = "Chat Commands"
        }
    }

    val enableChatCommands by autoRefresh(boolean(false) {
        name = TranslatableValue("Enable Chat Commands")
        description = TranslatableValue("Enable custom chat commands for Skyblock.")
    })

    val partyChatCommands by boolean(true) {
        name = TranslatableValue("Party Chat Commands")
        description = TranslatableValue("Enable chat commands in party chat.")
        condition = { enableChatCommands }
    }

    val guildChatCommands by boolean(false) {
        name = TranslatableValue("Guild Chat Commands")
        description = TranslatableValue("Enable chat commands in guild chat.")
        condition = { enableChatCommands }
    }

    val odin by boolean(true) {
        name = TranslatableValue("Odin Command")
        description = TranslatableValue("Enable the !odin command in chat commands.")
        condition = { enableChatCommands }
    }

    val coords by boolean(true) {
        description = TranslatableValue("Enable the !coords command in chat commands.")
        condition = { enableChatCommands }
    }

    val boop by boolean(true) {
        description = TranslatableValue("Enable the !boop command in chat commands.")
        condition = { enableChatCommands }
    }

    val coinFlip by boolean(true) {
        description = TranslatableValue("Enable the !cf command in chat commands.")
        condition = { enableChatCommands }
    }

    val eightBall by boolean(true) {
        description = TranslatableValue("Enable the !8ball command in chat commands.")
        condition = { enableChatCommands }
    }

    val dice by boolean(true) {
        description = TranslatableValue("Enable the !dice command in chat commands.")
        condition = { enableChatCommands }
    }

    val racism by boolean(true) {
        description = TranslatableValue("Enable the !racism command in chat commands.")
        condition = { enableChatCommands }
    }

    val fps by boolean(true) {
        description = TranslatableValue("Enable the !fps command in chat commands.")
        condition = { enableChatCommands }
    }

    val time by boolean(false) {
        description = TranslatableValue("Enable the !time command in chat commands.")
        condition = { enableChatCommands }
    }

    val location by boolean(true) {
        description = TranslatableValue("Enable the !location command in chat commands.")
        condition = { enableChatCommands }
    }

    val holding by boolean(true) {
        description = TranslatableValue("Enable the !holding command in chat commands.")
        condition = { enableChatCommands }
    }

    val partyWarp by boolean(true) {
        description = TranslatableValue("Enable the !warp command in party chat commands.")
        condition = { enableChatCommands }
    }

    val partyAllInvite by boolean(true) {
        description = TranslatableValue("Enable the !allinvite command in party chat commands.")
        condition = { enableChatCommands }
    }

    val partyTransfer by boolean(true) {
        description = TranslatableValue("Enable the !transfer command in party chat commands.")
        condition = { enableChatCommands }
    }

    val partyPromote by boolean(true) {
        description = TranslatableValue("Enable the !promote command in party chat commands.")
        condition = { enableChatCommands }
    }

    val partyDemote by boolean(true) {
        description = TranslatableValue("Enable the !demote command in party chat commands.")
        condition = { enableChatCommands }
    }

    init {
        separator {
            this.title = "No Cursor Reset"
        }
    }

    val noCursorReset by boolean(false) {
        name = TranslatableValue("No Cursor Reset")
        description = TranslatableValue("Prevent the cursor from resetting when going between inventory.")
    }

    init {
        separator {
            this.title = "Wardrobe Keybinds"
        }
    }

    val wardrobeKeybinds by autoRefresh(boolean(false) {
        name = TranslatableValue("Enable Wardrobe Keybinds")
        description = TranslatableValue("Enable keybinds for wardrobe slots.")
    })

    val unequipKeybind by key(0) {
        name = TranslatableValue("Unequip Keybind")
        description = TranslatableValue("Key to unequip the currently equipped item in the wardrobe.")
        condition = { wardrobeKeybinds }
    }

    val nextPageKeybind by key(0) {
        name = TranslatableValue("Next Page Keybind")
        description = TranslatableValue("Key to go to the next page in the wardrobe.")
        condition = { wardrobeKeybinds }
    }

    val previousPageKeybind by key(0) {
        name = TranslatableValue("Previous Page Keybind")
        description = TranslatableValue("Key to go to the previous page in the wardrobe.")
        condition = { wardrobeKeybinds }
    }

    val disallowUnequipKeybind by boolean(false) {
        name = TranslatableValue("Disallow Unequip")
        description = TranslatableValue("Disallow unequipping items using keybinds in the wardrobe. If enabled, you cannot unequip items using the keybinds.")
        condition = { wardrobeKeybinds }
    }

    val wardrobe1 by key(GLFW.GLFW_KEY_1) {
        name = TranslatableValue("Wardrobe Slot 1 Keybind")
        description = TranslatableValue("Key to equip the first slot in the wardrobe.")
        condition = { wardrobeKeybinds }
    }

    val wardrobe2 by key(GLFW.GLFW_KEY_2) {
        name = TranslatableValue("Wardrobe Slot 2 Keybind")
        description = TranslatableValue("Key to equip the second slot in the wardrobe.")
        condition = { wardrobeKeybinds }
    }

    val wardrobe3 by key(GLFW.GLFW_KEY_3) {
        name = TranslatableValue("Wardrobe Slot 3 Keybind")
        description = TranslatableValue("Key to equip the third slot in the wardrobe.")
        condition = { wardrobeKeybinds }
    }

    val wardrobe4 by key(GLFW.GLFW_KEY_4) {
        name = TranslatableValue("Wardrobe Slot 4 Keybind")
        description = TranslatableValue("Key to equip the fourth slot in the wardrobe.")
        condition = { wardrobeKeybinds }
    }

    val wardrobe5 by key(GLFW.GLFW_KEY_5) {
        name = TranslatableValue("Wardrobe Slot 5 Keybind")
        description = TranslatableValue("Key to equip the fifth slot in the wardrobe.")
        condition = { wardrobeKeybinds }
    }

    val wardrobe6 by key(GLFW.GLFW_KEY_6) {
        name = TranslatableValue("Wardrobe Slot 6 Keybind")
        description = TranslatableValue("Key to equip the sixth slot in the wardrobe.")
        condition = { wardrobeKeybinds }
    }

    val wardrobe7 by key(GLFW.GLFW_KEY_7) {
        name = TranslatableValue("Wardrobe Slot 7 Keybind")
        description = TranslatableValue("Key to equip the seventh slot in the wardrobe.")
        condition = { wardrobeKeybinds }
    }

    val wardrobe8 by key(GLFW.GLFW_KEY_8) {
        name = TranslatableValue("Wardrobe Slot 8 Keybind")
        description = TranslatableValue("Key to equip the eighth slot in the wardrobe.")
        condition = { wardrobeKeybinds }
    }

    val wardrobe9 by key(GLFW.GLFW_KEY_9) {
        name = TranslatableValue("Wardrobe Slot 9 Keybind")
        description = TranslatableValue("Key to equip the ninth slot in the wardrobe.")
        condition = { wardrobeKeybinds }
    }

    init {
        separator {
            this.title = "Ragnarock Axe"
        }
    }

    val ragnarockAlert by boolean(false) {
        name = TranslatableValue("Ragnarock Axe Alert")
        description = TranslatableValue("Enable an alert when the Ragnarock Axe is activated.")
    }

    val ragnarockCancelAlert by boolean(false) {
        name = TranslatableValue("Ragnarock Axe Cancel Alert")
        description = TranslatableValue("Enable an alert when the Ragnarock Axe is canceled.")
    }

    init {
        separator {
            this.title = "Spring Boots"
        }
    }

    val springBoots by boolean(false) {
        name = TranslatableValue("Spring Boots")
        description = TranslatableValue("Enable Spring Boots goal estimation rendering.")
    }
}