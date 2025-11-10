package com.odtheking.odin.features.impl.floor7.termsim

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.PacketEvent
import com.odtheking.odin.events.TerminalEvent
import com.odtheking.odin.events.core.EventBus
import com.odtheking.odin.events.core.EventPriority
import com.odtheking.odin.events.core.on
import com.odtheking.odin.events.core.onSend
import com.odtheking.odin.features.impl.floor7.TerminalSounds
import com.odtheking.odin.utils.handlers.LimitedTickTask
import net.minecraft.client.gui.screens.inventory.ContainerScreen
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.SimpleContainer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.PlayerEquipment
import net.minecraft.world.inventory.ChestMenu
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

open class TermSimGUI(
    val name: String,
    val size: Int,
    private val inv: SimpleContainer = SimpleContainer(size)
) : ContainerScreen(
    ChestMenu(
        if (size <= 9) MenuType.GENERIC_9x1
        else if (size <= 18) MenuType.GENERIC_9x2
        else if (size <= 27) MenuType.GENERIC_9x3
        else if (size <= 36) MenuType.GENERIC_9x4
        else if (size <= 45) MenuType.GENERIC_9x5
        else MenuType.GENERIC_9x6,
        0, Inventory(mc.player, PlayerEquipment(mc.player)), inv, size / 9
    ),
    Inventory(mc.player, PlayerEquipment(mc.player)),
    Component.literal(name)
) {
    val blackPane = ItemStack(Items.BLACK_STAINED_GLASS_PANE).apply { set(DataComponents.CUSTOM_NAME, Component.literal("")) }
    val guiInventorySlots get() = menu?.slots?.subList(0, size) ?: emptyList()
    private var doesAcceptClick = true
    protected var ping = 0L

    open fun create() {
        guiInventorySlots.forEach { it.setSlot(blackPane) }
    }

    fun open(terminalPing: Long = 0L) {
        LimitedTickTask(1, 1) {
            mc.setScreen(this)
            create()
            ping = terminalPing
        }
    }

    open fun slotClick(slot: Slot, button: Int) {}

    override fun onClose() {
        EventBus.unsubscribe(this)
        doesAcceptClick = true
        super.onClose()
    }

    override fun init() {
        super.init()
        EventBus.subscribe(this)
    }

    override fun removed() {
        EventBus.unsubscribe(this)
        super.removed()
    }

    init {
        on<TerminalEvent.Solved> {
            if (mc.screen !== this@TermSimGUI) return@on
            PacketEvent.Receive(ClientboundContainerClosePacket(-2)).postAndCatch()
            StartGUI.open(ping)
        }

        onSend<ServerboundContainerClickPacket>  (EventPriority.LOW) {
            if (mc.screen !== this@TermSimGUI) return@onSend
            delaySlotClick(guiInventorySlots.getOrNull(slotNum.toInt()) ?: return@onSend, buttonNum.toInt())
            it.cancel()
        }
    }

//    @EventHandler(priority = EventPriority.LOWEST)
//    fun onPacketReceive(event: PacketEvent.Receive) {
//        val packet = event.packet as? ClientboundContainerSetSlotPacket ?: return
//        if (OdinMain.mc.screen !== this || packet.func_149175_c() == -2 || event.packet.func_149173_d() !in 0 until size) return
//        packet.func_149174_e()?.let { mc.thePlayer?.inventoryContainer?.putStackInSlot(packet.func_149173_d(), it) }
//        event.isCanceled = true
//     }

    private fun delaySlotClick(slot: Slot, button: Int) {
        if (mc.screen == StartGUI) return slotClick(slot, button)
        if (!doesAcceptClick || slot.container != inv || slot.item?.item == Items.BLACK_STAINED_GLASS_PANE) return
        doesAcceptClick = false
        LimitedTickTask((ping / 50).toInt().coerceAtLeast(1), 1) {
            if (mc.screen != this) return@LimitedTickTask
            doesAcceptClick = true
            slotClick(slot, button)
        }
    }


    override fun slotClicked(slot: Slot, slotId: Int, button: Int, actionType: ClickType) {
        delaySlotClick(slot, slotId)
    }

    protected fun createNewGui(block: (Slot) -> ItemStack) {
        PacketEvent.Receive(ClientboundOpenScreenPacket(0, MenuType.GENERIC_9x3, Component.literal(name))).postAndCatch()
        guiInventorySlots.forEach { it.setSlot(block(it)) }
    }

    protected fun Slot.setSlot(stack: ItemStack) {
        PacketEvent.Receive(ClientboundContainerSetSlotPacket(-2, 0, index, stack)).postAndCatch()
        setSlot(stack)
    }

    protected fun playTermSimSound() {
        if (!TerminalSounds.enabled || !TerminalSounds.clickSounds) mc.player?.playSound(
            SoundEvents.EXPERIENCE_ORB_PICKUP,
            1f, 1f
        )
    }
}