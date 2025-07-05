package me.odinmod.odin.features.impl.render

import me.odinmod.odin.clickgui.settings.Setting.Companion.withDependency
import me.odinmod.odin.clickgui.settings.impl.BooleanSetting
import me.odinmod.odin.clickgui.settings.impl.ColorSetting
import me.odinmod.odin.clickgui.settings.impl.SelectorSetting
import me.odinmod.odin.events.PacketEvent
import me.odinmod.odin.events.RenderEvent
import me.odinmod.odin.features.Module
import me.odinmod.odin.utils.*
import me.odinmod.odin.utils.Color.Companion.withAlpha
import me.odinmod.odin.utils.skyblock.Island
import me.odinmod.odin.utils.skyblock.LocationUtils
import meteordevelopment.orbit.EventHandler
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.ChunkSectionPos
import net.minecraft.util.math.Vec3d
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sign

object Etherwarp: Module(
    name = "Etherwarp",
    description = "Provides configurable visual feedback for etherwarp."
) {
    private val render by BooleanSetting("Show Etherwarp Guess", true, desc = "Shows where etherwarp will take you.")
    private val color by ColorSetting("Color", Colors.MINECRAFT_GOLD.withAlpha(.5f), allowAlpha = true, desc = "Color of the box.").withDependency { render }
    private val renderFail by BooleanSetting("Show when failed", true, desc = "Shows the box even when the guess failed.").withDependency { render }
    private val failColor by ColorSetting("Fail Color", Colors.MINECRAFT_RED.withAlpha(.5f), allowAlpha = true, desc = "Color of the box if guess failed.").withDependency { renderFail }
    private val renderStyle by SelectorSetting("Render Style", "Outline", listOf("Outline", "Filled", "Filled Outline"), desc = "Style of the box.").withDependency { render }

    private var etherPos: EtherPos? = null

    @EventHandler
    fun onRenderLast(event: RenderEvent.Last) {
        if (mc.player?.isSneaking == false || mc.currentScreen != null) return
        val customData = mc.player?.mainHandStack?.getCustomData()?.takeIf { it.getInt("ethermerge", 0) == 1 || it.getItemId() == "ETHERWARP_CONDUIT" } ?: return

        etherPos = getEtherPos(56.0 + customData.getInt("tuned_transmission", 0))
        if (etherPos?.succeeded != true && !renderFail) return
        val color = if (etherPos?.succeeded == true) color.rgba.floatValues() else failColor.rgba.floatValues()
        etherPos?.pos?.let {
            when (renderStyle) {
                0 -> drawBox(Box(it), event.context, color)
                1 -> drawFilledBox(Box(it), event.context, color)
                2 -> {
                    drawBox(Box(it), event.context, color)
                    drawFilledBox(Box(it), event.context, color.withAlpha(0.5f))
                }
            }
        }
    }


    @EventHandler
    fun onPacketReceive(event: PacketEvent.Receive) = with (event.packet) {
        if (this !is PlayerInteractItemC2SPacket || !LocationUtils.currentArea.isArea(Island.SinglePlayer) || mc.player?.isSneaking == false || mc.currentScreen != null) return@with
        mc.player?.mainHandStack?.getCustomData()?.takeIf { it.getInt("ethermerge", 0) == 1 || it.getItemId() == "ETHERWARP_CONDUIT" } ?: return@with
        etherPos?.pos?.let {
            if (etherPos?.succeeded == false) return@let
            mc.executeSync {
                mc.player?.networkHandler?.sendPacket(
                    PlayerMoveC2SPacket.Full(it.x + 0.5, it.y + 1.05, it.z + 0.5, mc.player?.yaw ?: 0f, mc.player?.pitch ?: 0f, mc.player?.isOnGround ?: false, false)
                )
                mc.player?.setPosition(it.x + 0.5, it.y + 1.05, it.z + 0.5)
                mc.player?.setVelocity(0.0, 0.0, 0.0)
            }
        }
    }

    private data class EtherPos(val succeeded: Boolean, val pos: BlockPos?, val state: BlockState?) {
        val vec: Vec3d? by lazy { pos?.let { Vec3d(it) } }
        companion object {
            val NONE = EtherPos(false, null, null)
        }
    }

    private fun getEtherPos(yaw: Float, pitch: Float, distance: Double, returnEnd: Boolean = false): EtherPos {
        val startPos = mc.player?.pos?.add(0.0, 1.54, 0.0) ?: return EtherPos.NONE
        val endPos = getLook(yaw, pitch).normalize().multiply(distance).add(startPos)
        return traverseVoxels(startPos, endPos).takeUnless { it == EtherPos.NONE && returnEnd } ?: EtherPos(true, endPos.toBlockPos(), null)
    }

    private fun getEtherPos(distance: Double): EtherPos =
        mc.player?.let { getEtherPos(it.yaw, it.pitch, distance) } ?: EtherPos.NONE

    /**
     * Traverses voxels from start to end and returns the first non-air block it hits.
     * @author Bloom
     */
    private fun traverseVoxels(start: Vec3d, end: Vec3d): EtherPos {
        val (x0, y0, z0) = start
        val (x1, y1, z1) = end

        var (x, y, z) = start.floorVec()
        val (endX, endY, endZ) = end.floorVec()

        val dirX = x1 - x0
        val dirY = y1 - y0
        val dirZ = z1 - z0

        val stepX = sign(dirX).toInt()
        val stepY = sign(dirY).toInt()
        val stepZ = sign(dirZ).toInt()

        val invDirX = if (dirX != 0.0) 1.0 / dirX else Double.MAX_VALUE
        val invDirY = if (dirY != 0.0) 1.0 / dirY else Double.MAX_VALUE
        val invDirZ = if (dirZ != 0.0) 1.0 / dirZ else Double.MAX_VALUE

        val tDeltaX = abs(invDirX * stepX)
        val tDeltaY = abs(invDirY * stepY)
        val tDeltaZ = abs(invDirZ * stepZ)

        var tMaxX = abs((x + max(stepX, 0) - x0) * invDirX)
        var tMaxY = abs((y + max(stepY, 0) - y0) * invDirY)
        var tMaxZ = abs((z + max(stepZ, 0) - z0) * invDirZ)

        repeat(1000) {
            val blockPos = BlockPos(x.toInt(), y.toInt(), z.toInt())
            val chunk = mc.world?.getChunk(ChunkSectionPos.getSectionCoord(blockPos.x), ChunkSectionPos.getSectionCoord(blockPos.z)) ?: return EtherPos.NONE
            val currentBlock = chunk.getBlockState(blockPos).takeIf { it.block is Block } ?: return EtherPos.NONE
            val currentBlockId = Block.getRawIdFromState(currentBlock)

            if (currentBlockId != 0) {
                if (validEtherwarpFeetIds.get(currentBlockId)) return EtherPos(false, blockPos, currentBlock)

                val footBlockId = Block.getRawIdFromState(chunk.getBlockState(BlockPos(blockPos.x, blockPos.y + 1, blockPos.z)))
                if (!validEtherwarpFeetIds.get(footBlockId)) return EtherPos(false, blockPos, currentBlock)

                val headBlockId = Block.getRawIdFromState(chunk.getBlockState(BlockPos(blockPos.x, blockPos.y + 2, blockPos.z)))
                if (!validEtherwarpFeetIds.get(headBlockId)) return EtherPos(false, blockPos, currentBlock)

                return EtherPos(true, blockPos, currentBlock)
            }

            if (x == endX && y == endY && z == endZ) return EtherPos.NONE

            when {
                tMaxX <= tMaxY && tMaxX <= tMaxZ -> {
                    tMaxX += tDeltaX
                    x += stepX
                }
                tMaxY <= tMaxZ -> {
                    tMaxY += tDeltaY
                    y += stepY
                }
                else -> {
                    tMaxZ += tDeltaZ
                    z += stepZ
                }
            }
        }

        return EtherPos.NONE
    }

    private val validEtherwarpFeetIds = BitSet(176).apply {
        arrayOf(
            Blocks.AIR, Blocks.FIRE, Blocks.SKELETON_SKULL, Blocks.PLAYER_HEAD, Blocks.LEVER,
            Blocks.STONE_BUTTON, Blocks.OAK_BUTTON, Blocks.TORCH, Blocks.TRIPWIRE_HOOK, Blocks.TRIPWIRE,
            Blocks.RAIL, Blocks.ACTIVATOR_RAIL, Blocks.SNOW, Blocks.CARROTS, Blocks.WHEAT, Blocks.POTATOES,
            Blocks.NETHER_WART, Blocks.PUMPKIN_STEM, Blocks.MELON_STEM, Blocks.REDSTONE_TORCH, Blocks.REDSTONE_WIRE,
            Blocks.POPPY, Blocks.DANDELION, Blocks.OAK_SAPLING, Blocks.FLOWER_POT, Blocks.DEAD_BUSH,
            Blocks.LADDER, Blocks.SUNFLOWER, Blocks.REPEATER, Blocks.COMPARATOR, Blocks.COBWEB, Blocks.LILY_PAD,
            Blocks.WATER, Blocks.LAVA, Blocks.VINE, Blocks.BROWN_MUSHROOM, Blocks.RED_MUSHROOM, Blocks.PISTON_HEAD,

            // All 16 carpets
            Blocks.WHITE_CARPET, Blocks.ORANGE_CARPET, Blocks.MAGENTA_CARPET, Blocks.LIGHT_BLUE_CARPET, Blocks.YELLOW_CARPET,
            Blocks.LIME_CARPET, Blocks.PINK_CARPET, Blocks.GRAY_CARPET, Blocks.LIGHT_GRAY_CARPET, Blocks.CYAN_CARPET, Blocks.PURPLE_CARPET,
            Blocks.BLUE_CARPET, Blocks.BROWN_CARPET, Blocks.GREEN_CARPET, Blocks.RED_CARPET, Blocks.BLACK_CARPET, Blocks.MOSS_CARPET
        ).forEach { set(Block.getRawIdFromState(it.defaultState)) }
    }
}