package com.rivavafi.ui.auth

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.GoogleAuthProvider
import com.rivavafi.data.repository.AuthRepository
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

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {
    companion object {
        private const val TAG = "AuthViewModel"
    }

    private val _authState = MutableStateFlow(AuthState.IDLE)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun resetState() {
        _authState.value = AuthState.IDLE
    var storedVerificationId: String = ""
    var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    var googleName: String = ""
    var googleEmail: String = ""
    var photoUrl: String = ""
    var phoneNumber: String = ""

    fun selectGoogleFlow() {
        _authState.value = AuthState.GOOGLE_SIGN_IN
    }

    fun selectPhoneFlow() {
        _authState.value = AuthState.PHONE_INPUT
    }

    fun resetToChoice() {
        _authState.value = AuthState.CHOICE
        _errorMessage.value = null
    }

    fun onGoogleSignInSuccess(idToken: String, name: String, email: String) {
        Log.d(TAG, "Received Google ID token. Starting Firebase credential sign-in.")
        viewModelScope.launch {
            _authState.value = AuthState.LOADING
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val result = repository.auth.signInWithCredential(credential).await()
                val uid = result.user?.uid ?: throw Exception("Failed to retrieve UID")

                repository.saveUserToFirestore(
                    uid = uid,
                    name = name,
                    email = email
                )

                _authState.value = AuthState.SUCCESS
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Authentication failed"
                _authState.value = AuthState.ERROR
            }
        }
    }
                val firebaseUser = result.user
                googleName = firebaseUser?.displayName ?: name
                googleEmail = firebaseUser?.email ?: email
                phoneNumber = firebaseUser?.phoneNumber.orEmpty()
                photoUrl = firebaseUser?.photoUrl?.toString().orEmpty()
                Log.d(TAG, "Firebase credential sign-in success for uid=${firebaseUser?.uid}")
                finalizeAuth(firebaseUser?.uid.orEmpty())
            } catch (e: Exception) {
                Log.e(TAG, "Firebase authentication failure after Google sign-in.", e)
                _errorMessage.value = e.message
                    ?: "Firebase authentication failed after Google account selection. Please try again."
                _authState.value = AuthState.CHOICE
            }
        }
    }

    fun onGoogleSignInError(message: String) {
        Log.e(TAG, "Google Sign-In error: $message")
        _errorMessage.value = message
        _authState.value = AuthState.GOOGLE_SIGN_IN
    }

    fun startPhoneNumberVerification(phone: String, activity: Activity) {
        if (phone.isBlank()) {
            _errorMessage.value = "Please enter a valid phone number."
            _authState.value = AuthState.PHONE_INPUT
            return
        }

        phoneNumber = phone
        _authState.value = AuthState.LOADING
        Log.d(TAG, "Starting OTP flow for phone=$phone")

        val options = PhoneAuthOptions.newBuilder(repository.auth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    Log.d(TAG, "Phone verification auto-completed.")
                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Log.e(TAG, "Phone verification failed", e)
                    _errorMessage.value = e.message ?: "Phone verification failed."
                    _authState.value = AuthState.PHONE_INPUT
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    storedVerificationId = verificationId
                    resendToken = token
                    Log.d(TAG, "OTP sent successfully.")
                    _authState.value = AuthState.OTP_VERIFICATION
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyOtp(code: String) {
        if (storedVerificationId.isBlank()) {
            _errorMessage.value = "Verification session expired. Please request OTP again."
            _authState.value = AuthState.PHONE_INPUT
            return
        }
        if (code.length < 6) {
            _errorMessage.value = "Please enter the 6-digit OTP."
            _authState.value = AuthState.OTP_VERIFICATION
            return
        }
        _authState.value = AuthState.LOADING
        Log.d(TAG, "Verifying OTP.")
        val credential = PhoneAuthProvider.getCredential(storedVerificationId, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            try {
                val result = repository.auth.signInWithCredential(credential).await()
                val firebaseUser = result.user
                googleName = firebaseUser?.displayName ?: googleName
                googleEmail = firebaseUser?.email.orEmpty()
                phoneNumber = firebaseUser?.phoneNumber ?: phoneNumber
                photoUrl = firebaseUser?.photoUrl?.toString().orEmpty()
                Log.d(TAG, "Phone credential sign-in success for uid=${firebaseUser?.uid}")
                finalizeAuth(result.user?.uid ?: "")
            } catch (e: Exception) {
                Log.e(TAG, "Phone credential sign-in failed", e)
                _errorMessage.value = e.message ?: "OTP verification failed."
                _authState.value = AuthState.OTP_VERIFICATION
            }
        }
    }

    private fun finalizeAuth(uid: String) {
        viewModelScope.launch {
            _authState.value = AuthState.LOADING
            try {
                if (uid.isBlank()) {
                    throw IllegalStateException("Unable to identify user account.")
                }
                repository.saveUserToFirestore(
                    uid = uid,
                    name = googleName,
                    email = googleEmail,
                    phone = phoneNumber,
                    photoUrl = photoUrl,
                    loginMethod = if (phoneNumber.isNotBlank() && googleEmail.isBlank()) "phone" else "google"
                )
                // Keep existing Apps Script sync to avoid breaking existing flow.
                repository.saveUserDataToSheet(
                    name = googleName,
                    email = googleEmail,
                    phone = phoneNumber,
                    uid = uid
                )
                _authState.value = AuthState.SUCCESS
            } catch (e: Exception) {
                Log.e(TAG, "Failed to finalize authentication", e)
                _errorMessage.value = e.message ?: "Authentication failed."
                _authState.value = AuthState.CHOICE
            }
        }
    }
}
