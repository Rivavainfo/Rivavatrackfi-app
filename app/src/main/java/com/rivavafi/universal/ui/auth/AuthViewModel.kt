package com.rivavafi.universal.ui.auth

import android.content.Context
import androidx.credentials.CustomCredential
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.GoogleAuthProvider
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

    init {
        if (repository.auth.currentUser != null) {
            _authState.value = AuthState.SUCCESS
        }
    }

    fun resetState() {
        _authState.value = AuthState.IDLE
        _errorMessage.value = null
    }

    fun setErrorMessage(message: String) {
        _errorMessage.value = message
        _authState.value = AuthState.ERROR
    }

    fun onGoogleSignIn(context: Context, webClientId: String) {
        viewModelScope.launch {
            _authState.value = AuthState.LOADING
            try {
                val credentialManager = repository.getCredentialManager(context)
                val request = repository.getGoogleCredentialRequest(webClientId)
                val result = credentialManager.getCredential(context, request)

                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        val idToken = googleIdTokenCredential.idToken

                        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                        val authResult = repository.auth.signInWithCredential(firebaseCredential).await()
                        val uid = authResult.user?.uid ?: throw Exception("Failed to retrieve UID")

                        repository.saveUserToFirestore(
                            uid = uid,
                            name = googleIdTokenCredential.displayName ?: "User",
                            email = googleIdTokenCredential.id ?: ""
                        )

                        _authState.value = AuthState.SUCCESS

                    } catch (e: GoogleIdTokenParsingException) {
                        _errorMessage.value = "Failed to parse Google ID Token"
                        _authState.value = AuthState.ERROR
                    }
                } else {
                    _errorMessage.value = "Unexpected credential type"
                    _authState.value = AuthState.ERROR
                }

            } catch (e: GetCredentialException) {
                // If it's a cancellation, we can just reset to IDLE, otherwise show error
                _errorMessage.value = e.message ?: "Google Sign-in failed"
                _authState.value = AuthState.ERROR
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Authentication failed"
                _authState.value = AuthState.ERROR
            }
        }
    }

    fun onEmailLogin(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _errorMessage.value = "Email and Password cannot be empty."
            _authState.value = AuthState.ERROR
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.LOADING
            try {
                repository.auth.signInWithEmailAndPassword(email, pass).await()
                _authState.value = AuthState.SUCCESS
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Login failed"
                _authState.value = AuthState.ERROR
            }
        }
    }

    fun onEmailRegister(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _errorMessage.value = "Email and Password cannot be empty."
            _authState.value = AuthState.ERROR
            return
        }
        if (pass.length < 6) {
            _errorMessage.value = "Password must be at least 6 characters."
            _authState.value = AuthState.ERROR
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.LOADING
            try {
                val result = repository.auth.createUserWithEmailAndPassword(email, pass).await()
                val uid = result.user?.uid ?: throw Exception("Failed to retrieve UID")

                repository.saveUserToFirestore(
                    uid = uid,
                    name = "Email User",
                    email = email
                )

                _authState.value = AuthState.SUCCESS
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Registration failed"
                _authState.value = AuthState.ERROR
            }
        }
    }
}
