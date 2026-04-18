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
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class StockRepository @Inject constructor(
    private val apiService: StockApiService,
    @ApplicationContext context: Context
) {
    private val apiKey = BuildConfig.FINNHUB_API_KEY
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    suspend fun getGuaranteedQuote(symbol: String): StockQuoteWithSource {
        val normalizedSymbol = normalizeSymbol(symbol)
        val prefix = cachePrefix(normalizedSymbol)

        return try {
            if (apiKey.isBlank()) {
                Log.w(TAG, "Finnhub API key missing, skipping live fetch for $normalizedSymbol")
                resolveFromCacheOrDefault(normalizedSymbol, prefix, "missing_api_key")
            } else {
                val response = apiService.getQuote(normalizedSymbol, apiKey)
                val body = response.body()
                Log.d(TAG, "Quote response for $normalizedSymbol -> code=${response.code()} body=$body")

                if (response.isSuccessful && body != null && body.c > 0.0) {
                    saveQuote(prefix, body.c, body.pc)
                    StockQuoteWithSource(
                        symbol = normalizedSymbol,
                        quote = body,
                        source = QuoteSource.LIVE
                    )
                } else {
                    val errorMsg = "api_failed_or_invalid: ${response.code()}"
                    tryScrapeFallback(normalizedSymbol, prefix, errorMsg)
                }
            }
        } catch (exception: Exception) {
            Log.e(TAG, "Quote fetch failed for $normalizedSymbol, attempting scrape fallback", exception)
            tryScrapeFallback(normalizedSymbol, prefix, "exception: ${exception.message}")
        }
    }

    private suspend fun tryScrapeFallback(symbol: String, prefix: String, reason: String): StockQuoteWithSource {
        return withContext(Dispatchers.IO) {
            try {
                val scrapeUrl = if (symbol.equals(IREDA_SYMBOL, ignoreCase = true)) {
                    "https://www.google.com/finance/quote/IREDA:NSE"
                } else {
                    "https://www.google.com/finance/quote/RTX:NYSE"
                }
                val doc = Jsoup.connect(scrapeUrl).get()
                val priceStr = doc.select("div.YMlKec.fxKbKc").first()?.text()?.replace(Regex("[^0-9.]"), "")
                val prevCloseDiv = doc.select("div.P6K39c").find { it.text().contains("Previous close", ignoreCase = true) }
                val prevCloseStr = (prevCloseDiv?.nextElementSibling()?.text() ?: doc.select("div.gyFHrc:contains(Previous close) > div.P6K39c").first()?.text())?.replace(Regex("[^0-9.]"), "")

                val price = priceStr?.toDoubleOrNull()
                val prevClose = prevCloseStr?.toDoubleOrNull() ?: price

                if (price != null && price > 0.0 && prevClose != null) {
                    Log.d(TAG, "Scraped fallback succeeded for $symbol -> price=$price, pc=$prevClose")
                    saveQuote(prefix, price, prevClose)
                    StockQuoteWithSource(
                        symbol = symbol,
                        quote = StockResponse(c = price, h = price, l = price, o = prevClose, pc = prevClose),
                        source = QuoteSource.SCRAPE
                    )
                } else {
                    resolveFromCacheOrDefault(symbol, prefix, reason)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Scrape fallback failed for $symbol", e)
                resolveFromCacheOrDefault(symbol, prefix, reason)
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

    private fun resolveFromCacheOrDefault(symbol: String, prefix: String, reason: String): StockQuoteWithSource {
        val cachedPrice = preferences.getFloat("${prefix}_price", -1f).toDouble()
        val cachedPc = preferences.getFloat("${prefix}_pc", -1f).toDouble()

        if (cachedPrice > 0.0 && cachedPc > 0.0) {
            Log.d(TAG, "Using cached quote for $symbol because $reason")
            return StockQuoteWithSource(
                symbol = symbol,
                quote = StockResponse(c = cachedPrice, h = cachedPrice, l = cachedPrice, o = cachedPc, pc = cachedPc),
                source = QuoteSource.CACHE
            )
        }

        val defaultQuote = when (symbol) {
            IREDA_SYMBOL -> StockResponse(c = 150.0, h = 150.0, l = 150.0, o = 148.0, pc = 148.0)
            else -> StockResponse(c = 100.0, h = 100.0, l = 100.0, o = 99.0, pc = 99.0)
        }

        Log.d(TAG, "Using hard fallback quote for $symbol because $reason")
        return StockQuoteWithSource(symbol = symbol, quote = defaultQuote, source = QuoteSource.DEFAULT)
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
    val source: QuoteSource
)
