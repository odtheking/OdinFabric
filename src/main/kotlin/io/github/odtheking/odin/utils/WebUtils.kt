package io.github.odtheking.odin.utils

import com.google.gson.JsonParser
import kotlinx.coroutines.withTimeoutOrNull
import java.io.InputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URI

fun setupConnection(url: String, timeout: Int = 5000, useCaches: Boolean = true): InputStream {
    val connection = URI(url).toURL().openConnection() as HttpURLConnection
    connection.setRequestMethod("GET")
    connection.setUseCaches(useCaches)
    connection.addRequestProperty("User-Agent", "Odin")
    connection.setReadTimeout(timeout)
    connection.setConnectTimeout(timeout)
    connection.setDoOutput(true)
    return connection.inputStream
}

fun fetchData(url: String, timeout: Int = 5000, useCaches: Boolean = true): String =
    setupConnection(url, timeout, useCaches).bufferedReader().use { it.readText() }

fun sendDataToServer(
    body: String,
    url: String = "https://gi2wsqbyse6tnfhqakbnq6f2su0vujgz.lambda-url.eu-north-1.on.aws/"
): String {
    return try {
        val connection = URI(url).toURL().openConnection() as HttpURLConnection
        connection.setRequestMethod("POST")
        connection.setDoOutput(true)

        with(OutputStreamWriter(connection.outputStream)) {
            write(body)
            flush()
        }

        connection.disconnect()

        connection.inputStream.bufferedReader().use { it.readText() }
    } catch (_: Exception) {
        ""
    }
}

suspend fun hasBonusPaulScore(): Boolean = withTimeoutOrNull(5000) {
    val response: String = URI("https://api.hypixel.net/resources/skyblock/election").toURL().readText()
    val jsonObject = JsonParser.parseString(response).asJsonObject
    val mayor = jsonObject.getAsJsonObject("mayor") ?: return@withTimeoutOrNull false
    val name = mayor.get("name")?.asString ?: return@withTimeoutOrNull false
    return@withTimeoutOrNull if (name == "Paul") {
        mayor.getAsJsonArray("perks")?.any { it.asJsonObject.get("name")?.asString == "EZPZ" } == true
    } else false
} == true