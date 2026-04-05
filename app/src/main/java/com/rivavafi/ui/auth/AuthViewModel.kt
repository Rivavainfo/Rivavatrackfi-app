package com.rivavafi.ui.auth

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

    private val _authState = MutableStateFlow(AuthState.IDLE)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun resetState() {
        _authState.value = AuthState.IDLE
        _errorMessage.value = null
    }

    fun onGoogleSignInSuccess(idToken: String, name: String, email: String) {
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
}
