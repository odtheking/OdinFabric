package com.odtheking.odin.features.impl.render

import com.google.gson.annotations.SerializedName
import com.odtheking.odin.OdinMod
import com.odtheking.odin.clickgui.ClickGUI
import com.odtheking.odin.clickgui.HudManager
import com.odtheking.odin.clickgui.settings.AlwaysActive
import com.odtheking.odin.clickgui.settings.impl.*
import com.odtheking.odin.events.WorldLoadEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Category
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.alert
import com.odtheking.odin.utils.getChatBreak
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.network.WebUtils.fetchJson
import com.odtheking.odin.utils.skyblock.LocationUtils
import kotlinx.coroutines.launch
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import org.lwjgl.glfw.GLFW
import java.net.URI

@AlwaysActive
object ClickGUIModule : Module(
    name = "Click GUI",
    description = "Allows you to customize the UI.",
    key = GLFW.GLFW_KEY_RIGHT_SHIFT
) {
    val enableNotification by BooleanSetting("Chat notifications", true, desc = "Sends a message when you toggle a module with a keybind")
    val clickGUIColor by ColorSetting("Color", Color(50, 150, 220), desc = "The color of the Click GUI.")

    val hypixelApiUrl by StringSetting("Api Server", "https://api.odtheking.com/hypixel/", 128, "The Hypixel API server to connect to.")
    val webSocketUrl by StringSetting("WebSocket Server", "https://api.odtheking.com/ws/", 128, "The Websocket server to connect to.")

    private val action by ActionSetting("Open HUD Editor", desc = "Opens the HUD editor when clicked.") { mc.setScreen(HudManager) }
    val devMessage by BooleanSetting("Developer Message", false, desc = "Sends development related messages to the chat.")

    override fun onKeybind() {
        toggle()
    }

    override fun onEnable() {
        mc.setScreen(ClickGUI)
        super.onEnable()
        toggle()
    }

    val panelSetting by MapSetting("Panel Settings", mutableMapOf<Category, PanelData>())
    data class PanelData(var x: Float = 10f, var y: Float = 10f, var extended: Boolean = true)

    fun resetPositions() {
        Category.entries.forEach {
            panelSetting[it] = PanelData(10f + 260f * it.ordinal, 10f, true)
        }
    }

    private const val RELEASE_LINK = "https://github.com/odtheking/OdinFabric/releases/latest"
    private var latestVersionNumber: String? = null
    private var hasSentUpdateMessage = false

    init {
        resetPositions()

        OdinMod.scope.launch {
            latestVersionNumber = checkNewerVersion(OdinMod.version.toString())
        }

        on<WorldLoadEvent> {
           if (!LocationUtils.isOnHypixel || hasSentUpdateMessage || latestVersionNumber == null) return@on
            hasSentUpdateMessage = true

            modMessage("""
            ${getChatBreak()}
                
            §3Update available: §f$latestVersionNumber
            """.trimIndent(), "")

            modMessage(Component.literal("§b$RELEASE_LINK").withStyle {
                it.withClickEvent(ClickEvent.OpenUrl(URI(RELEASE_LINK)))
                    .withHoverEvent(HoverEvent.ShowText(Component.literal(RELEASE_LINK)))
            }, "")

            modMessage("""
            
            ${getChatBreak()}§r
            
            """.trimIndent(), "")
            alert("Odin Update Available")

        }
    }

    private suspend fun checkNewerVersion(currentVersion: String): String? {
        val newest = fetchJson<Release>("https://api.github.com/repos/odtheking/OdinFabric/releases/latest").getOrElse { return null }

        return if (isSecondNewer(currentVersion, newest.tagName)) newest.tagName else null
    }

    private fun isSecondNewer(currentVersion: String, previousVersion: String?): Boolean {
        if (currentVersion.isEmpty() || previousVersion.isNullOrEmpty()) return false

        val (major, minor, patch) = currentVersion.split(".").mapNotNull { it.toIntOrNull() }
        val (major2, minor2, patch2) = previousVersion.split(".").mapNotNull { it.toIntOrNull() }

        return when {
            major > major2 -> false
            major < major2 -> true
            minor > minor2 -> false
            minor < minor2 -> true
            patch > patch2 -> false
            patch < patch2 -> true
            else -> false // equal, or something went wrong, either way it's best to assume it's false.
        }
    }

    private data class Release(
        @SerializedName("tag_name")
        val tagName: String
    )
}