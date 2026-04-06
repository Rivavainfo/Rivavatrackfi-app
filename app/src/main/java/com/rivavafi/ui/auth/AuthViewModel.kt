package com.rivavafi.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
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
    GOOGLE_LOGIN,
    EMAIL_LOGIN,
    EMAIL_REGISTER,
    PHONE_INPUT,
    PHONE_OTP,
    LOADING,
    SUCCESS,
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

    private var storedVerificationId: String = ""
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var currentPhone: String = ""

    fun resetState() {
        _authState.value = AuthState.CHOICE
        _errorMessage.value = null
    }

    fun setAuthState(state: AuthState) {
        _authState.value = state
        _errorMessage.value = null
    }

    fun onGoogleSignInSuccess(idToken: String, name: String, email: String) {
        viewModelScope.launch {
            _authState.value = AuthState.LOADING
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val result = repository.auth.signInWithCredential(credential).await()
                val user = result.user ?: throw Exception("Failed to retrieve user")

                repository.saveUserToFirestore(
                    uid = user.uid,
                    name = user.displayName ?: name,
                    email = user.email ?: email,
                    phone = user.phoneNumber ?: "",
                    provider = "google"
                )
                _authState.value = AuthState.SUCCESS
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    fun loginWithEmail(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.LOADING
            try {
                repository.auth.signInWithEmailAndPassword(email, pass).await()
                _authState.value = AuthState.SUCCESS
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    fun registerWithEmail(name: String, email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.LOADING
            try {
                val result = repository.auth.createUserWithEmailAndPassword(email, pass).await()
                val uid = result.user?.uid ?: throw Exception("Failed to retrieve UID")

                repository.saveUserToFirestore(
                    uid = uid,
                    name = name,
                    email = email,
                    phone = "",
                    provider = "email"
                )
                _authState.value = AuthState.SUCCESS
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    fun startPhoneLogin(phone: String, activity: Activity) {
        currentPhone = phone
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
                    handleException(e)
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    storedVerificationId = verificationId
                    resendToken = token
                    _authState.value = AuthState.PHONE_OTP
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyOtp(code: String) {
        _authState.value = AuthState.LOADING
        try {
            val credential = PhoneAuthProvider.getCredential(storedVerificationId, code)
            signInWithPhoneAuthCredential(credential)
        } catch (e: Exception) {
            handleException(e)
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            try {
                val result = repository.auth.signInWithCredential(credential).await()
                val uid = result.user?.uid ?: throw Exception("Failed to retrieve UID")

                repository.saveUserToFirestore(
                    uid = uid,
                    name = "Phone User",
                    email = "",
                    phone = currentPhone,
                    provider = "phone"
                )
                _authState.value = AuthState.SUCCESS
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    private fun handleException(e: Exception) {
        _errorMessage.value = when (e) {
            is FirebaseAuthWeakPasswordException -> "Password is too weak"
            is FirebaseAuthInvalidCredentialsException -> "Invalid email or bad credentials"
            else -> e.localizedMessage ?: "An unknown error occurred"
        }
        _authState.value = AuthState.ERROR
    }
}
