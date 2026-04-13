package com.rivavafi.universal.data.network

import retrofit2.http.GET
import retrofit2.http.Query

data class YahooQuoteResponse(
    val quoteResponse: YahooQuoteResultWrapper?
)

data class YahooQuoteResultWrapper(
    val result: List<YahooQuoteItem>?
)

data class YahooQuoteItem(
    val symbol: String?,
    val regularMarketPrice: Double?,
    val regularMarketChangePercent: Double?
)

interface YahooFinanceApi {
    @GET("v7/finance/quote")
    suspend fun getQuotes(
        @Query("symbols") symbols: String
    ): YahooQuoteResponse
}
