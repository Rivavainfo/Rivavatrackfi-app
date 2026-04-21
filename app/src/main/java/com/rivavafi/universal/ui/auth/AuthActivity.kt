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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
import androidx.compose.foundation.ExperimentalFoundationApi
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.rivavafi.universal.HomeActivity
import com.rivavafi.universal.R
import com.rivavafi.universal.ui.theme.*
import com.rivavafi.universal.ui.theme.glassMorphism
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
            val profileNameMissing = user.displayName.isNullOrBlank()

            if (isPhoneAuth || isGoogleAuth || user.isEmailVerified) {
                goToHome(profileNameMissing)
                return
            }
        }

        handleIntent(intent)

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

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { handleIntent(it) }
    }

    private fun handleIntent(intent: Intent) {
        val action = intent.action
        val data = intent.data

        if (Intent.ACTION_VIEW == action && data != null) {
            val mode = data.getQueryParameter("mode")
            val oobCode = data.getQueryParameter("oobCode")

            if (oobCode != null) {
                when (mode) {
                    "verifyEmail" -> {
                        viewModel.verifyEmailActionCode(oobCode) {
                            Toast.makeText(this, "Email verified successfully!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    "resetPassword" -> {
                        val resetIntent = Intent(this, SetNewPasswordActivity::class.java).apply {
                            putExtra("oobCode", oobCode)
                        }
                        startActivity(resetIntent)
                    }
                    else -> {
                        // Fallback for paths if mode is not explicitly passed by Firebase
                        if (data.path?.contains("/verify") == true) {
                            viewModel.verifyEmailActionCode(oobCode) {
                                Toast.makeText(this, "Email verified successfully!", Toast.LENGTH_SHORT).show()
                            }
                        } else if (data.path?.contains("/reset") == true) {
                            val resetIntent = Intent(this, SetNewPasswordActivity::class.java).apply {
                                putExtra("oobCode", oobCode)
                            }
                            startActivity(resetIntent)
                        }
                    }
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
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
fun AuthScreenContent(
    viewModel: AuthViewModel,
    onLoginSuccess: (Boolean) -> Unit,
    onNavigateToOtp: (String, String) -> Unit,
    onNavigateToReset: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var isLoginMode by remember { mutableStateOf(true) }
    var phoneNumber by remember { mutableStateOf("") }
    var authMethod by remember { mutableStateOf("INITIAL") } // INITIAL, EMAIL_LOGIN, EMAIL_REGISTER, PHONE
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Moving Image Carousel
            val images = listOf(
                "https://images.unsplash.com/photo-1611974789855-9c2a0a7236a3?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1590283603385-17ffb3a7f29f?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1460925895917-afdab827c52f?auto=format&fit=crop&w=800&q=80"
            )
            val titles = listOf("Track smarter", "Invest better", "Grow faster")

            val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { images.size })

            LaunchedEffect(Unit) {
                while (true) {
                    kotlinx.coroutines.delay(3000)
                    val nextPage = (pagerState.currentPage + 1) % images.size
                    pagerState.animateScrollToPage(nextPage)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.4f)
            ) {
                androidx.compose.foundation.pager.HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        coil.compose.AsyncImage(
                            model = images[page],
                            contentDescription = null,
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color(0xFF0A0A0A)),
                                        startY = 100f
                                    )
                                )
                        )
                        Text(
                            text = titles[page],
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            ),
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Glassmorphism Login Container
            androidx.compose.material3.Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp)),
                colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color(0xFF111111)),
                elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (authMethod == "INITIAL") {
                        // Email Login Button
                        Button(
                            onClick = { authMethod = "EMAIL_LOGIN" },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6), contentColor = Color.White),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp, pressedElevation = 1.dp),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Icon(Icons.Outlined.Email, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Continue with Email", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }

                        // Google Login Button
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp, pressedElevation = 1.dp),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AccountCircle,
                                contentDescription = "Google",
                                modifier = Modifier.size(24.dp),
                                tint = AmoledBlack
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Continue with Google", color = AmoledBlack, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }

                        // Phone Login Button
                        Button(
                            onClick = { authMethod = "PHONE" },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34D399), contentColor = Color.White),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp, pressedElevation = 1.dp),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Icon(Icons.Outlined.Phone, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Continue with Phone", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(onClick = { authMethod = "EMAIL_REGISTER" }) {
                            Text("Don't have an account? Sign up", color = Color(0xFF00A3FF), fontWeight = FontWeight.SemiBold)
                        }
                    } else if (authMethod == "EMAIL_LOGIN" || authMethod == "EMAIL_REGISTER") {
                        val isRegister = authMethod == "EMAIL_REGISTER"
                        Text(
                            text = if (isRegister) "Sign Up" else "Sign In",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )

                        if (isRegister) {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Full Name", color = Color.White.copy(0.7f)) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF3B82F6),
                                    unfocusedBorderColor = Color.White.copy(0.2f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp)
                            )
                        }

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email", color = Color.White.copy(0.7f)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF3B82F6),
                                unfocusedBorderColor = Color.White.copy(0.2f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password", color = Color.White.copy(0.7f)) },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF3B82F6),
                                unfocusedBorderColor = Color.White.copy(0.2f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        )

                        if (!isRegister) {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                                TextButton(onClick = onNavigateToReset) {
                                    Text("Forgot Password?", color = Color(0xFF00A3FF))
                                }
                            }
                        }

                        Button(
                            onClick = {
                                if (!isRegister) {
                                    viewModel.onEmailLogin(email, password)
                                } else {
                                    viewModel.onEmailRegister(email, password) {
                                        showEmailVerificationUI = true
                                        // Update name in local preferences if required, currently not explicitly handled in original save logic unless returning from verify
                                    }
                                }
                            },
                            enabled = authState != AuthState.LOADING,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6), contentColor = Color.White),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp, pressedElevation = 1.dp),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            if (authState == AuthState.LOADING) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text(if (isRegister) "Create Account" else "Sign In", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                            }
                        }

                        TextButton(onClick = { authMethod = if (isRegister) "EMAIL_LOGIN" else "EMAIL_REGISTER" }) {
                            Text(if (isRegister) "Already have an account? Sign In" else "Don't have an account? Sign up", color = Color(0xFF00A3FF))
                        }

                        TextButton(onClick = { authMethod = "INITIAL" }) {
                            Text("Back to options", color = Color.White.copy(alpha = 0.7f))
                        }
                    } else if (authMethod == "PHONE") {
                        Text(
                            text = "Phone Sign In",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = { Text("Phone Number", color = Color.White.copy(0.7f)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF3B82F6),
                                unfocusedBorderColor = Color.White.copy(0.2f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Text("+91", modifier = Modifier.padding(start = 16.dp, end = 8.dp), color = Color.White)
                            },
                            shape = RoundedCornerShape(16.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34D399), contentColor = Color.White),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp, pressedElevation = 1.dp),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            if (authState == AuthState.LOADING) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text("Send OTP", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                            }
                        }

                        TextButton(onClick = { authMethod = "EMAIL_REGISTER" }) {
                            Text("Don't have an account? Sign up", color = Color(0xFF00A3FF))
                        }

                        TextButton(onClick = { authMethod = "INITIAL" }) {
                            Text("Back to options", color = Color.White.copy(alpha = 0.7f))
                        }
                    }
                }
            }
        }
    }
}
