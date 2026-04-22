import re

with open("app/src/main/java/com/rivavafi/universal/ui/auth/AuthViewModel.kt", "r") as f:
    content = f.read()

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

with open("app/src/main/java/com/rivavafi/universal/ui/auth/AuthViewModel.kt", "w") as f:
    f.write(content)
