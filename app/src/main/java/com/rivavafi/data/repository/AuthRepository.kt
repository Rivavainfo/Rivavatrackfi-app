package com.rivavafi.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.rivavafi.data.network.GoogleAppsScriptApi
import com.rivavafi.data.network.UserAuthData
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val api: GoogleAppsScriptApi
) {
    val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

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
            "provider" to provider,
            "created_at" to System.currentTimeMillis().toString()
        )

        try {
            firestore.collection("users").document(uid).set(userData).await()

            // Post to Google Apps Script
            val scriptData = UserAuthData(
                name = name,
                email = email,
                phone = phone,
                uid = uid,
                verifiedStatus = "true",
                timestamp = System.currentTimeMillis().toString()
            )
            try {
                api.saveUserData(scriptData)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    fun logout() {
        auth.signOut()
    }
}
