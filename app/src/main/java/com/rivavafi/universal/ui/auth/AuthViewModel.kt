package com.rivavafi.universal.ui.auth

import android.util.Log
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
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState.IDLE)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _phoneAuthState = MutableStateFlow(PhoneAuthState.IDLE)
    val phoneAuthState: StateFlow<PhoneAuthState> = _phoneAuthState.asStateFlow()

    private var _verificationId: String? = null
    private var _resendToken: PhoneAuthProvider.ForceResendingToken? = null

    init {
        if (repository.auth.currentUser != null) {
            _authState.value = AuthState.SUCCESS
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
                repository.saveUserToFirestore(
                    uid = uid,
                    name = name,
                    email = email
                )
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

                repository.auth.currentUser?.reload()?.await()

                if (repository.auth.currentUser?.isEmailVerified == true) {
                    repository.auth.currentUser?.let { repository.sendUserToSheet(it, "Email") }
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
    }

    fun onEmailRegister(email: String, pass: String) {
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
                    name = "Email User",
                    email = email
                )

                result.user?.sendEmailVerification()?.await()
                repository.auth.signOut()
                _errorMessage.value = "Registration successful. Please verify your email before logging in."
                _authState.value = AuthState.IDLE
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Email registration failed", e)
                _errorMessage.value = e.message ?: "Registration failed"
                _authState.value = AuthState.IDLE
            }
        }
    }

    private val phoneAuthCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            viewModelScope.launch {
                _authState.value = AuthState.LOADING
                try {
                    val authResult = repository.auth.signInWithCredential(credential).await()
                    val uid = authResult.user?.uid ?: throw Exception("Failed to retrieve UID")

                    repository.saveUserToFirestore(
                        uid = uid,
                        name = "Phone User",
                        email = ""
                    )
                    authResult.user?.let { repository.sendUserToSheet(it, "Phone") }
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
            _errorMessage.value = e.message ?: "Verification failed"
            _phoneAuthState.value = PhoneAuthState.ERROR
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
        }
    }

    fun startPhoneVerification(phoneNumber: String, activity: android.app.Activity) {
        if (phoneNumber.isBlank() || phoneNumber.length < 12) { // Minimum "+91" + 10 digits = 13 chars usually, but accepting any formatted properly
            _errorMessage.value = "Invalid phone number"
            return
        }

        _authState.value = AuthState.LOADING

        val optionsBuilder = PhoneAuthOptions.newBuilder(repository.auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(phoneAuthCallbacks)

        PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
    }

    fun resendOtp(phoneNumber: String, activity: android.app.Activity) {
        if (_resendToken == null) {
            _errorMessage.value = "Cannot resend code. Please try again later."
            return
        }
        _authState.value = AuthState.LOADING

        val optionsBuilder = PhoneAuthOptions.newBuilder(repository.auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(phoneAuthCallbacks)
            .setForceResendingToken(_resendToken!!)

        PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
    }

    fun verifyOtp(otp: String) {
        if (_verificationId == null || otp.isBlank()) {
            _errorMessage.value = "Invalid OTP or missing verification ID."
            return
        }

        _authState.value = AuthState.LOADING
        viewModelScope.launch {
            try {
                val credential = PhoneAuthProvider.getCredential(_verificationId!!, otp)
                val authResult = repository.auth.signInWithCredential(credential).await()
                val uid = authResult.user?.uid ?: throw Exception("Failed to retrieve UID")

                repository.saveUserToFirestore(
                    uid = uid,
                    name = "Phone User",
                    email = ""
                )
                authResult.user?.let { repository.sendUserToSheet(it, "Phone") }

                _phoneAuthState.value = PhoneAuthState.SUCCESS
                _authState.value = AuthState.SUCCESS
            } catch (e: Exception) {
                Log.e("AuthViewModel", "OTP verification failed", e)
                _errorMessage.value = "Invalid OTP. Please try again."
                _phoneAuthState.value = PhoneAuthState.ERROR
                _authState.value = AuthState.IDLE
            }
        }
    }
}
