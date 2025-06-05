package me.odinmod.odin.utils

import me.odinmod.odin.OdinMod.mc
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkSectionPos
import net.minecraft.util.math.Vec3d
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sign

object EtherWarpHelper {
    data class EtherPos(val succeeded: Boolean, val pos: BlockPos?, val state: BlockState?) {
        inline val vec: Vec3d? get() = pos?.let { Vec3d(it) }
        companion object {
            val NONE = EtherPos(false, null, null)
        }
    }
    var etherPos: EtherPos = EtherPos.NONE

    /**
     * Gets the position of an entity in the "ether" based on the player's view direction.
     *
     * @param pos The initial position of the entity.
     * @param yaw The yaw angle representing the player's horizontal viewing direction.
     * @param pitch The pitch angle representing the player's vertical viewing direction.
     * @return An `EtherPos` representing the calculated position in the "ether" or `EtherPos.NONE` if the player is not present.
     */
    fun getEtherPos(yaw: Float, pitch: Float, distance: Double = 60.0, returnEnd: Boolean = false): EtherPos {
        val player = mc.player ?: return EtherPos.NONE
        val startPos = player.eyePos
        val endPos = getLook(yaw = yaw, pitch = pitch).normalize().multiply(distance).add(startPos)
        return traverseVoxels(startPos, endPos).takeUnless { it == EtherPos.NONE && returnEnd } ?: EtherPos(true, endPos.toBlockPos(), null)
    }

    fun getEtherPos(distance: Double =  56.0 /*+ mc.thePlayer.heldItem.getTunerBonus*/): EtherPos {
        val player = mc.player ?: return EtherPos.NONE
        return getEtherPos(player.yaw, player.pitch, distance)
    }

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
            val xInt = x.toInt()
            val yInt = y.toInt()
            val zInt = z.toInt()
            val chunk = mc.world?.getChunk(ChunkSectionPos.getSectionCoord(xInt), ChunkSectionPos.getSectionCoord(zInt)) ?: return EtherPos.NONE
            val currentBlock = chunk.getBlockState(BlockPos(xInt, yInt, zInt))
            val currentBlockId = Block.getRawIdFromState(currentBlock)

            if (currentBlockId != 0) {
                if (validEtherwarpFeetIds.get(currentBlockId)) return EtherPos(false, BlockPos(xInt, yInt, zInt), currentBlock)

                val footBlockId = Block.getRawIdFromState(chunk.getBlockState(BlockPos(xInt, yInt + 1, zInt)))
                if (!validEtherwarpFeetIds.get(footBlockId)) return EtherPos(false, BlockPos(xInt, yInt, zInt), currentBlock)

                val headBlockId = Block.getRawIdFromState(chunk.getBlockState(BlockPos(xInt, yInt + 2, zInt)))
                if (!validEtherwarpFeetIds.get(headBlockId)) return EtherPos(false, BlockPos(xInt, yInt, zInt), currentBlock)

                return EtherPos(true, BlockPos(xInt, yInt, zInt), currentBlock)
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
            Blocks.BLUE_CARPET, Blocks.BROWN_CARPET, Blocks.GREEN_CARPET, Blocks.RED_CARPET, Blocks.BLACK_CARPET
        ).forEach { set(Block.getRawIdFromState(it.defaultState)) }
    }
}