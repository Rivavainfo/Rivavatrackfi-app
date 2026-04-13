package com.rivavafi.universal.domain.repository

import com.rivavafi.universal.data.network.WebSocketManager
import com.rivavafi.universal.domain.api.MarketItem
import com.rivavafi.universal.domain.api.News
import com.rivavafi.universal.domain.api.StockApiService
import com.rivavafi.universal.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Singleton
class MarketRepository @Inject constructor(
    private val webSocketManager: WebSocketManager,
    private val apiService: StockApiService
) {
    private val apiKey = BuildConfig.FINNHUB_API_KEY
    private val scope = CoroutineScope(Dispatchers.IO)

    private val currentItems = mutableMapOf<String, MarketItem>()
    private val _marketState = MutableStateFlow<List<MarketItem>>(emptyList())
    val marketState: Flow<List<MarketItem>> = _marketState

    init {
        webSocketManager.connect()
        scope.launch {
            webSocketManager.updates.collect { json ->
                val dataArray = json.getAsJsonArray("data")
                var changed = false
                dataArray.forEach { element ->
                    val obj = element.asJsonObject
                    val symbol = obj.get("s").asString
                    val price = obj.get("p").asDouble

                    val existing = currentItems[symbol]
                    if (existing == null || existing.price != price) {
                        val change = existing?.let { price - it.price } ?: 0.0
                        val percentChange = existing?.let { (change / it.price) * 100 } ?: 0.0
                        val type = if (symbol.contains("BINANCE")) "crypto" else "stock"
                        currentItems[symbol] = MarketItem(symbol, price, change, percentChange, type)
                        changed = true
                    }
                }
                if (changed) {
                    _marketState.value = currentItems.values.toList()
                }
            }
        }
    }

    suspend fun fetchIredaFallback() {
        try {
            val response = apiService.getQuote("IREDA.NS", apiKey)
            val symbol = "IREDA.NS"
            val price = response.c
            val existing = currentItems[symbol]
            if (existing == null || existing.price != price) {
                currentItems[symbol] = MarketItem(symbol, price, response.d, response.dp, "stock")
                _marketState.value = currentItems.values.toList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun fetchNews(): List<News> {
        return try {
            val response = apiService.getMarketNews("general", apiKey)
            val keywords = listOf("rtx", "ireda", "crypto", "bitcoin", "ethereum")
            response.filter { item ->
                val text = "${item.headline} ${item.summary}".lowercase()
                keywords.any { text.contains(it) }
            }.map {
                News(
                    title = it.headline,
                    description = it.summary,
                    imageUrl = it.image,
                    source = it.source,
                    url = it.url,
                    publishedAt = it.datetime
                )
            }.take(10) // Limit list
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
