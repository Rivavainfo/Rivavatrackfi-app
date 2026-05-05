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

    data class UserSessionState(
        val isNewUser: Boolean,
        val existingName: String?,
        val onboardingCompleted: Boolean,
        val photoUrl: String?
    )

    suspend fun saveUserToFirestore(
        uid: String,
        name: String?,
        email: String?,
        phoneNumber: String?,
        authProvider: String,
        isVerified: Boolean = false,
        photoUrl: String? = null
    ): UserSessionState {
        val userData = hashMapOf<String, Any?>()
        userData["uid"] = uid
        userData["google_uid"] = uid
        userData["auth_provider"] = authProvider
        name?.takeIf { it.isNotBlank() }?.let { userData["full_name"] = it }
        email?.takeIf { it.isNotBlank() }?.let { userData["email"] = it }
        phoneNumber?.takeIf { it.isNotBlank() }?.let { userData["phone_number"] = it }
        if (isVerified) {
            userData["phone_verified"] = true
            if (authProvider == "email") {
                userData["email_verified"] = true
            }
        }
        photoUrl?.takeIf { it.isNotBlank() }?.let { userData["photo_url"] = it }
        userData["updated_at"] = com.google.firebase.firestore.FieldValue.serverTimestamp()
        userData["last_login_at"] = com.google.firebase.firestore.FieldValue.serverTimestamp()

        return runCatching {
            val docRef = firestore.collection("users").document(uid)
            val docSnap = docRef.get().await()
            val existingName = docSnap.getString("full_name")
            val existingPhotoUrl = docSnap.getString("photo_url")

            val onboardingCompleted = docSnap.getBoolean("onboarding_completed") ?: false
            val isNewUser = !docSnap.exists() || !onboardingCompleted

            if (!docSnap.exists()) {
                userData["created_at"] = com.google.firebase.firestore.FieldValue.serverTimestamp()
                userData["onboarding_completed"] = false
                userData["auto_message_tracking_enabled"] = false
                docRef.set(userData).await()
                UserSessionState(true, null, false, photoUrl)
            } else {
                // Do not overwrite existing values with null/blanks
                if (existingName?.isNotBlank() == true) userData.remove("full_name")
                if (docSnap.getString("email")?.isNotBlank() == true) userData.remove("email")
                if (docSnap.getString("phone_number")?.isNotBlank() == true) userData.remove("phone_number")
                if (existingPhotoUrl?.isNotBlank() == true) userData.remove("photo_url")

                docRef.set(userData, SetOptions.merge()).await()
                UserSessionState(isNewUser, existingName, onboardingCompleted, existingPhotoUrl)
            }
        }.getOrElse { firestoreError ->
            Log.w("AuthRepository", "Firestore sync failed. Continuing authenticated session.", firestoreError)
            UserSessionState(false, null, true, null) // Default to treating them as returning if Firestore fails
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
