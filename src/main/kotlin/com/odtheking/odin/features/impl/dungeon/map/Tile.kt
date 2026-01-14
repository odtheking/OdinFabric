package com.odtheking.odin.features.impl.dungeon.map

import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Colors

abstract class Tile(val pos: Vec2i) {
    abstract fun size(): Vec2i
    abstract fun placement(): Vec2i
    abstract fun color(): Array<Color>
}

class Unknown(pos: Vec2i) : Tile(pos) {
    override fun size(): Vec2i {
        return Vec2i(0, 0)
    }

    override fun placement(): Vec2i {
        return Vec2i(0, 0)
    }

    override fun color(): Array<Color> {
        return arrayOf(Colors.MINECRAFT_RED)
    }
}