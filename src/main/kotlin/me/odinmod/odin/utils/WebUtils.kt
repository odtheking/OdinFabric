package me.odinmod.odin.utils

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

fun sendDataToServer(body: String, url: String = "https://gi2wsqbyse6tnfhqakbnq6f2su0vujgz.lambda-url.eu-north-1.on.aws/"): String {
    return try {
        val connection = URI(url).toURL().openConnection() as HttpURLConnection
        connection.setRequestMethod("POST")
        connection.setDoOutput(true)

        with (OutputStreamWriter(connection.outputStream)) {
            write(body)
            flush()
        }

       // val responseCode = connection.responseCode
       // if (RandomPlayers.isDev) println("Response Code: $responseCode")

        val response = connection.inputStream.bufferedReader().use { it.readText() }
       // if (RandomPlayers.isDev) println("Response: $response")

        connection.disconnect()

        response
    } catch (_: Exception) { "" }
}
