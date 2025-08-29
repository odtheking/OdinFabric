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
        if (blockAmount == 0f && !it) return@HUD 0f to 0f
        var width = 1
        width += drawStringWidth("Height: ", width, 1, Colors.MINECRAFT_LIGHT_PURPLE, true)
        width += drawStringWidth(getColor(blockAmount), width, 1, Colors.WHITE, true)
        width to mc.textRenderer.fontHeight
    }

    private val pitchSet = setOf(0.82539684f, 0.8888889f, 0.93650794f, 1.0476191f, 1.1746032f, 1.3174603f, 1.7777778f)
    private var blockAmount = 0f
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
                    in pitchSet -> highCount++
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
        if (!LocationUtils.isInSkyblock || blockAmount == 0f) return
        mc.player?.pos?.addVec(y = blockAmount)?.let { event.context.drawWireFrameBox(Box.from(it), Colors.MINECRAFT_RED) }
    }

    @EventHandler
    fun onTick(event: TickEvent.End) {
        if (!LocationUtils.isInSkyblock || !(mc.player?.isSneaking == false || !(EquipmentSlot.FEET isItem "SPRING_BOOTS"))) return
        highCount = 0
        lowCount = 0
    }

    private fun getColor(blocks: Float): String {
        return when {
            blocks <= 13.5 -> "§c"
            blocks <= 22.5 -> "§e"
            blocks <= 33.0 -> "§6"
            blocks <= 43.5 -> "§a"
            else -> "§b"
        } + blocks
    }

    private val blocksList = listOf(
        0.0f, 3.0f, 6.5f, 9.0f, 11.5f, 13.5f, 16.0f, 18.0f, 19.0f,
        20.5f, 22.5f, 25.0f, 26.5f, 28.0f, 29.0f, 30.0f, 31.0f, 33.0f,
        34.0f, 35.5f, 37.0f, 38.0f, 39.5f, 40.0f, 41.0f, 42.5f, 43.5f,
        44.0f, 45.0f, 46.0f, 47.0f, 48.0f, 49.0f, 50.0f, 51.0f, 52.0f,
        53.0f, 54.0f, 55.0f, 56.0f, 57.0f, 58.0f, 59.0f, 60.0f, 61.0f
    )
}
