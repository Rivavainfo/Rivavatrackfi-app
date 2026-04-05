package com.rivavafi.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepository @Inject constructor() {
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
        }
    }
}
