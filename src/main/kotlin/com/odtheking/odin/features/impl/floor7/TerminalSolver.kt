package com.odtheking.odin.features.impl.floor7

import com.odtheking.mixin.accessors.AbstractContainerScreenAccessor
import com.odtheking.odin.clickgui.settings.AlwaysActive
import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.*
import com.odtheking.odin.events.ChatPacketEvent
import com.odtheking.odin.events.GuiEvent
import com.odtheking.odin.events.TerminalEvent
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.core.*
import com.odtheking.odin.features.Module
import com.odtheking.odin.features.impl.floor7.terminalhandler.*
import com.odtheking.odin.utils.*
import com.odtheking.odin.utils.Color.Companion.darker
import com.odtheking.odin.utils.ui.rendering.NVGSpecialRenderer
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.*
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import org.lwjgl.glfw.GLFW

@AlwaysActive // So it can be used in other modules
object TerminalSolver : Module(
    name = "Terminal Solver",
    description = "Renders solution for terminals in floor 7."
) {
    val renderType by SelectorSetting("Mode", "Normal", arrayListOf("Normal", "Custom GUI"), desc = "How the terminal solver should render.")
    val customTermSize by NumberSetting("Term Size", 1f, 1f, 3f, 0.1f, desc = "The size of the custom terminal GUI.").withDependency { renderType == 1 }
    private val normalTermSize by NumberSetting("Normal Term Size", 3, 1, 5, 1, desc = "The GUI scale increase for normal terminal GUI.").withDependency { renderType != 1 }
    val roundness by NumberSetting("Roundness", 9f, 0f, 15f, 1f, desc = "The roundness of the custom terminal gui.").withDependency { renderType == 1 }
    val gap by NumberSetting("Gap", 5f, 0f, 15f, 1f, desc = "The gap between the slots in the custom terminal gui.").withDependency { renderType == 1 }

    private val solverSettings by DropdownSetting("Solver Functionality")
    private val cancelToolTip by BooleanSetting("Stop Tooltips", true, desc = "Stops rendering tooltips in terminals.").withDependency { renderType == 0 && solverSettings }
    private val middleClickGUI by BooleanSetting("Middle Click GUI", true, desc = "Replaces right click with middle click in terminals.").withDependency { renderType == 0 && solverSettings }
    private val blockIncorrectClicks by BooleanSetting("Block Incorrect Clicks", true, desc = "Blocks incorrect clicks in terminals.").withDependency { renderType == 0 && solverSettings }
    private val cancelMelodySolver by BooleanSetting("Stop Melody Solver", false, desc = "Stops rendering the melody solver.").withDependency { solverSettings }
    val showNumbers by BooleanSetting("Show Numbers", true, desc = "Shows numbers in the order terminal.").withDependency { solverSettings }
    val firstClickProt by NumberSetting("First Click Protection", 350, 350, 700, 10, unit = "ms", desc = "The amount of time after opening a terminal where clicks are blocked to prevent bans (recommended value is 500 minus your ping).").withDependency { blockIncorrectClicks && solverSettings }
    val hideClicked by BooleanSetting("Hide Clicked", false, desc = "Visually hides your first click before a gui updates instantly to improve perceived response time. Does not affect actual click time.").withDependency { solverSettings }
    private val terminalReloadThreshold by NumberSetting("Solution resolve timeout", 600, 300, 1000, 10, unit = "ms", desc = "The amount of time before the terminal reloads after a click wasn't registered while using hide clicked.").withDependency { hideClicked && solverSettings }
    private val debug by BooleanSetting("Debug", false, desc = "Shows debug terminals.").withDependency { solverSettings }

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
    val melodyPointerColor by ColorSetting("Melody Pointer", Colors.MINECRAFT_GREEN, true, desc = "Color of the location for pressing for melody.").withDependency { showColors && !cancelMelodySolver }
    val melodyBackgroundColor by ColorSetting("Melody Background", Colors.gray38, true, desc = "Color of the background slot in melody.").withDependency { showColors && !cancelMelodySolver }

    var currentTerm: TerminalHandler? = null
        private set
    var lastTermOpened: TerminalHandler? = null
        private set
    private val termSolverRegex = Regex("^(.{1,16}) activated a terminal! \\((\\d)/(\\d)\\)$")
    private val startsWithRegex = Regex("What starts with: '(\\w+)'?")
    private val selectAllRegex = Regex("Select all the (.+) items!")
    private var lastClickTime = 0L
    @JvmStatic val termSize get() = if (enabled && renderType == 0 && currentTerm != null) normalTermSize else 1

    init {
        onReceive<ClientboundOpenScreenPacket> (EventPriority.HIGHEST) {
            currentTerm?.let { if ((!it.isClicked && it.windowCount <= 2) || System.currentTimeMillis() - it.timeOpened < firstClickProt) leftTerm() }
            val windowName = title.string ?: return@onReceive
            val newTermType = TerminalTypes.entries.find { terminal -> windowName.startsWith(terminal.windowName) }?.takeIf { it != currentTerm?.type } ?: return@onReceive

            currentTerm = when (newTermType) {
                TerminalTypes.PANES -> PanesHandler()

                TerminalTypes.RUBIX -> RubixHandler()

                TerminalTypes.NUMBERS -> NumbersHandler()

                TerminalTypes.STARTS_WITH ->
                    StartsWithHandler(startsWithRegex.find(windowName)?.groupValues?.get(1) ?: return@onReceive modMessage("Failed to find letter, please report this!"))

                TerminalTypes.SELECT ->
                    SelectAllHandler(DyeColor.entries.find { it.name.replace("_", " ").equals(selectAllRegex.find(windowName)?.groupValues?.get(1)?.replace("SILVER", "LIGHT GRAY"), true) } ?: return@onReceive modMessage("Failed to find color, please report this!"))

                TerminalTypes.MELODY -> MelodyHandler()
            }

            currentTerm?.let {
                devMessage("§aNew terminal: §6${it.type.name}")
                TerminalEvent.Opened(it).postAndCatch()
                if (enabled && renderType == 0) mc.execute { mc.resizeDisplay() }
                lastTermOpened = it
            }
        }

        onReceive<ClientboundContainerClosePacket> {
            leftTerm()
        }

        onSend<ServerboundContainerClosePacket> {
            leftTerm()
        }

        on<ChatPacketEvent> {
            termSolverRegex.find(value)?.let { message ->
                if (message.groupValues[1] == mc.player?.name?.string) lastTermOpened?.let { TerminalEvent.Solved(it).postAndCatch() }
            }
        }

        onSend<ServerboundContainerClickPacket> {
            lastClickTime = System.currentTimeMillis()
            currentTerm?.isClicked = true
        }

        on<TickEvent.End> {
            if (System.currentTimeMillis() - lastClickTime >= terminalReloadThreshold && currentTerm?.isClicked == true) currentTerm?.let {
                val menu = (mc.screen as? AbstractContainerScreen<*>)?.menu ?: return@let
                GuiEvent.SlotUpdate(mc.screen ?: return@on, ClientboundContainerSetSlotPacket(menu.containerId, 0, it.type.windowSize - 1, ItemStack.EMPTY), menu).postAndCatch()
                it.isClicked = false
            }
        }

        on<GuiEvent.KeyPress> {
            if (!enabled || currentTerm == null) return@on

            if (renderType == 1 && !(currentTerm?.type == TerminalTypes.MELODY && cancelMelodySolver)) {
                if (mc.options.keyDrop.matches(input)) {
                    currentTerm?.type?.getGUI()?.mouseClicked(screen, if (input.hasControlDown()) GLFW.GLFW_MOUSE_BUTTON_2 else GLFW.GLFW_MOUSE_BUTTON_3)

                    cancel()
                    return@on
                }
            }
        }

        on<GuiEvent.MouseClick> (EventPriority.HIGH) {
            if (!enabled || currentTerm == null) return@on

            if (renderType == 1 && !(currentTerm?.type == TerminalTypes.MELODY && cancelMelodySolver)) {
                currentTerm?.type?.getGUI()?.mouseClicked(screen, if (click.button() == 0) GLFW.GLFW_MOUSE_BUTTON_3 else click.button())
                cancel()
                return@on
            }
        }

        on<GuiEvent.SlotClick> (EventPriority.HIGH) {
            val term = currentTerm ?: return@on
            term.isClicked = true
            if (!enabled || (term.type == TerminalTypes.MELODY && cancelMelodySolver)) return@on

            if (
                System.currentTimeMillis() - term.timeOpened < firstClickProt ||
                (blockIncorrectClicks && !term.canClick(slotId, button))
            ) return@on cancel()

            if (middleClickGUI) {
                term.click(slotId, if (button == 0) GLFW.GLFW_MOUSE_BUTTON_3 else button, hideClicked && !term.isClicked)
                cancel()
                return@on
            }

            if (hideClicked && !term.isClicked) term.simulateClick(slotId, button)
        }

        on<GuiEvent.DrawBackground> {
            if (!enabled || currentTerm == null || (currentTerm?.type == TerminalTypes.MELODY && cancelMelodySolver) || renderType != 1) return@on

            NVGSpecialRenderer.draw(guiGraphics, 0, 0, guiGraphics.guiWidth(), guiGraphics.guiHeight()) {
                currentTerm?.type?.getGUI()?.render()
            }

            cancel()
        }

        on<GuiEvent.Draw> {
            if (debug) currentTerm?.let { term ->
                val menu = (mc.screen as? AbstractContainerScreen<*>)?.menu ?: return@let
                val debugInfo = listOf(
                    "§6Terminal Debug Info:",
                    "§7Type: §f${term.type.name}",
                    "§7Window Name: §f${mc.screen?.title?.string}",
                    "§7Container ID: §f${menu.containerId}",
                    "§7Time Open: §f${System.currentTimeMillis() - term.timeOpened}ms",
                    "§7Is Clicked: §f${term.isClicked}",
                    "§7Window Count: §f${term.windowCount}",
                    "§7Solution: §f${term.solution.joinToString(", ")}",
                    "§7Items: §f${menu.items?.subList(0, term.type.windowSize)?.filter { !it.isEmpty && it.item != Items.BLACK_STAINED_GLASS_PANE }?.joinToString(" §8| §f") { it.itemName?.string ?: "Unknown" } ?: "N/A"}"
                )

                debugInfo.forEachIndexed { index, line ->
                    guiGraphics.drawWordWrap(mc.font, Component.literal(line), 5, 20 + (index * 10), 300, Colors.WHITE.rgba)
                }

                menu.items?.forEachIndexed { index, stack ->
                    guiGraphics.renderItem(stack, 5 + (index % 9) * 18, 250 + (index / 9) * 18)
                    guiGraphics.renderItemDecorations(mc.font, stack, 5 + (index % 9) * 18, 250 + (index / 9) * 18)
                }
            }
            if (!enabled || currentTerm == null || (currentTerm?.type == TerminalTypes.MELODY && cancelMelodySolver)) return@on
            if (renderType == 1) {
                cancel()
                return@on
            }
            val screen = (screen as? AbstractContainerScreen<*>) as? AbstractContainerScreenAccessor ?: return@on
            guiGraphics.fill(screen.x + 7, screen.y + 16, screen.x + screen.width - 7, screen.y + screen.height - 96, backgroundColor.rgba)
        }

        on<GuiEvent.DrawSlot> {
            val term = currentTerm ?: return@on
            if (!enabled || (term.type == TerminalTypes.MELODY && cancelMelodySolver)) return@on

            val slotIndex = slot.index
            val inventorySize = (screen as? AbstractContainerScreen<*>)?.menu?.slots?.size ?: return@on

            if (slotIndex <= inventorySize - 37) cancel()
            if (slotIndex !in term.solution) return@on

            when (term.type) {
                TerminalTypes.PANES -> guiGraphics.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, panesColor.rgba)

                TerminalTypes.STARTS_WITH, TerminalTypes.SELECT ->
                    guiGraphics.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, startsWithColor.rgba)

                TerminalTypes.NUMBERS -> {
                    val index = term.solution.indexOf(slotIndex)
                    if (index < 3) {
                        val color = when (index) {
                            0 -> orderColor
                            1 -> orderColor2
                            else -> orderColor3
                        }.rgba
                        guiGraphics.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, color)
                        cancel()
                    }
                    val amount = slot.item?.count?.toString() ?: ""
                    if (showNumbers) guiGraphics.drawCenteredString(screen.font, amount, slot.x + 8, slot.y + 4, Colors.WHITE.rgba)
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

                        guiGraphics.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, color.rgba)
                        guiGraphics.drawCenteredString(screen.font, text.toString(), slot.x + 8, slot.y + 4, Colors.WHITE.rgba)
                    }
                }

                TerminalTypes.MELODY -> {
                    guiGraphics.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, when {
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
            devMessage("§cLeft terminal: §6${it.type.name}")
            TerminalEvent.Closed(it).postAndCatch()
            EventBus.unsubscribe(it)
            currentTerm = null
            if (enabled && renderType == 0) mc.execute { mc.resizeDisplay() }
        }
    }
}