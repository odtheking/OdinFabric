package com.odtheking.odin.utils

import com.odtheking.odin.clickgui.settings.impl.MapSetting
import com.odtheking.odin.config.Config

class PersonalBest(private val mapSetting: MapSetting<Int, Float, MutableMap<Int, Float>>) {
    
    /**
     * Updates the personal best for a specific puzzle
     * 
     * @param index The name of the puzzle
     * @param time The new time achieved
     * @param unit The unit of measurement for display
     * @param message The message prefix to display
     * @param sendOnlyPB Whether to only send message on new PB
     */
    fun time(index: Int, time: Float, unit: String = "s§7!", message: String, sendOnlyPB: Boolean = false, alwaysSendPB: Boolean = false, sendMessage: Boolean = true) {
        var msg = "$message$time$unit"
        val oldPB = mapSetting.value[index] ?: 9999f

        if (oldPB > time) {
            mapSetting.value[index] = time
            Config.save()
            msg += " §7(§d§lNew PB§r§7) Old PB was §8$oldPB"
            if (sendMessage) modMessage(msg)
        } else if (!sendOnlyPB && sendMessage) modMessage("$msg ${if (alwaysSendPB) "(§8$oldPB§7)" else ""}")
    }
}