package com.rivavafi.universal.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.rivavafi.universal.data.repository.AuthRepository
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
    SUCCESS
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState.IDLE)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        if (repository.auth.currentUser != null) {
            _authState.value = AuthState.SUCCESS
        }
    }

    fun resetState() {
        _authState.value = AuthState.IDLE
        _errorMessage.value = null
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
                _authState.value = AuthState.SUCCESS
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Email login failed", e)
                _errorMessage.value = mapAuthError(e, fallback = "Login failed. Please check your credentials.")
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

                _authState.value = AuthState.SUCCESS
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Email registration failed", e)
                _errorMessage.value = mapAuthError(e, fallback = "Registration failed. Please try again.")
                _authState.value = AuthState.IDLE
            }
        }
    }

    private fun mapAuthError(throwable: Throwable, fallback: String): String {
        return when (throwable) {
            is FirebaseAuthInvalidCredentialsException -> "Invalid credentials. Please try again."
            is FirebaseAuthInvalidUserException -> "Account not found. Please register first."
            else -> fallback
        }
    }
}
