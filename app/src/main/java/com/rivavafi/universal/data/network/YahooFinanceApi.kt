package com.rivavafi.universal.data.network

import retrofit2.http.GET
import retrofit2.http.Query

interface YahooFinanceApi {
    @GET("v8/finance/chart/IREDA.NS")
    suspend fun getIredaStockData(
        @Query("range") range: String = "1d",
        @Query("interval") interval: String = "1m"
    ): YahooFinanceResponse

    @GET("v7/finance/quote")
    suspend fun getFallbackQuotes(
        @Query("symbols") symbols: String
    ): YahooQuoteResponse
}

data class YahooFinanceResponse(
    val chart: Chart?
)

data class Chart(
    val result: List<Result>?
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

data class YahooQuoteResponse(
    val quoteResponse: QuoteResponseObj?
)

data class QuoteResponseObj(
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
