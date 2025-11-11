package com.odtheking.odin.features.impl.render

import com.google.gson.JsonParser
import com.mojang.blaze3d.vertex.PoseStack
import com.odtheking.odin.OdinMod
import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.*
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.*
import kotlinx.coroutines.launch
import net.minecraft.client.renderer.entity.state.AvatarRenderState

object PlayerSize : Module(
    name = "Player Size",
    description = "Changes the size of the player."
) {
    private val devSize by BooleanSetting("Dev Size", true, desc = "Toggles client side dev size for your own player.").withDependency { isRandom }
    private val devSizeX by NumberSetting("Size X", 1f, -1, 3f, 0.1, desc = "X scale of the dev size.")
    private val devSizeY by NumberSetting("Size Y", 1f, -1, 3f, 0.1, desc = "Y scale of the dev size.")
    private val devSizeZ by NumberSetting("Size Z", 1f, -1, 3f, 0.1, desc = "Z scale of the dev size.")
    private val devWings by BooleanSetting("Wings", false, desc = "Toggles dragon wings.").withDependency { isRandom }
    private val devWingsColor by ColorSetting("Wings Color", Colors.WHITE, desc = "Color of the dev wings.").withDependency { devWings && isRandom }
    private var showHidden by DropdownSetting("Show Hidden", false).withDependency { isRandom }
    private val passcode by StringSetting("Passcode", "odin", desc = "Passcode for dev features.").withDependency { showHidden && isRandom }

    private val sendDevData by ActionSetting("Send Dev Data", desc = "Sends dev data to the server.") {
        showHidden = false
        fun valid(v: Float) = (v in 0.8f..1.6f) || (v in -1.0f..-0.8f)
        if (!valid(devSizeX) || !valid(devSizeY) || !valid(devSizeZ)) {
            modMessage("Global values must be between 0.8..1.6 or -1..-0.8")
            return@ActionSetting
        }
        OdinMod.scope.launch {
            modMessage(
                sendDataToServer(
                    body = "${mc.player?.name?.tryCollapseToString()}, [${devWingsColor.red},${devWingsColor.green},${devWingsColor.blue}], [$devSizeX,$devSizeY,$devSizeZ], $devWings, , $passcode",
                    "https://tj4yzotqjuanubvfcrfo7h5qlq0opcyk.lambda-url.eu-north-1.on.aws/"
                )
            )
            updateCustomProperties()
        }
    }.withDependency { isRandom }


    var randoms: HashMap<String, RandomPlayer> = HashMap()
    val isRandom get() = randoms.containsKey(mc.user.name)

    data class RandomPlayer(
        val scale: Triple<Float, Float, Float>,
        val wings: Boolean = false,
        val wingsColor: Color = Colors.WHITE,
        val customName: String,
        val isDev: Boolean
    )

    @JvmStatic
    fun preRenderCallbackScaleHook(entityRenderer: AvatarRenderState, matrix: PoseStack) {
        if (enabled && entityRenderer.nameTag == mc.player?.name && !randoms.containsKey(entityRenderer.nameTag?.string)) {
            if (devSizeY < 0) matrix.translate(0f, devSizeY * 2, 0f)
            matrix.scale(devSizeX, devSizeY, devSizeZ)
        }
        if (!randoms.containsKey(entityRenderer.nameTag?.string)) return
        if (!devSize && entityRenderer.nameTag == mc.player?.name) return
        val random = randoms[entityRenderer.nameTag?.string] ?: return
        if (random.scale.second < 0) matrix.translate(0f, random.scale.second * 2, 1f)
        matrix.scale(random.scale.first, random.scale.second, random.scale.third)
    }

    private val pattern = Regex("Decimal\\('(-?\\d+(?:\\.\\d+)?)'\\)")

    fun updateCustomProperties() {
        val data =
            fetchData("https://tj4yzotqjuanubvfcrfo7h5qlq0opcyk.lambda-url.eu-north-1.on.aws/").replace(pattern) { match -> match.groupValues[1] }
                .ifEmpty { null } ?: return
        JsonParser.parseString(data)?.asJsonArray?.forEach {
            val jsonElement = it.asJsonObject
            val randomsName = jsonElement.get("DevName")?.asString ?: return@forEach
            val size = jsonElement.get("Size")?.asJsonArray?.let { sizeArray ->
                Triple(
                    sizeArray[0].asFloat,
                    sizeArray[1].asFloat,
                    sizeArray[2].asFloat
                )
            } ?: return@forEach
            val wings = jsonElement.get("Wings")?.asBoolean == true
            val wingsColor = jsonElement.get("WingsColor")?.asJsonArray?.let { colorArray ->
                Color(
                    colorArray[0].asInt,
                    colorArray[1].asInt,
                    colorArray[2].asInt
                )
            } ?: Colors.WHITE
            val customName = jsonElement.get("CustomName")?.asString?.replace("COLOR", "§") ?: ""
            val isDev = jsonElement.get("IsDev")?.asBoolean ?: false
            randoms[randomsName] =
                RandomPlayer(size, wings, Color(wingsColor.red, wingsColor.green, wingsColor.blue), customName, isDev)
        }
    }

    init {
        OdinMod.scope.launch {
            updateCustomProperties()
        }
    }
}