package com.rivavafi.universal.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
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
import javax.inject.Inject

class AuthRepository @Inject constructor() {
    val auth = FirebaseAuth.getInstance()
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

    suspend fun saveUserToFirestore(uid: String, name: String, email: String) {
        val userData = hashMapOf<String, Any>(
            "uid" to uid,
            "name" to name,
            "email" to email
        )

        runCatching {
            val docRef = firestore.collection("users").document(uid)
            val docSnap = docRef.get().await()

            if (!docSnap.exists()) {
                userData["timestamp"] = System.currentTimeMillis().toString()
                docRef.set(userData).await()
            } else {
                docRef.set(userData, SetOptions.merge()).await()
            }
        }.onFailure { firestoreError ->
            Log.w("AuthRepository", "Firestore sync failed. Continuing authenticated session.", firestoreError)
        }

        // Sync with Google Sheets
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val currentTimestamp = dateFormat.format(Date())

            val sheetData = UserAuthData(
                name = name,
                email = email,
                phone = "",
                uid = uid,
                verifiedStatus = "verified",
                timestamp = currentTimestamp
            )

            val response = googleAppsScriptApi.saveUserData(sheetData)
            if (response.isSuccessful) {
                Log.d("AuthRepository", "Successfully appended user to Google Sheet")
            } else {
                Log.e("AuthRepository", "Failed to append to Google Sheet: ${response.code()}")
            }
        } catch (sheetError: Exception) {
            Log.e("AuthRepository", "Error saving to Google Sheets", sheetError)
        }
    }
}
