package com.rivavafi.universal.domain.api

import retrofit2.http.GET
import retrofit2.http.Query

interface CryptoApiService {
    @GET("api/v3/simple/price")
    suspend fun getSimplePrices(
        @Query("ids") ids: String,
        @Query("vs_currencies") vsCurrencies: String = "usd",
        @Query("include_24hr_change") include24hrChange: Boolean = true
    ): Map<String, Map<String, Double>>
}
