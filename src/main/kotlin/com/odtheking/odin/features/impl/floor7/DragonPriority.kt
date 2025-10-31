package com.odtheking.odin.features.impl.floor7

import com.odtheking.odin.features.impl.floor7.WitherDragons.dragonPriorityToggle
import com.odtheking.odin.features.impl.floor7.WitherDragons.easyPower
import com.odtheking.odin.features.impl.floor7.WitherDragons.normalPower
import com.odtheking.odin.features.impl.floor7.WitherDragons.paulBuff
import com.odtheking.odin.features.impl.floor7.WitherDragons.soloDebuff
import com.odtheking.odin.features.impl.floor7.WitherDragons.soloDebuffOnAll
import com.odtheking.odin.utils.alert
import com.odtheking.odin.utils.devMessage
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.skyblock.dungeon.Blessing
import com.odtheking.odin.utils.skyblock.dungeon.DungeonClass
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils

object DragonPriority {

    private val defaultOrder = listOf(WitherDragonsEnum.Red, WitherDragonsEnum.Orange, WitherDragonsEnum.Blue, WitherDragonsEnum.Purple, WitherDragonsEnum.Green)

    fun findPriority(spawningDragons: MutableList<WitherDragonsEnum>): WitherDragonsEnum =
        if (!dragonPriorityToggle) spawningDragons.minBy { defaultOrder.indexOf(it) }
        else sortPriority(spawningDragons)

    fun displaySpawningDragon(dragon: WitherDragonsEnum) {
        if (dragon == WitherDragonsEnum.None) return
        if (WitherDragons.dragonTitle && WitherDragons.enabled) alert("§${dragon.colorCode}${dragon.name} is spawning!", true)
        if (dragonPriorityToggle && WitherDragons.enabled) modMessage("§${dragon.colorCode}${dragon.name} §7is your priority dragon!")
    }

    private fun sortPriority(spawningDragons: MutableList<WitherDragonsEnum>): WitherDragonsEnum {
        val totalPower = Blessing.POWER.current * (if (paulBuff) 1.25 else 1.0) + (if (Blessing.TIME.current > 0) 2.5 else 0.0)
        val playerClass = DungeonUtils.currentDungeonPlayer.clazz.apply { if (this == DungeonClass.Unknown) modMessage("§cFailed to get dungeon class.") }

        val dragonList = listOf(WitherDragonsEnum.Orange, WitherDragonsEnum.Green, WitherDragonsEnum.Red, WitherDragonsEnum.Blue, WitherDragonsEnum.Purple)

        val priorityList = if (totalPower >= normalPower || (spawningDragons.any { it == WitherDragonsEnum.Purple } && totalPower >= easyPower)) {
            if (playerClass == DungeonClass.Berserk || playerClass == DungeonClass.Mage) dragonList
            else dragonList.reversed()
        } else defaultOrder

        spawningDragons.sortBy { priorityList.indexOf(it) }

        if (totalPower >= easyPower) {
            if (soloDebuff == 0 && playerClass == DungeonClass.Tank && (spawningDragons.any { it == WitherDragonsEnum.Purple } || soloDebuffOnAll))
                spawningDragons.sortByDescending { priorityList.indexOf(it) }
            else if (playerClass == DungeonClass.Healer && (spawningDragons.any { it == WitherDragonsEnum.Purple } || soloDebuffOnAll))
                spawningDragons.sortByDescending { priorityList.indexOf(it) }
        }

        devMessage("§7Priority: §6$totalPower §7Class: §${playerClass.colorCode}${playerClass.name} §7Dragons: §a${
            spawningDragons.joinToString(", ") { it.name }
        } §7-> §c${priorityList.joinToString(", ") { it.name.first().toString() }}")

        return spawningDragons[0]
    }
}

