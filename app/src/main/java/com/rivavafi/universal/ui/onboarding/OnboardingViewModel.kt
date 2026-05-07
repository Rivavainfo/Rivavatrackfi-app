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
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import android.content.Context
import com.rivavafi.universal.utils.PrefsManager

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val authRepository: AuthRepository,
    private val emailService: EmailService,
    private val savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context
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
        val cleanedName = name.trim()
        if (cleanedName.isBlank()) return

        viewModelScope.launch {
            val generatedUsername = generateUsername(cleanedName)
            preferencesRepository.setUserName(cleanedName)
            preferencesRepository.saveUsername(generatedUsername)
            PrefsManager(context).updateName(cleanedName)
            PrefsManager(context).updateUsername(generatedUsername)

            FirebaseAuth.getInstance().currentUser?.let { user ->
                val uid = user.uid
                firestore.collection("users").document(uid)
                    .set(
                        mapOf(
                            "full_name" to cleanedName,
                            "display_name" to cleanedName,
                            "username" to generatedUsername
                        ),
                        com.google.firebase.firestore.SetOptions.merge()
                    )

                authRepository.sendUserToSheet(user, "Google", cleanedName)
            }
        }
    }

    fun savePhoneNumber(phoneNumber: String, onSuccess: () -> Unit) {
        val cleanedPhoneNumber = phoneNumber.trim()
        if (cleanedPhoneNumber.isBlank()) {
            _errorMessage.value = "Invalid phone number"
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            preferencesRepository.setUserPhone(cleanedPhoneNumber)
            PrefsManager(context).updatePhone(cleanedPhoneNumber)

            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                _isLoading.value = false
                onSuccess()
                return@launch
            }

            firestore.collection("users").document(user.uid)
                .set(mapOf(
                    "phone_number" to cleanedPhoneNumber
                ), com.google.firebase.firestore.SetOptions.merge())
                .addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.e("OnboardingViewModel", "Failed to save phone number", task.exception)
                    }
                    _isLoading.value = false
                    onSuccess()
                }
        }
    }

    private fun generateUsername(name: String): String {
        return name.lowercase()
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')
            .ifBlank { "rivava_user" }
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
