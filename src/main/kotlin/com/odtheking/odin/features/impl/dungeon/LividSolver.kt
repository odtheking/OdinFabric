package com.odtheking.odin.features.impl.dungeon

import com.odtheking.odin.events.BlockUpdateEvent
import com.odtheking.odin.events.PacketEvent
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.WorldLoadEvent
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.handlers.LimitedTickTask
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.render.drawWireFrameBox
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import meteordevelopment.orbit.EventHandler
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket
import net.minecraft.util.math.BlockPos

object LividSolver : Module(
    name = "Livid Solver",
    description = "Provides a visual cue for the correct Livid's location in the boss fight."
) {
    private val woolLocation = BlockPos(5, 108, 43)
    private var currentLivid = Livid.HOCKEY

    @EventHandler
    fun onBlockChange(event: BlockUpdateEvent) {
        if (!DungeonUtils.inBoss || !DungeonUtils.isFloor(5) || event.pos != woolLocation) return
        currentLivid = Livid.entries.find { livid -> livid.wool.defaultState == event.updated.block.defaultState } ?: return
        LimitedTickTask((mc.player?.getStatusEffect(StatusEffects.BLINDNESS)?.duration ?: 0) - 20, 1) {
            modMessage("Found Livid: §${currentLivid.colorCode}${currentLivid.entityName}")
        }
    }

    @EventHandler
    fun onPacketReceive(event: PacketEvent.Receive) {
        if (event.packet !is EntityTrackerUpdateS2CPacket || !DungeonUtils.inBoss || !DungeonUtils.isFloor(5)) return
        LimitedTickTask((mc.player?.getStatusEffect(StatusEffects.BLINDNESS)?.duration ?: 0) - 20, 1) {
            currentLivid.entity = (mc.world?.getEntityById(event.packet.id) as? PlayerEntity)?.takeIf { it.name.string == "${currentLivid.entityName} Livid" } ?: return@LimitedTickTask
        }
    }

    @EventHandler
    fun onRenderEvent(event: RenderEvent.Last) {
        if (!DungeonUtils.inBoss || !DungeonUtils.isFloor(5) || mc.player?.getStatusEffect(StatusEffects.BLINDNESS) != null) return
        currentLivid.entity?.let { entity ->
            event.drawWireFrameBox(entity.boundingBox, currentLivid.color, depth = true)
        }
    }

    @EventHandler
    fun onWorldLoad(event: WorldLoadEvent) {
        currentLivid = Livid.HOCKEY
        currentLivid.entity = null
    }

    private enum class Livid(val entityName: String, val colorCode: Char, val color: Color, val wool: Block) {
        VENDETTA("Vendetta", 'f', Colors.WHITE, Blocks.WHITE_WOOL),
        CROSSED("Crossed", 'd', Colors.MINECRAFT_DARK_PURPLE, Blocks.MAGENTA_WOOL),
        ARCADE("Arcade", 'e', Colors.MINECRAFT_YELLOW, Blocks.YELLOW_WOOL),
        SMILE("Smile", 'a', Colors.MINECRAFT_GREEN, Blocks.LIME_WOOL),
        DOCTOR("Doctor", '7', Colors.MINECRAFT_GRAY, Blocks.GRAY_WOOL),
        PURPLE("Purple", '5', Colors.MINECRAFT_DARK_PURPLE, Blocks.PURPLE_WOOL),
        SCREAM("Scream", '9', Colors.MINECRAFT_BLUE, Blocks.BLUE_WOOL),
        FROG("Frog", '2', Colors.MINECRAFT_DARK_GREEN, Blocks.GREEN_WOOL),
        HOCKEY("Hockey", 'c', Colors.MINECRAFT_RED, Blocks.RED_WOOL);

        var entity: PlayerEntity? = null
    }
}