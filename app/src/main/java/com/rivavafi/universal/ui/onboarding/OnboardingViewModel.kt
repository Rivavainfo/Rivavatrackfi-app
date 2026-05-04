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

    fun savePhoneNumber(phoneNumber: String, onSuccess: () -> Unit) {
        if (phoneNumber.isBlank()) {
            _errorMessage.value = "Invalid phone number"
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val user = FirebaseAuth.getInstance().currentUser
                if (user == null) {
                    _errorMessage.value = "User not logged in."
                    _isLoading.value = false
                    return@launch
                }

                firestore.collection("users").document(user.uid)
                    .set(mapOf(
                        "phone_number" to phoneNumber,
                        "phone_verified" to false
                    ), com.google.firebase.firestore.SetOptions.merge())
                    .await()

                _isLoading.value = false
                onSuccess()
            } catch (e: Exception) {
                Log.e("OnboardingViewModel", "Failed to save phone number", e)
                _errorMessage.value = "Failed to save phone number."
                _isLoading.value = false
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
