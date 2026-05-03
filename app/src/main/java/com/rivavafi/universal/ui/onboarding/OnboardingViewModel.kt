package com.rivavafi.universal.ui.onboarding

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.rivavafi.universal.data.preferences.UserPreferencesRepository
import com.rivavafi.universal.data.repository.AuthRepository
import com.rivavafi.universal.data.repository.EmailService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val authRepository: AuthRepository,
    private val emailService: EmailService,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    val userName = preferencesRepository.userNameFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ""
    )

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var _verificationId: String?
        get() = savedStateHandle.get("verificationId")
        set(value) { savedStateHandle["verificationId"] = value }

    private var _resendToken: PhoneAuthProvider.ForceResendingToken?
        get() = savedStateHandle.get("resendToken")
        set(value) { savedStateHandle["resendToken"] = value }

    fun setErrorMessage(msg: String) {
        _errorMessage.value = msg
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun saveName(name: String) {
        viewModelScope.launch {
            preferencesRepository.saveUserName(name)
            FirebaseAuth.getInstance().currentUser?.let { user ->
                val uid = user.uid
                firestore.collection("users").document(uid)
                    .set(mapOf("full_name" to name), com.google.firebase.firestore.SetOptions.merge())

                authRepository.sendUserToSheet(user, "Google", name)
            }
        }
    }

    private fun createPhoneAuthCallbacks(onCodeSentCallback: (String) -> Unit) = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // Do not link credential directly here to avoid auto-linking errors
            // If auto-retrieval works, we can simulate success or let the user click verify
            _isLoading.value = false
        }

        override fun onVerificationFailed(e: FirebaseException) {
            val message = when (e) {
                is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> "Invalid phone number"
                is com.google.firebase.FirebaseTooManyRequestsException -> "Quota exceeded. Try again later."
                else -> e.message ?: "Verification failed"
            }
            _errorMessage.value = message
            _isLoading.value = false
        }

        override fun onCodeAutoRetrievalTimeOut(verificationId: String) {
            _verificationId = verificationId
            _errorMessage.value = "OTP retrieval timeout. You can request a new one."
            _isLoading.value = false
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            _verificationId = verificationId
            _resendToken = token
            _isLoading.value = false
            onCodeSentCallback(verificationId)
        }
    }

    fun startPhoneVerification(phoneNumber: String, activity: android.app.Activity, onCodeSentCallback: (String) -> Unit) {
        if (phoneNumber.isBlank()) {
            _errorMessage.value = "Invalid phone number"
            return
        }

        _isLoading.value = true

        val optionsBuilder = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
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
        _isLoading.value = true

        val optionsBuilder = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(createPhoneAuthCallbacks(onCodeSentCallback))
            .setForceResendingToken(_resendToken!!)

        PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
    }

    fun verifyOtp(verificationId: String, otp: String, phoneNumber: String, onSuccess: () -> Unit, onError: () -> Unit) {
        val activeVerificationId = _verificationId ?: verificationId
        if (activeVerificationId.isBlank() || otp.isBlank()) {
            _errorMessage.value = "Invalid OTP or missing verification ID."
            onError()
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val credential = PhoneAuthProvider.getCredential(activeVerificationId, otp)
                val user = FirebaseAuth.getInstance().currentUser
                if (user == null) {
                    _errorMessage.value = "User not logged in."
                    _isLoading.value = false
                    onError()
                    return@launch
                }

                // Link the phone credential to the Google account
                user.linkWithCredential(credential).await()

                // Update Firestore
                firestore.collection("users").document(user.uid)
                    .set(mapOf("phone_number" to phoneNumber, "phone_verified" to true), com.google.firebase.firestore.SetOptions.merge())
                    .await()

                _isLoading.value = false
                onSuccess()
            } catch (e: Exception) {
                Log.e("OnboardingViewModel", "OTP verification failed", e)
                val msg = if (e is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException) "Invalid OTP code" else "OTP verification failed or account already linked."
                _errorMessage.value = msg
                _isLoading.value = false
                onError()
            }
        }
    }

    fun setSmsTrackingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setSmsTrackingEnabled(enabled)
            FirebaseAuth.getInstance().currentUser?.let { user ->
                val uid = user.uid
                firestore.collection("users").document(uid)
                    .set(mapOf("auto_message_tracking_enabled" to enabled), com.google.firebase.firestore.SetOptions.merge())
            }
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            preferencesRepository.setOnboardingCompleted(true)
            FirebaseAuth.getInstance().currentUser?.let { user ->
                val uid = user.uid

                // Check if we already marked onboarding complete to prevent duplicate emails
                val docSnap = firestore.collection("users").document(uid).get().await()
                val alreadyCompleted = docSnap.getBoolean("onboarding_completed") ?: false

                if (!alreadyCompleted) {
                    firestore.collection("users").document(uid)
                        .set(mapOf("onboarding_completed" to true), com.google.firebase.firestore.SetOptions.merge())

                    user.email?.let { email ->
                        val name = preferencesRepository.getUserName() ?: ""
                        emailService.sendWelcomeEmail(email, name)
                    }
                }
            }
        }
    }
}
