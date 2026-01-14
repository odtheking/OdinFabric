package com.odtheking.odin.features.impl.dungeon.map

data class Vec2i(val x: Int, val z: Int) {
    fun add(other: Vec2i): Vec2i = Vec2i(x + other.x, z + other.z)
    fun add(x0: Int, z0: Int): Vec2i = Vec2i(x + x0, z + z0)

    fun multiply(number: Int): Vec2i = Vec2i(x * number, z * number)
    fun multiply(number: Double): Vec2i = Vec2i((x * number).toInt(), (z * number).toInt())

    fun divide(number: Int): Vec2i = Vec2i(x / number, z / number)
    fun divide(number: Double): Vec2i = Vec2i((x / number).toInt(), (z / number).toInt())

    fun mapIndex(): Int = z * 128 + x
    fun roomListIndex(): Int = x * 22 + z * 2
}