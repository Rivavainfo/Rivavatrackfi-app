package com.trackfi.domain.repository

import com.trackfi.domain.api.FinnhubQuoteResponse
import com.trackfi.domain.api.StockApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockRepository @Inject constructor(
    private val apiService: StockApiService
) {
    // API key should ideally be injected or from BuildConfig. For safety & demo, if an empty string
    // is passed or if the user doesn't have a key, we fall back to emitting mock data.
    private val API_KEY = com.trackfi.BuildConfig.FINNHUB_API_KEY

    fun getRealtimeQuote(symbol: String): Flow<Result<FinnhubQuoteResponse>> = flow {
        if (API_KEY.isBlank()) {
            emit(Result.failure(Exception("Missing API Key")))
            return@flow
        }
        try {
            val response = apiService.getQuote(symbol, API_KEY)
            emit(Result.success(response))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
