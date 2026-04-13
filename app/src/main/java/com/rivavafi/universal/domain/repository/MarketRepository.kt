package com.rivavafi.universal.domain.repository

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.rivavafi.universal.BuildConfig
import com.rivavafi.universal.data.network.WebSocketManager
import com.rivavafi.universal.data.network.YahooFinanceApi
import com.rivavafi.universal.domain.api.MarketItem
import com.rivavafi.universal.domain.api.News
import com.rivavafi.universal.domain.api.NewsApi
import com.rivavafi.universal.domain.api.StockApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import com.rivavafi.universal.domain.api.StockApi
import com.rivavafi.universal.domain.api.StockApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.rivavafi.universal.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarketRepository @Inject constructor(
    private val webSocketManager: WebSocketManager
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    private val finnhubApi = Retrofit.Builder()
        .baseUrl("https://finnhub.io/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(StockApiService::class.java)

    private val alphaApi = Retrofit.Builder()
        .baseUrl("https://www.alphavantage.co/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(StockApi::class.java)

    private val yahooApi = Retrofit.Builder()
        .baseUrl("https://query1.finance.yahoo.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(YahooFinanceApi::class.java)

    private val newsApi = Retrofit.Builder()
        .baseUrl("https://newsapi.org/v2/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(NewsApi::class.java)

    private val scraperClient = OkHttpClient()

    private val stockSymbols = listOf("RTX", "IREDA.NS")
    private val cryptoSymbols = listOf("BINANCE:BTCUSDT", "BINANCE:ETHUSDT")

    private val stockCache = mutableMapOf<String, MarketItem>()
    private val cryptoCache = mutableMapOf<String, MarketItem>()
    private var newsCache: List<News> = defaultNewsItems()

    private val _cryptoState = MutableStateFlow(cryptoSymbols.map { defaultMarketItem(it, "crypto") })
    val cryptoState: Flow<List<MarketItem>> = _cryptoState

    private val _stockState = MutableStateFlow(stockSymbols.map { defaultMarketItem(it, "stock") })
    val stockState: Flow<List<MarketItem>> = _stockState

    init {
        stockSymbols.forEach { stockCache[it] = defaultMarketItem(it, "stock") }
        cryptoSymbols.forEach { cryptoCache[it] = defaultMarketItem(it, "crypto") }

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
                handleWebSocketData(json)
            }
        }
    }

    suspend fun fetchStocks(): List<MarketItem> {
        val updated = stockSymbols.map { symbol ->
            getStockData(symbol)
        }

        _stockState.value = updated
        return updated
    }

    suspend fun getStockData(symbol: String): MarketItem {
        repeat(3) { attempt ->
            val item = tryFinnhubStock(symbol)
                ?: tryAlphaStock(symbol)
                ?: tryYahooStock(symbol)
                ?: tryScraperStock(symbol)
                ?: stockCache[symbol]

            if (item != null) {
                stockCache[symbol] = item
                return item
            }

            if (attempt < 2) {
                delay(5_000)
            }
        }

        return stockCache[symbol] ?: defaultMarketItem(symbol, "stock")
    }

    suspend fun fetchCryptoSnapshot(): List<MarketItem> {
        val resolved = kotlinx.coroutines.coroutineScope {
            cryptoSymbols.map { symbol ->
                async {
                    try {
                        val quote = finnhubApi.getQuote(symbol = symbol, apiKey = BuildConfig.FINNHUB_API_KEY)
                        Log.d("API_DEBUG", "Finnhub crypto $symbol => $quote")
                        MarketItem(
                            symbol = symbol,
                            price = quote.c,
                            change = quote.d,
                            percentChange = quote.dp,
                            type = "crypto"
                        )
                    } catch (e: Exception) {
                        Log.d("API_DEBUG", "Finnhub crypto failed for $symbol: ${e.message}")
                        cryptoCache[symbol] ?: defaultMarketItem(symbol, "crypto")
                    }
                }
            }.map { it.await() }
        }

        resolved.forEach { cryptoCache[it.symbol] = it }
        _cryptoState.value = resolved
        return resolved
    }

    suspend fun fetchNews(): List<News> {
        val resolved = tryNewsApi()
            ?: tryFinnhubNews()
            ?: tryKeywordNews()
            ?: newsCache

        newsCache = if (resolved.isNotEmpty()) resolved else newsCache
        return newsCache
    }

    private suspend fun tryFinnhubStock(symbol: String): MarketItem? {
        return try {
            val quote = finnhubApi.getQuote(symbol = symbol, apiKey = BuildConfig.FINNHUB_API_KEY)
            Log.d("API_DEBUG", "Finnhub stock $symbol => $quote")
            if (quote.c == 0.0) return null
            MarketItem(symbol = symbol, price = quote.c, change = quote.d, percentChange = quote.dp, type = "stock")
        } catch (e: Exception) {
            Log.d("API_DEBUG", "Finnhub stock failed for $symbol: ${e.message}")
            null
        }
    }

    private suspend fun tryAlphaStock(symbol: String): MarketItem? {
        return try {
            val alphaSymbol = if (symbol == "IREDA.NS") "IREDA.BSE" else symbol
            val response = alphaApi.getStockPrice(symbol = alphaSymbol, apiKey = BuildConfig.ALPHA_VANTAGE_API_KEY)
            Log.d("API_DEBUG", "AlphaVantage stock $symbol => $response")

            val price = response.quote?.price?.toDoubleOrNull() ?: return null
            val percent = response.quote?.changePercent?.replace("%", "")?.toDoubleOrNull() ?: 0.0
            val change = (price * percent) / 100
            MarketItem(symbol = symbol, price = price, change = change, percentChange = percent, type = "stock")
        } catch (e: Exception) {
            Log.d("API_DEBUG", "AlphaVantage failed for $symbol: ${e.message}")
            null
        }
    }

    private suspend fun tryYahooStock(symbol: String): MarketItem? {
        return try {
            val response = yahooApi.getQuotes(symbols = symbol)
            Log.d("API_DEBUG", "Yahoo stock $symbol => $response")
            val quote = response.quoteResponse?.result?.firstOrNull() ?: return null
            val price = quote.regularMarketPrice ?: return null
            val percent = quote.regularMarketChangePercent ?: 0.0
            val change = (price * percent) / 100
            MarketItem(symbol = symbol, price = price, change = change, percentChange = percent, type = "stock")
        } catch (e: Exception) {
            Log.d("API_DEBUG", "Yahoo failed for $symbol: ${e.message}")
            null
        }
    }

    private suspend fun tryScraperStock(symbol: String): MarketItem? {
        return try {
            val url = "https://r.jina.ai/http://query1.finance.yahoo.com/v7/finance/quote?symbols=$symbol"
            val request = Request.Builder().url(url).build()
            val body = scraperClient.newCall(request).execute().use { it.body?.string().orEmpty() }
            Log.d("API_DEBUG", "Scraper stock $symbol => $body")

            val rawJson = body.substringAfter('{', "").let { if (it.isBlank()) "" else "{$it" }
            if (rawJson.isBlank()) return null

            val json = Gson().fromJson(rawJson, JsonObject::class.java)
            val quote = json
                .getAsJsonObject("quoteResponse")
                ?.getAsJsonArray("result")
                ?.firstOrNull()
                ?.asJsonObject
                ?: return null

            val price = quote.get("regularMarketPrice")?.asDouble ?: return null
            val percent = quote.get("regularMarketChangePercent")?.asDouble ?: 0.0
            val change = (price * percent) / 100
            MarketItem(symbol = symbol, price = price, change = change, percentChange = percent, type = "stock")
        } catch (e: Exception) {
            Log.d("API_DEBUG", "Scraper fallback failed for $symbol: ${e.message}")
            null
        }
    }

    private suspend fun tryNewsApi(): List<News>? {
        return try {
            val response = newsApi.getFinanceNews()
            Log.d("API_DEBUG", "NewsApi top headlines => $response")
            response.articles.toNewsItems("NewsAPI").takeIf { it.isNotEmpty() }
        } catch (e: Exception) {
            Log.d("API_DEBUG", "NewsApi failed: ${e.message}")
            null
        }
    }

    private suspend fun tryFinnhubNews(): List<News>? {
        return try {
            val response = finnhubApi.getMarketNews(apiKey = BuildConfig.FINNHUB_API_KEY)
            Log.d("API_DEBUG", "Finnhub news => $response")
            response.map {
                News(
                    title = it.headline.ifBlank { "Market update" },
                    description = it.summary,
                    imageUrl = it.image,
                    source = it.source,
                    url = it.url,
                    publishedAt = it.datetime
                )
            }.take(10).takeIf { it.isNotEmpty() }
        } catch (e: Exception) {
            Log.d("API_DEBUG", "Finnhub news failed: ${e.message}")
            null
        }
        return emptyList()
    }

    private suspend fun tryKeywordNews(): List<News>? {
        return try {
            val response = newsApi.getEverythingNews(query = "stocks OR crypto OR market OR investing")
            Log.d("API_DEBUG", "NewsApi keyword fallback => $response")
            response.articles.toNewsItems("KeywordFeed").takeIf { it.isNotEmpty() }
        } catch (e: Exception) {
            Log.d("API_DEBUG", "Keyword news fallback failed: ${e.message}")
            null
        }
    }

    private fun handleWebSocketData(json: JsonObject) {
        val dataArray = json.getAsJsonArray("data") ?: return
        var changed = false

        dataArray.forEach { element ->
            val obj = element.asJsonObject
            val symbol = obj.get("s")?.asString ?: return@forEach
            val price = obj.get("p")?.asDouble ?: return@forEach
            val existing = cryptoCache[symbol]
            val change = existing?.let { price - it.price } ?: 0.0
            val percentChange = existing?.takeIf { it.price != 0.0 }?.let { (change / it.price) * 100 } ?: 0.0

            val updated = MarketItem(symbol, price, change, percentChange, "crypto")
            if (existing == null || existing.price != price) {
                cryptoCache[symbol] = updated
                changed = true
            }
            Log.d("API_DEBUG", "WebSocket crypto $symbol => $updated")
        }

        if (changed) {
            _cryptoState.value = cryptoSymbols.map { symbol ->
                cryptoCache[symbol] ?: defaultMarketItem(symbol, "crypto")
            }
        }
    }

    private fun List<com.rivavafi.universal.domain.api.Article>.toNewsItems(source: String): List<News> {
        return map {
            News(
                title = it.title ?: "Market update",
                description = it.description ?: "Fresh market coverage.",
                imageUrl = it.urlToImage ?: "",
                source = source,
                url = it.url ?: "",
                publishedAt = System.currentTimeMillis() / 1000
            )
        }.take(10)
    }

    private fun defaultMarketItem(symbol: String, type: String): MarketItem =
        MarketItem(symbol = symbol, price = 0.0, change = 0.0, percentChange = 0.0, type = type)

    private fun defaultNewsItems(): List<News> = listOf(
        News(
            title = "Markets are updating",
            description = "Live data engine is reconnecting and will refresh shortly.",
            imageUrl = "",
            source = "Rivava",
            url = "",
            publishedAt = System.currentTimeMillis() / 1000
        )
    )
}
