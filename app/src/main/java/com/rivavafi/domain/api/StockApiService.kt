package com.rivavafi.domain.api

import retrofit2.http.GET
import retrofit2.http.Query

data class FinnhubQuoteResponse(
    val c: Double, // Current price
    val d: Double, // Change
    val dp: Double, // Percent change
    val h: Double, // High price of the day
    val l: Double, // Low price of the day
    val o: Double, // Open price of the day
    val pc: Double // Previous close price
)

data class FinnhubCompanyProfileResponse(
    val name: String?,
    val ticker: String?,
    val logo: String?,
    val finnhubIndustry: String?,
    val marketCapitalization: Double?
)

data class FinnhubNewsResponse(
    val id: Long,
    val headline: String,
    val summary: String,
    val url: String,
    val image: String,
    val source: String,
    val datetime: Long
)

interface StockApiService {
    @GET("api/v1/quote")
    suspend fun getQuote(
        @Query("symbol") symbol: String,
        @Query("token") apiKey: String
    ): FinnhubQuoteResponse

    @GET("api/v1/stock/profile2")
    suspend fun getCompanyProfile(
        @Query("symbol") symbol: String,
        @Query("token") apiKey: String
    ): FinnhubCompanyProfileResponse

    @GET("api/v1/news")
    suspend fun getMarketNews(
        @Query("category") category: String = "general",
        @Query("token") apiKey: String
    ): List<FinnhubNewsResponse>

    @GET("api/v1/company-news")
    suspend fun getCompanyNews(
        @Query("symbol") symbol: String,
        @Query("from") fromDate: String,
        @Query("to") toDate: String,
        @Query("token") apiKey: String
    ): List<FinnhubNewsResponse>
}
