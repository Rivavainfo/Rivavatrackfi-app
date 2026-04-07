package com.rivavafi.universal

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

object AuthHelper {
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
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}
