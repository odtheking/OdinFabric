package com.odtheking.odin.events

import com.odtheking.odin.events.core.Event
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.game.ClientboundSoundPacket
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.level.block.state.BlockState

open class SecretPickupEvent : Event() {
    class Interact(val blockPos: BlockPos, val blockState: BlockState) : SecretPickupEvent()
    class Item(val entity: ItemEntity) : SecretPickupEvent()
    class Bat(val packet: ClientboundSoundPacket) : SecretPickupEvent()
}