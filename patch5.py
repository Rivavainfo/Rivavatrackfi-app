import re
with open("app/src/main/java/com/rivavafi/universal/ui/onboarding/OnboardingViewModel.kt", "r") as f:
    content = f.read()

content = re.sub(r'val errorMessage: StateFlow<String\?> = _errorMessage.asStateFlow\(\)',
                 r'''val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var lastPhoneNumber: String? = null
''', content)

phone_logic = '''
    fun startPhoneVerification(phoneNumber: String) {
        lastPhoneNumber = phoneNumber
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = authRepository.sendOtp(phoneNumber)
                if (result.isSuccess) {
                    _isLoading.value = false
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to send OTP"
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error: unable to send OTP."
                _isLoading.value = false
            }
        }
    }

    fun verifyOtp(code: String, onSuccess: () -> Unit) {
        val phone = lastPhoneNumber ?: return
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = authRepository.verifyOtpAndSignIn(phone, code)
                if (result.isSuccess) {
                    saveUserToFirestoreAndComplete(onSuccess)
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Invalid OTP"
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error: unable to verify OTP."
                _isLoading.value = false
            }
        }
    }

    fun resendOtp() {
        lastPhoneNumber?.let { startPhoneVerification(it) }
    }

    private fun saveUserToFirestoreAndComplete(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val user = FirebaseAuth.getInstance().currentUser
                if (user == null) {
                    _errorMessage.value = "User not found"
                    _isLoading.value = false
                    return@launch
                }
                val uid = user.uid
                val email = user.email ?: ""
                val photoUrl = user.photoUrl?.toString() ?: ""
                val name = preferencesRepository.getUserName() ?: ""
                val phone = lastPhoneNumber ?: ""

                val userData = mapOf(
                    "uid" to uid,
                    "username" to name,
                    "email" to email,
                    "phoneNumber" to phone,
                    "preference" to "Not Selected",
                    "premiumStatus" to false,
                    "profileImage" to photoUrl,
                    "createdAt" to com.google.firebase.Timestamp.now(),
                    "lastLoginAt" to com.google.firebase.Timestamp.now(),
                    "authProvider" to "google"
                )

                firestore.collection("therivavadata").document(uid)
                    .set(userData, com.google.firebase.firestore.SetOptions.merge())
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            viewModelScope.launch {
                                preferencesRepository.setOnboardingCompleted(true)
                                _isLoading.value = false
                                onSuccess()
                            }
                        } else {
                            _errorMessage.value = task.exception?.message ?: "Failed to save data"
                            _isLoading.value = false
                        }
                    }

            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to complete onboarding"
                _isLoading.value = false
            }
        }
    }
'''
content = content.replace('    fun savePhoneNumber(phoneNumber: String, onSuccess: () -> Unit) {', phone_logic + '\n    fun savePhoneNumber(phoneNumber: String, onSuccess: () -> Unit) {')

with open("app/src/main/java/com/rivavafi/universal/ui/onboarding/OnboardingViewModel.kt", "w") as f:
    f.write(content)
