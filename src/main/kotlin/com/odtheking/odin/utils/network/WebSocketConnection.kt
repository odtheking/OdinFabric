package com.odtheking.odin.utils.network

import com.odtheking.odin.OdinMod.logger
import com.odtheking.odin.OdinMod.okClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString


fun webSocket(func: WebSocketConnection.() -> Unit) = WebSocketConnection().apply(func)

class WebSocketConnection {
    @Volatile
    private var _webSocket: WebSocket? = null
    private var onMessageFunc: (String) -> Unit = { }

    fun onMessage(func: (String) -> Unit) {
        onMessageFunc = func
    }

    val connected get() = _webSocket != null

    fun send(message: String) {
        if (!connected) {
            logger.warn("Cannot send message: WebSocket not connected")
            return
        }
        _webSocket?.send(message)
    }

    fun connect(url: String) {
        shutdown()
        val request = Request.Builder().url(url).build()
        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                logger.info("WebSocket connected to $url")
                _webSocket = webSocket
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                onMessageFunc(text)
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                onMessageFunc(bytes.utf8())
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(code, reason)
                logger.info("WebSocket closing: $code / $reason")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                logger.error("WebSocket error: ${t.message}", t)
            }
        }

        _webSocket = okClient.newWebSocket(request, listener)
    }

    fun shutdown() {
        _webSocket?.close(1000, "Client shutdown")
        _webSocket = null
    }
}