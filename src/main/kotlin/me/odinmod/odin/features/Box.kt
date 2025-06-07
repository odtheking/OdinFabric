package me.odinmod.odin.features

import me.odinmod.odin.events.RenderEvent
import me.odinmod.odin.utils.drawBox
import me.odinmod.odin.utils.drawFilledBox
import me.odinmod.odin.utils.drawSphere
import me.odinmod.odin.utils.renderText
import meteordevelopment.orbit.EventHandler
import net.minecraft.text.Text
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d


object Box {
    val debugBox = Box(0.0, 100.0, 0.0, 1.0, 101.0, 1.0)
    val RED = listOf(1f, 0f, 0f, 1f)
    val WHITE_TRANSPARENT = listOf(1f, 1f, 1f, .4f)
    val center = Vec3d(3.5, 100.5, 0.5)
    val radius = 1.0
    val segments = 24
    val GREEN = listOf(0f, 1f, 0f, 1f) // green

    @EventHandler
    fun onRenderLast(event: RenderEvent.Last) {
        drawFilledBox(debugBox, event.context, WHITE_TRANSPARENT)
        drawBox(debugBox, event.context, RED)
        renderText(
            event.context,
            Text.of("Odon").asOrderedText(),
            Vec3d(0.5, 101.5, 0.5),
            1f, 0f, false,
        )
        drawSphere(center, radius, segments, event.context, GREEN)

    }
}