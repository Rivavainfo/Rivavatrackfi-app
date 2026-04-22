import re

with open("app/src/main/java/com/rivavafi/universal/ui/auth/AuthViewModel.kt", "r") as f:
    content = f.read()

# Replace onEmailRegister logic
old_register = """    fun onEmailRegister(email: String, pass: String, name: String, onVerificationSent: () -> Unit) {
        if (email.isBlank() || pass.isBlank()) {
            Log.w("AuthViewModel", "Email register rejected: Email or password was blank")
            _errorMessage.value = "Email and Password cannot be empty."
            _authState.value = AuthState.IDLE
            return
        }
        if (pass.length < 6) {
            Log.w("AuthViewModel", "Email register rejected: Password too short")
            _errorMessage.value = "Password must be at least 6 characters."
            _authState.value = AuthState.IDLE
            return
        }
        viewModelScope.launch {
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
        }
    }"""

new_register = """    fun onEmailRegister(email: String, pass: String, name: String, onVerificationSent: () -> Unit) {
        if (email.isBlank() || pass.isBlank()) {
            Log.w("AuthViewModel", "Email register rejected: Email or password was blank")
            _errorMessage.value = "Email and Password cannot be empty."
            _authState.value = AuthState.IDLE
            return
        }
        if (pass.length < 6) {
            Log.w("AuthViewModel", "Email register rejected: Password too short")
            _errorMessage.value = "Password must be at least 6 characters."
            _authState.value = AuthState.IDLE
            return
        }
        viewModelScope.launch {
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
        }
    }"""

content = content.replace(old_register, new_register)

# Update onEmailLogin
old_login = """    fun onEmailLogin(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            Log.w("AuthViewModel", "Email login rejected: Email or password was blank")
            _errorMessage.value = "Email and Password cannot be empty."
            _authState.value = AuthState.IDLE
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.LOADING
            try {
                Log.d("AuthViewModel", "Attempting email login for: $email")
                repository.auth.signInWithEmailAndPassword(email, pass).await()
                Log.d("AuthViewModel", "Email login successful")

                repository.auth.currentUser?.reload()?.await()

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
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Email login failed", e)
                _errorMessage.value = e.message ?: "Login failed"
                _authState.value = AuthState.IDLE
            }
        }
    }"""

new_login = """    fun onEmailLogin(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            Log.w("AuthViewModel", "Email login rejected: Email or password was blank")
            _errorMessage.value = "Email and Password cannot be empty."
            _authState.value = AuthState.IDLE
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.LOADING
            try {
                Log.d("AuthViewModel", "Attempting email login for: $email")
                repository.auth.signInWithEmailAndPassword(email, pass).await()
                Log.d("AuthViewModel", "Email login successful")

                val uid = repository.auth.currentUser?.uid ?: throw Exception("Failed to retrieve UID")
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
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Email login failed", e)
                _errorMessage.value = e.message ?: "Login failed"
                _authState.value = AuthState.IDLE
            }
        }
    }"""

content = content.replace(old_login, new_login)

with open("app/src/main/java/com/rivavafi/universal/ui/auth/AuthViewModel.kt", "w") as f:
    f.write(content)
