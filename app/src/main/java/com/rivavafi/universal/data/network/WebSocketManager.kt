package com.rivavafi.universal.data.network

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.rivavafi.universal.BuildConfig
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import okhttp3.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebSocketManager @Inject constructor(
    private val client: OkHttpClient
) {
    private var webSocket: WebSocket? = null
    private val _updates = MutableSharedFlow<JsonObject>(extraBufferCapacity = 10)
    val updates: SharedFlow<JsonObject> = _updates

    fun connect() {
        if (webSocket != null) return

        val request = Request.Builder()
            .url("wss://ws.finnhub.io?token=${BuildConfig.FINNHUB_API_KEY}")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                subscribe("RTX")
                subscribe("BINANCE:BTCUSDT")
                subscribe("BINANCE:ETHUSDT")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val json = Gson().fromJson(text, JsonObject::class.java)
                    if (json.has("data")) {
                        _updates.tryEmit(json)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                this@WebSocketManager.webSocket = null
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                this@WebSocketManager.webSocket = null
                // Auto reconnect
                Thread.sleep(5000)
                connect()
            }
        })
    }

    private fun subscribe(symbol: String) {
        val json = """{"type":"subscribe","symbol":"$symbol"}"""
        webSocket?.send(json)
    }
}
