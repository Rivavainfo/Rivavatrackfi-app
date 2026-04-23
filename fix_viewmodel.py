import re

with open("app/src/main/java/com/rivavafi/universal/ui/auth/AuthViewModel.kt", "r") as f:
    content = f.read()

# Replace onEmailRegister logic
old_register = """        viewModelScope.launch {
            _authState.value = AuthState.LOADING
            try {
                Log.d("AuthViewModel", "Attempting email registration for: $email")
                val result = repository.auth.createUserWithEmailAndPassword(email, pass).await()
                val uid = result.user?.uid ?: throw Exception("Failed to retrieve UID")

                Log.d("AuthViewModel", "Registration successful. Saving to Firestore and Sheets...")
                repository.saveUserToFirestore(
                    uid = uid,
                    name = name.ifBlank { "Email User" },
                    email = email
                )
                userPreferencesRepository.saveUserName(name)

                val settings = com.google.firebase.auth.ActionCodeSettings.newBuilder()
                    .setUrl("https://rivava.in/verify")
                    .setHandleCodeInApp(true)
                    .build()

                result.user?.sendEmailVerification(settings)?.await()

                _errorMessage.value = "Registration successful. Please verify your email."
                _authState.value = AuthState.IDLE
                onVerificationSent()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Email registration failed", e)
                _errorMessage.value = e.message ?: "Registration failed"
                _authState.value = AuthState.IDLE
            }
        }"""

new_register = """        viewModelScope.launch {
            _authState.value = AuthState.LOADING
            if (repository.checkEmailExists(email)) {
                _errorMessage.value = "Email is already registered. Please sign in."
                _authState.value = AuthState.IDLE
                return@launch
            }

            try {
                Log.d("AuthViewModel", "Attempting email registration for: $email")
                val result = repository.auth.createUserWithEmailAndPassword(email, pass).await()
                val uid = result.user?.uid ?: throw Exception("Failed to retrieve UID")

                Log.d("AuthViewModel", "Registration successful. Saving to Firestore and Sheets...")
                repository.saveUserToFirestore(
                    uid = uid,
                    name = name.ifBlank { "Email User" },
                    email = email,
                    isVerified = false
                )
                userPreferencesRepository.saveUserName(name)

                repository.sendVerificationEmail(email, uid)

                _errorMessage.value = "Registration successful. Please check your inbox to verify."
                _authState.value = AuthState.IDLE
                onVerificationSent()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Email registration failed", e)
                _errorMessage.value = e.message ?: "Registration failed"
                _authState.value = AuthState.IDLE
            }
        }"""

content = content.replace(old_register, new_register)

# Replace checkEmailVerified logic
old_check = """    fun checkEmailVerified(onVerified: () -> Unit, onNotVerified: () -> Unit) {
        viewModelScope.launch {
            _authState.value = AuthState.LOADING
            try {
                repository.auth.currentUser?.reload()?.await()
                if (repository.auth.currentUser?.isEmailVerified == true) {
                    val firestoreNew = repository.saveUserToFirestore(
                        uid = repository.auth.currentUser?.uid ?: "",
                        name = "Email User",
                        email = repository.auth.currentUser?.email ?: ""
                    )
                    val firebaseNew = repository.auth.currentUser?.displayName.isNullOrBlank()
                    val isNew = firestoreNew || firebaseNew
                    _isNewUser.value = isNew
                    if (isNew) {
                        repository.auth.currentUser?.let { repository.sendUserToSheet(it, "Email") }
                    }
                    _authState.value = AuthState.SUCCESS
                    onVerified()
                } else {
                    _errorMessage.value = "Email not verified yet. Please check your inbox."
                    _authState.value = AuthState.IDLE
                    onNotVerified()
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Failed to check email verification", e)
                _errorMessage.value = "Failed to verify email status: ${e.message}"
                _authState.value = AuthState.IDLE
                onNotVerified()
            }
        }
    }"""

new_check = """    fun checkEmailVerified(onVerified: () -> Unit, onNotVerified: () -> Unit) {
        viewModelScope.launch {
            _authState.value = AuthState.LOADING
            try {
                val uid = repository.auth.currentUser?.uid
                if (uid != null) {
                    val isVerified = repository.checkVerificationStatus(uid)
                    if (isVerified) {
                        val firestoreNew = repository.saveUserToFirestore(
                            uid = uid,
                            name = "Email User",
                            email = repository.auth.currentUser?.email ?: "",
                            isVerified = true
                        )
                        val firebaseNew = repository.auth.currentUser?.displayName.isNullOrBlank()
                        val isNew = firestoreNew || firebaseNew
                        _isNewUser.value = isNew
                        if (isNew) {
                            repository.auth.currentUser?.let { repository.sendUserToSheet(it, "Email") }
                        }
                        _authState.value = AuthState.SUCCESS
                        onVerified()
                    } else {
                        _errorMessage.value = "Email not verified yet. Please check your inbox."
                        _authState.value = AuthState.IDLE
                        onNotVerified()
                    }
                } else {
                    _errorMessage.value = "Failed to get user. Please try logging in again."
                    _authState.value = AuthState.IDLE
                    onNotVerified()
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Failed to check email verification", e)
                _errorMessage.value = "Failed to verify email status: ${e.message}"
                _authState.value = AuthState.IDLE
                onNotVerified()
            }
        }
    }"""

content = content.replace(old_check, new_check)

# Update onEmailLogin
old_login = """                repository.auth.currentUser?.reload()?.await()

                if (repository.auth.currentUser?.isEmailVerified == true) {
                    val uid = repository.auth.currentUser?.uid ?: throw Exception("Failed to retrieve UID")
                    val firestoreNew = repository.saveUserToFirestore(uid, "Email User", email)
                    val firebaseNew = repository.auth.currentUser?.displayName.isNullOrBlank()
                    val isNew = firestoreNew || firebaseNew
                    _isNewUser.value = isNew
                    if (isNew) {
                        repository.auth.currentUser?.let { repository.sendUserToSheet(it, "Email") }
                    }
                    _authState.value = AuthState.SUCCESS
                } else {
                    repository.auth.signOut()
                    _errorMessage.value = "Please verify your email before logging in."
                    _authState.value = AuthState.IDLE
                }"""

new_login = """                val uid = repository.auth.currentUser?.uid ?: throw Exception("Failed to retrieve UID")
                val isVerified = repository.checkVerificationStatus(uid)
                if (isVerified) {
                    val firestoreNew = repository.saveUserToFirestore(uid, "Email User", email, isVerified = true)
                    val firebaseNew = repository.auth.currentUser?.displayName.isNullOrBlank()
                    val isNew = firestoreNew || firebaseNew
                    _isNewUser.value = isNew
                    if (isNew) {
                        repository.auth.currentUser?.let { repository.sendUserToSheet(it, "Email") }
                    }
                    _authState.value = AuthState.SUCCESS
                } else {
                    repository.auth.signOut()
                    _errorMessage.value = "Please verify your email before logging in."
                    _authState.value = AuthState.IDLE
                }"""

content = content.replace(old_login, new_login)

# Add resend function
resend_func = """
    fun resendVerificationEmail() {
        viewModelScope.launch {
            _authState.value = AuthState.LOADING
            try {
                val user = repository.auth.currentUser
                if (user != null && user.email != null) {
                    repository.sendVerificationEmail(user.email!!, user.uid)
                    _errorMessage.value = "Verification email resent successfully."
                } else {
                    _errorMessage.value = "User not found. Please log in again."
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Failed to resend verification email", e)
                _errorMessage.value = "Failed to resend verification email."
            } finally {
                _authState.value = AuthState.IDLE
            }
        }
    }
"""
content = content + resend_func

with open("app/src/main/java/com/rivavafi/universal/ui/auth/AuthViewModel.kt", "w") as f:
    f.write(content)
