package com.odtheking.odin.features.impl.dungeon

import com.odtheking.odin.events.BlockUpdateEvent
import com.odtheking.odin.events.PacketEvent
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.render.drawText
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.toFixed
import meteordevelopment.orbit.EventHandler
import net.minecraft.block.FlowerPotBlock
import net.minecraft.network.packet.s2c.common.CommonPingS2CPacket
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import java.util.concurrent.CopyOnWriteArrayList

object TerracottaTimer : Module(
    name = "Terracotta Timer",
    description = "Displays the time until the terracotta respawns."
) {
    private var terracottaSpawning = CopyOnWriteArrayList<Terracotta>()
    private data class Terracotta(val pos: BlockPos, var time: Float)

    @EventHandler
    fun onBlockPacket(event: BlockUpdateEvent) {
        if (DungeonUtils.isFloor(6) && DungeonUtils.inBoss && event.updated.block is FlowerPotBlock && terracottaSpawning.none { it.pos == event.pos })
            terracottaSpawning.add(Terracotta(event.pos, if (DungeonUtils.floor?.isMM == true) 12f else 15f))
    }

    @EventHandler
    fun onPacketReceive(event: PacketEvent.Receive) {
        if (event.packet is CommonPingS2CPacket)
            terracottaSpawning.removeAll {
                it.time -= .05f
                it.time <= 0
            }
    }

    @EventHandler
    fun onRenderWorld(event: RenderEvent.Last) {
        if (!DungeonUtils.inBoss || !DungeonUtils.isFloor(6) || terracottaSpawning.isEmpty()) return
        terracottaSpawning.forEach {
            event.drawText(Text.of("§${getColor(it.time)}${it.time.toFixed()}s").asOrderedText(), it.pos.toCenterPos(), depth = false, scale = 1f)
        }
    }

    private fun getColor(time: Float): Char {
        return when {
            time > 5f -> 'a'
            time > 2f -> '6'
            else -> 'c'
        }
    }
}