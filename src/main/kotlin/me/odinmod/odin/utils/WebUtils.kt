package me.odinmod.odin.utils

import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URI

fun setupConnection(
    url: String,
    timeout: Int = 5000,
    useCaches: Boolean = true,
): InputStream {
    val connection = URI(url).toURL().openConnection() as HttpURLConnection
    connection.setRequestMethod("GET")
    connection.setUseCaches(useCaches)
    connection.addRequestProperty("User-Agent", "Aurora")
    connection.setReadTimeout(timeout)
    connection.setConnectTimeout(timeout)
    connection.setDoOutput(true)
    return connection.inputStream
}