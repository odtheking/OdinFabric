package com.odtheking.odin.features.impl.floor7.termsim

import com.odtheking.odin.OdinMod.EVENT_BUS
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.PacketEvent
import com.odtheking.odin.events.TerminalEvent
import com.odtheking.odin.features.impl.floor7.TerminalSounds
import com.odtheking.odin.utils.handlers.LimitedTickTask
import meteordevelopment.orbit.EventHandler
import meteordevelopment.orbit.EventPriority
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.component.DataComponentTypes
import net.minecraft.entity.player.PlayerEquipment
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket
import net.minecraft.screen.GenericContainerScreenHandler
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.screen.slot.Slot
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text

open class TermSimGUI(
    val name: String,
    val size: Int,
    private val inv: SimpleInventory = SimpleInventory(size)
) : GenericContainerScreen(
    GenericContainerScreenHandler(
        if (size <= 9) ScreenHandlerType.GENERIC_9X1
        else if (size <= 18) ScreenHandlerType.GENERIC_9X2
        else if (size <= 27) ScreenHandlerType.GENERIC_9X3
        else if (size <= 36) ScreenHandlerType.GENERIC_9X4
        else if (size <= 45) ScreenHandlerType.GENERIC_9X5
        else ScreenHandlerType.GENERIC_9X6,
        0, PlayerInventory(mc.player, PlayerEquipment(mc.player)), inv, size / 9
    ),
    PlayerInventory(mc.player, PlayerEquipment(mc.player)),
    Text.literal(name)
) {
    val blackPane = ItemStack(Items.BLACK_STAINED_GLASS_PANE).apply { set(DataComponentTypes.CUSTOM_NAME, Text.literal("")) }
    val guiInventorySlots get() = handler?.slots?.subList(0, size) ?: emptyList()
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

    @EventHandler
    fun onTerminalSolved(event: TerminalEvent.Solved) {
        if (mc.currentScreen !== this) return
        PacketEvent.Receive(CloseScreenS2CPacket(-2)).postAndCatch()
        StartGUI.open(ping)
    }

    open fun slotClick(slot: Slot, button: Int) {}

    override fun close() {
        EVENT_BUS.unsubscribe(this)
        doesAcceptClick = true
        super.close()
    }

    override fun init() {
        super.init()
        EVENT_BUS.subscribe(this)
    }

    override fun removed() {
        EVENT_BUS.unsubscribe(this)
        super.removed()
    }

    @EventHandler(priority = EventPriority.LOW)
    fun onPacketSend(event: PacketEvent.Send) {
        val packet = event.packet as? ClickSlotC2SPacket ?: return
        if (mc.currentScreen !== this) return
        delaySlotClick(guiInventorySlots.getOrNull(packet.slot.toInt()) ?: return, packet.button.toInt())
        event.cancel()
    }

//    @EventHandler(priority = EventPriority.LOWEST)
//    fun onPacketReceive(event: PacketEvent.Receive) {
//        val packet = event.packet as? ScreenHandlerSlotUpdateS2CPacket ?: return
//        if (OdinMain.mc.currentScreen !== this || packet.func_149175_c() == -2 || event.packet.func_149173_d() !in 0 until size) return
//        packet.func_149174_e()?.let { mc.thePlayer?.inventoryContainer?.putStackInSlot(packet.func_149173_d(), it) }
//        event.isCanceled = true
//     }

    private fun delaySlotClick(slot: Slot, button: Int) {
        if (mc.currentScreen == StartGUI) return slotClick(slot, button)
        if (!doesAcceptClick || slot.inventory != inv || slot.stack?.item == Items.BLACK_STAINED_GLASS_PANE) return
        doesAcceptClick = false
        LimitedTickTask((ping / 50).toInt().coerceAtLeast(1), 1) {
            if (mc.currentScreen != this) return@LimitedTickTask
            doesAcceptClick = true
            slotClick(slot, button)
        }
    }

    override fun onMouseClick(slot: Slot?, slotId: Int, button: Int, actionType: SlotActionType?) {
        slot?.let { delaySlotClick(it, slotId) }
    }

    protected fun createNewGui(block: (Slot) -> ItemStack) {
        PacketEvent.Receive(OpenScreenS2CPacket(0, ScreenHandlerType.GENERIC_9X3, Text.literal(name))).postAndCatch()
        guiInventorySlots.forEach { it.setSlot(block(it)) }
    }

    protected fun Slot.setSlot(stack: ItemStack) {
        PacketEvent.Receive(ScreenHandlerSlotUpdateS2CPacket(-2, 0, id, stack)).postAndCatch()
        setStack(stack)
    }

    protected fun playTermSimSound() {
        if (!TerminalSounds.enabled || !TerminalSounds.clickSounds) mc.player?.playSound(
            SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
            1f, 1f
        )
    }
}