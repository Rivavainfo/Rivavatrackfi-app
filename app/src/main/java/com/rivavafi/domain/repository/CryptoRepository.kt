package com.rivavafi.domain.repository

import com.rivavafi.domain.api.CryptoApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CryptoRepository @Inject constructor(
    private val apiService: CryptoApiService
) {
    fun getSimplePrices(ids: String): Flow<Result<Map<String, Map<String, Double>>>> = flow {
        try {
            val response = apiService.getSimplePrices(ids)
            emit(Result.success(response))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
