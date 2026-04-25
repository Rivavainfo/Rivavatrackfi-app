package com.rivavafi.universal.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.rivavafi.universal.data.preferences.UserPreferencesRepository
import com.rivavafi.universal.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    val userName = preferencesRepository.userNameFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ""
    )

    fun saveName(name: String) {
        viewModelScope.launch {
            preferencesRepository.saveUserName(name)
            FirebaseAuth.getInstance().currentUser?.let { user ->
                val uid = user.uid
                firestore.collection("users").document(uid)
                    .set(mapOf("name" to name), com.google.firebase.firestore.SetOptions.merge())
                val provider = user.providerData.firstOrNull()?.providerId?.let {
                    when (it) {
                        "password" -> "Email"
                        "phone" -> "Phone"
                        "google.com" -> "Google"
                        else -> "Unknown"
                    }
                } ?: "Unknown"
                authRepository.sendUserToSheet(user, provider, name)
            }
        }
    }

    fun setSmsTrackingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setSmsTrackingEnabled(enabled)
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            preferencesRepository.setOnboardingCompleted(true)
        }
    }
}
