package com.odtheking.odin.features.impl.skyblock

import com.odtheking.odin.clickgui.settings.impl.SelectorSetting
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.render.drawCylinder
import com.odtheking.odin.utils.render.drawFilledBox
import com.odtheking.odin.utils.render.drawLine
import com.odtheking.odin.utils.render.drawStyledBox
import com.odtheking.odin.utils.render.drawWireFrameBox

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

    init {
        on<RenderEvent.Last> {
            if (mc.player == null) return@on

            drawWireFrameBox(
                box = mc.player!!.boundingBox.expand(2.0, 2.0, 2.0),
                color = Color(0x7eb4c7ff),
            )

            drawLine(
                points = listOf(
                    mc.player!!.eyePos,
                    mc.player!!.eyePos.add(0.0, 5.0, 0.0)
                ),
                color = Color(0x7eb4c7ff),
                depth = false,
            )

            drawCylinder(
                center = mc.player!!.entityPos,
                radius = 1f,
                height = 1f,
                color = Color(0x7eb4c7ff),
            )

            drawStyledBox(
                box = mc.player!!.boundingBox.expand(1.0, 1.0, 1.0),
                color = Color(0x7eb4c7ff),
                style = boxStyle,
            )

            drawFilledBox(
                box = mc.player!!.boundingBox.expand(0.5, 0.5, 0.5),
                color = Color(0x7eb4c7ff),
            )
        }
    }

}