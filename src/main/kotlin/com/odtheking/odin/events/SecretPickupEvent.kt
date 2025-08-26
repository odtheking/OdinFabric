package com.odtheking.odin.events

import com.odtheking.odin.events.core.Event
import net.minecraft.block.BlockState
import net.minecraft.entity.ItemEntity
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket
import net.minecraft.util.math.BlockPos

open class SecretPickupEvent : Event() {
    class Interact(val blockPos: BlockPos, val blockState: BlockState) : SecretPickupEvent()
    class Item(val entity: ItemEntity) : SecretPickupEvent()
    class Bat(val packet: PlaySoundS2CPacket) : SecretPickupEvent()
}