package io.github.odtheking.odin.utils

import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import org.joml.Matrix4f
import kotlin.math.floor

data class Vec2(val x: Int, val z: Int)

operator fun Vec3d.component1(): Double = x
operator fun Vec3d.component2(): Double = y
operator fun Vec3d.component3(): Double = z

operator fun BlockPos.component1(): Int = x
operator fun BlockPos.component2(): Int = y
operator fun BlockPos.component3(): Int = z

operator fun Vec3d.unaryMinus(): Vec3d = Vec3d(-x, -y, -z)

fun Vec3d.floorVec(): Vec3d =
    Vec3d(floor(x), floor(y), floor(z))

fun Vec3d.toBlockPos(): BlockPos =
    BlockPos(x.toInt(), y.toInt(), z.toInt())

fun Matrix4f.translate(vec: Vec3d): Matrix4f = this.translate(vec.x.toFloat(), vec.y.toFloat(), vec.z.toFloat())

fun Vec3d.addVec(x: Double = 0.0, y: Double = 0.0, z: Double = 0.0): Vec3d =
    Vec3d(this.x + x, this.y + y, this.z + z)