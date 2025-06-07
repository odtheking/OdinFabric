package me.odinmod.odin.features

import me.odinmod.odin.events.RenderEvent
import me.odinmod.odin.utils.drawBox
import me.odinmod.odin.utils.renderText
import meteordevelopment.orbit.EventHandler
import net.minecraft.text.Text
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d


object Box {
    val debugBox = Box(0.0, 100.0, 0.0, 1.0, 101.0, 1.0)
    val RED = listOf(1f, 0f, 0f, 1f)

    @EventHandler
    fun onRenderLast(event: RenderEvent.Last) {
        drawBox(debugBox, event.context, RED)
        renderText(
            event.context,
            Text.of("Odon").asOrderedText(),
            Vec3d(0.5, 101.5, 0.5),
            1f, 0f, false,
        )
    }
}