package com.rivavafi.universal.domain.repository

import com.rivavafi.universal.data.network.WebSocketManager
import com.rivavafi.universal.domain.api.MarketItem
import com.rivavafi.universal.domain.api.News
import com.rivavafi.universal.domain.api.StockApi
import com.rivavafi.universal.domain.api.NewsApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Singleton
class MarketRepository @Inject constructor(
    private val webSocketManager: WebSocketManager
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    private val alphaApi = Retrofit.Builder()
        .baseUrl("https://www.alphavantage.co/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(StockApi::class.java)

    private val newsApi = Retrofit.Builder()
        .baseUrl("https://newsapi.org/v2/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(NewsApi::class.java)

    private val currentItems = mutableMapOf<String, MarketItem>()
    private val _cryptoState = MutableStateFlow<List<MarketItem>>(emptyList())
    val cryptoState: Flow<List<MarketItem>> = _cryptoState

    private val _stockState = MutableStateFlow<List<MarketItem>>(emptyList())
    val stockState: Flow<List<MarketItem>> = _stockState

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
                    _cryptoState.value = currentItems.values.filter { it.type == "crypto" }.toList()
                }
            }
        }
    }

    suspend fun fetchStocks(): List<MarketItem> {
        val symbols = listOf("IREDA.BSE", "RTX")
        val updatedStocks = mutableListOf<MarketItem>()

        for (symbol in symbols) {
            try {
                val response = alphaApi.getStockPrice(symbol = symbol, apiKey = "1JCULNPFKQXWC62U")
                val quote = response.quote
                if (quote != null && quote.price != null && quote.changePercent != null) {
                    val priceStr = quote.price
                    val changeStr = quote.changePercent.replace("%", "")

                    val price = priceStr.toDoubleOrNull() ?: 0.0
                    val changePercent = changeStr.toDoubleOrNull() ?: 0.0
                    // Alpha Vantage gives absolute change? Let's just calculate it or fake it to 0.0 since we have percent.
                    val change = 0.0

                    val item = MarketItem(symbol, price, change, changePercent, "stock")
                    currentItems[symbol] = item
                    updatedStocks.add(item)
                } else {
                    // Fallback to existing if rate limited
                    currentItems[symbol]?.let { updatedStocks.add(it) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Fallback
                currentItems[symbol]?.let { updatedStocks.add(it) }
            }
        }

        _stockState.value = updatedStocks.toList()
        return updatedStocks
    }

    suspend fun fetchNews(): List<News> {
        return try {
            var response = newsApi.getFinanceNews()
            if (response.articles.isEmpty()) {
                response = newsApi.getEverythingNews()
            }

            response.articles.map {
                News(
                    title = it.title ?: "No Title",
                    description = it.description ?: "",
                    imageUrl = it.urlToImage ?: "",
                    source = "News",
                    url = it.url ?: "",
                    publishedAt = System.currentTimeMillis() / 1000
                )
            }.take(10) // Limit list
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
