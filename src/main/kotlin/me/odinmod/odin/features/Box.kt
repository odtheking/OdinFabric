package me.odinmod.odin.features

import me.odinmod.odin.OdinMod.mc
import me.odinmod.odin.events.RenderEvent
import me.odinmod.odin.utils.*
import meteordevelopment.orbit.EventHandler
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d


object Box {
    val debugBox = Box(0.0, 100.0, 0.0, 1.0, 101.0, 1.0)
    val RED = Formatting.RED.floatValues()
    val WHITE_TRANSPARENT = Formatting.WHITE.floatValues().withAlpha(.5f)
    val center = Vec3d(3.5, 100.5, 0.5)
    val radius = 1.0
    val segments = 24
    val GREEN = Formatting.GREEN.floatValues() // green

    @EventHandler
    fun onRenderLast(event: RenderEvent.Last) {
        if (!FabricLoader.getInstance().isDevelopmentEnvironment && !mc.debugHud.shouldShowDebugHud()) return
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