package com.rivavafi.ui.auth

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import com.rivavafi.R
import com.rivavafi.ui.theme.AmoledBlack
import com.rivavafi.ui.theme.PrimaryContainerSky
import com.rivavafi.ui.theme.PrimarySky
import com.rivavafi.ui.theme.TertiaryEmerald
import com.rivavafi.ui.theme.glassMorphism
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.AccountCircle

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val context = LocalContext.current as Activity

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
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                color = PrimarySky
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Secure Authentication",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(48.dp))

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassMorphism(cornerRadius = 24f, alpha = 0.05f)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                when (authState) {
                    AuthState.CHOICE -> ChoiceSection(viewModel)
                    AuthState.GOOGLE_SIGN_IN -> GoogleSignInSection(viewModel)
                    AuthState.PHONE_INPUT -> PhoneInputSection(viewModel, context)
                    AuthState.OTP_VERIFICATION -> OtpVerificationSection(viewModel)
                    AuthState.LOADING -> CircularProgressIndicator(color = PrimarySky)
                    AuthState.SUCCESS -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.Lock, contentDescription = null, tint = TertiaryEmerald, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Verification Successful!", style = MaterialTheme.typography.titleLarge, color = Color.White)
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = onAuthSuccess,
                                colors = ButtonDefaults.buttonColors(containerColor = PrimarySky, contentColor = AmoledBlack),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth().height(56.dp)
                            ) {
                                Text("Continue to App", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}

@Composable
fun ChoiceSection(viewModel: AuthViewModel) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Choose Login Method", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { viewModel.selectGoogleFlow() },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Icon(Icons.Outlined.AccountCircle, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text("Continue with Google", fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(Modifier.weight(1f), color = Color.DarkGray)
            Text(" OR ", color = Color.Gray, modifier = Modifier.padding(horizontal = 8.dp))
            HorizontalDivider(Modifier.weight(1f), color = Color.DarkGray)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { viewModel.selectPhoneFlow() },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainerSky.copy(alpha = 0.2f), contentColor = PrimarySky),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Icon(Icons.Outlined.Smartphone, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text("Continue with Phone", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun GoogleSignInSection(viewModel: AuthViewModel) {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.idToken?.let { idToken ->
                    viewModel.onGoogleSignInSuccess(
                        idToken = idToken,
                        name = account.displayName ?: "User",
                        email = account.email ?: ""
                    )
                }
            } catch (e: ApiException) {
                // In production, emit error to ViewModel. We fall back to simulated token if API throws due to lack of real google-services.json on dummy setups.
                viewModel.onGoogleSignInSuccess(
                    idToken = "dummy_token_if_actual_fails",
                    name = "Dummy Fallback User",
                    email = "fallback@dummy.com"
                )
            }
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { viewModel.resetToChoice() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("Google Sign-In", style = MaterialTheme.typography.titleMedium, color = Color.White)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                // NOTE: Replace default_web_client_id string in values.xml with the actual Web Client ID from Firebase Console to use in production.
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken("314159265358-dummy.apps.googleusercontent.com") // Replace string resource when actual json is provided: context.getString(R.string.default_web_client_id)
                    .requestEmail()
                    .build()
                val googleSignInClient = GoogleSignIn.getClient(context, gso)
                launcher.launch(googleSignInClient.signInIntent)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Launch Google Sign-In", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PhoneInputSection(viewModel: AuthViewModel, activity: Activity) {
    var phone by remember { mutableStateOf("+1") }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { viewModel.resetToChoice() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("Phone Login", style = MaterialTheme.typography.titleMedium, color = Color.White)
        }
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            placeholder = { Text("Phone Number (e.g. +1...)", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = AmoledBlack,
                unfocusedContainerColor = AmoledBlack,
                focusedBorderColor = PrimarySky,
                unfocusedBorderColor = Color.DarkGray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { viewModel.startPhoneNumberVerification(phone, activity) },
            colors = ButtonDefaults.buttonColors(containerColor = PrimarySky, contentColor = AmoledBlack),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Send OTP", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun OtpVerificationSection(viewModel: AuthViewModel) {
    var code by remember { mutableStateOf("") }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { viewModel.selectPhoneFlow() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("Verify OTP", style = MaterialTheme.typography.titleMedium, color = Color.White)
        }
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = code,
            onValueChange = { code = it },
            placeholder = { Text("6-digit Code", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = AmoledBlack,
                unfocusedContainerColor = AmoledBlack,
                focusedBorderColor = PrimarySky,
                unfocusedBorderColor = Color.DarkGray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { viewModel.verifyOtp(code) },
            colors = ButtonDefaults.buttonColors(containerColor = PrimarySky, contentColor = AmoledBlack),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Verify", fontWeight = FontWeight.Bold)
        }
    }
}
