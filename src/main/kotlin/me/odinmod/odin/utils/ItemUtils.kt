package me.odinmod.odin.utils

import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.LoreComponent
import net.minecraft.component.type.NbtComponent
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.text.Text
import net.minecraft.util.Formatting

private const val ID = "id"
private const val UUID = "uuid"

fun ItemStack.getCustomData(): NbtCompound =
    getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt()

fun ItemStack.getItemId(): String =
    getCustomData().getString(ID, "")

fun NbtCompound.getItemId(): String =
    getString(ID, "")

fun ItemStack.getItemUUID(): String =
    getCustomData().getString(UUID, "")

fun ItemStack.getLore(): List<Text> =
    getOrDefault(DataComponentTypes.LORE, LoreComponent.DEFAULT).styledLines()

fun ItemStack.getLoreText(): List<String> =
    getLore().map { it.string }

enum class ItemRarity(
    val loreName: String,
    val colorCode: String,
    val color: Formatting
) {
    COMMON("COMMON", "§f", Formatting.WHITE),
    UNCOMMON("UNCOMMON", "§2", Formatting.GREEN),
    RARE("RARE", "§9", Formatting.BLUE),
    EPIC("EPIC", "§5", Formatting.DARK_PURPLE),
    LEGENDARY("LEGENDARY", "§6", Formatting.GOLD),
    MYTHIC("MYTHIC", "§d", Formatting.LIGHT_PURPLE),
    DIVINE("DIVINE", "§b", Formatting.AQUA),
    SPECIAL("SPECIAL", "§c", Formatting.RED),
    VERY_SPECIAL("VERY SPECIAL", "§c", Formatting.RED);
}

private val rarityRegex = Regex("(${ItemRarity.entries.joinToString("|") { it.loreName }}) ?([A-Z ]+)?")

fun ItemStack.getSkyblockRarity(): ItemRarity? {
    val lore = getLoreText()
    for (i in lore.indices.reversed()) {
        val rarity = rarityRegex.find(lore[i])?.groups?.get(1)?.value ?: continue
        return ItemRarity.entries.find { it.loreName == rarity }
    }
    return null
}