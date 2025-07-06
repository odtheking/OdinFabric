package me.odinmod.odin.features.impl.render

import me.odinmod.odin.OdinMod.mc
import me.odinmod.odin.events.RenderEvent
import me.odinmod.odin.utils.Color.Companion.withAlpha
import me.odinmod.odin.utils.Colors
import me.odinmod.odin.utils.render.*
import meteordevelopment.orbit.EventHandler
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
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
        event.context.drawFilledBox(debugBox.offset(5.0, 5.0, 5.0), Colors.WHITE.withAlpha(0.5f))
        event.context.drawWireFrameBox(debugBox, Colors.MINECRAFT_RED)
        event.context.drawText(
            Text.of("Odon").asOrderedText(),
            Vec3d(0.5, 101.5, 0.5),
            1f, false
        )
        event.context.drawSphere(center, radius, segments, Colors.MINECRAFT_GREEN)
        event.context.drawBeaconBeam(BlockPos(2, 100, 0), Colors.MINECRAFT_BLUE)

        event.context.drawCustomBeacon("Odin", BlockPos(2, 100, 5), Colors.MINECRAFT_BLUE)
    }
}