package com.rivavafi.universal.data.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

interface YahooFinanceApi {
    @GET("v8/finance/chart/IREDA.NS")
    suspend fun getIredaStockData(
        @Query("range") range: String = "1d",
        @Query("interval") interval: String = "1m"
    ): YahooFinanceResponse

    @GET("v7/finance/quote")
    suspend fun getStocks(
        @Query("symbols") symbols: String
    ): YahooResponse
}

data class YahooFinanceResponse(
    val chart: Chart?
)

data class Chart(
    val result: List<Result>?
)

data class Result(
    val meta: Meta?
)

data class Meta(
    val regularMarketPrice: Double?,
    val previousClose: Double?
)

data class YahooResponse(
    val quoteResponse: QuoteResponse?
)

data class QuoteResponse(
    val result: List<YahooStock>?
)

data class YahooStock(
    val symbol: String,
    val regularMarketPrice: Double?,
    val regularMarketChangePercent: Double?
)
