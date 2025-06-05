package me.odinmod.odin.commands

import com.github.stivais.commodore.Commodore
import com.github.stivais.commodore.utils.GreedyString
import me.odinmod.odin.utils.modMessage
import me.odinmod.odin.utils.setClipboardContent

val mainCommand = Commodore("odin") {
    runs {
        modMessage("Hello world!")
    }

    literal("copy").runs { greedyString: GreedyString ->
        setClipboardContent(greedyString.string)
    }
}