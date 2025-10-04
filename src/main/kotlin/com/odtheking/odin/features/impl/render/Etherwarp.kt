package com.odtheking.odin.features.impl.render

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.*
import com.odtheking.odin.events.PacketEvent
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.*
import com.odtheking.odin.utils.Color.Companion.multiplyAlpha
import com.odtheking.odin.utils.Color.Companion.withAlpha
import com.odtheking.odin.utils.render.drawFilledBox
import com.odtheking.odin.utils.render.drawWireFrameBox
import com.odtheking.odin.utils.skyblock.Island
import com.odtheking.odin.utils.skyblock.LocationUtils
import meteordevelopment.orbit.EventHandler
import net.minecraft.block.*
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket
import net.minecraft.registry.Registries
import net.minecraft.sound.SoundEvent
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.ChunkSectionPos
import net.minecraft.util.math.Vec3d
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sign

object Etherwarp : Module(
    name = "Etherwarp",
    description = "Provides configurable visual feedback for etherwarp."
) {
    private val render by BooleanSetting("Show Guess", true, desc = "Shows where etherwarp will take you.")
    private val color by ColorSetting("Color", Colors.MINECRAFT_GOLD.withAlpha(.5f), allowAlpha = true, desc = "Color of the box.").withDependency { render }
    private val renderFail by BooleanSetting("Show when failed", true, desc = "Shows the box even when the guess failed.").withDependency { render }
    private val failColor by ColorSetting("Fail Color", Colors.MINECRAFT_RED.withAlpha(.5f), allowAlpha = true, desc = "Color of the box if guess failed.").withDependency { renderFail }
    private val renderStyle by SelectorSetting("Render Style", "Outline", listOf("Outline", "Filled", "Filled Outline"), desc = "Style of the box.").withDependency { render }
    private val useServerPosition by BooleanSetting("Use Server Position", false, desc = "Uses the server position for etherwarp instead of the client position.").withDependency { render }
    private val fullBlock by BooleanSetting("Full Block", false, desc = "Renders the the 1x1x1 block instead of it's actual size.").withDependency { render }

    private val dropdown by DropdownSetting("Sounds", false)
    private val sounds by BooleanSetting("Custom Sounds", false, desc = "Plays the selected custom sound when you etherwarp.").withDependency { dropdown }
    private val customSound by StringSetting("Custom Sound", "entity.experience_orb.pickup", desc = "Name of a custom sound to play.", length = 64).withDependency { sounds && dropdown }
    private val reset by ActionSetting("Play sound", desc = "Plays the selected sound.") { playSoundAtPlayer(SoundEvent.of(Identifier.of(customSound))) }.withDependency { sounds && dropdown }

    private var etherPos: EtherPos? = null

    @EventHandler
    fun onRenderLast(event: RenderEvent.Last) {
        if (mc.player?.isSneaking == false || mc.currentScreen != null) return

        etherPos = getEtherPos(
            if (useServerPosition) mc.player?.lastPos else mc.player?.pos,
            56.0 + (isEtherwarpItem()?.getInt("tuned_transmission", 0) ?: return),
            etherWarp = true
        )
        if (etherPos?.succeeded != true && !renderFail) return
        val color = if (etherPos?.succeeded == true) color else failColor
        etherPos?.pos?.let { pos ->
            val box = if (fullBlock) Box(pos) else
                mc.world?.getBlockState(pos)?.getOutlineShape(mc.world, pos)?.asCuboid()
                    ?.takeIf { !it.isEmpty }?.boundingBox?.offset(pos) ?: Box(pos)

            when (renderStyle) {
                0 -> event.context.drawWireFrameBox(box, color)
                1 -> event.context.drawFilledBox(box, color)
                2 -> {
                    event.context.drawWireFrameBox(box, color)
                    event.context.drawFilledBox(box, color.multiplyAlpha(0.5f))
                }
            }
        }
    }

    @EventHandler
    fun onSoundPacket(event: PacketEvent.Receive) = with(event.packet) {
        if (!sounds || this !is PlaySoundS2CPacket || sound.value() != SoundEvents.ENTITY_ENDER_DRAGON_HURT || volume != 1f || pitch != 0.53968257f) return
        mc.execute { playSoundAtPlayer(SoundEvent.of(Identifier.of(customSound))) }
        event.cancel()
    }

    @EventHandler
    fun onPacketReceive(event: PacketEvent.Receive) = with(event.packet) {
        if (this !is PlayerInteractItemC2SPacket || !LocationUtils.currentArea.isArea(Island.SinglePlayer) || mc.player?.isSneaking == false || mc.currentScreen != null || isEtherwarpItem() == null) return

        etherPos?.pos?.let {
            if (etherPos?.succeeded == false) return@let
            mc.executeSync {
                mc.player?.networkHandler?.sendPacket(
                    PlayerMoveC2SPacket.Full(
                        it.x + 0.5,
                        it.y + 1.05,
                        it.z + 0.5,
                        mc.player?.yaw ?: 0f,
                        mc.player?.pitch ?: 0f,
                        mc.player?.isOnGround ?: false,
                        false
                    )
                )
                mc.player?.setPosition(it.x + 0.5, it.y + 1.05, it.z + 0.5)
                mc.player?.setVelocity(0.0, 0.0, 0.0)
            }
        }
        Unit
    }

    private fun isEtherwarpItem(): NbtCompound? =
        mc.player?.mainHandStack?.customData?.takeIf {
            it.getInt("ethermerge", 0) == 1 || it.itemId == "ETHERWARP_CONDUIT"
        }

    data class EtherPos(val succeeded: Boolean, val pos: BlockPos?, val state: BlockState?) {
        val vec: Vec3d? by lazy { pos?.let { Vec3d(it) } }

        companion object {
            val NONE = EtherPos(false, null, null)
        }
    }

    fun getEtherPos(
        position: Vec3d?,
        distance: Double,
        returnEnd: Boolean = false,
        etherWarp: Boolean = false
    ): EtherPos {
        val player = mc.player ?: return EtherPos.NONE
        if (position == null) return EtherPos.NONE
        val eyeHeight = if (player.isSneaking) {
            if (LocationUtils.currentArea.isArea(Island.Galatea)) 1.27 else 1.54 // Use modern sneak height in Galatea
        } else 1.62

        val startPos = position.addVec(y = eyeHeight)
        val endPos = player.rotationVector?.multiply(distance)?.add(startPos) ?: return EtherPos.NONE
        return traverseVoxels(startPos, endPos, etherWarp).takeUnless { it == EtherPos.NONE && returnEnd } ?: EtherPos(true, endPos.toBlockPos(), null)
    }

    /**
     * Traverses voxels from start to end and returns the first non-air block it hits.
     * @author Bloom
     */
    private fun traverseVoxels(start: Vec3d, end: Vec3d, etherWarp: Boolean): EtherPos {
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
            val chunk = mc.world?.getChunk(
                ChunkSectionPos.getSectionCoord(blockPos.x),
                ChunkSectionPos.getSectionCoord(blockPos.z)
            ) ?: return EtherPos.NONE
            val currentBlock = chunk.getBlockState(blockPos).takeIf { it.block is Block } ?: return EtherPos.NONE

            val currentBlockId = Block.getRawIdFromState(currentBlock.block.defaultState)

            if ((!validEtherwarpFeetIds.get(currentBlockId) && etherWarp) || (currentBlockId != 0 && !etherWarp)) {
                if (!etherWarp && validEtherwarpFeetIds.get(currentBlockId)) return EtherPos(
                    false,
                    blockPos,
                    currentBlock.block.defaultState
                )

                val footBlockId = Block.getRawIdFromState(
                    chunk.getBlockState(
                        BlockPos(
                            blockPos.x,
                            blockPos.y + 1,
                            blockPos.z
                        )
                    ).block.defaultState
                )
                if (!validEtherwarpFeetIds.get(footBlockId)) return EtherPos(false, blockPos, currentBlock)

                val headBlockId = Block.getRawIdFromState(
                    chunk.getBlockState(
                        BlockPos(
                            blockPos.x,
                            blockPos.y + 2,
                            blockPos.z
                        )
                    ).block.defaultState
                )
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

    private val validTypes = setOf(
        ButtonBlock::class, CarpetBlock::class, SkullBlock::class,
        WallSkullBlock::class, LadderBlock::class, SaplingBlock::class,
        FlowerBlock::class, StemBlock::class, CropBlock::class,
        RailBlock::class, SnowBlock::class,
        TripwireBlock::class, TripwireHookBlock::class, FireBlock::class,
        AirBlock::class, TorchBlock::class, FlowerPotBlock::class,
        TallFlowerBlock::class, ShortPlantBlock::class, BushBlock::class,
        SeagrassBlock::class, TallSeagrassBlock::class, SugarCaneBlock::class,
        FluidBlock::class, VineBlock::class, MushroomPlantBlock::class,
        PistonHeadBlock::class, DyedCarpetBlock::class, CobwebBlock::class,
        DryVegetationBlock::class, SmallDripleafBlock::class, LeverBlock::class,
        NetherWartBlock::class, NetherPortalBlock::class, RedstoneWireBlock::class,
        ComparatorBlock::class, RedstoneTorchBlock::class, RepeaterBlock::class
    )

    private val validEtherwarpFeetIds = BitSet(0).apply {
        Registries.BLOCK.forEach { block ->
            if (validTypes.any { it.isInstance(block) }) set(Block.getRawIdFromState(block.defaultState))
        }
    }
}