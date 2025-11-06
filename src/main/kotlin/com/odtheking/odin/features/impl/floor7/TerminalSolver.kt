package com.odtheking.odin.features.impl.floor7

import com.odtheking.mixin.accessors.HandledScreenAccessor
import com.odtheking.odin.clickgui.settings.AlwaysActive
import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.*
import com.odtheking.odin.events.ChatPacketEvent
import com.odtheking.odin.events.GuiEvent
import com.odtheking.odin.events.PacketEvent
import com.odtheking.odin.events.TerminalEvent
import com.odtheking.odin.events.core.*
import com.odtheking.odin.features.Module
import com.odtheking.odin.features.impl.floor7.terminalhandler.*
import com.odtheking.odin.features.impl.floor7.termsim.TermSimGUI
import com.odtheking.odin.utils.*
import com.odtheking.odin.utils.Color.Companion.darker
import com.odtheking.odin.utils.handlers.TickTask
import com.odtheking.odin.utils.ui.rendering.NVGSpecialRenderer
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.screen.sync.ItemStackHash
import net.minecraft.util.DyeColor
import org.lwjgl.glfw.GLFW

@AlwaysActive // So it can be used in other modules
object TerminalSolver : Module(
    name = "Terminal Solver",
    description = "Renders solution for terminals in floor 7."
) {
    private val debug by BooleanSetting("Debug Messages", false, desc = "Enables debug messages for the terminal solver.")
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

    init {
        onReceive<OpenScreenS2CPacket> {
            currentTerm?.let { if (!it.isClicked && mc.currentScreen !is TermSimGUI) leftTerm() }
            val windowName = name?.string ?: return@onReceive
            val newTermType = TerminalTypes.entries.find { terminal -> windowName.startsWith(terminal.windowName) }?.takeIf { it != currentTerm?.type } ?: return@onReceive

            currentTerm = when (newTermType) {
                TerminalTypes.PANES -> PanesHandler()

                TerminalTypes.RUBIX -> RubixHandler()

                TerminalTypes.NUMBERS -> NumbersHandler()

                TerminalTypes.STARTS_WITH ->
                    StartsWithHandler(startsWithRegex.find(windowName)?.groupValues?.get(1) ?: return@onReceive modMessage("Failed to find letter, please report this!"))

                TerminalTypes.SELECT ->
                    SelectAllHandler(DyeColor.entries.find { it.name.replace("_", " ").equals(selectAllRegex.find(windowName)?.groupValues?.get(1), true) } ?: return@onReceive modMessage("Failed to find color, please report this!"))

                TerminalTypes.MELODY -> MelodyHandler()
            }

            currentTerm?.let {
                devMessage("§aNew terminal: §6${it.type.name}")
                TerminalEvent.Opened(it).postAndCatch()
                lastTermOpened = it
            }
        }

        onReceive<CloseScreenS2CPacket> {
            leftTerm()
        }

        on<ChatPacketEvent> {
            termSolverRegex.find(value)?.let { message ->
                if (message.groupValues[1] == mc.player?.name?.string) lastTermOpened?.let { TerminalEvent.Solved(it).postAndCatch() }
            }
        }

        onSend<CloseHandledScreenC2SPacket> {
            leftTerm()
        }

        onSend<ClickSlotC2SPacket> {
            lastClickTime = System.currentTimeMillis()
            currentTerm?.isClicked = true
        }

        onSend<CloseHandledScreenC2SPacket> {
            leftTerm()
        }

        TickTask(0, true) {
            if (System.currentTimeMillis() - lastClickTime >= terminalReloadThreshold && currentTerm?.isClicked == true) currentTerm?.let {
                PacketEvent.Send(ClickSlotC2SPacket(mc.player?.currentScreenHandler?.syncId ?: -1, 0, 0, 0, SlotActionType.PICKUP, Int2ObjectMaps.emptyMap(), ItemStackHash.EMPTY)).postAndCatch()
                it.isClicked = false
            }
        }

        on<GuiEvent.MouseClick> (EventPriority.HIGH) {
            if (!enabled || currentTerm == null) return@on

            if (renderType == 1 && !(currentTerm?.type == TerminalTypes.MELODY && cancelMelodySolver)) {
                currentTerm?.type?.getGUI()?.mouseClicked(screen, click.button())
                cancel()
                return@on
            }

            val slotIndex = (screen as HandledScreenAccessor).focusedSlot?.id ?: return@on

            if (blockIncorrectClicks && currentTerm?.canClick(slotIndex, click.button()) == false) {
                cancel()
                return@on
            }

            if (middleClickGUI) {
                currentTerm?.click(slotIndex, if (click.button() == 0) GLFW.GLFW_MOUSE_BUTTON_3 else click.button(), hideClicked && currentTerm?.isClicked == false)
                cancel()
                return@on
            }

            if (hideClicked && currentTerm?.isClicked == false) {
                currentTerm?.simulateClick(slotIndex, click.button())
                currentTerm?.isClicked = true
            }
        }

        on<GuiEvent.DrawBackground> {
            if (!enabled || currentTerm == null || (currentTerm?.type == TerminalTypes.MELODY && cancelMelodySolver) || renderType != 1) return@on

            NVGSpecialRenderer.draw(drawContext, 0, 0, drawContext.scaledWindowWidth, drawContext.scaledWindowHeight) {
                currentTerm?.type?.getGUI()?.render()
            }

            cancel()
        }

        on<GuiEvent.Draw> {
            if (!enabled || currentTerm == null || (currentTerm?.type == TerminalTypes.MELODY && cancelMelodySolver)) return@on
            if (renderType == 1) {
                cancel()
                return@on
            }
            val screen = (screen as? HandledScreen<*>) as? HandledScreenAccessor ?: return@on
            drawContext.fill(screen.x + 7, screen.y + 16, screen.x + screen.width - 7, screen.y + screen.height - 96, backgroundColor.rgba)
        }

        on<GuiEvent.DrawSlot> {
            val term = currentTerm ?: return@on
            if (!enabled || renderType == 1 || currentTerm?.type == null || (term.type == TerminalTypes.MELODY && cancelMelodySolver)) return@on

            val slotIndex = slot.id
            val inventorySize = (screen as? HandledScreen<*>)?.screenHandler?.slots?.size ?: return@on

            if (!debug) cancel()
            if (slotIndex !in term.solution || slotIndex > inventorySize - 37) return@on

            when (term.type) {
                TerminalTypes.PANES -> drawContext.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, panesColor.rgba)

                TerminalTypes.STARTS_WITH, TerminalTypes.SELECT ->
                    drawContext.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, startsWithColor.rgba)

                TerminalTypes.NUMBERS -> {
                    val index = term.solution.indexOf(slot.index)
                    if (index < 3) {
                        val color = when (index) {
                            0 -> orderColor
                            1 -> orderColor2
                            else -> orderColor3
                        }.rgba
                        drawContext.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, color)
                        cancel()
                    }
                    val amount = slot.stack?.count?.toString() ?: ""
                    if (showNumbers) drawContext.drawCenteredTextWithShadow(screen.textRenderer, amount, slot.x + 8, slot.y + 4, Colors.WHITE.rgba)
                }

                TerminalTypes.RUBIX -> {
                    val needed = term.solution.count { it == slotIndex }
                    val text = if (needed < 3) needed else (needed - 5)
                    if (text != 0) {
                        val color = when (text) {
                            2 -> rubixColor2
                            1 -> rubixColor1
                            -2 -> oppositeRubixColor2
                            else -> oppositeRubixColor1
                        }

                        drawContext.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, color.rgba)
                        drawContext.drawCenteredTextWithShadow(screen.textRenderer, text.toString(), slot.x + 8, slot.y + 4, Colors.WHITE.rgba)
                    }
                }

                TerminalTypes.MELODY -> {
                    drawContext.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, when {
                        slotIndex / 9 == 0 || slotIndex / 9 == 5 -> melodyColumColor
                        (slotIndex % 9).equalsOneOf(1, 2, 3, 4, 5) -> melodyPointerColor
                        else -> melodyPointerColor
                    }.rgba)
                }
            }
        }

        on<GuiEvent.DrawTooltip> {
            if (enabled && cancelToolTip && currentTerm != null) cancel()
        }
    }

    private fun leftTerm() {
        currentTerm?.let {
            EventBus.unsubscribe(it)
            devMessage("§cLeft terminal: §6${it.type.name}")
            TerminalEvent.Closed(it).postAndCatch()
            currentTerm?.type?.getGUI()?.closeGui()
            currentTerm = null
        }
    }
}