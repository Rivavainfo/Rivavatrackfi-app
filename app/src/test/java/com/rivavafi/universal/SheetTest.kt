package com.rivavafi.universal

import com.rivavafi.universal.data.network.GoogleAppsScriptApi
import com.rivavafi.universal.data.network.UserAuthData
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun main() {
    val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
    val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    val api = Retrofit.Builder()
        .baseUrl("https://script.google.com/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(GoogleAppsScriptApi::class.java)

    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val currentTimestamp = dateFormat.format(Date())

    val sheetData = UserAuthData(
        name = "Jules Test Run",
        email = "jules.test@example.com",
        phone = "+15550000000",
        uid = "test-uid-jules",
        verifiedStatus = "test_run",
        timestamp = currentTimestamp
    )

    runBlocking {
        println("Sending test data to Google Sheets...")
        try {
            val response = api.saveUserData(sheetData)
            if (response.isSuccessful) {
                println("SUCCESS! Login test run is successful and connected to the sheet.")
            } else {
                println("FAILED. Response code: ${response.code()}")
            }
        } catch (e: Exception) {
            println("ERROR making request: ${e.message}")
            e.printStackTrace()
        }
    }
}
