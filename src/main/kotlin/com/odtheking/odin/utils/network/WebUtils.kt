package com.odtheking.odin.utils.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.odtheking.odin.OdinMod.logger
import com.odtheking.odin.OdinMod.okClient
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.apache.http.impl.EnglishReasonPhraseCatalog
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

object WebUtils {
    private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    private val JSON = "application/json; charset=utf-8".toMediaType()
    val gson: Gson = GsonBuilder().create()

    suspend inline fun <reified T> fetchJson(url: String, json: Gson = gson): Result<T> = runCatching {
        json.fromJson<T>(fetchString(url).getOrElse { return Result.failure(it) }, T::class.java)
    }

    suspend fun fetchString(url: String): Result<String> =
        executeRequest(Request.Builder().url(url).build())
            .mapCatching { response -> response.use { it.body.string() } }
            .onFailure { logger.warn("Failed to fetch from $url: ${it.message}") }

    suspend fun getInputStream(url: String): Result<InputStream> =
        executeRequest(Request.Builder().url(url).build())
            .map { response -> response.body.byteStream() }
            .onFailure { logger.warn("Failed to get input stream from $url: ${it.message}") }

    suspend fun postData(url: String, body: String): Result<String> =
        executeRequest(Request.Builder().url(url).post(body.toRequestBody(JSON)).build())
            .mapCatching { response -> response.use { it.body.string() } }
            .onFailure { logger.warn("Failed to post data to $url: ${it.message}") }

    private suspend fun executeRequest(request: Request): Result<Response> = suspendCancellableCoroutine { cont ->
        logger.info("Making request to ${request.url}")

        val call = okClient.newCall(request)

        cont.invokeOnCancellation {
            logger.info("Cancelling request to ${request.url}")
            call.cancel()
        }

        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (cont.isActive) {
                    logger.warn("Request failed for ${request.url}: ${e.message}")
                    cont.resume(Result.failure(e))
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (!cont.isActive) {
                    response.close()
                    return
                }

                if (response.isSuccessful) cont.resume(Result.success(response))
                else {
                    response.close()
                    cont.resume(Result.failure(InputStreamException(response.code, request.url.toString())))
                }
            }
        })
    }

    fun createClient(): OkHttpClient = OkHttpClient.Builder().apply {
        dispatcher(Dispatcher().apply {
            maxRequests = 10
            maxRequestsPerHost = 5
        })

        readTimeout(10, TimeUnit.SECONDS)
        connectTimeout(5, TimeUnit.SECONDS)
        writeTimeout(10, TimeUnit.SECONDS)

        retryOnConnectionFailure(true)

        addInterceptor { chain ->
            chain.request().newBuilder()
                .header("Accept", "application/json")
                .header("User-Agent", USER_AGENT)
                .build()
                .let { chain.proceed(it) }
        }
    }.build()

    suspend fun hasBonusPaulScore(): Boolean {
        val response = fetchJson<JsonObject>("https://api.hypixel.net/resources/skyblock/election")
            .getOrElse {
                logger.warn("Failed to fetch election data: ${it.message}")
                return false
            }

        val mayor = response.getAsJsonObject("mayor") ?: return false
        val name = mayor.get("name")?.asString ?: return false
        return name == "Paul" && mayor.getAsJsonArray("perks")?.any { it.asJsonObject.get("name")?.asString == "EZPZ" } == true
    }

    class InputStreamException(code: Int, url: String) : Exception("Failed to get input stream from $url: ${EnglishReasonPhraseCatalog.INSTANCE.getReason(code, null)}")
}