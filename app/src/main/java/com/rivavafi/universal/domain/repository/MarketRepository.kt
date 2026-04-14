package com.rivavafi.universal.domain.repository

import com.rivavafi.universal.data.network.YahooFinanceApi
import com.rivavafi.universal.data.network.YahooStock
import javax.inject.Inject
import javax.inject.Singleton
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Singleton
class MarketRepository @Inject constructor() {
    private val api = Retrofit.Builder()
        .baseUrl("https://query1.finance.yahoo.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(YahooFinanceApi::class.java)

    private val cache = mutableMapOf<String, YahooStock>()

    suspend fun getStocks(): List<YahooStock> {
        try {
            val response = api.getStocks("RTX,IREDA.NS")

            if (response.quoteResponse?.result?.isNotEmpty() == true) {
                response.quoteResponse.result.forEach {
                    if (it.regularMarketPrice != null) {
                        cache[it.symbol] = it
                    }
                }
                return response.quoteResponse.result
            }

        } catch (e: Exception) {
            // ignore
        }

        // fallback
        return cache.values.toList()
    }
}
