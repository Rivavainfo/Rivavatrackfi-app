package com.rivavafi.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.rivavafi.data.network.GoogleAppsScriptApi
import com.rivavafi.data.network.UserAuthData
import kotlinx.coroutines.tasks.await
import android.util.Log
import javax.inject.Inject

class AuthRepository @Inject constructor() {
class AuthRepository @Inject constructor(
    private val api: GoogleAppsScriptApi
) {
    companion object {
        private const val TAG = "AuthRepository"
    }

    val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun saveUserToFirestore(uid: String, name: String, email: String) {
        val userData = hashMapOf(
            "uid" to uid,
            "name" to name,
            "email" to email,
            "timestamp" to System.currentTimeMillis().toString()
        )

        try {
            firestore.collection("users").document(uid).set(userData).await()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
            Log.e(TAG, "Failed to sync auth data to Apps Script", e)
        }
    }

    suspend fun saveUserToFirestore(
        uid: String,
        name: String,
        email: String,
        phone: String,
        photoUrl: String,
        loginMethod: String
    ) {
        val userDocRef = firestore.collection("users").document(uid)
        val existingSnapshot = userDocRef.get().await()

        val userPayload = mapOf(
            "uid" to uid,
            "name" to name,
            "email" to email,
            "phone" to phone,
            "photoUrl" to photoUrl,
            "loginMethod" to loginMethod,
            "createdAt" to if (existingSnapshot.exists()) {
                existingSnapshot.get("createdAt") ?: FieldValue.serverTimestamp()
            } else {
                FieldValue.serverTimestamp()
            }
        )
        userDocRef
            .set(userPayload)
            .await()
        Log.d(TAG, "User profile stored in Firestore for uid=$uid")
    }
}
