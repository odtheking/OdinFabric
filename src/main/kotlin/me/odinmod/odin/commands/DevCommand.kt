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
import me.odinmod.odin.utils.customData
import me.odinmod.odin.utils.modMessage
import me.odinmod.odin.utils.sendCommand
import me.odinmod.odin.utils.setClipboardContent
import me.odinmod.odin.utils.skyblock.KuudraUtils
import me.odinmod.odin.utils.skyblock.LocationUtils
import me.odinmod.odin.utils.skyblock.SkyblockPlayer
import me.odinmod.odin.utils.skyblock.Supply
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket
import net.minecraft.text.Text

val devCommand = Commodore("oddev") {

    literal("treehudtest").runs {
        TreeHud.currentTreeCommandTest()
    }

    literal("getitem").runs {
        modMessage("Item in hand: ${mc.player?.mainHandStack?.customData}")
    }

    literal("giveaotv").runs { tuners: Int? ->
        sendCommand("give @p minecraft:diamond_shovel[minecraft:custom_name={\"text\":\"Aspect Of The Void\",\"color\":\"dark_purple\"},minecraft:custom_data={ethermerge:1,\"tuned_transmission\":${tuners ?: 0}}]")
    }

    literal("debug").runs {
        modMessage("Hypixel: ${LocationUtils.isOnHypixel}, Skyblock: ${LocationUtils.isInSkyblock}, Area: ${LocationUtils.currentArea.displayName}")
        modMessage("SkyblockPlayer: ${SkyblockPlayer.currentHealth}/${SkyblockPlayer.maxHealth}❤, ${SkyblockPlayer.currentMana}/${SkyblockPlayer.maxMana}✎, ${SkyblockPlayer.overflowMana}ʬ, ${SkyblockPlayer.currentDefense}❈ Defense, EHP: ${SkyblockPlayer.effectiveHP}")
    }

    literal("simulate").runs { greedyString: GreedyString ->
        PacketEvent.Receive(GameMessageS2CPacket(Text.of(greedyString.string), false)).postAndCatch()
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

    literal("kuudra").runs {
        modMessage("""  
            |inKuudra: ${KuudraUtils.inKuudra}, tier: ${KuudraUtils.kuudraTier}, phase: ${KuudraUtils.phase}    
            |kuudraTeammates: ${KuudraUtils.freshers.map { it.key }}
            |giantZombies: ${KuudraUtils.giantZombies.joinToString { it.pos.toString() }}
            |supplies: ${Supply.entries.joinToString { "${it.name} -> ${it.isActive}" }}
            |kuudraEntity: ${KuudraUtils.kuudraEntity}
            |builders: ${KuudraUtils.playersBuildingAmount}
            |build: ${KuudraUtils.buildDonePercentage}
            |buildingPiles: ${KuudraUtils.buildingPiles.joinToString { it.pos.toString() }}
        """.trimIndent())
    }
}