package com.odtheking.odin.utils

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.utils.skyblock.dungeon.tiles.Rotations
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import org.joml.Matrix4f
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

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

fun Vec3d.addVec(x: Number = 0.0, y: Number = 0.0, z: Number = 0.0): Vec3d =
    Vec3d(this.x + x.toDouble(), this.y + y.toDouble(), this.z + z.toDouble())

/**
 * Rotates a Vec3 around the given rotation.
 * @param rotation The rotation to rotate around
 * @return The rotated Vec3
 */
fun BlockPos.rotateAroundNorth(rotation: Rotations): BlockPos =
    when (rotation) {
        Rotations.NORTH -> BlockPos(-this.x, this.y, -this.z)
        Rotations.WEST ->  BlockPos(-this.z, this.y, this.x)
        Rotations.SOUTH -> BlockPos(this.x, this.y, this.z)
        Rotations.EAST ->  BlockPos(this.z, this.y, -this.x)
        else -> this
    }

/**
 * Rotates a Vec3 to the given rotation.
 * @param rotation The rotation to rotate to
 * @return The rotated Vec3
 */
fun BlockPos.rotateToNorth(rotation: Rotations): BlockPos =
    when (rotation) {
        Rotations.NORTH -> BlockPos(-this.x, this.y, -this.z)
        Rotations.WEST ->  BlockPos(this.z, this.y, -this.x)
        Rotations.SOUTH -> BlockPos(this.x, this.y, this.z)
        Rotations.EAST ->  BlockPos(-this.z, this.y, this.x)
        else -> this
    }

fun BlockPos.addRotationCoords(rotation: Rotations, x: Int, z: Int): BlockPos =
    when (rotation) {
        Rotations.NORTH -> BlockPos(this.x + x, this.y, this.z + z)
        Rotations.SOUTH -> BlockPos(this.x - x, this.y, this.z - z)
        Rotations.WEST ->  BlockPos(this.x + z, this.y, this.z - x)
        Rotations.EAST ->  BlockPos(this.x - z, this.y, this.z + x)
        else -> this
    }

fun isXZInterceptable(box: Box, range: Double, pos: Vec3d, yaw: Float, pitch: Float): Boolean {
    val start = getPositionEyes(pos)
    val goal = start.add(getLook(yaw, pitch).multiply(range))

    return isVecInZ(start.intermediateWithXValue(goal, box.minX), box) ||
            isVecInZ(start.intermediateWithXValue(goal, box.maxX), box) ||
            isVecInX(start.intermediateWithZValue(goal, box.minZ), box) ||
            isVecInX(start.intermediateWithZValue(goal, box.maxZ), box)
}

private fun getPositionEyes(pos: Vec3d): Vec3d =
    Vec3d(pos.x, pos.y + (mc.player?.eyeY ?: 0.0), pos.z) // Add eye height like old getPositionEyes

private fun getLook(yaw: Float, pitch: Float): Vec3d {
    val f2 = -cos(-pitch * 0.017453292f).toDouble()
    return Vec3d(
        sin(-yaw * 0.017453292f - 3.1415927f) * f2,
        sin(-pitch * 0.017453292f).toDouble(),
        cos(-yaw * 0.017453292f - 3.1415927f) * f2
    )
}

private fun isVecInX(vec: Vec3d?, box: Box): Boolean =
    vec != null && vec.x >= box.minX && vec.x <= box.maxX

private fun isVecInZ(vec: Vec3d?, box: Box): Boolean =
    vec != null && vec.z >= box.minZ && vec.z <= box.maxZ

private fun Vec3d.intermediateWithXValue(goal: Vec3d, x: Double): Vec3d? {
    val dx = goal.x - this.x
    if (dx * dx < 1e-8) return null
    val t = (x - this.x) / dx
    return if (t in 0.0..1.0) Vec3d(
        this.x + dx * t,
        this.y + (goal.y - this.y) * t,
        this.z + (goal.z - this.z) * t
    ) else null
}

private fun Vec3d.intermediateWithZValue(goal: Vec3d, z: Double): Vec3d? {
    val dz = goal.z - this.z
    if (dz * dz < 1e-8) return null
    val t = (z - this.z) / dz
    return if (t in 0.0..1.0) Vec3d(
        this.x + (goal.x - this.x) * t,
        this.y + (goal.y - this.y) * t,
        this.z + dz * t
    ) else null
}