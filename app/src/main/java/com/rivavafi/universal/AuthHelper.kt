package com.rivavafi.universal

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.rivavafi.universal.data.network.GoogleAppsScriptApi
import com.rivavafi.universal.data.network.UserAuthData
import kotlinx.coroutines.tasks.await
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AuthHelper {
    private val firestore = FirebaseFirestore.getInstance()

    private val googleAppsScriptApi: GoogleAppsScriptApi by lazy {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()

        Retrofit.Builder()
            .baseUrl("https://script.google.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GoogleAppsScriptApi::class.java)
    }

    suspend fun saveUserToFirestore(
        uid: String,
        name: String,
        email: String,
        phone: String,
        provider: String
    ) {
        val userData = hashMapOf(
            "uid" to uid,
            "name" to name,
            "email" to email,
            "phone" to phone,
            "provider" to provider
        )

        try {
            val documentRef = firestore.collection("users").document(uid)
            val documentSnapshot = documentRef.get().await()

            val mapToSave = userData.toMutableMap<String, Any>()

            if (!documentSnapshot.exists()) {
                mapToSave["created_at"] = System.currentTimeMillis()
                documentRef.set(mapToSave).await()
            } else {
                documentRef.set(mapToSave, SetOptions.merge()).await()
            }

            // Send data to Google Apps Script (Google Sheets)
            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val currentTimestamp = dateFormat.format(Date())

                val sheetData = UserAuthData(
                    name = name,
                    email = email,
                    phone = phone,
                    uid = uid,
                    verifiedStatus = provider,
                    timestamp = currentTimestamp
                )

                val response = googleAppsScriptApi.saveUserData(sheetData)
                if (!response.isSuccessful) {
                    Log.e("AuthHelper", "Failed to save to Google Sheets: ${response.code()}")
                } else {
                    Log.d("AuthHelper", "Successfully saved to Google Sheets")
                }
            } catch (sheetError: Exception) {
                Log.e("AuthHelper", "Error saving to Google Sheets", sheetError)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}
