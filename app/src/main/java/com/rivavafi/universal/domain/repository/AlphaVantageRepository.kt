package com.rivavafi.universal.domain.repository

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

    private val API_KEY = "1JCULNPFKQXWC62U"

    suspend fun getStock(symbol: String): Quote? {
        return try {
            val response = api.getStockPrice(symbol = symbol, apiKey = API_KEY)
            val data = response.quote

            if (data != null && data.price != null) {
                cache[symbol] = data
                data
            } else {
                cache[symbol]
            }

        } catch (e: Exception) {
            cache[symbol]
        }
    }
}
