package me.odinmod.odin.commands

import com.github.stivais.commodore.Commodore
import com.github.stivais.commodore.utils.GreedyString
import me.odinmod.odin.OdinMod.mc
import me.odinmod.odin.features.foraging.TreeHud
import me.odinmod.odin.utils.getCustomData
import me.odinmod.odin.utils.modMessage
import me.odinmod.odin.utils.sendCommand
import me.odinmod.odin.utils.setClipboardContent
import me.odinmod.odin.utils.skyblock.LocationUtils
import me.odinmod.odin.utils.skyblock.SkyblockPlayer

val mainCommand = Commodore("odin") {
    runs {
        modMessage("Hello world!")
    }

    literal("copy").runs { greedyString: GreedyString ->
        setClipboardContent(greedyString.string)
    }

    literal("treehudtest").runs {
        TreeHud.currentTreeCommandTest()
    }

    literal("getitem").runs {
        modMessage("Item in hand: ${mc.player?.mainHandStack?.getCustomData()}")
    }

    literal("giveaotv").runs { tuners: String? ->
        sendCommand("give @p minecraft:diamond_shovel[minecraft:custom_data={\"ethermerge\":1${if (tuners != null) ",\"tuned_transmission\":$tuners" else ""}}]")
    }

    literal("debug").runs {
        modMessage("Hypixel: ${LocationUtils.isOnHypixel}, Skyblock: ${LocationUtils.isInSkyblock}, Area: ${LocationUtils.currentArea.displayName}")
        modMessage("SkyblockPlayer: ${SkyblockPlayer.currentHealth}/${SkyblockPlayer.maxHealth}❤, ${SkyblockPlayer.currentMana}/${SkyblockPlayer.maxMana}✎, ${SkyblockPlayer.overflowMana}ʬ, ${SkyblockPlayer.currentDefense}❈ Defense, EHP: ${SkyblockPlayer.effectiveHP}")
    }
}