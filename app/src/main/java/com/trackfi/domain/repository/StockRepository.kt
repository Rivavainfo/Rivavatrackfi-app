package com.trackfi.domain.repository

import com.trackfi.domain.api.FinnhubCompanyProfileResponse
import com.trackfi.domain.api.FinnhubNewsResponse
import com.trackfi.domain.api.FinnhubQuoteResponse
import com.trackfi.domain.api.StockApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Singleton
class StockRepository @Inject constructor(
    private val apiService: StockApiService
) {
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

    fun getCompanyProfile(symbol: String): Flow<Result<FinnhubCompanyProfileResponse>> = flow {
        if (API_KEY.isBlank()) {
            emit(Result.failure(Exception("Missing API Key")))
            return@flow
        }
        try {
            val response = apiService.getCompanyProfile(symbol, API_KEY)
            emit(Result.success(response))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun getMarketNews(): Flow<Result<List<FinnhubNewsResponse>>> = flow {
        if (API_KEY.isBlank()) {
            emit(Result.failure(Exception("Missing API Key")))
            return@flow
        }
        try {
            val response = apiService.getMarketNews("general", API_KEY)
            emit(Result.success(response.take(10)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun getCompanyNews(symbol: String): Flow<Result<List<FinnhubNewsResponse>>> = flow {
        if (API_KEY.isBlank()) {
            emit(Result.failure(Exception("Missing API Key")))
            return@flow
        }
        try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val today = format.format(Date())
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -14)
            val twoWeeksAgo = format.format(cal.time)

            val response = apiService.getCompanyNews(symbol, twoWeeksAgo, today, API_KEY)
            emit(Result.success(response.take(5)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
