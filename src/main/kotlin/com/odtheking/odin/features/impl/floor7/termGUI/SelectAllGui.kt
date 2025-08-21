package com.odtheking.odin.features.impl.floor7.termGUI

import com.odtheking.odin.features.impl.floor7.TerminalSolver
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.equalsOneOf

object SelectAllGui : TermGui() {

    override fun renderTerminal(slotCount: Int) {
        renderBackground(slotCount, 7)

        for (index in 9..slotCount) {
            if ((index % 9).equalsOneOf(0, 8)) continue
            val inSolution = index in currentSolution
            val startColor = if (inSolution) TerminalSolver.selectColor else Colors.TRANSPARENT
            val endColor = if (inSolution) TerminalSolver.selectColor else TerminalSolver.selectColor
            if (colorAnimations[index] != null || inSolution) renderSlot(index, startColor, endColor)
        }
    }
}