package me.odinmod.odin.utils

import me.odinmod.odin.OdinMod.mc
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import org.joml.Matrix4f
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

operator fun Vec3d.component1(): Double = x
operator fun Vec3d.component2(): Double = y
operator fun Vec3d.component3(): Double = z

operator fun Vec3d.unaryMinus(): Vec3d = Vec3d(-x, -y, -z)

fun Vec3d.floorVec(): Vec3d =
    Vec3d(floor(x), floor(y), floor(z))

fun getLook(yaw: Float = mc.player?.yaw ?: 0f, pitch: Float = mc.player?.pitch ?: 0f): Vec3d {
    val f2 = -cos(-pitch * 0.017453292f).toDouble()
    return Vec3d(
        sin(-yaw * 0.017453292f - 3.1415927f) * f2,
        sin(-pitch * 0.017453292f).toDouble(),
        cos(-yaw * 0.017453292f - 3.1415927f) * f2
    )
}

fun Vec3d.toBlockPos(): BlockPos =
    BlockPos(x.toInt(), y.toInt(), z.toInt())

fun Matrix4f.translate(vec: Vec3d): Matrix4f = this.translate(vec.x.toFloat(), vec.y.toFloat(), vec.z.toFloat())