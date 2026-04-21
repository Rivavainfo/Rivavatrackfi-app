package com.rivavafi.universal.domain.repository

import com.rivavafi.universal.BuildConfig
import com.rivavafi.universal.domain.api.StockResponse
import com.rivavafi.universal.domain.api.CryptoApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CryptoRepository @Inject constructor(
    private val apiService: CryptoApiService
) {
    private val apiKey = BuildConfig.FINNHUB_API_KEY

    fun getCryptoQuote(cryptoId: String): Flow<Result<StockResponse>> = flow {
        if (apiKey.isBlank()) {
            emit(Result.failure(Exception("Missing API Key")))
            return@flow
        }

        val symbol = when (cryptoId.lowercase()) {
            "bitcoin" -> "BINANCE:BTCUSDT"
            "ethereum" -> "BINANCE:ETHUSDT"
            "solana" -> "BINANCE:SOLUSDT"
            else -> "BINANCE:BTCUSDT"
        }

        try {
            val response = apiService.getCryptoQuote(symbol, apiKey)
            emit(Result.success(response))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
