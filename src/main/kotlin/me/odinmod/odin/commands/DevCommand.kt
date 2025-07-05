package me.odinmod.odin.commands

import com.github.stivais.commodore.Commodore
import com.github.stivais.commodore.utils.GreedyString
import kotlinx.coroutines.launch
import me.odinmod.odin.OdinMod
import me.odinmod.odin.OdinMod.mc
import me.odinmod.odin.events.PacketEvent
import me.odinmod.odin.features.ModuleManager.generateFeatureList
import me.odinmod.odin.features.impl.foraging.TreeHud
import me.odinmod.odin.features.impl.render.PlayerSize
import me.odinmod.odin.utils.getCustomData
import me.odinmod.odin.utils.modMessage
import me.odinmod.odin.utils.sendCommand
import me.odinmod.odin.utils.setClipboardContent
import me.odinmod.odin.utils.skyblock.LocationUtils
import me.odinmod.odin.utils.skyblock.SkyblockPlayer
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket
import net.minecraft.text.Text

val devCommand = Commodore("oddev") {

    literal("treehudtest").runs {
        TreeHud.currentTreeCommandTest()
    }

    literal("getitem").runs {
        modMessage("Item in hand: ${mc.player?.mainHandStack?.getCustomData()}")
    }

    literal("giveaotv").runs { tuners: Int? ->
        sendCommand("give @p minecraft:diamond_shovel[minecraft:custom_name={\"text\":\"Aspect Of The Void\",\"color\":\"dark_purple\"},minecraft:custom_data={ethermerge:1,\"tuned_transmission\":${tuners ?: 0}}]")
    }

    literal("debug").runs {
        modMessage("Hypixel: ${LocationUtils.isOnHypixel}, Skyblock: ${LocationUtils.isInSkyblock}, Area: ${LocationUtils.currentArea.displayName}")
        modMessage("SkyblockPlayer: ${SkyblockPlayer.currentHealth}/${SkyblockPlayer.maxHealth}❤, ${SkyblockPlayer.currentMana}/${SkyblockPlayer.maxMana}✎, ${SkyblockPlayer.overflowMana}ʬ, ${SkyblockPlayer.currentDefense}❈ Defense, EHP: ${SkyblockPlayer.effectiveHP}")
    }

    literal("simulate").runs { greedyString: GreedyString ->
        PacketEvent.Receive(GameMessageS2CPacket(Text.literal(greedyString.string), false)).postAndCatch()
        modMessage("§8Simulated message: ${greedyString.string}")
    }

    literal("updatedevs").runs {
        OdinMod.scope.launch {
            PlayerSize.updateCustomProperties()
            modMessage("Updated devs.")
        }
    }

    literal("generatefeaturelist").runs {
        setClipboardContent(generateFeatureList())
        modMessage("Generated feature list and copied to clipboard.")
    }
}