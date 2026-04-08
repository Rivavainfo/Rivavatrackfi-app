package com.rivavafi.universal.data.repository

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepository @Inject constructor() {
    val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getGoogleCredentialRequest(webClientId: String): GetCredentialRequest {
        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(false)
            .build()

        return GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
    }

    suspend fun getCredentialManager(context: Context): CredentialManager {
        return CredentialManager.create(context)
    }

    suspend fun saveUserToFirestore(uid: String, name: String, email: String) {
        val userData = hashMapOf<String, Any>(
            "uid" to uid,
            "name" to name,
            "email" to email
        )

        try {
            val docRef = firestore.collection("users").document(uid)
            val docSnap = docRef.get().await()

            if (!docSnap.exists()) {
                userData["timestamp"] = System.currentTimeMillis().toString()
                docRef.set(userData).await()
            } else {
                docRef.set(userData, SetOptions.merge()).await()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}
