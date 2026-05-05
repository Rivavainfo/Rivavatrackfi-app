import re

with open("app/src/main/java/com/rivavafi/universal/data/repository/AuthRepository.kt", "r") as f:
    content = f.read()

SEARCH = """    suspend fun saveUserToFirestore(
        uid: String,
        name: String?,
        email: String?,
        phoneNumber: String?,
        authProvider: String,
        isVerified: Boolean = false
    ): Pair<Boolean, String?> {"""

REPLACE = """    data class UserSessionState(
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
    ): UserSessionState {"""

content = content.replace(SEARCH, REPLACE)

SEARCH_BLOCK = """        val userData = hashMapOf<String, Any?>()
        userData["google_uid"] = uid
        userData["full_name"] = name?.takeIf { it.isNotBlank() }
        userData["email"] = email?.takeIf { it.isNotBlank() }
        userData["phone_number"] = phoneNumber?.takeIf { it.isNotBlank() }
        if (isVerified) {
            userData["phone_verified"] = true
        }
        userData["updated_at"] = com.google.firebase.firestore.FieldValue.serverTimestamp()

        return runCatching {
            val docRef = firestore.collection("users").document(uid)
            val docSnap = docRef.get().await()
            val existingName = docSnap.getString("full_name").orEmpty()

            val onboardingCompleted = docSnap.getBoolean("onboarding_completed") ?: false
            val isNewUser = !docSnap.exists() || !onboardingCompleted

            if (!docSnap.exists()) {
                userData["created_at"] = com.google.firebase.firestore.FieldValue.serverTimestamp()
                userData["onboarding_completed"] = false
                userData["auto_message_tracking_enabled"] = false
                docRef.set(userData).await()
            } else {
                docRef.set(userData.filterValues { it != null }, SetOptions.merge()).await()
            }
            Pair(isNewUser, docSnap.getString("full_name"))
        }.getOrElse { firestoreError ->
            Log.w("AuthRepository", "Firestore sync failed. Continuing authenticated session.", firestoreError)
            Pair(false, null) // Default to treating them as returning if Firestore fails, to not force onboarding
        }"""

REPLACE_BLOCK = """        val userData = hashMapOf<String, Any?>()
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
        }"""

content = content.replace(SEARCH_BLOCK, REPLACE_BLOCK)

with open("app/src/main/java/com/rivavafi/universal/data/repository/AuthRepository.kt", "w") as f:
    f.write(content)
