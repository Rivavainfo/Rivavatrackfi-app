package com.rivavafi.universal.ui.auth

import android.content.Context
import android.util.Log
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
        _authState.value = AuthState.IDLE // Maintain IDLE state so UI fields remain accessible
    }

    fun onGoogleSignIn(context: Context, webClientId: String) {
        viewModelScope.launch {
            _authState.value = AuthState.LOADING
            try {
                Log.d("AuthViewModel", "Starting CredentialManager request for Web Client ID")
                val credentialManager = repository.getCredentialManager(context)
                val request = repository.getGoogleCredentialRequest(webClientId)
                val result = credentialManager.getCredential(context, request)

                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        val idToken = googleIdTokenCredential.idToken

                        Log.d("AuthViewModel", "Successfully extracted ID Token, exchanging with Firebase...")
                        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                        val authResult = repository.auth.signInWithCredential(firebaseCredential).await()
                        val uid = authResult.user?.uid ?: throw Exception("Failed to retrieve UID")

                        Log.d("AuthViewModel", "Firebase auth successful. Saving to Firestore and Sheets...")
                        repository.saveUserToFirestore(
                            uid = uid,
                            name = googleIdTokenCredential.displayName ?: "User",
                            email = googleIdTokenCredential.id ?: ""
                        )

                        _authState.value = AuthState.SUCCESS

                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e("AuthViewModel", "Failed to parse Google ID Token", e)
                        _errorMessage.value = "Failed to parse Google ID Token: ${e.message}"
                        _authState.value = AuthState.IDLE
                    }
                } else {
                    Log.e("AuthViewModel", "Unexpected credential type returned")
                    _errorMessage.value = "Unexpected credential type. Please try again."
                    _authState.value = AuthState.IDLE
                }

            } catch (e: androidx.credentials.exceptions.NoCredentialException) {
                Log.w("AuthViewModel", "No credentials available on device", e)
                _errorMessage.value = "No Google accounts found on this device."
                _authState.value = AuthState.IDLE
            } catch (e: GetCredentialException) {
                if (e is androidx.credentials.exceptions.GetCredentialCancellationException) {
                     Log.d("AuthViewModel", "User cancelled Google Sign In")
                     _authState.value = AuthState.IDLE
                } else {
                    Log.e("AuthViewModel", "GetCredentialException during Google Sign In", e)
                    _errorMessage.value = "Sign-in error: ${e.message}"
                    _authState.value = AuthState.IDLE
                }
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
                _authState.value = AuthState.SUCCESS
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

                _authState.value = AuthState.SUCCESS
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Email registration failed", e)
                _errorMessage.value = e.message ?: "Registration failed"
                _authState.value = AuthState.IDLE
            }
        }
    }
}
