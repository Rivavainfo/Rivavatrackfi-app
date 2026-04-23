package com.rivavafi.universal.domain.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

data class SendVerificationRequest(
    val email: String,
    val uid: String
)

data class VerificationStatusResponse(
    val isVerified: Boolean
)

interface AuthApiService {
    @POST("sendVerificationEmail")
    suspend fun sendVerificationEmail(@Body request: SendVerificationRequest): Response<Unit>

    @GET("checkVerification")
    suspend fun checkVerification(@Query("uid") uid: String): Response<VerificationStatusResponse>
}
