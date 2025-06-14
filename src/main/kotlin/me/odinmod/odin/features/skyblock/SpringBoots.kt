package me.odinmod.odin.features.skyblock

import me.odinmod.odin.OdinMod.mc
import me.odinmod.odin.events.PacketEvent
import me.odinmod.odin.events.RenderEvent
import me.odinmod.odin.utils.drawBox
import me.odinmod.odin.utils.equalsOneOf
import me.odinmod.odin.utils.floatValues
import me.odinmod.odin.utils.getItemId
import me.odinmod.odin.utils.handlers.TickTask
import me.odinmod.odin.utils.skyblock.LocationUtils
import meteordevelopment.orbit.EventHandler
import net.minecraft.entity.EquipmentSlot
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Formatting
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d

object SpringBoots {

    init {
        TickTask(1) {
            if (!LocationUtils.isInSkyblock) return@TickTask
            if (mc.player?.isSneaking == false || mc.player?.getEquippedStack(EquipmentSlot.FEET)?.getItemId() != "SPRING_BOOTS") pitchCounts.fill(0)
            val index = pitchCounts.sum().coerceIn(blocksList.indices)
            blockPos = blocksList[index].let { if (it != 0.0) mc.player?.pos?.add(0.0, it, 0.0) else null }
        }
    }

    private val blocksList: List<Double> = listOf(
        0.0, 3.0, 6.5, 9.0, 11.5, 13.5, 16.0, 18.0, 19.0,
        20.5, 22.5, 25.0, 26.5, 28.0, 29.0, 30.0, 31.0, 33.0,
        34.0, 35.5, 37.0, 38.0, 39.5, 40.0, 41.0, 42.5, 43.5,
        44.0, 45.0, 46.0, 47.0, 48.0, 49.0, 50.0, 51.0, 52.0,
        53.0, 54.0, 55.0, 56.0, 57.0, 58.0, 59.0, 60.0, 61.0
    )

    private val pitchCounts = IntArray(2) { 0 }
    private var blockPos: Vec3d? = null

    @EventHandler
    fun onPacketReceive(event: PacketEvent.Receive) {
        if (!LocationUtils.isInSkyblock) return

        val packet = event.packet as? PlaySoundS2CPacket ?: return
        val id = packet.sound.value().id

        when {
            SoundEvents.BLOCK_NOTE_BLOCK_PLING.matchesId(id) -> {
                if (mc.player?.isSneaking != true || mc.player?.getEquippedStack(EquipmentSlot.FEET)?.getItemId() != "SPRING_BOOTS") return
                when (packet.pitch) {
                    0.6984127f -> pitchCounts[0] = (pitchCounts[0] + 1).takeIf { it <= 2 } ?: 0
                    0.82539684f, 0.8888889f, 0.93650794f, 1.0476191f, 1.1746032f, 1.3174603f, 1.7777778f -> pitchCounts[1]++
                }
            }
            SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH.id == id ->
                if (packet.pitch.equalsOneOf(0.0952381f, 1.6984127f)) pitchCounts.fill(0)
        }
    }

    @EventHandler
    fun onRenderWorld(event: RenderEvent.Last) {
        if (!LocationUtils.isInSkyblock) return
        blockPos?.let { drawBox(Box.from(it), event.context, Formatting.RED.floatValues()) }
    }
}