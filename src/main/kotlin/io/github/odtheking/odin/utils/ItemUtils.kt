package io.github.odtheking.odin.utils

import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.LoreComponent
import net.minecraft.component.type.NbtComponent
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.text.Text

const val ID = "id"
const val UUID = "uuid"

inline val ItemStack.customData: NbtCompound
    get() =
        getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt()

inline val ItemStack.itemId: String
    get() =
        customData.getString(ID, "")

inline val NbtCompound.itemId: String
    get() =
        getString(ID, "")

inline val ItemStack.itemUUID: String
    get() =
        customData.getString(UUID, "")

inline val ItemStack.lore: List<Text>
    get() =
        getOrDefault(DataComponentTypes.LORE, LoreComponent.DEFAULT).styledLines()

inline val ItemStack.loreString: List<String>
    get() =
        lore.map { it.string }

enum class ItemRarity(
    val loreName: String,
    val colorCode: String,
    val color: Color
) {
    COMMON("COMMON", "§f", Colors.WHITE),
    UNCOMMON("UNCOMMON", "§2", Colors.MINECRAFT_GREEN),
    RARE("RARE", "§9", Colors.MINECRAFT_BLUE),
    EPIC("EPIC", "§5", Colors.MINECRAFT_DARK_PURPLE),
    LEGENDARY("LEGENDARY", "§6", Colors.MINECRAFT_GOLD),
    MYTHIC("MYTHIC", "§d", Colors.MINECRAFT_LIGHT_PURPLE),
    DIVINE("DIVINE", "§b", Colors.MINECRAFT_AQUA),
    SPECIAL("SPECIAL", "§c", Colors.MINECRAFT_RED),
    VERY_SPECIAL("VERY SPECIAL", "§c", Colors.MINECRAFT_RED);
}

private val rarityRegex = Regex("(${ItemRarity.entries.joinToString("|") { it.loreName }}) ?([A-Z ]+)?")

fun ItemStack.getSkyblockRarity(): ItemRarity? {
    val lore = loreString
    for (i in lore.indices.reversed()) {
        val rarity = rarityRegex.find(lore[i])?.groups?.get(1)?.value ?: continue
        return ItemRarity.entries.find { it.loreName == rarity }
    }
    return null
}