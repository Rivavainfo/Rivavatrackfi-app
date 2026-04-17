package com.rivavafi.universal.data.repository

import com.rivavafi.universal.data.network.YahooFinanceApi
import com.rivavafi.universal.data.network.YahooStock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YahooStockRepository @Inject constructor(
    private val api: YahooFinanceApi
) {
    val cache = mutableMapOf<String, YahooStock>()

    suspend fun getStocks(): List<YahooStock> {
        return try {
            val response = api.getStockQuotes("RTX,IREDA.NS")
            if (response.quoteResponse.result.isNotEmpty()) {
                response.quoteResponse.result.forEach {
                    if (it.regularMarketPrice != null) {
                        cache[it.symbol] = it
                    }
                }
                response.quoteResponse.result
            } else {
                cache.values.toList()
            }
        } catch (e: Exception) {
            cache.values.toList()
        }
    }
}
