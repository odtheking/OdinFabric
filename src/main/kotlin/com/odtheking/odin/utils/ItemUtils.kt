package com.odtheking.odin.utils

import com.mojang.authlib.properties.Property
import com.mojang.authlib.properties.PropertyMap
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.item.component.ItemLore
import net.minecraft.world.item.component.ResolvableProfile
import java.util.*

const val ID = "id"
const val UUID = "uuid"

inline val ItemStack.customData: CompoundTag
    get() =
        getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag()

inline val ItemStack.itemId: String
    get() =
        customData.getString(ID).orElse("")

inline val CompoundTag.itemId: String
    get() =
        getString(ID).orElse("")

inline val ItemStack.itemUUID: String
    get() =
        customData.getString(UUID).orElse("")

inline val ItemStack.lore: List<Component>
    get() =
        getOrDefault(DataComponents.LORE, ItemLore.EMPTY).styledLines()

inline val ItemStack.loreString: List<String>
    get() =
        lore.map { it.string }

inline val ItemStack.magicalPower: Int
    get() =
        getSkyblockRarity()?.mp?.let { if (itemId == "HEGEMONY_ARTIFACT") it * 2 else it } ?: 0

val ItemStack.texture: String?
    get() =
        get(DataComponents.PROFILE)?.gameProfile()?.properties?.get("textures")?.firstOrNull()?.value

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
    val profile = ResolvableProfile(
        Optional.empty(),
        Optional.empty(),
        properties
    )
    stack.set(DataComponents.PROFILE, profile)
    return stack
}

fun ItemStack.isEtherwarpItem(): CompoundTag? =
    customData.takeIf {
        it.getInt("ethermerge").orElse(0) == 1 || it.itemId == "ETHERWARP_CONDUIT"
    }

fun ItemStack.hasGlint(): Boolean =
    componentsPatch.get(DataComponents.ENCHANTMENT_GLINT_OVERRIDE)?.isPresent == true