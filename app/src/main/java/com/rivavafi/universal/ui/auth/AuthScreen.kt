package com.rivavafi.universal.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.border
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rivavafi.universal.R
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import com.rivavafi.universal.ui.theme.AmoledBlack
import com.rivavafi.universal.ui.theme.PrimarySky
import com.rivavafi.universal.ui.theme.PrimaryContainerSky
import com.rivavafi.universal.ui.theme.glassMorphism
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.foundation.layout.Row
import kotlinx.coroutines.delay

tailrec fun android.content.Context.getActivity(): android.app.Activity? = when (this) {
    is android.app.Activity -> this
    is android.content.ContextWrapper -> baseContext.getActivity()
    else -> null
}

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AmoledBlack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Rivava+",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = PrimarySky
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Secure Authentication",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(48.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF161616))
                    .glassMorphism(cornerRadius = 24f)
                    .padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    errorMessage?.let {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    when (authState) {
                        AuthState.IDLE, AuthState.ERROR -> AuthMethodsSection(viewModel)
                        AuthState.LOADING -> CircularProgressIndicator(color = PrimarySky)
                        AuthState.SUCCESS -> {
                            LaunchedEffect(Unit) {
                                onAuthSuccess()
                            }
                            Text(
                                text = "Verification Successful!",
                                style = MaterialTheme.typography.titleMedium,
                                color = PrimarySky
                            )
                        }
                    }
                }
            }
        }
    }
}

enum class LoginMethod {
    EMAIL, PHONE
}

@Composable
fun AuthMethodsSection(viewModel: AuthViewModel) {
    var loginMethod by remember { mutableStateOf(LoginMethod.EMAIL) }
    var isLoginMode by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val phoneAuthState by viewModel.phoneAuthState.collectAsState()

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(
                onClick = {
                    viewModel.resetState()
                    loginMethod = LoginMethod.EMAIL
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Email", color = if (loginMethod == LoginMethod.EMAIL) PrimarySky else MaterialTheme.colorScheme.onSurfaceVariant)
            }
            TextButton(
                onClick = {
                    viewModel.resetState()
                    loginMethod = LoginMethod.PHONE
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Phone", color = if (loginMethod == LoginMethod.PHONE) PrimarySky else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // We will store the current phone number globally for resend capability
        var currentPhoneNumber by remember { mutableStateOf("") }
        if (loginMethod == LoginMethod.EMAIL) {
            OutlinedTextField(
                value = email,
                onValueChange = {
                    viewModel.resetState()
                    email = it
                },
                label = { Text("Email", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimarySky,
                    focusedLabelColor = PrimarySky,
                    cursorColor = PrimarySky
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = {
                    viewModel.resetState()
                    password = it
                },
                label = { Text("Password", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimarySky,
                    focusedLabelColor = PrimarySky,
                    cursorColor = PrimarySky
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (isLoginMode) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { viewModel.onForgotPassword(email) }) {
                        Text("Forgot Password?", color = PrimarySky)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (isLoginMode) {
                        viewModel.onEmailLogin(email, password)
                    } else {
                        viewModel.onEmailRegister(email, password)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(
                        brush = Brush.linearGradient(listOf(PrimarySky, PrimaryContainerSky)),
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                Icon(Icons.Outlined.Email, contentDescription = null, tint = AmoledBlack)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isLoginMode) "Sign in with Email" else "Register with Email", fontWeight = FontWeight.Bold, color = AmoledBlack)
            }

            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = {
                viewModel.resetState()
                isLoginMode = !isLoginMode
            }) {
                Text(if (isLoginMode) "Don't have an account? Register" else "Already have an account? Login", color = PrimarySky)
            }
        } else {
            if (phoneAuthState == PhoneAuthState.IDLE || phoneAuthState == PhoneAuthState.ERROR) {
                PhoneSignInSection(viewModel) { num -> currentPhoneNumber = num }
            } else if (phoneAuthState == PhoneAuthState.CODE_SENT) {
                var otp by remember { mutableStateOf("") }
                var timer by remember { mutableStateOf(60) }
                val context = LocalContext.current

                LaunchedEffect(Unit) {
                    while (timer > 0) {
                        delay(1000)
                        timer--
                    }
                }

                Text(
                    "Enter 6-digit OTP",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OtpInputField(otpText = otp, onOtpChange = { otp = it })
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        if (otp.length == 6) {
                            viewModel.verifyOtp(otp)
                        } else {
                            viewModel.setErrorMessage("Please enter all 6 digits")
                        }
                    },
                    enabled = otp.length == 6,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimarySky, contentColor = AmoledBlack),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("Verify OTP", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = {
                        context.getActivity()?.let { viewModel.resendOtp(currentPhoneNumber, it) }
                    },
                    enabled = timer == 0
                ) {
                    Text(if (timer > 0) "Resend OTP in ${timer}s" else "Resend OTP", color = if (timer > 0) MaterialTheme.colorScheme.onSurfaceVariant else PrimarySky)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("OR", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(16.dp))

        GoogleSignInSection(viewModel)
    }
}

@Composable
fun PhoneSignInSection(viewModel: AuthViewModel, onPhoneNumberSubmit: (String) -> Unit) {
    var phoneNumber by remember { mutableStateOf("") }
    val context = LocalContext.current

    OutlinedTextField(
        value = phoneNumber,
        onValueChange = { phoneNumber = it },
        label = { Text("Phone Number", color = MaterialTheme.colorScheme.onSurfaceVariant) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimarySky,
            focusedLabelColor = PrimarySky,
            cursorColor = PrimarySky
        ),
        modifier = Modifier.fillMaxWidth(),
        leadingIcon = {
            Text("+91", modifier = Modifier.padding(start = 16.dp, end = 8.dp), color = MaterialTheme.colorScheme.onSurface)
        }
    )
    Spacer(modifier = Modifier.height(24.dp))
    Button(
        onClick = {
            val digitsOnly = phoneNumber.replace(Regex("\\D"), "")
            val cleanNumber = if (digitsOnly.startsWith("91") && digitsOnly.length > 10) {
                digitsOnly.substring(2)
            } else {
                digitsOnly
            }
            val formattedNumber = "+91$cleanNumber"
            onPhoneNumberSubmit(formattedNumber)
            context.getActivity()?.let {
                viewModel.startPhoneVerification(formattedNumber, it)
            } ?: viewModel.setErrorMessage("Error: Could not retrieve Activity context for Phone Auth.")
        },
        colors = ButtonDefaults.buttonColors(containerColor = PrimarySky, contentColor = AmoledBlack),
        modifier = Modifier.fillMaxWidth().height(56.dp)
    ) {
        Icon(Icons.Outlined.Phone, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Send OTP", fontWeight = FontWeight.Bold)
    }
}

@Composable
fun OtpInputField(otpText: String, onOtpChange: (String) -> Unit) {
    BasicTextField(
        value = otpText,
        onValueChange = {
            if (it.length <= 6) {
                onOtpChange(it)
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
        decorationBox = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(6) { index ->
                    val char = when {
                        index >= otpText.length -> ""
                        else -> otpText[index].toString()
                    }
                    val isFocused = otpText.length == index
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .border(
                                width = if (isFocused) 2.dp else 1.dp,
                                color = if (isFocused) PrimarySky else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .background(
                                color = if (isFocused) PrimarySky.copy(alpha = 0.1f) else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun GoogleSignInSection(viewModel: AuthViewModel) {
    val context = LocalContext.current

    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
            if (account != null && account.idToken != null) {
                viewModel.onGoogleSignInSuccess(
                    idToken = account.idToken!!,
                    name = account.displayName ?: "User",
                    email = account.email ?: "",
                    photoUrl = account.photoUrl?.toString() ?: ""
                )
            } else {
                viewModel.setErrorMessage("Sign-in failed: ID Token is null")
            }
        } catch (e: com.google.android.gms.common.api.ApiException) {
            viewModel.setErrorMessage("Google Sign-in failed (Code: ${e.statusCode}): ${e.message}")
        }
    }

    Button(
        onClick = {
            val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val googleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(context, gso)
            // Force sign out to clear stuck cache and show account picker
            googleSignInClient.signOut().addOnCompleteListener {
                launcher.launch(googleSignInClient.signInIntent)
            }
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Icon(Icons.Outlined.AccountCircle, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Sign in with Google", fontWeight = FontWeight.SemiBold)
    }
}
