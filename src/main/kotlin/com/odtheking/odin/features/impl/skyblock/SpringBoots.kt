package com.odtheking.odin.features.impl.skyblock

import com.odtheking.odin.events.PacketEvent
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.addVec
import com.odtheking.odin.utils.equalsOneOf
import com.odtheking.odin.utils.isItem
import com.odtheking.odin.utils.render.drawStringWidth
import com.odtheking.odin.utils.render.drawWireFrameBox
import com.odtheking.odin.utils.skyblock.LocationUtils
import meteordevelopment.orbit.EventHandler
import net.minecraft.entity.EquipmentSlot
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.Box

object SpringBoots : Module(
    name = "Spring Boots",
    description = "Shows the current jump height of your spring boots."
) {
    private val hud by HUD("Spring Boots", "Shows the how high you will jump.") {
        if (blockAmount == 0.0 && !it) return@HUD 0f to 0f
        var width = 1f
        width += drawStringWidth("Height: ", width, 1f, Colors.MINECRAFT_LIGHT_PURPLE, true)
        width += drawStringWidth(getColor(blockAmount), width, 1f, Colors.WHITE, true)
        width to mc.textRenderer.fontHeight
    }

    private var blockAmount = 0.0
    private var highCount = 0
    private var lowCount = 0

    @EventHandler
    fun onPacketReceive(event: PacketEvent.Receive) = with(event.packet) {
        if (!LocationUtils.isInSkyblock || this !is PlaySoundS2CPacket) return
        val id = sound.value().id

        when {
            SoundEvents.BLOCK_NOTE_BLOCK_PLING.matchesId(id) && mc.player?.isSneaking == true && EquipmentSlot.FEET isItem "SPRING_BOOTS" ->
                when (pitch) {
                    0.6984127f -> lowCount = (lowCount + 1).coerceAtMost(2)
                    in setOf(
                        0.82539684f,
                        0.8888889f,
                        0.93650794f,
                        1.0476191f,
                        1.1746032f,
                        1.3174603f,
                        1.7777778f
                    ) -> highCount++
                }

            SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH.id == id && pitch.equalsOneOf(0.0952381f, 1.6984127f) -> {
                highCount = 0
                lowCount = 0
            }
        }

        blockAmount = blocksList[(lowCount + highCount).coerceIn(blocksList.indices)]
    }

    @EventHandler
    fun onRenderWorld(event: RenderEvent.Last) {
        if (!LocationUtils.isInSkyblock || blockAmount == 0.0) return
        mc.player?.pos?.addVec(y = blockAmount)
            ?.let { event.context.drawWireFrameBox(Box.from(it), Colors.MINECRAFT_RED) }
    }

    @EventHandler
    fun onTick(event: TickEvent.End) {
        if (!LocationUtils.isInSkyblock || !(mc.player?.isSneaking == false || EquipmentSlot.FEET isItem "SPRING_BOOTS")) return
        highCount = 0
        lowCount = 0
    }

    private fun getColor(blocks: Double): String {
        return when {
            blocks <= 13.5 -> "§c"
            blocks <= 22.5 -> "§e"
            blocks <= 33.0 -> "§6"
            blocks <= 43.5 -> "§a"
            else -> "§b"
        } + blocks
    }

    private val blocksList = listOf(
        0.0, 3.0, 6.5, 9.0, 11.5, 13.5, 16.0, 18.0, 19.0,
        20.5, 22.5, 25.0, 26.5, 28.0, 29.0, 30.0, 31.0, 33.0,
        34.0, 35.5, 37.0, 38.0, 39.5, 40.0, 41.0, 42.5, 43.5,
        44.0, 45.0, 46.0, 47.0, 48.0, 49.0, 50.0, 51.0, 52.0,
        53.0, 54.0, 55.0, 56.0, 57.0, 58.0, 59.0, 60.0, 61.0
    )
}
