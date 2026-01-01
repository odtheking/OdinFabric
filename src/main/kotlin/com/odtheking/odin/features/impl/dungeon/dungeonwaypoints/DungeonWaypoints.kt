package com.odtheking.odin.features.impl.dungeon.dungeonwaypoints

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.*
import com.odtheking.odin.config.DungeonWaypointConfig
import com.odtheking.odin.events.*
import com.odtheking.odin.events.core.on
import com.odtheking.odin.events.core.onReceive
import com.odtheking.odin.features.Module
import com.odtheking.odin.features.impl.render.Etherwarp
import com.odtheking.odin.utils.*
import com.odtheking.odin.utils.render.drawBoxes
import com.odtheking.odin.utils.render.drawStyledBox
import com.odtheking.odin.utils.render.drawText
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils.getRealCoords
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils.getRelativeCoords
import com.odtheking.odin.utils.skyblock.dungeon.tiles.Room
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult
import org.lwjgl.glfw.GLFW

/**
 * Custom Waypoints for Dungeons
 * @author Bonsai
 */
object DungeonWaypoints : Module(
    name = "Dungeon Waypoints",
    description = "Custom Waypoints for Dungeon Rooms."
) {
    private var allowEdits by BooleanSetting("Allow Edits", false, desc = "Allows you to edit waypoints.")
    private var allowMidair by BooleanSetting("Allow Midair", false, desc = "Allows waypoints to be placed midair if they reach the end of distance without hitting a block.").withDependency { allowEdits }
    private var reachColor by ColorSetting("Reach Color", Color(0, 255, 213, 0.43f), true, desc = "Color of the reach box highlight.").withDependency { allowEdits }
    private val allowTextEdit by BooleanSetting("Allow Text Edit", true, desc = "Allows you to set the text of a waypoint while sneaking.").withDependency { allowEdits }

    private val renderTitle by BooleanSetting("Render Title", true, desc = "Renders the titles of waypoints")
    private val titleScale by NumberSetting("Title Scale", 1f, 0.1f, 4f, increment = 0.1f, desc = "The scale of the titles of waypoints.").withDependency { renderTitle }
    private val disableDepth by BooleanSetting("Global Depth", false, desc = "Disables depth testing for all waypoints.")

    private val settingsDropDown by DropdownSetting("Next Waypoint Settings")
    var waypointType by SelectorSetting("Waypoint Type", WaypointType.NONE.displayName, WaypointType.entries.map { it.displayName }, desc = "The type of waypoint you want to place.").withDependency { settingsDropDown }
    private val colorPallet by SelectorSetting("Color pallet", "None", arrayListOf("None", "Aqua", "Magenta", "Yellow", "Lime", "Red"), desc = "The color pallet of the next waypoint you place.").withDependency { settingsDropDown }
    var color by ColorSetting("Color", Colors.MINECRAFT_GREEN, true, desc = "The color of the next waypoint you place.").withDependency { colorPallet == 0 && settingsDropDown }
    var filled by BooleanSetting("Filled", false, desc = "If the next waypoint you place should be 'filled'.").withDependency { settingsDropDown }
    var depthCheck by BooleanSetting("Depth check", false, desc = "Whether the next waypoint you place should have a depth check.").withDependency { settingsDropDown }
    var useBlockSize by BooleanSetting("Use block size", true, desc = "Use the size of the block you click for waypoint size.").withDependency { settingsDropDown }
    var sizeX by NumberSetting("Size X", 1.0, .1, 5.0, 0.01, desc = "The X size of the next waypoint you place.").withDependency { !useBlockSize && settingsDropDown }
    var sizeY by NumberSetting("Size Y", 1.0, .1, 5.0, 0.01, desc = "The Y size of the next waypoint you place.").withDependency { !useBlockSize && settingsDropDown }
    var sizeZ by NumberSetting("Size Z", 1.0, .1, 5.0, 0.01, desc = "The Z size of the next waypoint you place.").withDependency { !useBlockSize && settingsDropDown }

    private val resetButton by ActionSetting("Reset Current Room", desc = "Resets the waypoints for the current room.") {
        val room = DungeonUtils.currentRoom ?: return@ActionSetting modMessage("§cRoom not found!")

        val waypoints = DungeonWaypointConfig.waypoints.getOrPut(room.data.name) { mutableListOf() }
        if (waypoints.isEmpty()) return@ActionSetting modMessage("§cCurrent room does not have any waypoints!")
        waypoints.clear()
        DungeonWaypointConfig.saveConfig()
        room.setWaypoints()
        modMessage("§aSuccessfully reset current room!")
    }

    private inline val selectedColor
        get() = when (colorPallet) {
            0 -> color
            1 -> Colors.MINECRAFT_DARK_AQUA
            2 -> Colors.MINECRAFT_DARK_PURPLE
            3 -> Colors.MINECRAFT_YELLOW
            4 -> Colors.MINECRAFT_GREEN
            5 -> Colors.MINECRAFT_RED
            else -> color
        }

    var lastEtherPos: BlockPos? = null
    var lastEtherTime = 0L

    init {
        DungeonWaypointConfig.loadConfig()

        on<WorldEvent.Load> {
            SecretWaypoints.resetSecrets()
        }

        onReceive<ClientboundPlayerPositionPacket> {
            SecretWaypoints.onEtherwarp(this)
        }

        on<SecretPickupEvent.Bat> {
            SecretWaypoints.onSecret(this)
        }

        on<SecretPickupEvent.Item> {
            SecretWaypoints.onSecret(this)
        }

        on<SecretPickupEvent.Interact> {
            SecretWaypoints.onSecret(this)
        }

        on<RoomEnterEvent> {
            room?.setWaypoints()
        }

        on<RenderEvent.Extract> {
            if (!DungeonUtils.inClear) return@on
            val room = DungeonUtils.currentRoom ?: return@on

            drawBoxes(room.waypoints, disableDepth)

            if (renderTitle) {
                for (waypoint in room.waypoints) {
                    if (waypoint.isClicked || waypoint.title == null) continue
                    drawText(
                        waypoint.title,
                        waypoint.blockPos.center.add(0.0, 0.1 * titleScale, 0.0), titleScale, waypoint.depth
                    )
                }
            }

            reachPosition?.takeIf { allowEdits }?.let { pos ->
                val aabb = if (!useBlockSize) AABB(BlockPos.ZERO).inflate((sizeX - 1.0) / 2.0, (sizeY - 1.0) / 2.0, (sizeZ - 1.0) / 2.0).move(pos) else
                    pos.getBlockBounds()?.move(pos) ?: AABB(pos)

                drawStyledBox(aabb, reachColor, style = if (filled) 0 else 1, depthCheck)
            }
        }

        on<InputEvent> {
            if (key.value != GLFW.GLFW_MOUSE_BUTTON_RIGHT || mc.screen != null) return@on
            val room = DungeonUtils.currentRoom ?: return@on
            mc.player?.mainHandItem?.isEtherwarpItem()?.let { item ->
                Etherwarp.getEtherPos(mc.player?.position(), 56.0 + item.getInt("tuned_transmission").orElse(0))
                .takeIf { it.succeeded && it.pos != null }
                ?.also {
                    lastEtherTime = System.currentTimeMillis()
                    lastEtherPos = it.pos
                }
            }
            if (!allowEdits) return@on
            val pos = reachPosition ?: return@on
            val blockPos = room.getRelativeCoords(pos)

            val aabb = if (!useBlockSize) AABB(BlockPos.ZERO).inflate((sizeX - 1.0) / 2.0, (sizeY - 1.0) / 2.0, (sizeZ - 1.0) / 2.0) else
                pos.getBlockBounds() ?: AABB(BlockPos.ZERO)

            val waypoints = getWaypoints(room)

            if (allowTextEdit && mc.player?.isCrouching == true) {
                TextPromptScreen.setCallback { text ->
                    waypoints.removeIf { it.blockPos == blockPos }
                    waypoints.add(
                        DungeonWaypoint(
                            blockPos, selectedColor.copy(), filled, depthCheck, aabb,
                            text, WaypointType.getByInt(waypointType)
                        )
                    )
                    devMessage("Added waypoint with $text at $blockPos")
                    DungeonWaypointConfig.saveConfig()
                    room.setWaypoints()
                }
                mc.setScreen(TextPromptScreen)

            } else if (waypoints.removeIf { it.blockPos == blockPos }) {
                devMessage("Removed waypoint at $blockPos")
                DungeonWaypointConfig.saveConfig()
                room.setWaypoints()
            } else {
                waypoints.add(
                    DungeonWaypoint(
                        blockPos, selectedColor.copy(), filled, depthCheck, aabb,
                        type = WaypointType.getByInt(waypointType)
                    )
                )
                devMessage("Added waypoint at $blockPos")
                DungeonWaypointConfig.saveConfig()
                room.setWaypoints()
            }
        }
    }

    private inline val reachPosition: BlockPos?
        get() {
            val hitResult = mc.hitResult
            return when {
                hitResult?.type == HitResult.Type.MISS && !allowMidair -> Etherwarp.getEtherPos(mc.player?.position(), 5.0).pos
                hitResult is BlockHitResult -> hitResult.blockPos
                else -> null
            }
        }

    fun Room.setWaypoints() {
        waypoints = mutableSetOf<DungeonWaypoint>().apply {
            DungeonWaypointConfig.waypoints[data.name]?.let { waypoints ->
                addAll(waypoints.map { waypoint ->
                    waypoint.copy(blockPos = getRealCoords(waypoint.blockPos))
                })
            }
        }
    }

    fun getWaypoints(room: Room): MutableList<DungeonWaypoint> =
        DungeonWaypointConfig.waypoints.getOrPut(room.data.name) { mutableListOf() }

    enum class WaypointType {
        NONE, NORMAL, SECRET, ETHERWARP;

        inline val displayName get() = name.lowercase().capitalizeFirst()

        companion object {
            fun getByInt(i: Int) = entries.getOrNull(i).takeIf { it != NONE }
            fun getByName(name: String): WaypointType? = entries.find { it.name == name.uppercase() }
        }
    }

    data class DungeonWaypoint(
        val blockPos: BlockPos, val color: Color,
        val filled: Boolean, val depth: Boolean,
        val aabb: AABB, val title: String? = null,
        var type: WaypointType? = null,
        @Transient var isClicked: Boolean = false,
    ) {
        inline val isSecret: Boolean get() = type == WaypointType.SECRET
    }

    override fun onKeybind() {
        allowEdits = !allowEdits
        modMessage("Dungeon Waypoint editing ${if (allowEdits) "§aenabled" else "§cdisabled"}§r!")
    }
}