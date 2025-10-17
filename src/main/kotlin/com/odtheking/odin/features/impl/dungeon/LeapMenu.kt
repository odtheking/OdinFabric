package com.odtheking.odin.features.impl.dungeon

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.*
import com.odtheking.odin.events.GuiEvent
import com.odtheking.odin.events.PacketEvent
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.*
import com.odtheking.odin.utils.Color.Companion.withAlpha
import com.odtheking.odin.utils.skyblock.dungeon.DungeonClass
import com.odtheking.odin.utils.skyblock.dungeon.DungeonPlayer
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils.leapTeammates
import com.odtheking.odin.utils.ui.HoverHandler
import com.odtheking.odin.utils.ui.getQuadrant
import com.odtheking.odin.utils.ui.rendering.NVGRenderer
import meteordevelopment.orbit.EventHandler
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.texture.GlTexture
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.util.Identifier
import org.lwjgl.glfw.GLFW

object LeapMenu : Module(
    name = "Leap Menu",
    description = "Renders a custom leap menu when in the Spirit Leap gui."
) {
    val type by SelectorSetting("Sorting", "Odin Sorting", arrayListOf("Odin Sorting", "A-Z Class", "A-Z Name", "Custom sorting", "No Sorting"), desc = "How to sort the leap menu. /od leaporder to configure custom sorting.")
    private val onlyClass by BooleanSetting("Only Classes", false, desc = "Renders classes instead of names.")
    private val colorStyle by BooleanSetting("Color Style", false, desc = "Which color style to use.")
    private val backgroundColor by ColorSetting("Background Color", Colors.gray38.withAlpha(0.75f), true, desc = "Color of the background of the leap menu.").withDependency { !colorStyle }
    private val scale by NumberSetting("Scale", 0.5f, 0.1f, 1f, 0.1f, desc = "Scale of the leap menu.", unit = "x")
    private val useNumberKeys by BooleanSetting("Use Number Keys", false, desc = "Use keyboard keys to leap to the player you want, going from left to right, top to bottom.")
    private val topLeftKeybind by KeybindSetting("Top Left", GLFW.GLFW_KEY_1, "Used to click on the first person in the leap menu.").withDependency { useNumberKeys }
    private val topRightKeybind by KeybindSetting("Top Right", GLFW.GLFW_KEY_2, "Used to click on the second person in the leap menu.").withDependency { useNumberKeys }
    private val bottomLeftKeybind by KeybindSetting("Bottom Left", GLFW.GLFW_KEY_3, "Used to click on the third person in the leap menu.").withDependency { useNumberKeys }
    private val bottomRightKeybind by KeybindSetting("Bottom right", GLFW.GLFW_KEY_4, "Used to click on the fourth person in the leap menu.").withDependency { useNumberKeys }
    private val leapAnnounce by BooleanSetting("Leap Announce", false, desc = "Announces when you leap to a player.")
    private val hoverHandler = List(4) { HoverHandler(200L) }

    private val EMPTY = DungeonPlayer("Empty", DungeonClass.Unknown, 0, Identifier.of("textures/entity/steve.png"))
    private val keybindList = listOf(topLeftKeybind, topRightKeybind, bottomLeftKeybind, bottomRightKeybind)
    private val leapedRegex = Regex("You have teleported to (\\w{1,16})!")
    private val imageCacheMap = mutableMapOf<String, Int>()

    @EventHandler
    fun onDrawScreen(event: GuiEvent.Draw) {
        val chest = (event.screen as? HandledScreen<*>) ?: return
        if (chest.title?.string?.equalsOneOf("Spirit Leap", "Teleport to Player") == false || leapTeammates.isEmpty() || leapTeammates.all { it == EMPTY }) return

        val halfWidth = mc.window.width / 2f
        val halfHeight = mc.window.height / 2f

        hoverHandler[0].handle(0f, 0f, halfWidth, halfHeight)
        hoverHandler[1].handle(halfWidth, 0f, halfWidth, halfHeight)
        hoverHandler[2].handle(0f, halfHeight, halfWidth, halfHeight)
        hoverHandler[3].handle(halfWidth, halfHeight, halfWidth, halfHeight)

        NVGRenderer.beginFrame(mc.window.width.toFloat(), mc.window.height.toFloat())
        NVGRenderer.scale(scale, scale)
        NVGRenderer.translate(halfWidth / scale, halfHeight / scale)
        val boxWidth = 800f
        val boxHeight = 300f
        leapTeammates.forEachIndexed { index, player ->
            if (player == EMPTY) return@forEachIndexed

            val x = when (index) {
                0, 2 -> -((mc.window.width - (boxWidth * 2f)) / 6f + boxWidth)
                else -> ((mc.window.width - (boxWidth * 2f)) / 6f)
            }
            val y = when (index) {
                0, 1 -> -((mc.window.height - (boxHeight * 2f)) / 8f + boxHeight)
                else -> ((mc.window.height - (boxHeight * 2f)) / 8f)
            }

            val expandValue = hoverHandler[index].anim.get(0f, 15f, !hoverHandler[index].hasStarted)
            NVGRenderer.rect(x - expandValue ,y - expandValue, boxWidth + expandValue * 2, boxHeight + expandValue * 2, (if (colorStyle) player.clazz.color else backgroundColor).rgba, 12f)
            imageCacheMap.getOrPut(player.locationSkin.path) {
                NVGRenderer.createNVGImage((mc.textureManager?.getTexture(player.locationSkin)?.glTexture as? GlTexture)?.glId ?: 0, 64, 64)
            }.let { glTextureId ->
                NVGRenderer.image(glTextureId, 64, 64, 8, 8, 8, 8, x + 30f, y + 30f, 240f, 240f, 9f)
            }

            NVGRenderer.textShadow(if (!onlyClass) player.name else player.clazz.name, x + 275f, y + 110f, 50f, if (!colorStyle) player.clazz.color.rgba else backgroundColor.rgba, NVGRenderer.defaultFont)
            if (!onlyClass || player.isDead) NVGRenderer.textShadow(if (player.isDead) "DEAD" else player.clazz.name, x + 275f, y + 180f, 40f, if (player.isDead) Colors.MINECRAFT_RED.rgba else Colors.WHITE.rgba, NVGRenderer.defaultFont)
        }
        NVGRenderer.endFrame()
        event.cancel()
    }

    @EventHandler
    fun onDrawBackground(event: GuiEvent.DrawBackground) {
        val chest = (event.screen as? HandledScreen<*>) ?: return
        if (!chest.title.string.equalsOneOf("Spirit Leap", "Teleport to Player") || leapTeammates.isEmpty() || leapTeammates.all { it == EMPTY }) return
        event.cancel()
    }

    @EventHandler
    fun mouseClicked(event: GuiEvent.MouseClick) {
        val chest = (event.screen as? HandledScreen<*>) ?: return
        if (chest.title?.string?.equalsOneOf("Spirit Leap", "Teleport to Player") == false || leapTeammates.isEmpty())  return

        val quadrant = getQuadrant()
        if ((type.equalsOneOf(1,2,3)) && leapTeammates.size < quadrant) return

        val playerToLeap = leapTeammates[quadrant - 1]
        if (playerToLeap == EMPTY) return
        if (playerToLeap.isDead) return modMessage("This player is dead, can't leap.")

        leapTo(playerToLeap.name, chest)

        event.cancel()
    }

    @EventHandler
    fun keyTyped(event: GuiEvent.KeyPress) {
        val chest = (event.screen as? HandledScreen<*>) ?: return
        if (!useNumberKeys || chest.title?.string?.equalsOneOf("Spirit Leap", "Teleport to Player") == false || keybindList.none { it.code == event.input.keycode } || leapTeammates.isEmpty()) return

        val index = keybindList.indexOfFirst { it.code == event.input.keycode }
        val playerToLeap = if (index + 1 > leapTeammates.size) return else leapTeammates[index]
        if (playerToLeap == EMPTY) return
        if (playerToLeap.isDead) return modMessage("This player is dead, can't leap.")

        leapTo(playerToLeap.name, chest)

        event.cancel()
    }

    private fun leapTo(name: String, screenHandler: HandledScreen<*>) {
        val index = screenHandler.screenHandler.slots.subList(11, 16).firstOrNull {
            it.stack?.name?.string?.substringAfter(' ').equals(name.noControlCodes, ignoreCase = true)
        }?.index ?: return modMessage("Can't find player $name. This shouldn't be possible! are you nicked?")
        modMessage("Teleporting to $name.")
        mc.interactionManager?.clickSlot(screenHandler.screenHandler.syncId, index, 0, SlotActionType.PICKUP, mc.player)
    }

    @EventHandler
    fun onPacketReceive(event: PacketEvent.Receive) {
        if (event.packet is GameMessageS2CPacket && !event.packet.overlay && leapAnnounce)
            leapedRegex.find(event.packet.content.string)?.groupValues?.let { sendCommand("pc Leaped to ${it[1]}!") }
    }

    /*private val leapTeammates: MutableList<DungeonPlayer> = mutableListOf(
        DungeonPlayer("Stiviaisd", DungeonClass.Healer, 50),
        DungeonPlayer("Odtheking", DungeonClass.Archer, 50),
        DungeonPlayer("Bonzi", DungeonClass.Mage, 47),
        DungeonPlayer("Cezar", DungeonClass.Tank, 38)
    )*/

    /**
     * Sorts the list of players based on their default quadrant and class priority.
     * The function first tries to place each player in their default quadrant. If the quadrant is already occupied,
     * the player is added to a second round list. After all players have been processed, the function fills the remaining
     * empty quadrants with the players from the second round list.
     *
     * @param players The list of players to be sorted.
     * @return An array of sorted players.
     */
    fun odinSorting(players: List<DungeonPlayer>): Array<DungeonPlayer> {
        val result = Array(4) { EMPTY }
        val secondRound = mutableListOf<DungeonPlayer>()

        for (player in players.sortedBy { it.clazz.priority }) {
            when {
                result[player.clazz.defaultQuadrant] == EMPTY -> result[player.clazz.defaultQuadrant] = player
                else -> secondRound.add(player)
            }
        }

        if (secondRound.isEmpty()) return result

        result.forEachIndexed { index, _ ->
            when {
                result[index] == EMPTY -> {
                    result[index] = secondRound.removeAt(0)
                    if (secondRound.isEmpty()) return result
                }
            }
        }
        return result
    }
}