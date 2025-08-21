package com.odtheking.odin.features.impl.floor7.termGUI

import com.odtheking.odin.features.impl.floor7.TerminalSolver
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.equalsOneOf

object PanesGui : TermGui() {

    override fun renderTerminal(slotCount: Int) {
        renderBackground(slotCount, 5)

        for (index in 9..<slotCount) {
            if ((index % 9).equalsOneOf(0, 1, 7, 8)) continue
            val inSolution = index in currentSolution

            val startColor = if (inSolution) TerminalSolver.panesColor else Colors.TRANSPARENT
            val endColor = if (inSolution) Colors.gray38 else TerminalSolver.panesColor
            renderSlot(index, startColor, endColor)
        }
    }
}