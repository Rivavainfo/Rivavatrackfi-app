package com.rivavafi.universal.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.rivavafi.universal.data.preferences.UserPreferencesRepository
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
    private val repository: AuthRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    data class SignedInUser(
        val name: String,
        val email: String,
        val photoUrl: String
    )

    companion object {
        private const val TAG = "AuthViewModel"
    }

    private val _authState = MutableStateFlow(AuthState.IDLE)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _signedInUser = MutableStateFlow<SignedInUser?>(null)
    val signedInUser: StateFlow<SignedInUser?> = _signedInUser.asStateFlow()

    init {
        val currentUser = repository.auth.currentUser
        if (currentUser != null) {
            hydrateUserSession(currentUser)
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

    fun onGoogleSignInSuccess(idToken: String, fallbackName: String, fallbackEmail: String, fallbackPhotoUrl: String = "") {
        viewModelScope.launch {
            _authState.value = AuthState.LOADING
            try {
                Log.d(TAG, "Successfully extracted ID Token, exchanging with Firebase...")
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = repository.auth.signInWithCredential(firebaseCredential).await()
                val user = authResult.user ?: throw Exception("Failed to retrieve Firebase user")
                val uid = user.uid
                val name = user.displayName ?: fallbackName
                val email = user.email ?: fallbackEmail
                val photoUrl = user.photoUrl?.toString() ?: fallbackPhotoUrl

                Log.d(TAG, "Firebase auth successful. Saving to Firestore and Sheets...")
                repository.saveUserToFirestore(
                    uid = uid,
                    name = name,
                    email = email
                )

                userPreferencesRepository.saveUserName(name)
                if (photoUrl.isNotBlank()) {
                    userPreferencesRepository.setProfileImageUri(photoUrl)
                }
                _signedInUser.value = SignedInUser(name = name, email = email, photoUrl = photoUrl)

                _authState.value = AuthState.SUCCESS
            } catch (e: Exception) {
                Log.e(TAG, "Generic Exception during Google Sign In", e)
                _errorMessage.value = e.message ?: "Authentication failed"
                _authState.value = AuthState.IDLE
            }
        }
    }

    fun onEmailLogin(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            Log.w(TAG, "Email login rejected: Email or password was blank")
            _errorMessage.value = "Email and Password cannot be empty."
            _authState.value = AuthState.IDLE
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.LOADING
            try {
                Log.d(TAG, "Attempting email login for: $email")
                val result = repository.auth.signInWithEmailAndPassword(email, pass).await()
                result.user?.let { hydrateUserSession(it) }
                Log.d(TAG, "Email login successful")
                _authState.value = AuthState.SUCCESS
            } catch (e: Exception) {
                Log.e(TAG, "Email login failed", e)
                _errorMessage.value = e.message ?: "Login failed"
                _authState.value = AuthState.IDLE
            }
        }
    }

    fun onEmailRegister(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            Log.w(TAG, "Email register rejected: Email or password was blank")
            _errorMessage.value = "Email and Password cannot be empty."
            _authState.value = AuthState.IDLE
            return
        }
        if (pass.length < 6) {
            Log.w(TAG, "Email register rejected: Password too short")
            _errorMessage.value = "Password must be at least 6 characters."
            _authState.value = AuthState.IDLE
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.LOADING
            try {
                Log.d(TAG, "Attempting email registration for: $email")
                val result = repository.auth.createUserWithEmailAndPassword(email, pass).await()
                val uid = result.user?.uid ?: throw Exception("Failed to retrieve UID")

                Log.d(TAG, "Registration successful. Saving to Firestore and Sheets...")
                repository.saveUserToFirestore(
                    uid = uid,
                    name = "Email User",
                    email = email
                )

                result.user?.let { hydrateUserSession(it) }
                _authState.value = AuthState.SUCCESS
            } catch (e: Exception) {
                Log.e(TAG, "Email registration failed", e)
                _errorMessage.value = e.message ?: "Registration failed"
                _authState.value = AuthState.IDLE
            }
        }
    }

    private fun hydrateUserSession(user: FirebaseUser) {
        _signedInUser.value = SignedInUser(
            name = user.displayName ?: "User",
            email = user.email.orEmpty(),
            photoUrl = user.photoUrl?.toString().orEmpty()
        )
    }
}
