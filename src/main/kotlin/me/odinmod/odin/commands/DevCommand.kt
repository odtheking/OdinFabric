package me.odinmod.odin.commands

import com.github.stivais.commodore.Commodore
import com.github.stivais.commodore.utils.GreedyString
import kotlinx.coroutines.launch
import me.odinmod.odin.OdinMod
import me.odinmod.odin.OdinMod.mc
import me.odinmod.odin.events.PacketEvent
import me.odinmod.odin.features.ModuleManager.generateFeatureList
import me.odinmod.odin.features.impl.foraging.TreeHud
import me.odinmod.odin.features.impl.nether.NoPre
import me.odinmod.odin.features.impl.render.PlayerSize
import me.odinmod.odin.utils.customData
import me.odinmod.odin.utils.modMessage
import me.odinmod.odin.utils.sendCommand
import me.odinmod.odin.utils.setClipboardContent
import me.odinmod.odin.utils.skyblock.KuudraUtils
import me.odinmod.odin.utils.skyblock.LocationUtils
import me.odinmod.odin.utils.skyblock.SkyblockPlayer
import me.odinmod.odin.utils.skyblock.Supply
import me.odinmod.odin.utils.skyblock.dungeon.Blessing
import me.odinmod.odin.utils.skyblock.dungeon.DungeonUtils
import me.odinmod.odin.utils.skyblock.dungeon.ScanUtils
import me.odinmod.odin.utils.skyblock.dungeon.ScanUtils.getRoomCenter
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

    literal("debug").runs { string: String ->
        modMessage("""
            |Version: ${OdinMod.version}
            |Hypixel: ${LocationUtils.isOnHypixel}
            ${
            when (string) {
                "kuudra" -> """
                        |inKuudra: ${KuudraUtils.inKuudra}, tier: ${KuudraUtils.kuudraTier}, phase: ${KuudraUtils.phase}
                        |kuudraTeammates: ${KuudraUtils.freshers.map { it.key }}
                        |giantZombies: ${KuudraUtils.giantZombies.joinToString { it.pos.toString() }}
                        |supplies: ${Supply.entries.joinToString { "${it.name} -> ${it.isActive}" }}
                        |kuudraEntity: ${KuudraUtils.kuudraEntity}
                        |builders: ${KuudraUtils.playersBuildingAmount}
                        |build: ${KuudraUtils.buildDonePercentage}
                        |buildingPiles: ${KuudraUtils.buildingPiles.joinToString { it.pos.toString() }}
                        |missing: ${NoPre.missing}
                    """.trimIndent()
                "dungeon" -> """
                        |inDungeons: ${DungeonUtils.inDungeons}
                        |InBoss: ${DungeonUtils.inBoss}
                        |Floor: ${DungeonUtils.floor?.name}
                        |Score: ${DungeonUtils.score}
                        |Secrets: (${DungeonUtils.secretCount} - ${DungeonUtils.neededSecretsAmount} - ${DungeonUtils.totalSecrets} - ${DungeonUtils.knownSecrets}) 
                        |mimicKilled: ${DungeonUtils.mimicKilled}
                        |Deaths: ${DungeonUtils.deathCount}, Crypts: ${DungeonUtils.cryptCount}
                        |BonusScore: ${DungeonUtils.getBonusScore}, isPaul: ${DungeonUtils.isPaul}
                        |OpenRooms: ${DungeonUtils.openRoomCount}, CompletedRooms: ${DungeonUtils.completedRoomCount} ${DungeonUtils.percentCleared}%, Blood Done: ${DungeonUtils.bloodDone}, Total: ${DungeonUtils.totalRooms}
                        |Puzzles (${DungeonUtils.puzzleCount}): ${DungeonUtils.puzzles.joinToString { "${it.name} (${it.status.toString()})" }}
                        |DungeonTime: ${DungeonUtils.dungeonTime}
                        |currentDungeonPlayer: ${DungeonUtils.currentDungeonPlayer.name}, ${DungeonUtils.currentDungeonPlayer.clazz}, ${DungeonUtils.currentDungeonPlayer.isDead}
                        |doorOpener: ${DungeonUtils.doorOpener}
                        |currentRoom: ${DungeonUtils.currentRoom?.data?.name}, roomsPassed: ${DungeonUtils.passedRooms.map { it.data.name }}
                        |Teammates: ${DungeonUtils.dungeonTeammates.joinToString { "§${it.clazz.colorCode}${it.name} (${it.clazz} [${it.clazzLvl}])§r" }}
                        |TeammatesNoSelf: ${DungeonUtils.dungeonTeammatesNoSelf.map { it.name }}
                        |LeapTeammates: ${DungeonUtils.leapTeammates.map { it.name }}
                        |Blessings: ${Blessing.entries.joinToString { "${it.name}: ${it.current}" }}
                    """.trimIndent()
                else -> """
                        |Current Area: ${LocationUtils.currentArea.displayName}
                    """.trimIndent()
            }
        }
        """.trimIndent(), "")
    }

    literal("roomdata").runs {
        val room = DungeonUtils.currentRoom
        val player = mc.player ?: return@runs
        val roomCenter = getRoomCenter(player.x.toInt(), player.z.toInt())
        val core = ScanUtils.getCore(roomCenter)
        modMessage(
            """
            Middle: ${roomCenter.x}, ${roomCenter.z}
            Room: ${DungeonUtils.currentRoomName}
            Core: $core
            Rotation: ${room?.rotation ?: "NONE"}
            Positions: ${room?.roomComponents?.joinToString { "(${it.x}, ${it.z})" } ?: "None"}
            """.trimIndent(), "")
        setClipboardContent(core.toString())
        modMessage("§aCopied $core to clipboard!")
    }
}