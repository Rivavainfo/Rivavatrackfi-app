package com.rivavafi.universal.domain.repository

import com.rivavafi.universal.BuildConfig
import com.rivavafi.universal.domain.api.Quote
import com.rivavafi.universal.domain.api.StockApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlphaVantageRepository @Inject constructor() {

    private val api = Retrofit.Builder()
        .baseUrl("https://www.alphavantage.co/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(StockApi::class.java)

    private val cache = mutableMapOf<String, Quote>()

    private val API_KEY = BuildConfig.ALPHA_VANTAGE_API_KEY

    suspend fun getStock(symbol: String): Quote? {
        val requestSymbols = candidateSymbols(symbol)

        for (requestSymbol in requestSymbols) {
            try {
                val response = api.getStockPrice(symbol = requestSymbol, apiKey = API_KEY)
                val data = response.quote

                if (data != null && data.price != null) {
                    val resolved = data.copy(symbol = symbol)
                    cache[symbol] = resolved
                    return resolved
                }
            } catch (_: Exception) {
                // try next candidate
            }
        }

        return cache[symbol]
    }

    private fun candidateSymbols(symbol: String): List<String> {
        return when (symbol.uppercase()) {
            "IREDA.BSE", "IREDA", "IREDA.NS" -> listOf("IREDA.BSE", "IREDA.NSE", "IREDA.NS")
            "RTX" -> listOf("RTX", "NYSE:RTX")
            else -> listOf(symbol)
        }
    }
}
