package com.odtheking.odin.utils

import com.mojang.authlib.properties.Property
import com.mojang.authlib.properties.PropertyMap
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.LoreComponent
import net.minecraft.component.type.NbtComponent
import net.minecraft.component.type.ProfileComponent
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.text.Text
import java.util.*

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

inline val ItemStack.magicalPower: Int
    get() =
        getSkyblockRarity()?.mp?.let { if (itemId == "HEGEMONY_ARTIFACT") it * 2 else it } ?: 0

val ItemStack.texture: String?
    get() =
        get(DataComponentTypes.PROFILE)?.gameProfile()?.properties?.get("textures")?.firstOrNull()?.value

enum class ItemRarity(
    val loreName: String,
    val colorCode: String,
    val color: Color,
    val mp: Int,
) {
    COMMON("COMMON", "§f", Colors.WHITE, 3),
    UNCOMMON("UNCOMMON", "§2", Colors.MINECRAFT_GREEN, 5),
    RARE("RARE", "§9", Colors.MINECRAFT_BLUE, 8),
    EPIC("EPIC", "§5", Colors.MINECRAFT_DARK_PURPLE, 12),
    LEGENDARY("LEGENDARY", "§6", Colors.MINECRAFT_GOLD, 16),
    MYTHIC("MYTHIC", "§d", Colors.MINECRAFT_LIGHT_PURPLE, 22),
    DIVINE("DIVINE", "§b", Colors.MINECRAFT_AQUA, 0),
    SPECIAL("SPECIAL", "§c", Colors.MINECRAFT_RED, 3),
    VERY_SPECIAL("VERY SPECIAL", "§c", Colors.MINECRAFT_RED, 5);
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

fun createSkullStack(textureHash: String): ItemStack {
    val stack = ItemStack(Items.PLAYER_HEAD)
    val properties = PropertyMap()
    properties.put(
        "textures",
        Property(
            "textures",
            Base64.getEncoder().encodeToString(
                "{\"textures\":{\"SKIN\":{\"url\":\"http://textures.minecraft.net/texture/$textureHash\"}}}".toByteArray()
            )
        )
    )
    val profile = ProfileComponent(
        Optional.empty(),
        Optional.empty(),
        properties
    )
    stack.set(DataComponentTypes.PROFILE, profile)
    return stack
}