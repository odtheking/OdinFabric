package me.odinmod.odin.config

import com.teamresourceful.resourcefulconfig.client.ConfigScreen
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import com.teamresourceful.resourcefulconfigkt.api.Entry
import com.teamresourceful.resourcefulconfigkt.api.builders.TypeBuilder
import me.odinmod.odin.OdinMod.mc

object ConfigUtils {

    fun <T, B : TypeBuilder> CategoryKt.autoRefresh(entry: Entry<T, B>) =
        observable(entry) { _, _ ->
            (mc.currentScreen as? ConfigScreen)?.updateOptions()
        }
}