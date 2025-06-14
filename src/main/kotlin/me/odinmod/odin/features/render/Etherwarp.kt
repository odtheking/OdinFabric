package me.odinmod.odin.features.render

import me.odinmod.odin.OdinMod.mc
import me.odinmod.odin.config.categories.RenderConfig
import me.odinmod.odin.events.PacketEvent
import me.odinmod.odin.events.RenderEvent
import me.odinmod.odin.utils.*
import me.odinmod.odin.utils.skyblock.Island
import me.odinmod.odin.utils.skyblock.LocationUtils
import meteordevelopment.orbit.EventHandler
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.client.util.InputUtil
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.ChunkSectionPos
import net.minecraft.util.math.Vec3d
import org.lwjgl.glfw.GLFW
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sign

object Etherwarp {

    private var etherPos: EtherPos? = null

    @EventHandler
    fun onRenderLast(event: RenderEvent.Last) {
        if (!RenderConfig.etherwarpHelper || !InputUtil.isKeyPressed(mc.window.handle, GLFW.GLFW_KEY_LEFT_SHIFT) || mc.currentScreen != null) return
        val mainHand = mc.player?.mainHandStack ?: return

        val customData = mainHand.getCustomData().takeIf { it.getInt("ethermerge", 0) == 1 || mainHand.getItemId() == "ETHERWARP_CONDUIT" } ?: return
        etherPos = getEtherPos(56.0 + customData.getInt("tuned_transmission", 0)).apply {
            if (etherPos?.succeeded == true || RenderConfig.showFailed) {
                val color = if (etherPos?.succeeded == true) RenderConfig.etherwarpHelperColor.floatValues() else RenderConfig.showFailedColor.floatValues()
                pos?.let {
                    when (RenderConfig.renderStyle) {
                        RenderStyle.OULINE -> drawBox(Box(it), event.context, color)

                        RenderStyle.FILLED -> drawFilledBox(Box(it), event.context, color)

                        RenderStyle.FILLED_OUTLINE -> {
                            drawFilledBox(Box(it), event.context, color.withAlpha(0.5f))
                            drawBox(Box(it), event.context, color)
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    fun onPacketReceived(event: PacketEvent.Send) {
        val packet = event.packet
        if (!RenderConfig.etherwarpHelper || packet !is PlayerInteractItemC2SPacket || !LocationUtils.currentArea.isArea(Island.SinglePlayer) || !InputUtil.isKeyPressed(mc.window.handle, GLFW.GLFW_KEY_LEFT_SHIFT) || mc.currentScreen != null) return
        mc.player?.mainHandStack?.let { stack -> stack.getCustomData().takeIf { it.getInt("ethermerge", 0) == 1 || stack.getItemId() == "ETHERWARP_CONDUIT" } ?: return }
        etherPos?.pos?.let {
            if (etherPos?.succeeded == false) return@let
            mc.executeSync {
                mc.player?.networkHandler?.sendPacket(
                    PlayerMoveC2SPacket.Full(
                        it.x + 0.5, it.y + 1.05, it.z + 0.5,
                        mc.player?.yaw ?: 0f, mc.player?.pitch ?: 0f, mc.player?.isOnGround ?: false,
                        false
                    )
                )
                mc.player?.setPosition(it.x + 0.5, it.y + 1.05, it.z + 0.5)
                mc.player?.setVelocity(0.0, 0.0, 0.0)
            }
        }
    }

    data class EtherPos(val succeeded: Boolean, val pos: BlockPos?, val state: BlockState?) {
        val vec: Vec3d? by lazy { pos?.let { Vec3d(it) } }
        companion object {
            val NONE = EtherPos(false, null, null)
        }
    }

    /**
     * Gets the position of an entity in the "ether" based on the player's view direction.
     *
     * @param yaw The yaw angle representing the player's horizontal viewing direction.
     * @param pitch The pitch angle representing the player's vertical viewing direction.
     * @return An `EtherPos` representing the calculated position in the "ether" or `EtherPos.NONE` if the player is not present.
     */
    fun getEtherPos(yaw: Float, pitch: Float, distance: Double, returnEnd: Boolean = false): EtherPos {
        val startPos = mc.player?.pos?.add(0.0, 1.62, 0.0) ?: return EtherPos.NONE
        val endPos = getLook(yaw, pitch).normalize().multiply(distance).add(startPos)
        return traverseVoxels(startPos, endPos).takeUnless { it == EtherPos.NONE && returnEnd } ?: EtherPos(true, endPos.toBlockPos(), null)
    }

    fun getEtherPos(distance: Double): EtherPos =
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