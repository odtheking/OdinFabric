package com.odtheking.odin.utils.network.hypixelapi

import com.odtheking.odin.OdinMod.logger
import com.odtheking.odin.features.impl.render.ClickGUIModule.hypixelApiUrl
import com.odtheking.odin.utils.network.WebUtils.fetchJson

object RequestUtils {
    private val uuidCache: HashMap<String, UuidData> = HashMap()
    private val cachedPlayerData: HashMap<String, Pair<HypixelData.PlayerInfo, Long>> = HashMap()

    private fun getServer(endpoint: EndPoint, uuid: String): String = hypixelApiUrl + endpoint.name.lowercase() + "/" + uuid

    suspend fun getProfile(name: String): Result<HypixelData.PlayerInfo> {
        getFromCache(name)?.let { return Result.success(it) }
        val uuidData = getUuid(name).getOrElse { return Result.failure(Exception(it.cause)) }
        return fetchJson<HypixelData.ProfilesData>(getServer(EndPoint.GET, uuidData.id)).map { it ->
            it.failed?.let { return Result.failure(Exception("Failed to get hypixel data: $it")) }
            HypixelData.PlayerInfo(it, uuidData.id, uuidData.name)
        }
    }

    private fun addToCache(profiles: HypixelData.PlayerInfo) {
        val time = System.currentTimeMillis()
        if (profiles.profileData.profiles.isEmpty()) return logger.info("Refusing to cache empty profile!")

        cachedPlayerData.entries
            .takeIf { it.size >= 5 }
            ?.maxByOrNull { time - it.value.second }?.key
            ?.let { cachedPlayerData.remove(it); logger.info("Removed $it from cache list.") }

        cachedPlayerData[profiles.name.lowercase()] = Pair(profiles, time)
        logger.info("Added ${profiles.name} to cache list. Cache size is now ${cachedPlayerData.size}/5.")
    }

    private fun getFromCache(name: String, time: Long = 600000) =
        cachedPlayerData[name.lowercase()]?.takeUnless { (System.currentTimeMillis() - it.second >= time) }?.first

    suspend fun getUuid(name: String): Result<UuidData> {
        val lowerName = name.lowercase()
        uuidCache[lowerName]?.let { return Result.success(it) }
        return fetchJson<UuidData>("https://api.minecraftservices.com/minecraft/profile/lookup/name/$name").onSuccess {
            uuidCache[lowerName] = it
        }
    }

    enum class EndPoint { SECRETS, GET }
    data class UuidData(val name: String, val id: String)
}