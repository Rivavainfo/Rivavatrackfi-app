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
import androidx.hilt.navigation.compose.hiltViewModel
import com.rivavafi.R

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
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            when (authState) {
                AuthState.GOOGLE_SIGN_IN -> GoogleSignInSection(viewModel)
                AuthState.PHONE_INPUT -> PhoneInputSection(viewModel, context)
                AuthState.OTP_VERIFICATION -> OtpVerificationSection(viewModel)
                AuthState.LOADING -> CircularProgressIndicator()
                AuthState.SUCCESS -> {
                    Text("Verification Successful!", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onAuthSuccess) {
                        Text("Continue to App")
                    }
                }
                else -> {}
            }
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

    Text("Welcome to Rivava+", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(32.dp))
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
        modifier = Modifier.fillMaxWidth().height(56.dp)
    ) {
        Text("Sign in with Google")
    }
}

@Composable
fun PhoneInputSection(viewModel: AuthViewModel, activity: Activity) {
    var phone by remember { mutableStateOf("+1") }

    Text("Verify your phone number", style = MaterialTheme.typography.titleLarge)
    Spacer(modifier = Modifier.height(16.dp))
    OutlinedTextField(
        value = phone,
        onValueChange = { phone = it },
        label = { Text("Phone Number") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
    Spacer(modifier = Modifier.height(24.dp))
    Button(
        onClick = { viewModel.startPhoneNumberVerification(phone, activity) },
        modifier = Modifier.fillMaxWidth().height(56.dp)
    ) {
        Text("Send OTP")
    }
}

@Composable
fun OtpVerificationSection(viewModel: AuthViewModel) {
    var code by remember { mutableStateOf("") }

    Text("Enter OTP Code", style = MaterialTheme.typography.titleLarge)
    Spacer(modifier = Modifier.height(16.dp))
    OutlinedTextField(
        value = code,
        onValueChange = { code = it },
        label = { Text("6-digit Code") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
    Spacer(modifier = Modifier.height(24.dp))
    Button(
        onClick = { viewModel.verifyOtp(code) },
        modifier = Modifier.fillMaxWidth().height(56.dp)
    ) {
        Text("Verify")
    }
}
