package com.rivavafi.universal.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.rivavafi.universal.domain.api.AuthApiService
import com.rivavafi.universal.domain.api.SendVerificationRequest
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

class AuthRepository @Inject constructor(
    private val authApiService: AuthApiService
) {
    val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun sendUserToSheet(user: FirebaseUser, provider: String, name: String? = null) {
        val client = OkHttpClient()

        val json = JSONObject()
        json.put("name", name ?: user.displayName ?: "")
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

    suspend fun saveUserToFirestore(
        uid: String,
        name: String?,
        email: String?,
        phoneNumber: String?,
        authProvider: String,
        isVerified: Boolean = false
    ): Pair<Boolean, String?> {
        val userData = hashMapOf<String, Any?>()
        userData["uid"] = uid
        userData["name"] = name?.takeIf { it.isNotBlank() }
        userData["email"] = email?.takeIf { it.isNotBlank() }
        userData["phoneNumber"] = phoneNumber?.takeIf { it.isNotBlank() }
        userData["authProvider"] = authProvider
        userData["isVerified"] = isVerified
        userData["updatedAt"] = com.google.firebase.firestore.FieldValue.serverTimestamp()

        return runCatching {
            val docRef = firestore.collection("users").document(uid)
            val docSnap = docRef.get().await()
            val existingName = docSnap.getString("name").orEmpty()
            val authProfileName = Firebase.auth.currentUser?.displayName.orEmpty()

            val isNewUser = !docSnap.exists() || existingName.isBlank()
            if (!docSnap.exists()) {
                userData["createdAt"] = com.google.firebase.firestore.FieldValue.serverTimestamp()
                docRef.set(userData).await()
            } else {
                docRef.set(userData.filterValues { it != null }, SetOptions.merge()).await()
            }
            Pair(isNewUser, docSnap.getString("name"))
        }.getOrElse { firestoreError ->
            Log.w("AuthRepository", "Firestore sync failed. Continuing authenticated session.", firestoreError)
            Pair(false, null) // Default to treating them as returning if Firestore fails, to not force onboarding
        }
    }

    suspend fun sendVerificationEmail(email: String, uid: String): Boolean {
        return try {
            val response = authApiService.sendVerificationEmail(SendVerificationRequest(email, uid))
            if (response.isSuccessful) {
                true
            } else {
                Log.w("AuthRepository", "API verification failed with ${response.code()}. Falling back to Firebase Auth.")
                auth.currentUser?.sendEmailVerification()?.await()
                true
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Failed to send verification email via API. Falling back to Firebase Auth.", e)
            try {
                auth.currentUser?.sendEmailVerification()?.await()
                true
            } catch (fallbackEx: Exception) {
                Log.e("AuthRepository", "Firebase Auth fallback failed", fallbackEx)
                false
            }
        }
    }

    suspend fun checkVerificationStatus(uid: String): Boolean {
        return try {
            val response = authApiService.checkVerification(uid)
            if (response.isSuccessful) {
                response.body()?.isVerified ?: false
            } else {
                Log.w("AuthRepository", "API check failed with ${response.code()}. Falling back to Firebase Auth.")
                auth.currentUser?.reload()?.await()
                auth.currentUser?.isEmailVerified == true
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Failed to check verification status via API. Falling back to Firebase Auth.", e)
            try {
                auth.currentUser?.reload()?.await()
                auth.currentUser?.isEmailVerified == true
            } catch (fallbackEx: Exception) {
                Log.e("AuthRepository", "Firebase Auth fallback failed", fallbackEx)
                false
            }
        }
    }

    suspend fun checkEmailExists(email: String): Boolean {
        return try {
            val result = auth.fetchSignInMethodsForEmail(email).await()
            !result.signInMethods.isNullOrEmpty()
        } catch (e: Exception) {
            Log.e("AuthRepository", "Failed to check if email exists", e)
            false
        }
    }
}
