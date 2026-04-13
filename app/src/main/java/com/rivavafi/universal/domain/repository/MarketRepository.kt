package com.rivavafi.universal.domain.repository

import android.util.Log
import com.rivavafi.universal.data.network.WebSocketManager
import com.rivavafi.universal.data.network.YahooFinanceApi
import com.rivavafi.universal.domain.api.MarketItem
import com.rivavafi.universal.domain.api.News
import com.rivavafi.universal.domain.api.StockApi
import com.rivavafi.universal.domain.api.NewsApi
import com.rivavafi.universal.domain.api.StockApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.rivavafi.universal.BuildConfig

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

    private val finnhubApi = Retrofit.Builder()
        .baseUrl("https://finnhub.io/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(StockApiService::class.java)

    private val yahooApi = Retrofit.Builder()
        .baseUrl("https://query1.finance.yahoo.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(YahooFinanceApi::class.java)

    private val cache = mutableMapOf<String, MarketItem>()
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

                    val existing = cache[symbol]
                    if (existing == null || existing.price != price) {
                        val change = existing?.let { price - it.price } ?: 0.0
                        val percentChange = existing?.let { (change / it.price) * 100 } ?: 0.0
                        val type = if (symbol.contains("BINANCE")) "crypto" else "stock"
                        cache[symbol] = MarketItem(symbol, price, change, percentChange, type)
                        changed = true
                    }
                }
                if (changed) {
                    _cryptoState.value = cache.values.filter { it.type == "crypto" }.toList()
                }
            }
        }
    }

    private suspend fun getStockData(symbol: String): MarketItem {
        // Try Finnhub First
        try {
            val res = finnhubApi.getQuote(symbol = symbol, apiKey = BuildConfig.FINNHUB_API_KEY)
            Log.d("API_DEBUG", "Finnhub Response for $symbol: $res")
            if (res.c > 0.0) { // Valid
                return MarketItem(symbol, res.c, res.d, res.dp, "stock").also { cache[symbol] = it }
            }
        } catch (e: Exception) {
            Log.d("API_DEBUG", "Finnhub Failed for $symbol: ${e.message}")
        }

        // Try Alpha Vantage
        try {
            val res = alphaApi.getStockPrice(symbol = symbol, apiKey = BuildConfig.ALPHA_VANTAGE_API_KEY)
            Log.d("API_DEBUG", "AlphaVantage Response for $symbol: $res")
            val q = res.quote
            if (q != null && q.price != null && q.changePercent != null) {
                val p = q.price.toDoubleOrNull() ?: 0.0
                val cp = q.changePercent.replace("%", "").toDoubleOrNull() ?: 0.0
                if (p > 0.0) {
                    return MarketItem(symbol, p, 0.0, cp, "stock").also { cache[symbol] = it }
                }
            }
        } catch (e: Exception) {
            Log.d("API_DEBUG", "AlphaVantage Failed for $symbol: ${e.message}")
        }

        // Try Yahoo Finance Fallback
        try {
            val res = yahooApi.getFallbackQuotes(symbols = symbol)
            Log.d("API_DEBUG", "Yahoo Response for $symbol: $res")
            val item = res.quoteResponse?.result?.firstOrNull()
            if (item != null && item.regularMarketPrice != null) {
                return MarketItem(symbol, item.regularMarketPrice, 0.0, item.regularMarketChangePercent ?: 0.0, "stock").also { cache[symbol] = it }
            }
        } catch (e: Exception) {
            Log.d("API_DEBUG", "Yahoo Failed for $symbol: ${e.message}")
        }

        // Try Cache
        val cachedItem = cache[symbol]
        if (cachedItem != null) {
            Log.d("API_DEBUG", "Returning cached data for $symbol")
            return cachedItem
        }

        throw Exception("All APIs and Cache failed for $symbol")
    }

    suspend fun fetchStocksWithRetry(): List<MarketItem> {
        val symbols = listOf("IREDA.BSE", "RTX")
        val updatedStocks = mutableListOf<MarketItem>()

        for (symbol in symbols) {
            var attempt = 0
            var success = false
            while (attempt < 3 && !success) {
                try {
                    val data = getStockData(symbol)
                    updatedStocks.add(data)
                    success = true
                } catch (e: Exception) {
                    attempt++
                    Log.d("API_DEBUG", "Retry $attempt for $symbol due to ${e.message}")
                    if (attempt < 3) delay(5000)
                }
            }
            if (!success) {
                cache[symbol]?.let { updatedStocks.add(it) } // Fallback to last known if available after retries
            }
        }

        if (updatedStocks.isNotEmpty()) {
            _stockState.value = updatedStocks.toList()
        }
        return updatedStocks
    }

    suspend fun fetchNewsWithRetry(): List<News> {
        var attempt = 0
        while (attempt < 3) {
            try {
                // Try NewsAPI
                var response = newsApi.getFinanceNews()
                Log.d("API_DEBUG", "NewsAPI Top Headlines Response: ${response.articles.size} articles")

                if (response.articles.isEmpty()) {
                    // Fallback Finnhub
                    val fhNews = finnhubApi.getMarketNews(category = "general", apiKey = BuildConfig.FINNHUB_API_KEY)
                    Log.d("API_DEBUG", "Finnhub News Response: ${fhNews.size} articles")
                    if (fhNews.isNotEmpty()) {
                        return fhNews.map {
                            News(it.headline, it.summary, it.image, it.source, it.url, it.datetime)
                        }.take(10)
                    }

                    // Fallback NewsAPI Keyword
                    response = newsApi.getEverythingNews()
                    Log.d("API_DEBUG", "NewsAPI Everything Response: ${response.articles.size} articles")
                }

                if (response.articles.isNotEmpty()) {
                    return response.articles.map {
                        News(
                            title = it.title ?: "No Title",
                            description = it.description ?: "",
                            imageUrl = it.urlToImage ?: "",
                            source = "News",
                            url = it.url ?: "",
                            publishedAt = System.currentTimeMillis() / 1000
                        )
                    }.take(10)
                }
            } catch (e: Exception) {
                Log.d("API_DEBUG", "News Fetch Failed on attempt ${attempt + 1}: ${e.message}")
            }
            attempt++
            if (attempt < 3) delay(5000)
        }
        return emptyList()
    }
}
