package me.odinmod.odin.utils

import me.odinmod.odin.OdinMod.mc
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.random.LocalRandom

fun playSoundAtPlayer(event: SoundEvent) =
    PositionedSoundInstance(event, SoundCategory.MASTER, 1f, 1f, LocalRandom(0L), mc.player?.blockPos ?: BlockPos(0, 0, 0))
        .also { mc.soundManager?.play(it) }

fun setTitle(title: String) {
    mc.inGameHud.setTitleTicks(5, 20, 5)
    mc.inGameHud.setTitle(Text.literal(title))
}

fun alert(title: String) {
    setTitle(title)
    playSoundAtPlayer(SoundEvent.of(SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH.id))
}

fun getPositionString(): String {
    with (mc.player?.blockPos ?: BlockPos(0, 0, 0)) {
        return "x: $x, y: $y, z: $z"
    }
}

private var lastClickSent = 0L

fun windowClick(slotId: Int, button: Int, mode: SlotActionType) {
    // DEV MESSAGE
    if (lastClickSent + 45 > System.currentTimeMillis()) return sendChatMessage("§cIgnoring click on slot §9$slotId.")
    mc.player?.currentScreenHandler?.let {
        if (slotId !in 0 until it.slots.size) return
//        if (mc.currentScreen is TermSimGUI) {
//            PacketEvent.Send(C0EPacketClickWindow(-2, slotId, button, mode, it.inventorySlots[slotId].stack, 0)).postAndCatch()
//            return
//        }
        mc.interactionManager?.clickSlot(it.syncId, slotId, button, mode, mc.player)
        //mc.netHandler?.networkManager?.sendPacket(C0EPacketClickWindow(it.windowId, slotId, button, mode, it.inventory[slotId], it.getNextTransactionID(mc.thePlayer?.inventory)))
    }
}

fun windowClick(slotId: Int, clickType: ClickType) {
    when (clickType) {
        is ClickType.Left -> windowClick(slotId, 0, SlotActionType.PICKUP)
        is ClickType.Right -> windowClick(slotId, 1, SlotActionType.PICKUP)
        is ClickType.Middle -> windowClick(slotId, 2, SlotActionType.CLONE)
        is ClickType.Shift -> windowClick(slotId, 0, SlotActionType.QUICK_MOVE)
    }
}

sealed class ClickType {
    data object Left   : ClickType()
    data object Right  : ClickType()
    data object Middle : ClickType()
    data object Shift  : ClickType()
}