package com.rivavafi.universal.ui.auth

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.rivavafi.universal.ui.theme.AmoledBlack
import com.rivavafi.universal.ui.theme.PrimarySky
import com.rivavafi.universal.ui.theme.TrackFiTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SetNewPasswordActivity : ComponentActivity() {

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val oobCode = intent.getStringExtra("oobCode")

        if (oobCode.isNullOrBlank()) {
            Toast.makeText(this, "Invalid or missing reset code.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setContent {
            TrackFiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = AmoledBlack
                ) {
                    SetNewPasswordContent(
                        viewModel = viewModel,
                        oobCode = oobCode,
                        onSuccess = {
                            Toast.makeText(this, "Password reset successful.", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SetNewPasswordContent(
    viewModel: AuthViewModel,
    oobCode: String,
    onSuccess: () -> Unit
) {
    var newPassword by remember { mutableStateOf("") }
    val authState by viewModel.authState.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Set New Password",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = PrimarySky
        )
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            label = { Text("New Password", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimarySky,
                focusedLabelColor = PrimarySky,
                cursorColor = PrimarySky
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage?.isNotEmpty() == true) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        Button(
            onClick = {
                if (newPassword.length >= 6) {
                    viewModel.resetPasswordWithActionCode(oobCode, newPassword) {
                        onSuccess()
                    }
                } else {
                    viewModel.setErrorMessage("Password must be at least 6 characters.")
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
                Text("Confirm Password", fontWeight = FontWeight.Bold)
            }
        }
    }
}
