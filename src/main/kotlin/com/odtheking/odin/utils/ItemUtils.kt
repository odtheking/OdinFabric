package com.odtheking.odin.utils

import com.google.common.collect.ImmutableMultimap
import com.mojang.authlib.GameProfile
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
const val UUID_STRING = "uuid"

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
        customData.getString(UUID_STRING, "")

inline val ItemStack.lore: List<Text>
    get() =
        getOrDefault(DataComponentTypes.LORE, LoreComponent.DEFAULT).styledLines()

inline val ItemStack.loreString: List<String>
    get() =
        lore.map { it.string }

val ItemStack.texture: String?
    get() =
        get(DataComponentTypes.PROFILE)?.gameProfile?.properties?.get("textures")?.firstOrNull()?.value

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

fun createSkullStack(textureHash: String): ItemStack {
    val stack = ItemStack(Items.PLAYER_HEAD)

    val property = Property(
        "textures",
        Base64.getEncoder().encodeToString("{\"textures\":{\"SKIN\":{\"url\":\"http://textures.minecraft.net/texture/$textureHash\"}}}".toByteArray())
    )
    val multimap = ImmutableMultimap.builder<String, Property>().put("textures", property).build()
    val gameProfile = GameProfile(UUID.randomUUID(), "_", PropertyMap(multimap))

    stack.set(DataComponentTypes.PROFILE, ProfileComponent.ofStatic(gameProfile))
    return stack
}

fun ItemStack.isEtherwarpItem(): NbtCompound? =
    customData.takeIf {
        it.getInt("ethermerge", 0) == 1 || it.itemId == "ETHERWARP_CONDUIT"
    }