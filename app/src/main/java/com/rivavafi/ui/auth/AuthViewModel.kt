package com.rivavafi.ui.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthProvider
import com.rivavafi.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject

enum class AuthState {
    CHOICE,
    GOOGLE_SIGN_IN,
    PHONE_INPUT,
    OTP_VERIFICATION,
    SUCCESS,
    LOADING,
    ERROR
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState.CHOICE)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    var storedVerificationId: String = ""
    var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    var googleName: String = ""
    var googleEmail: String = ""
    var phoneNumber: String = ""

    fun selectGoogleFlow() {
        _authState.value = AuthState.GOOGLE_SIGN_IN
    }

    fun selectPhoneFlow() {
        _authState.value = AuthState.PHONE_INPUT
    }

    fun resetToChoice() {
        _authState.value = AuthState.CHOICE
        _errorMessage.value = null
    }

    fun onGoogleSignInSuccess(idToken: String, name: String, email: String) {
        viewModelScope.launch {
            _authState.value = AuthState.LOADING
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val result = repository.auth.signInWithCredential(credential).await()
                val firebaseUser = result.user
                googleName = firebaseUser?.displayName ?: name
                googleEmail = firebaseUser?.email ?: email
                finalizeAuth(firebaseUser?.uid.orEmpty())
            } catch (e: Exception) {
                _errorMessage.value = e.message
                _authState.value = AuthState.CHOICE
            }
        }
    }

    fun onGoogleSignInError(message: String) {
        _errorMessage.value = message
        _authState.value = AuthState.GOOGLE_SIGN_IN
    }

    fun startPhoneNumberVerification(phone: String, activity: Activity) {
        phoneNumber = phone
        _authState.value = AuthState.LOADING

        val options = PhoneAuthOptions.newBuilder(repository.auth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    _errorMessage.value = e.message
                    _authState.value = AuthState.PHONE_INPUT
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    storedVerificationId = verificationId
                    resendToken = token
                    _authState.value = AuthState.OTP_VERIFICATION
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyOtp(code: String) {
        _authState.value = AuthState.LOADING
        val credential = PhoneAuthProvider.getCredential(storedVerificationId, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            try {
                val result = repository.auth.signInWithCredential(credential).await()
                finalizeAuth(result.user?.uid ?: "")
            } catch (e: Exception) {
                _errorMessage.value = e.message
                _authState.value = AuthState.OTP_VERIFICATION
            }
        }
    }

    private fun finalizeAuth(uid: String) {
        viewModelScope.launch {
            _authState.value = AuthState.LOADING
            try {
                if (uid.isBlank()) {
                    throw IllegalStateException("Unable to identify user account.")
                }
                repository.saveUserToFirestore(
                    uid = uid,
                    name = googleName,
                    email = googleEmail
                )
                // Keep existing Apps Script sync to avoid breaking existing flow.
                repository.saveUserDataToSheet(
                    name = googleName,
                    email = googleEmail,
                    phone = phoneNumber,
                    uid = uid
                )
                _authState.value = AuthState.SUCCESS
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Authentication failed."
                _authState.value = AuthState.CHOICE
            }
        }
    }
}
