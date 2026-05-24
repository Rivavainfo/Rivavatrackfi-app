package com.rivavafi.universal.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.GoogleAuthProvider
import com.rivavafi.universal.data.preferences.UserPreferencesRepository
import com.rivavafi.universal.data.repository.AuthRepository
import com.rivavafi.universal.data.repository.UserEntitlementRepository
import com.google.firebase.FirebaseException
import java.util.concurrent.TimeUnit
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import com.rivavafi.universal.data.model.User
import com.rivavafi.universal.data.repository.UserRepository
import com.rivavafi.universal.data.repository.FirebaseUserManager
import com.rivavafi.universal.data.model.UserModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class AuthFormState(
    val emailError: String? = null,
    val phoneError: String? = null,
    val isFormValid: Boolean = false
)

enum class AuthState {
    IDLE,
    LOADING,
    SUCCESS,
    ERROR
}

enum class PhoneAuthState {
    IDLE,
    CODE_SENT,
    SUCCESS,
    ERROR
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val userEntitlementRepository: UserEntitlementRepository,
    private val userRepository: UserRepository,
    @ApplicationContext private val context: Context,
    private val firebaseUserManager: FirebaseUserManager
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState.IDLE)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _phoneAuthState = MutableStateFlow(PhoneAuthState.IDLE)
    val phoneAuthState: StateFlow<PhoneAuthState> = _phoneAuthState.asStateFlow()

    private val _isNewUser = MutableStateFlow<Boolean?>(null)
    val isNewUser: StateFlow<Boolean?> = _isNewUser.asStateFlow()

    private val _authFormState = MutableStateFlow(AuthFormState())
    val authFormState: StateFlow<AuthFormState> = _authFormState.asStateFlow()

    private fun restoreUserSession(sessionState: com.rivavafi.universal.data.repository.AuthRepository.UserSessionState) {
        viewModelScope.launch {
            if (!sessionState.existingName.isNullOrBlank()) {
                userPreferencesRepository.saveUserName(sessionState.existingName)
            }
            if (!sessionState.photoUrl.isNullOrBlank()) {
                userPreferencesRepository.setProfileImageUri(sessionState.photoUrl)
            }
            if (sessionState.onboardingCompleted) {
                userPreferencesRepository.setOnboardingCompleted(true)
            }
            userEntitlementRepository.syncEntitlement()
            _authState.value = AuthState.SUCCESS
        }
    }

    init {
        val user = repository.auth.currentUser
        if (user != null) {
            // APP START LOGIC: If logged in, update cache
            val appUser = User(
                uid = user.uid,
                name = user.displayName,
                email = user.email,
                photo = user.photoUrl?.toString(),
                phone = user.phoneNumber
            )
            userRepository.cacheUserLocally(context, appUser)

            _isNewUser.value = false // if already logged in, they are not a new user
            viewModelScope.launch {
                val providerId = user.providerData.firstOrNull()?.providerId
                val isEmailAuth = providerId == "password" || providerId == "email"

                if (isEmailAuth) {
                    val isVerified = repository.checkVerificationStatus(user.uid)
                    if (isVerified || user.isEmailVerified) {

                        userEntitlementRepository.syncEntitlement()
                        _authState.value = AuthState.SUCCESS
                    } else {
                        // User is signed in but not verified. Do not sign out so they can resend.
                        _errorMessage.value = "Please verify your email before continuing."
                        _authState.value = AuthState.IDLE
                    }
                } else {

                        userEntitlementRepository.syncEntitlement()
                        _authState.value = AuthState.SUCCESS
                }
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.IDLE
        _phoneAuthState.value = PhoneAuthState.IDLE
        _errorMessage.value = null
    }

    fun setErrorMessage(message: String) {
        _errorMessage.value = message
        _authState.value = AuthState.IDLE // Maintain IDLE state so UI fields remain accessible
    }

    fun getCachedUser(): User? {
        return userRepository.getCachedUser(context)
    }

    fun onGoogleSignInSuccess(idToken: String, name: String, email: String, photoUrl: String = "") {
        viewModelScope.launch {
            _authState.value = AuthState.LOADING
            try {
                Log.d("AuthViewModel", "Successfully extracted ID Token, exchanging with Firebase...")
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = repository.auth.signInWithCredential(firebaseCredential).await()

                // Set state back to IDLE so the UI can proceed to PROFILE_COMPLETION
                _authState.value = AuthState.IDLE
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Google Sign-in failed", e)
                _errorMessage.value = e.message ?: "Login failed"
                _authState.value = AuthState.IDLE
            }
        }
    }

    fun saveUserProfileCompletion(name: String, phone: String, preference: String, email: String, photoUrl: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _authState.value = AuthState.LOADING
            try {
                val user = repository.auth.currentUser ?: throw Exception("User not authenticated")
                val uid = user.uid

                val theDataUser = UserModel(
                    uid = uid,
                    name = name,
                    email = email,
                    phone = phone,
                    preference = preference,
                    profileImage = photoUrl.ifBlank { null },
                    loginProvider = "google",
                    isPhoneVerified = false // By default from Google
                )
                firebaseUserManager.saveUserToFirestore(theDataUser)

                val sessionState = repository.saveUserToFirestore(
                    uid = uid,
                    name = name,
                    email = email,
                    phoneNumber = phone,
                    authProvider = "google",
                    isVerified = true,
                    photoUrl = photoUrl
                )

                val isNew = sessionState.isNewUser
                _isNewUser.value = isNew

                userPreferencesRepository.saveUserName(name)
                if (photoUrl.isNotBlank()) {
                    userPreferencesRepository.setProfileImageUri(photoUrl)
                }

                val appUser = User(
                    uid = uid,
                    name = name,
                    email = email,
                    photo = photoUrl.ifBlank { null },
                    phone = phone
                )
                userRepository.saveUserToFirestore(appUser)
                userRepository.cacheUserLocally(context, appUser)

                if (sessionState.onboardingCompleted) {
                    userPreferencesRepository.setOnboardingCompleted(true)
                }

                userEntitlementRepository.syncEntitlement()
                _authState.value = AuthState.SUCCESS
                onComplete(isNew)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Profile completion failed", e)
                _errorMessage.value = e.message ?: "Failed to save profile"
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


                val uid = repository.auth.currentUser?.uid ?: throw Exception("Failed to retrieve UID")
                val isVerified = repository.checkVerificationStatus(uid)

                // SAVE TO THEDATA (handles both create if not exists and update lastLoginAt if exists)
                val theDataUser = UserModel(
                    uid = uid,
                    email = email,
                    loginProvider = "email",
                    isPhoneVerified = false
                )
                firebaseUserManager.saveUserToFirestore(theDataUser)

                if (isVerified) {

                    val sessionState = repository.saveUserToFirestore(
                        uid = uid,
                        name = null,
                        email = email,
                        phoneNumber = null,
                        authProvider = "email",
                        isVerified = true
                    )
                    val firebaseNew = false
                    val isNew = sessionState.isNewUser || firebaseNew
                    _isNewUser.value = isNew
                    if (isNew) {
                        repository.auth.currentUser?.let { repository.sendUserToSheet(it, "Email") }
                    } else {
                        if (!sessionState.existingName.isNullOrBlank()) {
                            userPreferencesRepository.saveUserName(sessionState.existingName)
                        }
                        if (!sessionState.photoUrl.isNullOrBlank()) {
                            userPreferencesRepository.setProfileImageUri(sessionState.photoUrl)
                        }
                    }
                    if (sessionState.onboardingCompleted) {
                        userPreferencesRepository.setOnboardingCompleted(true)
                    }

                    userEntitlementRepository.syncEntitlement()
                    _authState.value = AuthState.SUCCESS
                } else {
                    // Do NOT sign out. The user needs an active session to use 'Resend Verification Email'.
                    _errorMessage.value = "Please verify your email before logging in."
                    _authState.value = AuthState.IDLE
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Email login failed", e)
                _errorMessage.value = e.message ?: "Login failed"
                _authState.value = AuthState.IDLE
            }
        }
    }

    fun onForgotPassword(email: String, onSuccess: () -> Unit) {
        if (email.isBlank()) {
            _errorMessage.value = "Please enter your email to reset your password."
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.LOADING
            try {
                repository.auth.sendPasswordResetEmail(email).await()
                _errorMessage.value = "Reset link sent to your email"
                _authState.value = AuthState.IDLE
                onSuccess()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Password reset failed", e)
                _errorMessage.value = e.message ?: "Failed to send reset email."
                _authState.value = AuthState.IDLE
            }
        }
    }

    fun onEmailRegister(email: String, pass: String, name: String, onVerificationSent: () -> Unit) {
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
            if (repository.checkEmailExists(email)) {
                _errorMessage.value = "Email is already registered. Please sign in."
                _authState.value = AuthState.IDLE
                return@launch
            }

            try {
                Log.d("AuthViewModel", "Attempting email registration for: $email")
                val result = repository.auth.createUserWithEmailAndPassword(email, pass).await()
                val uid = result.user?.uid ?: throw Exception("Failed to retrieve UID")


                Log.d("AuthViewModel", "Registration successful. Saving to Firestore and Sheets...")

                // SAVE TO THEDATA
                val theDataUser = UserModel(
                    uid = uid,
                    name = name.ifBlank { null },
                    email = email,
                    loginProvider = "email",
                    isPhoneVerified = false
                )
                firebaseUserManager.saveUserToFirestore(theDataUser)

                repository.saveUserToFirestore(
                    uid = uid,
                    name = name.ifBlank { null },
                    email = email,
                    phoneNumber = null,
                    authProvider = "email",
                    isVerified = false
                )
                userPreferencesRepository.saveUserName(name)

                repository.sendVerificationEmail(email, uid)

                _errorMessage.value = "Registration successful. Please check your inbox to verify."
                _authState.value = AuthState.IDLE
                onVerificationSent()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Email registration failed", e)
                _errorMessage.value = e.message ?: "Registration failed"
                _authState.value = AuthState.IDLE
            }
        }
    }

    fun verifyEmailActionCode(oobCode: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _authState.value = AuthState.LOADING
            try {
                repository.auth.applyActionCode(oobCode).await()
                _authState.value = AuthState.IDLE
                onSuccess()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Failed to verify email with oobCode", e)
                _errorMessage.value = "Failed to verify email: ${e.message}"
                _authState.value = AuthState.IDLE
            }
        }
    }

    fun resetPasswordWithActionCode(oobCode: String, newPassword: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _authState.value = AuthState.LOADING
            try {
                repository.auth.confirmPasswordReset(oobCode, newPassword).await()
                _authState.value = AuthState.IDLE
                onSuccess()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Failed to reset password with oobCode", e)
                _errorMessage.value = "Failed to reset password: ${e.message}"
                _authState.value = AuthState.IDLE
            }
        }
    }

    fun checkEmailVerified(onVerified: () -> Unit, onNotVerified: () -> Unit) {
        viewModelScope.launch {
            _authState.value = AuthState.LOADING
            try {
                val uid = repository.auth.currentUser?.uid
                if (uid != null) {
                    val isVerified = repository.checkVerificationStatus(uid)
                    if (isVerified) {

                        val sessionState = repository.saveUserToFirestore(
                            uid = uid,
                            name = null,
                            email = repository.auth.currentUser?.email ?: "",
                            phoneNumber = null,
                            authProvider = "email",
                            isVerified = true
                        )
                        val firebaseNew = false
                        val isNew = sessionState.isNewUser || firebaseNew
                        _isNewUser.value = isNew
                        if (isNew) {
                            repository.auth.currentUser?.let { repository.sendUserToSheet(it, "Email") }
                        } else {
                            if (!sessionState.existingName.isNullOrBlank()) {
                                userPreferencesRepository.saveUserName(sessionState.existingName)
                            }
                            if (!sessionState.photoUrl.isNullOrBlank()) {
                                userPreferencesRepository.setProfileImageUri(sessionState.photoUrl)
                            }
                        }
                        if (sessionState.onboardingCompleted) {
                            userPreferencesRepository.setOnboardingCompleted(true)
                        }

                        userEntitlementRepository.syncEntitlement()
                        _authState.value = AuthState.SUCCESS
                        onVerified()
                    } else {
                        _errorMessage.value = "Email not verified yet. Please check your inbox."
                        _authState.value = AuthState.IDLE
                        onNotVerified()
                    }
                } else {
                    _errorMessage.value = "Failed to get user. Please try logging in again."
                    _authState.value = AuthState.IDLE
                    onNotVerified()
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Failed to check email verification", e)
                _errorMessage.value = "Failed to verify email status: ${e.message}"
                _authState.value = AuthState.IDLE
                onNotVerified()
            }
        }
    }

    fun startPhoneVerification(phoneNumber: String, onCodeSentCallback: (String) -> Unit) {
        if (phoneNumber.isBlank()) {
            _errorMessage.value = "Invalid phone number"
            return
        }

        _authState.value = AuthState.LOADING
        viewModelScope.launch {
            try {
                Log.d("AuthViewModel", "Initiating send OTP API request for phone: $phoneNumber")
                val result = repository.sendOtp(phoneNumber)
                if (result.isSuccess) {
                    Log.d("AuthViewModel", "Successfully sent OTP to $phoneNumber")
                    _phoneAuthState.value = PhoneAuthState.CODE_SENT
                    _authState.value = AuthState.IDLE
                    onCodeSentCallback(phoneNumber)
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "Failed to send OTP due to an unknown API error"
                    Log.e("AuthViewModel", "API failure when sending OTP: $errorMsg", result.exceptionOrNull())
                    _errorMessage.value = errorMsg
                    _phoneAuthState.value = PhoneAuthState.ERROR
                    _authState.value = AuthState.IDLE
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Network or unexpected exception during send OTP: ${e.message}", e)
                _errorMessage.value = "Network error: unable to send OTP. Please try again."
                _phoneAuthState.value = PhoneAuthState.ERROR
                _authState.value = AuthState.IDLE
            }
        }
    }

    fun resendOtp(phoneNumber: String, onCodeSentCallback: (String) -> Unit) {
        if (phoneNumber.isBlank()) {
            _errorMessage.value = "Invalid phone number"
            return
        }
        _authState.value = AuthState.LOADING
        viewModelScope.launch {
            try {
                Log.d("AuthViewModel", "Initiating resend OTP API request for phone: $phoneNumber")
                val result = repository.sendOtp(phoneNumber)
                if (result.isSuccess) {
                    Log.d("AuthViewModel", "Successfully resent OTP to $phoneNumber")
                    _phoneAuthState.value = PhoneAuthState.CODE_SENT
                    _authState.value = AuthState.IDLE
                    onCodeSentCallback(phoneNumber)
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "Failed to resend OTP due to an unknown API error"
                    Log.e("AuthViewModel", "API failure when resending OTP: $errorMsg", result.exceptionOrNull())
                    _errorMessage.value = errorMsg
                    _phoneAuthState.value = PhoneAuthState.ERROR
                    _authState.value = AuthState.IDLE
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Network or unexpected exception during resend OTP: ${e.message}", e)
                _errorMessage.value = "Network error: unable to resend OTP. Please try again."
                _phoneAuthState.value = PhoneAuthState.ERROR
                _authState.value = AuthState.IDLE
            }
        }
    }

    fun normalizePhoneNumber(input: String): String? {
        val digits = input.replace(Regex("\\D"), "")
        return when {
            digits.length == 10 -> "+91$digits"
            digits.length > 10 -> "+$digits"
            else -> null
        }
    }

    fun validatePhone(phone: String) {
        val e164Regex = Regex("^\\+[1-9]\\d{1,14}$")
        if (phone.isNotEmpty() && !e164Regex.matches(phone)) {
            _authFormState.value = _authFormState.value.copy(phoneError = "Invalid E.164 phone format (e.g. +14155552671)")
        } else {
            _authFormState.value = _authFormState.value.copy(phoneError = null)
        }
        updateFormValidity()
    }

    fun validateEmail(email: String) {
        if (email.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _authFormState.value = _authFormState.value.copy(emailError = "Invalid email address")
        } else {
            _authFormState.value = _authFormState.value.copy(emailError = null)
        }
        updateFormValidity()
    }

    private fun updateFormValidity() {
        val state = _authFormState.value
        _authFormState.value = state.copy(isFormValid = state.emailError == null && state.phoneError == null)
    }

    fun verifyOtp(otp: String, phoneNumber: String, email: String?, onSuccess: () -> Unit, onError: () -> Unit) {
        if (phoneNumber.isBlank() || otp.isBlank()) {
            _errorMessage.value = "Invalid OTP or missing phone number."
            onError()
            return
        }

        _authState.value = AuthState.LOADING
        viewModelScope.launch {
            try {
                Log.d("AuthViewModel", "Starting OTP verification for $phoneNumber")
                val result = repository.verifyOtpAndSignIn(phoneNumber, otp)
                if (result.isSuccess) {
                    Log.d("AuthViewModel", "OTP verified successfully. Retrieving UID...")
                    val uid = result.getOrNull() ?: throw Exception("Failed to retrieve UID")


                    Log.d("AuthViewModel", "UID retrieved: $uid. Saving user to Firestore...")

                    // SAVE TO THEDATA
                    val theDataUser = UserModel(
                        uid = uid,
                        email = email,
                        phone = phoneNumber,
                        loginProvider = "phone",
                        isPhoneVerified = true
                    )
                    firebaseUserManager.saveUserToFirestore(theDataUser)

                    val sessionState = repository.saveUserToFirestore(
                        uid = uid,
                        name = null,
                        email = email,
                        phoneNumber = phoneNumber,
                        authProvider = "phone",
                        isVerified = true
                    )

                    val isNew = sessionState.isNewUser
                    _isNewUser.value = isNew
                    if (phoneNumber.isNotBlank()) {
                        userPreferencesRepository.saveUserPhone(phoneNumber)
                    }
                    if (isNew) {
                        repository.auth.currentUser?.let { repository.sendUserToSheet(it, "Phone") }
                    } else {
                        if (!sessionState.existingName.isNullOrBlank()) {
                            userPreferencesRepository.saveUserName(sessionState.existingName)
                        }
                        if (!sessionState.photoUrl.isNullOrBlank()) {
                            userPreferencesRepository.setProfileImageUri(sessionState.photoUrl)
                        }
                    }
                    if (sessionState.onboardingCompleted) {
                        userPreferencesRepository.setOnboardingCompleted(true)
                    }

                    Log.d("AuthViewModel", "User session saved. Syncing entitlements...")
                    _phoneAuthState.value = PhoneAuthState.SUCCESS
                    userEntitlementRepository.syncEntitlement()
                    _authState.value = AuthState.SUCCESS
                    Log.d("AuthViewModel", "Sign-in complete. Executing success callback.")
                    onSuccess()
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "Verification failed"
                    Log.e("AuthViewModel", "verifyOtpAndSignIn failed with: $errorMsg")
                    throw result.exceptionOrNull() ?: Exception(errorMsg)
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "OTP verification failed. Exception: ${e.javaClass.simpleName}, Message: ${e.message}", e)
                _errorMessage.value = e.message ?: "Invalid OTP"
                _phoneAuthState.value = PhoneAuthState.ERROR
                _authState.value = AuthState.IDLE
                onError()
            }
        }
    }

    fun resendVerificationEmail() {
        viewModelScope.launch {
            _authState.value = AuthState.LOADING
            try {
                val user = repository.auth.currentUser
                if (user != null && user.email != null) {
                    repository.sendVerificationEmail(user.email!!, user.uid)
                    _errorMessage.value = "Verification email resent successfully."
                } else {
                    _errorMessage.value = "User not found. Please log in again."
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Failed to resend verification email", e)
                _errorMessage.value = "Failed to resend verification email."
            } finally {
                _authState.value = AuthState.IDLE
            }
        }
    }
}
