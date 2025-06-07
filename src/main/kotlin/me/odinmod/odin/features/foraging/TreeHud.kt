package me.odinmod.odin.features.foraging

import me.odinmod.odin.utils.handlers.MobCache
import me.odinmod.odin.utils.modMessage
import net.minecraft.entity.decoration.ArmorStandEntity

object TreeHud {
    val foragingStatus = MobCache {
        it is ArmorStandEntity && it.displayName?.string?.matches(Regex("^\\b\\w+\\b TREE \\d+%$")) == true
    }

    fun currentTreeCommandTest() {
        val entity = foragingStatus.getClosestEntity()

        modMessage(entity?.displayName?.string)
    }
}