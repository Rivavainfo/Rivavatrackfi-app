package com.trackfi.data.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

interface YahooFinanceApi {
    @GET("v8/finance/chart/IREDA.NS")
    suspend fun getIredaStockData(
        @Query("range") range: String = "1d",
        @Query("interval") interval: String = "1m"
    ): YahooFinanceResponse
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
