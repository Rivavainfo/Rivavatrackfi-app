package com.rivavafi.universal.ui.auth

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
import com.rivavafi.universal.R
import com.rivavafi.universal.ui.theme.AmoledBlack
import com.rivavafi.universal.ui.theme.PrimaryContainerSky
import com.rivavafi.universal.ui.theme.PrimarySky
import com.rivavafi.universal.ui.theme.TertiaryEmerald
import com.rivavafi.universal.ui.theme.glassMorphism
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
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
                    AuthState.IDLE -> GoogleSignInSection(viewModel)
                    AuthState.LOADING -> CircularProgressIndicator(color = PrimarySky)
                    AuthState.SUCCESS -> {
                        LaunchedEffect(Unit) {
                            onAuthSuccess()
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.Lock, contentDescription = null, tint = TertiaryEmerald, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Verification Successful!", style = MaterialTheme.typography.titleLarge, color = Color.White)
                        }
                    }
                    AuthState.ERROR -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Login Failed", color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.resetState() },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimarySky, contentColor = AmoledBlack)
                            ) {
                                Text("Try Again")
                            }
                        }
                    }
                }
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
                // Production error handling
            }
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Continue to Rivava+", style = MaterialTheme.typography.titleMedium, color = Color.White)
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(context.getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
                val googleSignInClient = GoogleSignIn.getClient(context, gso)
                launcher.launch(googleSignInClient.signInIntent)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Icon(Icons.Outlined.AccountCircle, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text("Sign in with Google", fontWeight = FontWeight.Bold)
        }
    }
}
