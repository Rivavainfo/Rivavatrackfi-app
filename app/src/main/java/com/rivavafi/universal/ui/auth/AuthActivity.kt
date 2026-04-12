package com.rivavafi.universal.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.rivavafi.universal.HomeActivity
import com.rivavafi.universal.R
import com.rivavafi.universal.ui.theme.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AuthActivity : ComponentActivity() {

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val providerId = user.providerData.firstOrNull()?.providerId
            val isPhoneAuth = providerId == "phone" || user.phoneNumber != null
            val isGoogleAuth = providerId == "google.com"

            if (isPhoneAuth || isGoogleAuth || user.isEmailVerified) {
                goToHome(false)
                return
            }
        }

        setContent {
            TrackFiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = AmoledBlack
                ) {
                    AuthScreenContent(
                        viewModel = viewModel,
                        onLoginSuccess = { isNewUser -> goToHome(isNewUser) },
                        onNavigateToOtp = { verificationId, phone -> goToOtp(verificationId, phone) },
                        onNavigateToReset = { goToResetPassword() }
                    )
                }
            }
        }
    }

    private fun goToHome(isNewUser: Boolean) {
        val intent = Intent(this, HomeActivity::class.java).apply {
            putExtra("isNewUser", isNewUser)
        }
        startActivity(intent)
        finish()
    }

    private fun goToOtp(verificationId: String, phone: String) {
        val intent = Intent(this, OtpActivity::class.java).apply {
            putExtra("verificationId", verificationId)
            putExtra("phone", phone)
        }
        startActivity(intent)
    }

    private fun goToResetPassword() {
        val intent = Intent(this, ResetPasswordActivity::class.java)
        startActivity(intent)
    }
}

@Composable
fun AuthScreenContent(
    viewModel: AuthViewModel,
    onLoginSuccess: (Boolean) -> Unit,
    onNavigateToOtp: (String, String) -> Unit,
    onNavigateToReset: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoginMode by remember { mutableStateOf(true) }
    var phoneNumber by remember { mutableStateOf("") }
    var authMethod by remember { mutableStateOf("EMAIL") } // EMAIL or PHONE
    var showEmailVerificationUI by remember { mutableStateOf(false) }

    val authState by viewModel.authState.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isNewUser by viewModel.isNewUser.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(authState) {
        if (authState == AuthState.SUCCESS) {
            onLoginSuccess(isNewUser ?: false)
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            if (it.isNotEmpty()) {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                viewModel.setErrorMessage("") // Clear after showing
            }
        }
    }

    if (showEmailVerificationUI) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Check your email and click verify",
                style = MaterialTheme.typography.titleLarge,
                color = PrimarySky,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    viewModel.checkEmailVerified(
                        onVerified = { onLoginSuccess(isNewUser ?: false) },
                        onNotVerified = { }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimarySky, contentColor = AmoledBlack)
            ) {
                Text("I have verified", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = { showEmailVerificationUI = false }) {
                Text("Back to Login", color = PrimarySky)
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo Placeholder
        Text(
            text = "Rivava+",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = PrimarySky
        )
        Spacer(modifier = Modifier.height(32.dp))

        if (authMethod == "EMAIL") {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimarySky,
                    focusedLabelColor = PrimarySky,
                    cursorColor = PrimarySky
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimarySky,
                    focusedLabelColor = PrimarySky,
                    cursorColor = PrimarySky
                ),
                modifier = Modifier.fillMaxWidth()
            )

            if (isLoginMode) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    TextButton(onClick = onNavigateToReset) {
                        Text("Forgot Password?", color = PrimarySky)
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = {
                    if (isLoginMode) {
                        viewModel.onEmailLogin(email, password)
                    } else {
                        viewModel.onEmailRegister(email, password) {
                            showEmailVerificationUI = true
                        }
                    }
                },
                enabled = authState != AuthState.LOADING,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(
                        brush = Brush.linearGradient(listOf(PrimarySky, PrimaryContainerSky)),
                        shape = RoundedCornerShape(16.dp)
                    ),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                if (authState == AuthState.LOADING) {
                    CircularProgressIndicator(color = AmoledBlack, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Outlined.Email, contentDescription = null, tint = AmoledBlack)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isLoginMode) "Sign in with Email" else "Register with Email", fontWeight = FontWeight.Bold, color = AmoledBlack)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = { isLoginMode = !isLoginMode }) {
                Text(if (isLoginMode) "Don't have an account? Register" else "Already have an account? Login", color = PrimarySky)
            }

            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = { authMethod = "PHONE" }) {
                Text("Use Phone Number instead", color = PrimarySky)
            }
        } else {
            // Phone Auth Section
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
                    (context as? android.app.Activity)?.let { act ->
                        viewModel.startPhoneVerification(formattedNumber, act) { verificationId ->
                            onNavigateToOtp(verificationId, formattedNumber)
                        }
                    }
                },
                enabled = authState != AuthState.LOADING,
                colors = ButtonDefaults.buttonColors(containerColor = PrimarySky, contentColor = AmoledBlack),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (authState == AuthState.LOADING) {
                    CircularProgressIndicator(color = AmoledBlack, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Outlined.Phone, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Send OTP", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = { authMethod = "EMAIL" }) {
                Text("Use Email instead", color = PrimarySky)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            Text(" OR ", color = MaterialTheme.colorScheme.onSurfaceVariant)
            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Google Sign-In
        val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
            contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
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
            } catch (e: ApiException) {
                viewModel.setErrorMessage("Google Sign-in failed (Code: ${e.statusCode}): ${e.message}")
            }
        }

        Button(
            onClick = {
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(context.getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
                val googleSignInClient = GoogleSignIn.getClient(context, gso)
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
}
