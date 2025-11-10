package com.odtheking.odin.utils.skyblock

import net.minecraft.core.BlockPos

enum class Supply(val pickUpSpot: BlockPos, val dropOffSpot: BlockPos, var isActive: Boolean = true) {
    Triangle(BlockPos(-67, 77, -122), BlockPos(-94, 78, -106)),
    X(BlockPos(-142, 77, -151), BlockPos(-106, 78, -112)),
    Equals(BlockPos(-65, 76, -87), BlockPos(-98, 78, -99)),
    Slash(BlockPos(-113, 77, -68), BlockPos(-106, 78, -99)),
    Shop(BlockPos(-81, 76, -143), BlockPos(-98, 78, -112)),
    xCannon(BlockPos(-143, 76, -125), BlockPos(-110, 78, -106)),
    Square(BlockPos(-143, 76, -80), BlockPos(0, 0, 0)),
    None(BlockPos(0, 0, 0), BlockPos(0, 0, 0));
}