package com.rivavafi.universal.ui.auth

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.GoogleAuthProvider
import com.rivavafi.universal.data.preferences.UserPreferencesRepository
import com.rivavafi.universal.data.repository.AuthRepository
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.FirebaseException
import java.util.concurrent.TimeUnit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class AuthFormState(
    val emailError: String? = null,
    val phoneError: String? = null,
    val isFormValid: Boolean = false
)

enum class AuthState {
    IDLE,
    LOADING,
    SUCCESS,
    ERROR
}

enum class PhoneAuthState {
    IDLE,
    CODE_SENT,
    SUCCESS,
    ERROR
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState.IDLE)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _phoneAuthState = MutableStateFlow(PhoneAuthState.IDLE)
    val phoneAuthState: StateFlow<PhoneAuthState> = _phoneAuthState.asStateFlow()

    private val _isNewUser = MutableStateFlow<Boolean?>(null)
    val isNewUser: StateFlow<Boolean?> = _isNewUser.asStateFlow()

    private val _authFormState = MutableStateFlow(AuthFormState())
    val authFormState: StateFlow<AuthFormState> = _authFormState.asStateFlow()

    private var _verificationId: String?
        get() = savedStateHandle.get("verificationId")
        set(value) { savedStateHandle["verificationId"] = value }

    private var _resendToken: PhoneAuthProvider.ForceResendingToken?
        get() = savedStateHandle.get("resendToken")
        set(value) { savedStateHandle["resendToken"] = value }

    init {
        val user = repository.auth.currentUser
        if (user != null) {
            _isNewUser.value = false // if already logged in, they are not a new user
            viewModelScope.launch {
                val providerId = user.providerData.firstOrNull()?.providerId
                val isEmailAuth = providerId == "password" || providerId == "email"

                if (isEmailAuth) {
                    val isVerified = repository.checkVerificationStatus(user.uid)
                    if (isVerified || user.isEmailVerified) {
                        _authState.value = AuthState.SUCCESS
                    } else {
                        // User is signed in but not verified. Do not sign out so they can resend.
                        _errorMessage.value = "Please verify your email before continuing."
                        _authState.value = AuthState.IDLE
                    }
                } else {
                    _authState.value = AuthState.SUCCESS
                }
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.IDLE
        _phoneAuthState.value = PhoneAuthState.IDLE
        _errorMessage.value = null
        _verificationId = null
        _resendToken = null
    }

    fun setErrorMessage(message: String) {
        _errorMessage.value = message
        _authState.value = AuthState.IDLE // Maintain IDLE state so UI fields remain accessible
    }

    fun onGoogleSignInSuccess(idToken: String, name: String, email: String, photoUrl: String = "") {
        viewModelScope.launch {
            _authState.value = AuthState.LOADING
            try {
                Log.d("AuthViewModel", "Successfully extracted ID Token, exchanging with Firebase...")
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = repository.auth.signInWithCredential(firebaseCredential).await()
                val uid = authResult.user?.uid ?: throw Exception("Failed to retrieve UID")

                Log.d("AuthViewModel", "Firebase auth successful. Saving to Firestore and Sheets...")
                val (firestoreNew, existingName) = repository.saveUserToFirestore(
                    uid = uid,
                    name = name,
                    email = email,
                    phoneNumber = null,
                    authProvider = "google",
                    isVerified = true
                )
                val firebaseNew = authResult.additionalUserInfo?.isNewUser == true
                _isNewUser.value = firebaseNew || firestoreNew
                authResult.user?.let { repository.sendUserToSheet(it, "Google") }

                userPreferencesRepository.saveUserName(name)
                if (photoUrl.isNotBlank()) {
                    userPreferencesRepository.setProfileImageUri(photoUrl)
                }

                _authState.value = AuthState.SUCCESS
            } catch (e: com.google.firebase.auth.FirebaseAuthException) {
                Log.e("AuthViewModel", "FirebaseAuthException during Google Sign In: ${e.errorCode}", e)
                _errorMessage.value = "Firebase Auth Error: ${e.message}"
                _authState.value = AuthState.IDLE
            } catch (e: com.google.android.gms.common.api.ApiException) {
                Log.e("AuthViewModel", "ApiException during Google Sign In. Code: ${e.statusCode}", e)
                _errorMessage.value = "Google API Error: ${e.message} (Code: ${e.statusCode})"
                _authState.value = AuthState.IDLE
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Generic Exception during Google Sign In", e)
                _errorMessage.value = e.message ?: "Authentication failed"
                _authState.value = AuthState.IDLE
            }
        }
    }

    fun onEmailLogin(email: String, pass: String) {
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
                    val (firestoreNew, existingName) = repository.saveUserToFirestore(
                        uid = uid,
                        name = null,
                        email = email,
                        phoneNumber = null,
                        authProvider = "email",
                        isVerified = true
                    )
                    val firebaseNew = false
                    val isNew = firestoreNew || firebaseNew
                    _isNewUser.value = isNew
                    if (isNew) {
                        repository.auth.currentUser?.let { repository.sendUserToSheet(it, "Email") }
                    } else if (!existingName.isNullOrBlank()) {
                        userPreferencesRepository.saveUserName(existingName)
                    }
                    _authState.value = AuthState.SUCCESS
                } else {
                    // Do NOT sign out. The user needs an active session to use 'Resend Verification Email'.
                    _errorMessage.value = "Please verify your email before logging in."
                    _authState.value = AuthState.IDLE
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Email login failed", e)
                _errorMessage.value = e.message ?: "Login failed"
                _authState.value = AuthState.IDLE
            }
        }
    }

    fun onForgotPassword(email: String, onSuccess: () -> Unit) {
        if (email.isBlank()) {
            _errorMessage.value = "Please enter your email to reset your password."
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.LOADING
            try {
                repository.auth.sendPasswordResetEmail(email).await()
                _errorMessage.value = "Reset link sent to your email"
                _authState.value = AuthState.IDLE
                onSuccess()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Password reset failed", e)
                _errorMessage.value = e.message ?: "Failed to send reset email."
                _authState.value = AuthState.IDLE
            }
        }
    }

    fun onEmailRegister(email: String, pass: String, name: String, onVerificationSent: () -> Unit) {
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
                    name = name.ifBlank { null },
                    email = email,
                    phoneNumber = null,
                    authProvider = "email",
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
    }

    fun verifyEmailActionCode(oobCode: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _authState.value = AuthState.LOADING
            try {
                repository.auth.applyActionCode(oobCode).await()
                _authState.value = AuthState.IDLE
                onSuccess()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Failed to verify email with oobCode", e)
                _errorMessage.value = "Failed to verify email: ${e.message}"
                _authState.value = AuthState.IDLE
            }
        }
    }

    fun resetPasswordWithActionCode(oobCode: String, newPassword: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _authState.value = AuthState.LOADING
            try {
                repository.auth.confirmPasswordReset(oobCode, newPassword).await()
                _authState.value = AuthState.IDLE
                onSuccess()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Failed to reset password with oobCode", e)
                _errorMessage.value = "Failed to reset password: ${e.message}"
                _authState.value = AuthState.IDLE
            }
        }
    }

    fun checkEmailVerified(onVerified: () -> Unit, onNotVerified: () -> Unit) {
        viewModelScope.launch {
            _authState.value = AuthState.LOADING
            try {
                val uid = repository.auth.currentUser?.uid
                if (uid != null) {
                    val isVerified = repository.checkVerificationStatus(uid)
                    if (isVerified) {
                        val (firestoreNew, existingName) = repository.saveUserToFirestore(
                            uid = uid,
                            name = null,
                            email = repository.auth.currentUser?.email ?: "",
                            phoneNumber = null,
                            authProvider = "email",
                            isVerified = true
                        )
                        val firebaseNew = false
                        val isNew = firestoreNew || firebaseNew
                        _isNewUser.value = isNew
                        if (isNew) {
                            repository.auth.currentUser?.let { repository.sendUserToSheet(it, "Email") }
                        } else if (!existingName.isNullOrBlank()) {
                            userPreferencesRepository.saveUserName(existingName)
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
    }

    private fun createPhoneAuthCallbacks(onCodeSentCallback: (String) -> Unit) = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            viewModelScope.launch {
                _authState.value = AuthState.LOADING
                try {
                    val authResult = repository.auth.signInWithCredential(credential).await()
                    val uid = authResult.user?.uid ?: throw Exception("Failed to retrieve UID")

                    val (firestoreNew, existingName) = repository.saveUserToFirestore(
                        uid = uid,
                        name = null,
                        email = null,
                        phoneNumber = repository.auth.currentUser?.phoneNumber,
                        authProvider = "phone",
                        isVerified = true
                    )
                    val firebaseNew = authResult.additionalUserInfo?.isNewUser == true
                    val isNew = firebaseNew || firestoreNew
                    _isNewUser.value = isNew
                    if (isNew) {
                        authResult.user?.let { repository.sendUserToSheet(it, "Phone") }
                    } else if (!existingName.isNullOrBlank()) {
                        userPreferencesRepository.saveUserName(existingName)
                    }
                    _phoneAuthState.value = PhoneAuthState.SUCCESS
                    _authState.value = AuthState.SUCCESS
                } catch (e: Exception) {
                    _errorMessage.value = e.message ?: "Phone auto-login failed"
                    _phoneAuthState.value = PhoneAuthState.ERROR
                    _authState.value = AuthState.IDLE
                }
            }
        }

        override fun onVerificationFailed(e: FirebaseException) {
            val message = when (e) {
                is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> "Invalid phone number"
                is com.google.firebase.FirebaseTooManyRequestsException -> "Quota exceeded. Try again later."
                else -> e.message ?: "Verification failed"
            }
            _errorMessage.value = message
            _phoneAuthState.value = PhoneAuthState.ERROR
            _authState.value = AuthState.IDLE
        }

        override fun onCodeAutoRetrievalTimeOut(verificationId: String) {
            _verificationId = verificationId
            _errorMessage.value = "OTP retrieval timeout. You can request a new one."
            _authState.value = AuthState.IDLE
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            _verificationId = verificationId
            _resendToken = token
            _phoneAuthState.value = PhoneAuthState.CODE_SENT
            _authState.value = AuthState.IDLE
            onCodeSentCallback(verificationId)
        }
    }

    fun startPhoneVerification(phoneNumber: String, activity: android.app.Activity, onCodeSentCallback: (String) -> Unit) {
        if (phoneNumber.isBlank()) {
            _errorMessage.value = "Invalid phone number"
            return
        }

        _authState.value = AuthState.LOADING

        val optionsBuilder = PhoneAuthOptions.newBuilder(repository.auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(createPhoneAuthCallbacks(onCodeSentCallback))

        PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
    }

    fun resendOtp(phoneNumber: String, activity: android.app.Activity, onCodeSentCallback: (String) -> Unit) {
        if (_resendToken == null) {
            _errorMessage.value = "Cannot resend code. Please try again later."
            return
        }
        _authState.value = AuthState.LOADING

        val optionsBuilder = PhoneAuthOptions.newBuilder(repository.auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(createPhoneAuthCallbacks(onCodeSentCallback))
            .setForceResendingToken(_resendToken!!)

        PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
    }

    fun validatePhone(phone: String) {
        val e164Regex = Regex("^\\+[1-9]\\d{1,14}$")
        if (phone.isNotEmpty() && !e164Regex.matches(phone)) {
            _authFormState.value = _authFormState.value.copy(phoneError = "Invalid E.164 phone format (e.g. +14155552671)")
        } else {
            _authFormState.value = _authFormState.value.copy(phoneError = null)
        }
        updateFormValidity()
    }

    fun validateEmail(email: String) {
        if (email.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _authFormState.value = _authFormState.value.copy(emailError = "Invalid email address")
        } else {
            _authFormState.value = _authFormState.value.copy(emailError = null)
        }
        updateFormValidity()
    }

    private fun updateFormValidity() {
        val state = _authFormState.value
        _authFormState.value = state.copy(isFormValid = state.emailError == null && state.phoneError == null)
    }

    fun verifyOtp(verificationId: String, otp: String, phoneNumber: String, email: String?, onSuccess: () -> Unit, onError: () -> Unit) {
        val activeVerificationId = _verificationId ?: verificationId
        if (activeVerificationId.isBlank() || otp.isBlank()) {
            _errorMessage.value = "Invalid OTP or missing verification ID."
            onError()
            return
        }

        _authState.value = AuthState.LOADING
        viewModelScope.launch {
            try {
                val credential = PhoneAuthProvider.getCredential(activeVerificationId, otp)
                val authResult = repository.auth.signInWithCredential(credential).await()
                val uid = authResult.user?.uid ?: throw Exception("Failed to retrieve UID")

                val (firestoreNew, existingName) = repository.saveUserToFirestore(
                    uid = uid,
                    name = null,
                    email = email,
                    phoneNumber = phoneNumber,
                    authProvider = "phone",
                    isVerified = true
                )

                val firebaseNew = authResult.additionalUserInfo?.isNewUser == true
                val isNew = firebaseNew || firestoreNew
                _isNewUser.value = isNew
                if (isNew) {
                    authResult.user?.let { repository.sendUserToSheet(it, "Phone") }
                } else if (!existingName.isNullOrBlank()) {
                    userPreferencesRepository.saveUserName(existingName)
                }

                _phoneAuthState.value = PhoneAuthState.SUCCESS
                _authState.value = AuthState.SUCCESS
                onSuccess()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "OTP verification failed", e)
                val msg = if (e is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException) "Invalid OTP code" else "OTP verification failed"
                _errorMessage.value = msg
                _phoneAuthState.value = PhoneAuthState.ERROR
                _authState.value = AuthState.IDLE
                onError()
            }
        }
    }

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
}
