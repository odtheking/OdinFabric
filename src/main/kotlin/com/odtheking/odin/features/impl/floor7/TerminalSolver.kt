package com.odtheking.odin.features.impl.floor7

import com.odtheking.mixin.accessors.HandledScreenAccessor
import com.odtheking.odin.OdinMod.EVENT_BUS
import com.odtheking.odin.clickgui.settings.AlwaysActive
import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.*
import com.odtheking.odin.events.GuiEvent
import com.odtheking.odin.events.PacketEvent
import com.odtheking.odin.events.TerminalEvent
import com.odtheking.odin.features.Module
import com.odtheking.odin.features.impl.floor7.terminalhandler.*
import com.odtheking.odin.features.impl.floor7.termsim.TermSimGUI
import com.odtheking.odin.utils.*
import com.odtheking.odin.utils.Color.Companion.darker
import com.odtheking.odin.utils.ui.rendering.NVGRenderer
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps
import meteordevelopment.orbit.EventHandler
import meteordevelopment.orbit.EventPriority
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket
import net.minecraft.network.packet.s2c.common.CommonPingS2CPacket
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.screen.sync.ItemStackHash
import org.lwjgl.glfw.GLFW

@AlwaysActive // So it can be used in other modules
object TerminalSolver : Module(
    name = "Terminal Solver",
    description = "Renders solution for terminals in floor 7."
) {
    val renderType by SelectorSetting("Mode", "Normal", arrayListOf("Normal", "Custom GUI"), desc = "How the terminal solver should render.")
    private val cancelToolTip by BooleanSetting("Stop Tooltips", true, desc = "Stops rendering tooltips in terminals.").withDependency { renderType != 1 }
    val hideClicked by BooleanSetting("Hide Clicked", false, desc = "Visually hides your first click before a gui updates instantly to improve perceived response time. Does not affect actual click time.")
    private val middleClickGUI by BooleanSetting("Middle Click GUI", true, desc = "Replaces right click with middle click in terminals.").withDependency { renderType != 1 }
    private val blockIncorrectClicks by BooleanSetting("Block Incorrect Clicks", true, desc = "Blocks incorrect clicks in terminals.").withDependency { renderType != 1 }
    private val cancelMelodySolver by BooleanSetting("Stop Melody Solver", false, desc = "Stops rendering the melody solver.")
    val showNumbers by BooleanSetting("Show Numbers", true, desc = "Shows numbers in the order terminal.")
    private val terminalReloadThreshold by NumberSetting("Reload Threshold", 600, 300, 1000, 10, unit = "ms", desc = "The amount of time in seconds before the terminal reloads.")
    val customTermSize by NumberSetting("Custom Term Size", 1f, 0.5f, 3f, 0.1f, desc = "The size of the custom terminal GUI.").withDependency { renderType == 1 }
    val customAnimations by BooleanSetting("Custom Animations", true, desc = "Enables animations for the custom terminal gui.").withDependency { renderType == 1 }
    val roundness by NumberSetting("Roundness", 9f, 0f, 15f, 1f, desc = "The roundness of the custom terminal gui.").withDependency { renderType == 1 }
    val gap by NumberSetting("Gap", 5f, 0f, 15f, 1f, desc = "The gap between the slots in the custom terminal gui.").withDependency { renderType == 1 }

    private val showColors by DropdownSetting("Color Settings")
    val backgroundColor by ColorSetting("Background", Colors.gray26, true, desc = "Background color of the terminal solver.").withDependency { showColors }

    val panesColor by ColorSetting("Panes", Colors.MINECRAFT_GREEN, true, desc = "Color of the panes terminal solver.").withDependency { showColors }

    val rubixColor1 by ColorSetting("Rubix 1", Colors.MINECRAFT_DARK_AQUA, true, desc = "Color of the rubix terminal solver for 1 click.").withDependency { showColors }
    val rubixColor2 by ColorSetting("Rubix 2", Color(0, 100, 100), true, desc = "Color of the rubix terminal solver for 2 click.").withDependency { showColors }
    val oppositeRubixColor1 by ColorSetting("Rubix -1", Color(170, 85, 0), true, desc = "Color of the rubix terminal solver for -1 click.").withDependency { showColors }
    val oppositeRubixColor2 by ColorSetting("Rubix -2", Color(210, 85, 0), true, desc = "Color of the rubix terminal solver for -2 click.").withDependency { showColors }

    val orderColor by ColorSetting("Order 1", Colors.MINECRAFT_GREEN, true, desc = "Color of the order terminal solver for 1st item.").withDependency { showColors }
    val orderColor2 by ColorSetting("Order 2", Colors.MINECRAFT_GREEN.darker(), true, desc = "Color of the order terminal solver for 2nd item.").withDependency { showColors }
    val orderColor3 by ColorSetting("Order 3", Colors.MINECRAFT_GREEN.darker().darker(), true, desc = "Color of the order terminal solver for 3rd item.").withDependency { showColors }

    val startsWithColor by ColorSetting("Starts With", Colors.MINECRAFT_DARK_AQUA, true, desc = "Color of the starts with terminal solver.").withDependency { showColors }

    val selectColor by ColorSetting("Select", Colors.MINECRAFT_DARK_AQUA, true, desc = "Color of the select terminal solver.").withDependency { showColors }

    val melodyColumColor by ColorSetting("Melody Column", Colors.MINECRAFT_DARK_PURPLE, true, desc = "Color of the colum indicator for melody.").withDependency { showColors && !cancelMelodySolver }
    val melodyRowColor by ColorSetting("Melody Row", Colors.MINECRAFT_RED, true, desc = "Color of the row indicator for melody.").withDependency { showColors && !cancelMelodySolver }
    val melodyPointerColor by ColorSetting("Melody Pointer", Colors.MINECRAFT_GREEN, true, desc = "Color of the location for pressing for melody.").withDependency { showColors && !cancelMelodySolver }

    var currentTerm: TerminalHandler? = null
        private set
    var lastTermOpened: TerminalHandler? = null
        private set
    private val termSolverRegex = Regex("^(.{1,16}) activated a terminal! \\((\\d)/(\\d)\\)$")
    private val startsWithRegex = Regex("What starts with: '(\\w+)'?")
    private val selectAllRegex = Regex("Select all the (.+) items!")
    private var lastClickTime = 0L

    @EventHandler
    fun onPacketReceive(event: PacketEvent.Receive) = with (event.packet) {
        when (this) {
            is OpenScreenS2CPacket -> {
                currentTerm?.let { if (!it.isClicked && mc.currentScreen !is TermSimGUI) leftTerm() }
                val windowName = name?.string ?: return
                val newTermType = TerminalTypes.entries.find { terminal -> windowName.startsWith(terminal.windowName) }?.takeIf { it != currentTerm?.type } ?: return

                currentTerm = when (newTermType) {
                    TerminalTypes.PANES -> PanesHandler()

                    TerminalTypes.RUBIX -> RubixHandler()

                    TerminalTypes.NUMBERS -> NumbersHandler()

                    TerminalTypes.STARTS_WITH ->
                        StartsWithHandler(startsWithRegex.find(windowName)?.groupValues?.get(1) ?: return modMessage("Failed to find letter, please report this!"))

                    TerminalTypes.SELECT ->
                        SelectAllHandler(selectAllRegex.find(windowName)?.groupValues?.get(1)?.replace("light blue", "aqua", true)?.replace("light gray", "silver", true)?.replace("_", " ") ?: return modMessage("Failed to find color, please report this!"))

                    TerminalTypes.MELODY -> MelodyHandler()
                }

                currentTerm?.let {
                    devMessage("§aNew terminal: §6${it.type.name}")
                    TerminalEvent.Opened(it).postAndCatch()
                    lastTermOpened = it
                }
            }

            is CloseScreenS2CPacket -> leftTerm()

            is GameMessageS2CPacket -> if (!overlay) {
                termSolverRegex.find(content?.string ?: return@with)?.let { message ->
                    if (message.groupValues[1] == mc.player?.name?.string) lastTermOpened?.let { TerminalEvent.Solved(it).postAndCatch() }
                }
            }
        }
    }

    @EventHandler
    fun onPacketSend(event: PacketEvent.Send) = with(event.packet) {
        when (this) {
            is CloseHandledScreenC2SPacket -> leftTerm()

            is ClickSlotC2SPacket -> {
                lastClickTime = System.currentTimeMillis()
                currentTerm?.isClicked = true
            }

            is CommonPingS2CPacket -> {
                if (System.currentTimeMillis() - lastClickTime >= terminalReloadThreshold && currentTerm?.isClicked == true) currentTerm?.let {
                    PacketEvent.Receive(ClickSlotC2SPacket(mc.player?.currentScreenHandler?.syncId ?: -1, 0, 0, 0, SlotActionType.PICKUP, Int2ObjectMaps.emptyMap(), ItemStackHash.EMPTY)).postAndCatch()
                    it.isClicked = false
                }
            }

            else -> return
        }
        if (event.packet is CloseHandledScreenC2SPacket) leftTerm()
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onGuiClick(event: GuiEvent.MouseClick) = with(currentTerm) {
        if (!enabled || this == null) return

        if (renderType == 1 && !(type == TerminalTypes.MELODY && cancelMelodySolver)) {
            currentTerm?.type?.getGUI()?.mouseClicked(event.screen, event.click.button())
            event.cancel()
            return
        }

        val slotIndex = (event.screen as HandledScreenAccessor).focusedSlot?.id ?: return

        if (blockIncorrectClicks && !canClick(slotIndex, event.click.button())) {
            event.cancel()
            return
        }

        if (middleClickGUI) {
            click(slotIndex, if (event.click.button() == 0) GLFW.GLFW_MOUSE_BUTTON_3 else event.click.button(), hideClicked && !isClicked)
            event.cancel()
            return
        }

        if (hideClicked && !isClicked) {
            simulateClick(slotIndex, event.click.button())
            isClicked = true
        }
    }

    @EventHandler
    fun onDrawBackground(event: GuiEvent.DrawBackground) {
        if (!enabled || currentTerm == null || (currentTerm?.type == TerminalTypes.MELODY && cancelMelodySolver) || renderType != 1) return

        NVGRenderer.beginFrame(mc.window.width.toFloat(), mc.window.height.toFloat())
        currentTerm?.type?.getGUI()?.render()
        NVGRenderer.endFrame()
        event.cancel()
    }

    @EventHandler
    fun onDrawGui(event: GuiEvent.Draw) {
        if (!enabled || currentTerm == null || (currentTerm?.type == TerminalTypes.MELODY && cancelMelodySolver)) return
        if (renderType == 1) {
            event.cancel()
            return
        }
        val screen = (event.screen as? HandledScreen<*>) as? HandledScreenAccessor ?: return
        event.drawContext.fill(screen.x + 7, screen.y + 16, screen.x + screen.width - 7, screen.y + screen.height - 96, backgroundColor.rgba)
    }

    @EventHandler
    fun drawSlot(event: GuiEvent.DrawSlot) = with(currentTerm) {
        if (!enabled || renderType == 1 || this?.type == null || (type == TerminalTypes.MELODY && cancelMelodySolver)) return

        val slotIndex = event.slot.id
        val inventorySize = (event.screen as? HandledScreen<*>)?.screenHandler?.slots?.size ?: return

        event.cancel()
        if (slotIndex !in solution || slotIndex > inventorySize - 37) return

        when (type) {
            TerminalTypes.PANES -> event.drawContext.fill(event.slot.x, event.slot.y, event.slot.x + 16, event.slot.y + 16, panesColor.rgba)

            TerminalTypes.STARTS_WITH, TerminalTypes.SELECT ->
                event.drawContext.fill(event.slot.x, event.slot.y, event.slot.x + 16, event.slot.y + 16, startsWithColor.rgba)

            TerminalTypes.NUMBERS -> {
                val index = solution.indexOf(event.slot.index)
                if (index < 3) {
                    val color = when (index) {
                        0 -> orderColor
                        1 -> orderColor2
                        else -> orderColor3
                    }.rgba
                    event.drawContext.fill(event.slot.x, event.slot.y, event.slot.x + 16, event.slot.y + 16, color)
                    event.cancel()
                }
                val amount = event.slot.stack?.count?.toString() ?: ""
                if (showNumbers) event.drawContext.drawText(event.screen.textRenderer, amount, event.slot.x + 8 - event.screen.textRenderer.getWidth(amount) / 2, event.slot.y + 4, Colors.WHITE.rgba, false)
            }

            TerminalTypes.RUBIX -> {
                val needed = solution.count { it == slotIndex }
                val text = if (needed < 3) needed else (needed - 5)
                if (text != 0) {
                    val color = when (text) {
                        2 -> rubixColor2
                        1 -> rubixColor1
                        -2 -> oppositeRubixColor2
                        else -> oppositeRubixColor1
                    }

                    event.drawContext.fill(event.slot.x, event.slot.y, event.slot.x + 16, event.slot.y + 16, color.rgba)
                    event.drawContext.drawText(event.screen.textRenderer, text.toString(), event.slot.x + 8 - event.screen.textRenderer.getWidth(text.toString()) / 2, event.slot.y + 4, Colors.WHITE.rgba, false)
                }
            }

            TerminalTypes.MELODY -> {
                event.drawContext.fill(event.slot.x, event.slot.y, event.slot.x + 16, event.slot.y + 16, when {
                    slotIndex / 9 == 0 || slotIndex / 9 == 5 -> melodyColumColor
                    (slotIndex % 9).equalsOneOf(1, 2, 3, 4, 5) -> melodyPointerColor
                    else -> melodyPointerColor
                }.rgba)
            }
        }
    }

    @EventHandler
    fun onTooltipDraw(event: GuiEvent.DrawTooltip) {
        if (enabled && cancelToolTip && currentTerm != null) event.cancel()
    }

    private fun leftTerm() {
        currentTerm?.let {
            EVENT_BUS.unsubscribe(it)
            devMessage("§cLeft terminal: §6${it.type.name}")
            TerminalEvent.Closed(it).postAndCatch()
            currentTerm?.type?.getGUI()?.closeGui()
            currentTerm = null
        }
    }
}