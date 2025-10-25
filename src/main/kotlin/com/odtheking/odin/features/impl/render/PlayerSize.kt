package com.odtheking.odin.features.impl.render

import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import com.odtheking.odin.OdinMod
import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.*
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.*
import com.odtheking.odin.utils.network.WebUtils.fetchJson
import com.odtheking.odin.utils.network.WebUtils.postData
import kotlinx.coroutines.launch
import net.minecraft.client.render.entity.state.PlayerEntityRenderState
import net.minecraft.client.util.math.MatrixStack

object PlayerSize : Module(
    name = "Player Size",
    description = "Changes the size of the player."
) {
    private val devSize by BooleanSetting(
        "Dev Size",
        true,
        desc = "Toggles client side dev size for your own player."
    ).withDependency { isRandom }
    private val devSizeX by NumberSetting("Size X", 1f, -1, 3f, 0.1, desc = "X scale of the dev size.")
    private val devSizeY by NumberSetting("Size Y", 1f, -1, 3f, 0.1, desc = "Y scale of the dev size.")
    private val devSizeZ by NumberSetting("Size Z", 1f, -1, 3f, 0.1, desc = "Z scale of the dev size.")
    private val devWings by BooleanSetting("Wings", false, desc = "Toggles dragon wings.").withDependency { isRandom }
    private val devWingsColor by ColorSetting(
        "Wings Color",
        Colors.WHITE,
        desc = "Color of the dev wings."
    ).withDependency { devWings && isRandom }
    private var showHidden by DropdownSetting("Show Hidden", false).withDependency { isRandom }
    private val passcode by StringSetting(
        "Passcode",
        "odin",
        desc = "Passcode for dev features."
    ).withDependency { showHidden && isRandom }

    const val DEV_SERVER = "https://api.odtheking.com/devs/"

    private val sendDevData by ActionSetting("Send Dev Data", desc = "Sends dev data to the server.") {
        showHidden = false
        OdinMod.scope.launch {
            val body = buildDevBody(
                mc.session.username ?: return@launch,
                devWingsColor, devSizeX, devSizeY,
                devSizeZ, devWings, " ", passcode
            )

            modMessage(postData(DEV_SERVER, body).getOrNull())
            updateCustomProperties()
        }
    }.withDependency { isRandom }


    private var randoms: HashMap<String, RandomPlayer> = HashMap()
    val isRandom get() = randoms.containsKey(mc.session?.username)

    data class RandomPlayer(
        @SerializedName("CustomName")   val customName: String?,
        @SerializedName("DevName")      val name: String,
        @SerializedName("IsDev")        val isDev: Boolean?,
        @SerializedName("WingsColor")   val wingsColor: Array<Int>,
        @SerializedName("Size")         val scale: Array<Float>,
        @SerializedName("Wings")        val wings: Boolean
    )

    @JvmStatic
    fun preRenderCallbackScaleHook(entityRenderer: PlayerEntityRenderState, matrix: MatrixStack) {
        if (enabled && entityRenderer.name == mc.player?.name?.string && !randoms.containsKey(entityRenderer.name)) {
            if (devSizeY < 0) matrix.translate(0f, devSizeY * 2, 0f)
            matrix.scale(devSizeX, devSizeY, devSizeZ)
        }
        if (!randoms.containsKey(entityRenderer.name)) return
        if (!devSize && entityRenderer.name == mc.player?.name?.string) return
        val random = randoms[entityRenderer.name] ?: return
        if (random.scale[1] < 0) matrix.translate(0f, random.scale[1] * 2, 1f)
        matrix.scale(random.scale[0], random.scale[1], random.scale[2])
    }

    private val pattern = Regex("Decimal\\('(-?\\d+(?:\\.\\d+)?)'\\)")

    suspend fun updateCustomProperties() {
        val response = fetchJson<Array<RandomPlayer>>("https://api.odtheking.com/devs/").getOrNull() ?: return
        randoms.putAll(response.associateBy { it.name })
    }

    init {
        OdinMod.scope.launch {
            updateCustomProperties()
        }
    }

    fun buildDevBody(devName: String, wingsColor: Color, sizeX: Float, sizeY: Float, sizeZ: Float, wings: Boolean, customName: String, password: String): String {
        return """ {
            "devName": "$devName",
            "wingsColor": [${wingsColor.red}, ${wingsColor.green}, ${wingsColor.blue}],
            "size": [$sizeX, $sizeY, $sizeZ],
            "wings": $wings,
            "customName": "$customName",
            "password": "$password"
        } """.trimIndent()
    }
}