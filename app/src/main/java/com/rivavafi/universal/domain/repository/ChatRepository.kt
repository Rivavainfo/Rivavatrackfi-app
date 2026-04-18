package com.rivavafi.universal.domain.repository

import com.rivavafi.universal.domain.api.ChatApiService
import com.rivavafi.universal.domain.api.ChatRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val apiService: ChatApiService
) {
    fun sendMessage(message: String): Flow<Result<String>> = flow {
        try {
            val response = apiService.sendMessage(ChatRequest(message))
            emit(Result.success(response.reply))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
