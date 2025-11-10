package com.odtheking.odin.features.impl.dungeon.dungeonwaypoints

import com.odtheking.odin.config.DungeonWaypointConfig
import com.odtheking.odin.events.SecretPickupEvent
import com.odtheking.odin.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints.DungeonWaypoint
import com.odtheking.odin.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints.WaypointType
import com.odtheking.odin.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints.getWaypoints
import com.odtheking.odin.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints.lastEtherPos
import com.odtheking.odin.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints.lastEtherTime
import com.odtheking.odin.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints.setWaypoints
import com.odtheking.odin.utils.devMessage
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils.getRelativeCoords
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket
import net.minecraft.world.phys.Vec3

object SecretWaypoints {

    private var routeTimer: Long? = null
    private var checkpoints = 0

    fun onSecret(event: SecretPickupEvent) {
        when (event) {
            is SecretPickupEvent.Interact -> clickSecret(event.blockPos, 0.0)
            is SecretPickupEvent.Bat -> clickSecret(BlockPos.containing(event.packet.x, event.packet.y, event.packet.z), 5.0)
            is SecretPickupEvent.Item -> clickSecret(event.entity.blockPosition(), 3.0)
        }
    }

    fun onEtherwarp(packet: ClientboundPlayerPositionPacket) {
        if (!DungeonUtils.inDungeons) return
        val etherPos = lastEtherPos ?: return
        if (System.currentTimeMillis() - lastEtherTime > 1000) return
        if (packet.change.position.distanceTo(Vec3(etherPos)) > 3) return
        val room = DungeonUtils.currentRoom ?: return
        val waypoints = getWaypoints(room)
        waypoints.find { wp ->
            wp.blockPos == room.getRelativeCoords(etherPos) && wp.type == WaypointType.ETHERWARP
        }?.let {
            it.isClicked = true
            lastEtherPos = null
            room.setWaypoints()
            lastEtherTime = 0L
        }
    }

    private fun clickSecret(pos: BlockPos, distance: Double) {
        val room = DungeonUtils.currentRoom ?: return
        val blockPos = room.getRelativeCoords(pos)

        val waypoints = getWaypoints(room)
        if (distance == 0.0) getWaypoints(room).find { wp -> wp.blockPos == blockPos && wp.secret && !wp.isClicked }
        else {
            waypoints.fold(null) { near: DungeonWaypoint?, wp ->
                val waypointDistance = wp.blockPos.distSqr(blockPos)
                if (waypointDistance <= distance && wp.secret && !wp.isClicked && (near == null || waypointDistance < near.blockPos.distSqr(blockPos))) wp
                else near
            }
        }?.let {
            it.isClicked = true
            room.setWaypoints()
            devMessage("clicked ${it.blockPos}")
        }
    }

    fun resetSecrets() {
        checkpoints = 0
        routeTimer = null

        DungeonWaypointConfig.waypoints.values.forEach { roomWaypoints ->
            roomWaypoints.forEach { it.isClicked = false }
        }

        DungeonUtils.currentRoom?.setWaypoints()
    }
}

