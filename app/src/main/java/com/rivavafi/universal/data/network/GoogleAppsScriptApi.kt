package com.rivavafi.universal.data.network

import retrofit2.http.Body
import retrofit2.http.POST

data class UserAuthData(
    val name: String,
    val email: String,
    val phone: String,
    val uid: String,
    val verifiedStatus: String,
    val timestamp: String
)

interface GoogleAppsScriptApi {
    @POST("macros/s/AKfycbx2EmeSjsbcD_bGTZQBmG7xwhUBEdvjL33k4GqqcH8lv-b4mmzzjAOtZt7FwQksVvhF/exec")
    suspend fun saveUserData(@Body userData: UserAuthData): retrofit2.Response<Void>
}
