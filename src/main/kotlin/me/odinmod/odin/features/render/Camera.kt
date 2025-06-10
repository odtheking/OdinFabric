package me.odinmod.odin.features.render

import me.odinmod.odin.OdinMod.mc
import me.odinmod.odin.config.categories.RenderConfig
import me.odinmod.odin.events.TickEvent
import meteordevelopment.orbit.EventHandler
import net.minecraft.client.option.Perspective

object Camera {

    @EventHandler
    fun onTick(event: TickEvent.End) {
        if (mc.options.perspective == Perspective.THIRD_PERSON_FRONT && RenderConfig.disableFrontCam)
            mc.options.perspective = Perspective.FIRST_PERSON
    }
}