package com.odtheking.odin.commands

import com.github.stivais.commodore.Commodore
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.features.ModuleManager
import com.odtheking.odin.features.impl.skyblock.PetKeybinds.petList
import com.odtheking.odin.utils.itemId
import com.odtheking.odin.utils.itemUUID
import com.odtheking.odin.utils.modMessage

val petCommand = Commodore("petkeys") {
    literal("add").runs {
        val petID = if (mc.player?.mainHandItem?.itemId == "PET") mc.player?.mainHandItem?.itemUUID else null
        if (petID == null) return@runs modMessage("§cYou can only add pets to the pet list!")
        if (petList.size >= 9) return@runs modMessage("§cYou cannot add more than 9 pets to the list. Remove a pet using §e/petkeys remove §cor clear the list using §e/petkeys clear§c.")
        if (petID in petList) return@runs modMessage("§cThis pet is already in the list!")

        petList.add(petID)
        modMessage("§aAdded this pet to the pet list in position §6${petList.indexOf(petID) + 1}§a!")
        ModuleManager.saveConfigurations()
    }

    literal("petpos").runs {
        val petID =
            if (mc.player?.mainHandItem?.itemId == "PET") mc.player?.mainHandItem?.itemUUID else return@runs modMessage(
                "§cThis is not a pet!"
            )
        if (petID !in petList) return@runs modMessage("§cThis pet is not in the list!")
        modMessage("§bThis pet is position §6${petList.indexOf(petID) + 1} §bin the list.")
    }

    literal("remove").runs {
        val petID =
            if (mc.player?.mainHandItem?.itemId == "PET") mc.player?.mainHandItem?.itemUUID else return@runs modMessage(
                "§cThis is not a pet!"
            )
        if (petID !in petList) return@runs modMessage("§cThis pet is not in the list!")

        petList.remove(petID)
        modMessage("§aRemoved this pet from the pet list!")
        ModuleManager.saveConfigurations()
    }

    literal("clear").runs {
        petList.clear()
        modMessage("§aCleared the pet list!")
        ModuleManager.saveConfigurations()
    }

    literal("list").runs {
        if (petList.isEmpty()) return@runs modMessage("§ePet list is empty")
        modMessage("§b§lPet list:§r\n§e${petList.joinToString("\n§e")}")
    }
}