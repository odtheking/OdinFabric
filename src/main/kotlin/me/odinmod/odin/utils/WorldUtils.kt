package me.odinmod.odin.utils

import me.odinmod.odin.OdinMod.Companion.mc
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos

fun getBlockAtPos(pos: BlockPos?) : BlockState? {
    return pos.let { mc.world?.getBlockState(pos) }
}