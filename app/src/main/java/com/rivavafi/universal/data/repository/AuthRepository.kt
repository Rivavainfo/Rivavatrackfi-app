package com.rivavafi.universal.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject

class AuthRepository @Inject constructor() {
    val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun sendUserToSheet(user: FirebaseUser, provider: String) {
        val client = OkHttpClient()

        val json = JSONObject()
        json.put("name", user.displayName ?: "")
        json.put("email", user.email ?: "")
        json.put("phone", user.phoneNumber ?: "")
        json.put("uid", user.uid)
        json.put("provider", provider)

        val body = RequestBody.create(
            "application/json".toMediaTypeOrNull(),
            json.toString()
        )

        val request = Request.Builder()
            .url("https://script.google.com/macros/s/AKfycbx2EmeSjsbcD_bGTZQBmG7xwhUBEdvjL33k4GqqcH8lv-b4mmzzjAOtZt7FwQksVvhF/exec")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("AuthRepository", "Failed to send user to sheet", e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d("AuthRepository", "Successfully sent user to sheet")
                } else {
                    Log.e("AuthRepository", "Failed to send user to sheet. Code: ${response.code}")
                }
                response.close()
            }
        })
    }

    suspend fun saveUserToFirestore(uid: String, name: String, email: String): Boolean {
        val userData = hashMapOf<String, Any>(
            "uid" to uid,
            "name" to name,
            "email" to email
        )

        return runCatching {
            val docRef = firestore.collection("users").document(uid)
            val docSnap = docRef.get().await()

            val isNewUser = !docSnap.exists()
            if (isNewUser) {
                userData["timestamp"] = System.currentTimeMillis().toString()
                docRef.set(userData).await()
            } else {
                docRef.set(userData, SetOptions.merge()).await()
            }
            isNewUser
        }.getOrElse { firestoreError ->
            Log.w("AuthRepository", "Firestore sync failed. Continuing authenticated session.", firestoreError)
            false // Default to treating them as returning if Firestore fails, to not force onboarding
        }
    }
}
