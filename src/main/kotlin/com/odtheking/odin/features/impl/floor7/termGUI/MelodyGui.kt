package com.odtheking.odin.features.impl.floor7.termGUI

import com.odtheking.odin.features.impl.floor7.TerminalSolver

object MelodyGui : TermGui() {

    override fun render() {
        setCurrentGui(this)
        itemIndexMap.clear()

        renderTerminal(TerminalSolver.currentTerm?.type?.windowSize?.minus(9) ?: 0)
    }

    override fun renderTerminal(slotCount: Int) {
        renderBackground(slotCount, 7, 3)

        (0..45).forEach { index ->
            val colum = index % 9
            val row = index / 9

            val color =
                when {
                    row == 0 -> if (index in currentSolution) TerminalSolver.melodyColumColor else return@forEach
                    colum == 7 && row in 1..4 -> if (index in currentSolution) TerminalSolver.melodyPointerColor else TerminalSolver.melodyBackgroundColor
                    colum in 1..5 -> {
                        if (index in currentSolution) TerminalSolver.melodyPointerColor
                        else TerminalSolver.melodyBackgroundColor
                    }
                    else -> return@forEach
                }

            renderSlot(index, color)
        }
    }
}