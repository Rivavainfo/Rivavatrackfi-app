package com.rivavafi.universal.ui.auth

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.rivavafi.universal.R
import com.rivavafi.universal.data.preferences.UserPreferencesRepository
import com.rivavafi.universal.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed interface GoogleSignInUiEvent {
    data object None : GoogleSignInUiEvent
    data class ShowFailurePopup(val message: String) : GoogleSignInUiEvent
    data object NavigateToWelcome : GoogleSignInUiEvent
}

@HiltViewModel
class GoogleSignInViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val credentialManager: CredentialManager = CredentialManager.create(context)
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _isSigningIn = MutableStateFlow(false)
    val isSigningIn: StateFlow<Boolean> = _isSigningIn.asStateFlow()

    private val _uiEvent = MutableStateFlow<GoogleSignInUiEvent>(GoogleSignInUiEvent.None)
    val uiEvent: StateFlow<GoogleSignInUiEvent> = _uiEvent.asStateFlow()

    fun consumeUiEvent() {
        _uiEvent.value = GoogleSignInUiEvent.None
    }

    /**
     * Method 1:
     * Runs Google sign-in on backend-safe coroutine context, validates Google ID token,
     * manages credentials, and signs in Firebase using auth credentials.
     */
    suspend fun runGoogleSignInOnBackend(activityContext: Context): Boolean {
        _isSigningIn.value = true
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(context.getString(R.string.default_web_client_id))
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                context = activityContext,
                request = request
            )

            val credential = result.credential
            if (credential !is CustomCredential || credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                _uiEvent.value = GoogleSignInUiEvent.ShowFailurePopup("Sign in failed")
                return false
            }

            val googleIdTokenCredential = try {
                GoogleIdTokenCredential.createFrom(credential.data)
            } catch (e: GoogleIdTokenParsingException) {
                _uiEvent.value = GoogleSignInUiEvent.ShowFailurePopup("Invalid Google token")
                return false
            }

            val idToken = googleIdTokenCredential.idToken
            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = authRepository.auth.signInWithCredential(firebaseCredential).await()
            val user = authResult.user ?: run {
                _uiEvent.value = GoogleSignInUiEvent.ShowFailurePopup("Sign in failed")
                return false
            }

            checkIfUserExistsAndManage(user)
        } catch (e: GetCredentialCancellationException) {
            _uiEvent.value = GoogleSignInUiEvent.ShowFailurePopup("Google sign-in was cancelled")
            false
        } catch (e: GetCredentialException) {
            _uiEvent.value = GoogleSignInUiEvent.ShowFailurePopup(e.message ?: "Sign in failed")
            false
        } catch (e: Exception) {
            _uiEvent.value = GoogleSignInUiEvent.ShowFailurePopup(e.message ?: "Sign in failed")
            false
        } finally {
            _isSigningIn.value = false
        }
    }

    /**
     * Method 2:
     * Checks if user exists, manages create/update flow, updates profile values,
     * and emits failure popup or success navigation event to Welcome screen.
     */
    suspend fun checkIfUserExistsAndManage(user: FirebaseUser): Boolean {
        return try {
            val uid = user.uid
            val email = user.email.orEmpty()
            val displayName = user.displayName?.takeIf { it.isNotBlank() } ?: "User"

            val userDocRef = firestore.collection("users").document(uid)
            val snapshot = userDocRef.get().await()

            val latestProfileData = mutableMapOf<String, Any>(
                "uid" to uid,
                "name" to displayName,
                "email" to email,
                "photoUrl" to (user.photoUrl?.toString() ?: ""),
                "lastSignInAt" to System.currentTimeMillis().toString()
            )

            if (snapshot.exists()) {
                userDocRef.update(latestProfileData).await()
            } else {
                latestProfileData["createdAt"] = System.currentTimeMillis().toString()
                userDocRef.set(latestProfileData).await()
            }

            userPreferencesRepository.saveUserName(displayName)
            user.photoUrl?.toString()?.let { photoUrl ->
                if (photoUrl.isNotBlank()) {
                    userPreferencesRepository.setProfileImageUri(photoUrl)
                }
            }

            _uiEvent.value = GoogleSignInUiEvent.NavigateToWelcome
            true
        } catch (e: Exception) {
            _uiEvent.value = GoogleSignInUiEvent.ShowFailurePopup(e.message ?: "Sign in failed")
            false
        }
    }

    fun clearCredentialState() {
        viewModelScope.launch {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (_: ClearCredentialException) {
                // Ignore clear state failures; non-blocking cleanup path.
            }
        }
    }
}
