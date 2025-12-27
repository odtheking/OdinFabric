package com.odtheking.odin.features.impl.skyblock

import com.odtheking.odin.clickgui.settings.DevModule
import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.clickgui.settings.impl.SelectorSetting
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.render.*
import net.minecraft.world.phys.AABB
import kotlin.math.ceil

@DevModule
object RenderTest : Module(
    name = "Render Test",
    description = "Test rendering stuff"
) {

    val boxStyle by SelectorSetting(
        name = "Styled Box Style",
        default = "FilledBox",
        options = listOf("FilledBox", "WireFrame", "Both"),
        desc = "Style of the styled box"
    )

    val boxCount by NumberSetting("Box Count", 0, 0, 200000, 1, desc = "Approx number of boxes to render")
    val boxLevels by NumberSetting("Box Levels", 4, 1, 12, 1, desc = "Vertical layers for boxes")

    init {
        on<RenderEvent.Extract> {
            if (mc.player == null) return@on

            drawWireFrameBox(
                aabb = mc.player!!.boundingBox.inflate(2.0, 2.0, 2.0),
                color = Color(0x7eb4c7ff),
            )

            drawLine(
                points = listOf(
                    mc.player!!.eyePosition,
                    mc.player!!.eyePosition.add(0.0, 5.0, 0.0)
                ),
                color = Color(0x7eb4c7ff),
                depth = false,
            )

            drawCylinder(
                center = mc.player!!.position(),
                radius = 1f,
                height = 1f,
                color = Color(0x7eb4c7ff),
            )

            drawStyledBox(
                aabb = mc.player!!.boundingBox.inflate(1.0, 1.0, 1.0),
                color = Color(0x7eb4c7ff),
                style = boxStyle,
            )

            drawFilledBox(
                aabb = mc.player!!.boundingBox.inflate(0.5, 0.5, 0.5),
                color = Color(0x7eb4c7ff),
            )

            try {
                val center = mc.player!!.position()

                run {
                    val desiredCount = boxCount.coerceAtLeast(0)
                    val layers = boxLevels.coerceAtLeast(1)

                    if (desiredCount > 0) {
                        val perLayer = ceil(desiredCount.toDouble() / layers.toDouble()).toInt()
                        val side = ceil(kotlin.math.sqrt(perLayer.toDouble())).toInt()
                        val range = side / 2

                        var drawn = 0
                        outer@ for (by in 0 until layers) {
                            val byOff = (by - (layers / 2)).toDouble()
                            for (bx in -range..range) {
                                for (bz in -range..range) {
                                    if (drawn >= desiredCount) break@outer

                                    val bxOff = bx.toDouble()
                                    val bzOff = bz.toDouble()

                                    val minX = center.x + bxOff - 0.45
                                    val minY = center.y + byOff - 0.45
                                    val minZ = center.z + bzOff - 0.45
                                    val aabb = AABB(minX, minY, minZ, minX + 0.9, minY + 0.9, minZ + 0.9)

                                    val cInt = (0xff000000.toInt() or (((bx + range) and 0xff) shl 16) or (((by + 32) and 0xff) shl 8) or ((bz + range) and 0xff))
                                    val c = Color(cInt)

                                    drawStyledBox(aabb = aabb, color = c, style = 2,)

                                    drawn++
                                }
                            }
                        }
                    }
                }

            } catch (_: Exception) { }
        }
    }

}