package me.odinmod.odin.config

import com.teamresourceful.resourcefulconfig.api.types.info.ResourcefulConfigLink
import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue
import com.teamresourceful.resourcefulconfigkt.api.ConfigKt
import me.odinmod.odin.OdinMod
import me.odinmod.odin.config.categories.RenderConfig
import me.odinmod.odin.config.categories.SkyblockConfig

object Config: ConfigKt("odin/config") {
    override val name = TranslatableValue("Odin ${OdinMod.version}")
    override val description = TranslatableValue("by Odtheking")
    override val links: Array<ResourcefulConfigLink> = arrayOf(
        ResourcefulConfigLink.create(
            "https://discord.gg/8Fqsg5xBP3",
            "discord",
            TranslatableValue("Join the Odin Mod Discord server for support and updates.")

        ),
        ResourcefulConfigLink.create(
            "https://github.com/odtheking/OdinFabric",
            "github",
            TranslatableValue("View the source code on GitHub."),
        )
    )
    override val hidden: Boolean = true

    init {
        category(SkyblockConfig)
        category(RenderConfig)
    }
}