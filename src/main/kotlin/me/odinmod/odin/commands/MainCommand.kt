package me.odinmod.odin.commands

import com.github.stivais.commodore.Commodore
import me.odinmod.odin.utils.modMessage

val mainCommand = Commodore("od") {
    runs {
        modMessage("Hello world!")
    }
}