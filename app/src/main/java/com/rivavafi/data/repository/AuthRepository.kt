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

    suspend fun saveUserDataToSheet(name: String, email: String, phone: String, uid: String) {
        val data = UserAuthData(
            name = name,
            email = email,
            phone = phone,
            uid = uid,
            verifiedStatus = "true",
            timestamp = System.currentTimeMillis().toString()
        )
        try {
            api.saveUserData(data)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun saveUserToFirestore(
        uid: String,
        name: String,
        email: String
    ) {
        val userPayload = mapOf(
            "uid" to uid,
            "name" to name,
            "email" to email,
            "provider" to "google",
            "updatedAt" to com.google.firebase.Timestamp.now()
        )
        firestore.collection("users")
            .document(uid)
            .set(userPayload)
            .await()
    }
}
