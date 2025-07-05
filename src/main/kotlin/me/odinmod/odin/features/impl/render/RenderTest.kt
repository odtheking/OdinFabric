package me.odinmod.odin.features.impl.render

import me.odinmod.odin.OdinMod.mc
import me.odinmod.odin.events.RenderEvent
import me.odinmod.odin.utils.Color.Companion.withAlpha
import me.odinmod.odin.utils.Colors
import me.odinmod.odin.utils.render.drawBox
import me.odinmod.odin.utils.render.drawFilledBox
import me.odinmod.odin.utils.render.drawSphere
import me.odinmod.odin.utils.render.renderText
import meteordevelopment.orbit.EventHandler
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.text.Text
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d


object RenderTest {
    val debugBox = Box(0.0, 100.0, 0.0, 1.0, 101.0, 1.0)
    val center = Vec3d(3.5, 100.5, 0.5)
    val radius = 1.0
    val segments = 24

    @EventHandler
    fun onRenderLast(event: RenderEvent.Last) {
        if (!FabricLoader.getInstance().isDevelopmentEnvironment && !mc.debugHud.shouldShowDebugHud()) return
        drawFilledBox(debugBox, event.context, Colors.WHITE.withAlpha(0.5f))
        drawBox(debugBox, event.context, Colors.MINECRAFT_RED)
        renderText(
            event.context,
            Text.of("Odon").asOrderedText(),
            Vec3d(0.5, 101.5, 0.5),
            1f, 0f, false,
        )
        drawSphere(center, radius, segments, event.context, Colors.MINECRAFT_GREEN)

    }
}