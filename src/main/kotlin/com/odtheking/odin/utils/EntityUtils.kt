package com.odtheking.odin.utils

import net.minecraft.entity.Entity
import net.minecraft.util.math.Vec3d

val Entity.pos : Vec3d get() = Vec3d(this.x, this.y, this.z)