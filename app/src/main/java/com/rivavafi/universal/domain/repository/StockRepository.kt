package com.rivavafi.universal.domain.repository

import android.content.Context
import android.util.Log
import com.rivavafi.universal.BuildConfig
import com.rivavafi.universal.domain.api.FinnhubCompanyProfileResponse
import com.rivavafi.universal.domain.api.FinnhubNewsResponse
import com.rivavafi.universal.domain.api.StockApiService
import com.rivavafi.universal.domain.api.StockResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jsoup.Jsoup
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject

@Singleton
class StockRepository @Inject constructor(
    private val apiService: StockApiService,
    @ApplicationContext context: Context
) {
    private val apiKey = BuildConfig.FINNHUB_API_KEY
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val massiveApi: com.rivavafi.universal.domain.api.MassiveApi by lazy {
        retrofit2.Retrofit.Builder()
            .baseUrl("https://api.massive.com/")
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
            .create(com.rivavafi.universal.domain.api.MassiveApi::class.java)
    }

    private val alphaApi: com.rivavafi.universal.domain.api.StockApi by lazy {
        retrofit2.Retrofit.Builder()
            .baseUrl("https://www.alphavantage.co/")
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
            .create(com.rivavafi.universal.domain.api.StockApi::class.java)
    }

    suspend fun getGuaranteedQuote(symbol: String): StockQuoteWithSource {
        val normalizedSymbol = normalizeSymbol(symbol)
        val prefix = cachePrefix(normalizedSymbol)
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        if (apiKey.isBlank() || apiKey == "d7r4hahr01qtpsm11kc0d7r4hahr01qtpsm11kcg") {
            Log.w(TAG, "Finnhub API key missing or default, skipping live fetch for $normalizedSymbol")
            val diagnostic = "[$timestamp] LIVE fetch skipped: Missing or placeholder API Key."
            return resolveFromCacheOrDefault(normalizedSymbol, prefix, "No valid API key provided", diagnostic)
        }

        var retryCount = 0
        var currentDelay = 1000L
        val maxRetries = 2
        var lastDiagnostic = ""
        var userReason = "Service unavailable"

        while (retryCount <= maxRetries) {
            try {
                val response = apiService.getQuote(normalizedSymbol, apiKey)
                val body = response.body()
                val code = response.code()
                val errorBodySnippet = response.errorBody()?.string()?.take(100) ?: "null"

                Log.d(TAG, "Quote response for $normalizedSymbol -> code=$code body=$body")

                if (response.isSuccessful && body != null && body.c > 0.0) {
                    saveQuote(prefix, body.c, body.pc)
                    return StockQuoteWithSource(
                        symbol = normalizedSymbol,
                        quote = body,
                        source = QuoteSource.LIVE,
                        userSafeReason = null,
                        diagnostics = "[$timestamp] LIVE success for $normalizedSymbol: code=$code, c=${body.c}, pc=${body.pc}"
                    )
                } else {
                    lastDiagnostic = "[$timestamp] LIVE HTTP Error: code=$code, body snippet=$errorBodySnippet"
                    if (code == 401) {
                         userReason = "Authentication failed with data provider"
                         break // Don't retry on 401
                    } else if (code == 429) {
                         userReason = "Rate limit exceeded. Too many requests."
                         break // Could retry, but Finnhub rate limits are often daily or minutely, so fallback is faster
                    } else if (code in 500..599) {
                         userReason = "Market data service is temporarily down"
                         // will retry
                    } else {
                         userReason = "Failed to fetch live data (HTTP $code)"
                         break // Client error other than 401/429
                    }
                }
            } catch (exception: Exception) {
                lastDiagnostic = "[$timestamp] LIVE Exception: type=${exception.javaClass.simpleName}, message=${exception.message}"
                userReason = "Network error while connecting to market data"
                Log.e(TAG, "Quote fetch failed for $normalizedSymbol (attempt ${retryCount + 1})", exception)
                // will retry
            }

            retryCount++
            if (retryCount <= maxRetries) {
                Log.d(TAG, "Retrying live fetch in $currentDelay ms...")
                delay(currentDelay)
                currentDelay *= 2
            }
        }

        return tryMassiveFallback(normalizedSymbol, prefix, userReason, lastDiagnostic)
    }

    private suspend fun tryMassiveFallback(symbol: String, prefix: String, reason: String, previousDiagnostics: String): StockQuoteWithSource {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        try {
            val response = massiveApi.getStockPrice(symbol = symbol, apiKey = BuildConfig.MASSIVE_API_KEY)
            val data = response.data

            if (data != null && data.price != null && data.price > 0.0) {
                val price = data.price
                val prevClose = data.previousClose ?: price
                val open = data.open ?: prevClose
                val high = data.high ?: prevClose
                val low = data.low ?: prevClose

                Log.d(TAG, "Massive fallback succeeded for $symbol -> price=$price")
                saveQuote(prefix, price, prevClose)
                return StockQuoteWithSource(
                    symbol = symbol,
                    quote = StockResponse(c = price, h = high, l = low, o = open, pc = prevClose),
                    source = QuoteSource.SCRAPE,
                    userSafeReason = null,
                    diagnostics = "$previousDiagnostics\n[$timestamp] MASSIVE success: price=$price"
                )
            } else {
                val massiveDiagnostic = "[$timestamp] MASSIVE Failed: invalid or empty price. Response=$response"
                return tryAlphaVantageFallback(symbol, prefix, reason, "$previousDiagnostics\n$massiveDiagnostic")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Massive fallback failed for $symbol", e)
            val massiveDiagnostic = "[$timestamp] MASSIVE Exception: type=${e.javaClass.simpleName}, message=${e.message}"
            return tryAlphaVantageFallback(symbol, prefix, reason, "$previousDiagnostics\n$massiveDiagnostic")
        }
    }

    private suspend fun tryAlphaVantageFallback(symbol: String, prefix: String, reason: String, previousDiagnostics: String): StockQuoteWithSource {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        try {
            val response = alphaApi.getStockPrice(symbol = symbol, apiKey = BuildConfig.ALPHA_VANTAGE_API_KEY)
            val priceStr = response.quote?.price
            val changePercentStr = response.quote?.changePercent

            val price = priceStr?.toDoubleOrNull()

            if (price != null && price > 0.0) {
                // Approximate previous close since AlphaVantage only gives change percent
                val changePct = changePercentStr?.replace("%", "")?.toDoubleOrNull() ?: 0.0
                val prevClose = price / (1 + (changePct / 100))

                Log.d(TAG, "AlphaVantage fallback succeeded for $symbol -> price=$price")
                saveQuote(prefix, price, prevClose)
                return StockQuoteWithSource(
                    symbol = symbol,
                    quote = StockResponse(c = price, h = price, l = price, o = prevClose, pc = prevClose),
                    source = QuoteSource.SCRAPE, // Reusing SCRAPE enum for fallback API
                    userSafeReason = null,
                    diagnostics = "$previousDiagnostics\n[$timestamp] ALPHA_VANTAGE success: price=$price"
                )
            } else {
                val alphaDiagnostic = "[$timestamp] ALPHA_VANTAGE Failed: invalid or empty price. Response=$response"
                return tryScrapeFallback(symbol, prefix, reason, "$previousDiagnostics\n$alphaDiagnostic")
            }
        } catch (e: Exception) {
            Log.e(TAG, "AlphaVantage fallback failed for $symbol", e)
            val alphaDiagnostic = "[$timestamp] ALPHA_VANTAGE Exception: type=${e.javaClass.simpleName}, message=${e.message}"
            return tryScrapeFallback(symbol, prefix, reason, "$previousDiagnostics\n$alphaDiagnostic")
        }
    }

    private suspend fun tryScrapeFallback(symbol: String, prefix: String, reason: String, previousDiagnostics: String): StockQuoteWithSource {
        return withContext(Dispatchers.IO) {
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            try {
                val yahooSymbol = if (symbol.equals(IREDA_SYMBOL, ignoreCase = true)) "IREDA.NS" else "RTX"
                val scrapeUrl = "https://query1.finance.yahoo.com/v8/finance/chart/$yahooSymbol"

                val jsonResponse = Jsoup.connect(scrapeUrl)
                    .userAgent("Mozilla/5.0")
                    .ignoreContentType(true)
                    .execute()
                    .body()

                val jsonObject = JSONObject(jsonResponse)
                val chart = jsonObject.optJSONObject("chart")
                val result = chart?.optJSONArray("result")?.optJSONObject(0)
                val meta = result?.optJSONObject("meta")

                val price = meta?.optDouble("regularMarketPrice")?.takeIf { !it.isNaN() }
                val prevClose = meta?.optDouble("previousClose")?.takeIf { !it.isNaN() }

                if (price != null && price > 0.0 && prevClose != null) {
                    Log.d(TAG, "Scraped fallback succeeded for $symbol -> price=$price, pc=$prevClose")
                    saveQuote(prefix, price, prevClose)
                    StockQuoteWithSource(
                        symbol = symbol,
                        quote = StockResponse(c = price, h = price, l = price, o = prevClose, pc = prevClose),
                        source = QuoteSource.SCRAPE,
                        userSafeReason = null,
                        diagnostics = "$previousDiagnostics\n[$timestamp] SCRAPE success: price=$price, prevClose=$prevClose"
                    )
                } else {
                    val bodySnippet = jsonResponse.take(100)
                    val scrapeDiagnostic = "[$timestamp] SCRAPE Failed: parsed price=$price, prevClose=$prevClose. Body snippet: $bodySnippet"
                    resolveFromCacheOrDefault(symbol, prefix, reason, "$previousDiagnostics\n$scrapeDiagnostic")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Scrape fallback failed for $symbol", e)
                val scrapeDiagnostic = "[$timestamp] SCRAPE Exception: type=${e.javaClass.simpleName}, message=${e.message}"
                resolveFromCacheOrDefault(symbol, prefix, reason, "$previousDiagnostics\n$scrapeDiagnostic")
            }
        }
    }

    fun getRealtimeQuote(symbol: String): Flow<Result<StockResponse>> = flow {
        val resolved = getGuaranteedQuote(symbol)
        emit(Result.success(resolved.quote))
    }

    fun getCompanyProfile(symbol: String): Flow<Result<FinnhubCompanyProfileResponse>> = flow {
        if (apiKey.isBlank()) {
            emit(Result.failure(Exception("Missing API Key")))
            return@flow
        }
        try {
            val response = apiService.getCompanyProfile(normalizeSymbol(symbol), apiKey)
            emit(Result.success(response))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun getMarketNews(): Flow<Result<List<FinnhubNewsResponse>>> = flow {
        if (apiKey.isBlank()) {
            emit(Result.failure(Exception("Missing API Key")))
            return@flow
        }
        try {
            val response = apiService.getMarketNews("general", apiKey)
            emit(Result.success(response.take(10)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun getCompanyNews(symbol: String): Flow<Result<List<FinnhubNewsResponse>>> = flow {
        if (apiKey.isBlank()) {
            emit(Result.failure(Exception("Missing API Key")))
            return@flow
        }
        try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val today = format.format(Date())
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -14)
            val twoWeeksAgo = format.format(cal.time)

            val response = apiService.getCompanyNews(normalizeSymbol(symbol), twoWeeksAgo, today, apiKey)
            emit(Result.success(response.take(5)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    private fun resolveFromCacheOrDefault(symbol: String, prefix: String, reason: String, previousDiagnostics: String): StockQuoteWithSource {
        val cachedPrice = preferences.getFloat("${prefix}_price", -1f).toDouble()
        val cachedPc = preferences.getFloat("${prefix}_pc", -1f).toDouble()
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        if (cachedPrice > 0.0 && cachedPc > 0.0) {
            Log.d(TAG, "Using cached quote for $symbol because $reason")
            return StockQuoteWithSource(
                symbol = symbol,
                quote = StockResponse(c = cachedPrice, h = cachedPrice, l = cachedPrice, o = cachedPc, pc = cachedPc),
                source = QuoteSource.CACHE,
                userSafeReason = "Showing last known prices. $reason",
                diagnostics = "$previousDiagnostics\n[$timestamp] CACHE success: retrieved price=$cachedPrice, pc=$cachedPc"
            )
        }

        val defaultQuote = when (symbol) {
            IREDA_SYMBOL -> StockResponse(c = 150.0, h = 150.0, l = 150.0, o = 148.0, pc = 148.0)
            else -> StockResponse(c = 100.0, h = 100.0, l = 100.0, o = 99.0, pc = 99.0)
        }

        Log.d(TAG, "Using hard fallback quote for $symbol because $reason")
        val cacheDiagnostic = "[$timestamp] CACHE failed: no valid data found in SharedPreferences"
        return StockQuoteWithSource(
            symbol = symbol,
            quote = defaultQuote,
            source = QuoteSource.DEFAULT,
            userSafeReason = "Unable to fetch live prices at this moment",
            diagnostics = "$previousDiagnostics\n$cacheDiagnostic\n[$timestamp] DEFAULT fallback used."
        )
    }

    private fun saveQuote(prefix: String, price: Double, previousClose: Double) {
        preferences.edit()
            .putFloat("${prefix}_price", price.toFloat())
            .putFloat("${prefix}_pc", previousClose.toFloat())
            .apply()
    }

    private fun normalizeSymbol(symbol: String): String = if (symbol.equals("IREDA", ignoreCase = true)) IREDA_SYMBOL else symbol

    private fun cachePrefix(symbol: String): String = if (symbol.equals(IREDA_SYMBOL, ignoreCase = true)) "IREDA" else "RTX"

    companion object {
        private const val TAG = "StockRepository"
        private const val PREFS_NAME = "stock_quotes_cache"
        const val IREDA_SYMBOL = "IREDA.NS"
    }
}

enum class QuoteSource { LIVE, SCRAPE, CACHE, DEFAULT }

data class StockQuoteWithSource(
    val symbol: String,
    val quote: StockResponse,
    val source: QuoteSource,
    val userSafeReason: String? = null,
    val diagnostics: String? = null
)
