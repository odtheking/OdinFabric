package com.odtheking.odin.utils.network

import com.odtheking.odin.OdinMod.logger
import com.odtheking.odin.OdinMod.okClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.util.concurrent.atomic.AtomicReference

fun webSocket(func: WebSocketConnection.() -> Unit) = WebSocketConnection().apply(func)

class WebSocketConnection {
    private val _webSocket = AtomicReference<WebSocket?>(null)
    private var onMessageFunc: (String) -> Unit = { }

    fun onMessage(func: (String) -> Unit) {
        onMessageFunc = func
    }

    val connected get() = _webSocket.get() != null

    fun send(message: String): Boolean {
        val ws = _webSocket.get()
        if (ws == null) {
            logger.warn("Cannot send message: WebSocket not connected")
            return false
        }
        return ws.send(message)
    }

    fun connect(url: String) {
        _webSocket.getAndSet(null)?.close(1000, "Reconnecting")

        val request = Request.Builder().url(url).build()
        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                logger.info("WebSocket connected to $url")
                _webSocket.set(webSocket)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                onMessageFunc(text)
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                onMessageFunc(bytes.utf8())
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                logger.info("WebSocket closing: $code / $reason")
                webSocket.close(1000, null)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                logger.info("WebSocket closed: $code / $reason")
                _webSocket.compareAndSet(webSocket, null)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                logger.error("WebSocket error: ${t.message}", t)
                _webSocket.compareAndSet(webSocket, null)
            }
        }

        okClient.newWebSocket(request, listener)
    }

    fun shutdown() {
        _webSocket.getAndSet(null)?.close(1000, "Client shutdown")
    }
}