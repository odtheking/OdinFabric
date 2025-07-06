package me.odinmod.odin.utils.skyblock

import net.minecraft.util.math.BlockPos

enum class SupplyPickUpSpot(val location: BlockPos) {
    Triangle(BlockPos(-67, 77, -122)),
    X(BlockPos(-142, 77, -151)),
    Equals(BlockPos(-65, 76, -87)),
    Slash(BlockPos(-113, 77, -68)),
    Shop(BlockPos(-81, 76, -143)),
    xCannon(BlockPos(-143, 76, -125)),
    Square(BlockPos(-143, 76, -80)),
    None(BlockPos(0, 0, 0))
}