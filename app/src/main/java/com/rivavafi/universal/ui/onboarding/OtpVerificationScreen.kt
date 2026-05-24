package com.rivavafi.universal.ui.onboarding

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rivavafi.universal.ui.components.RivavaBrandDisplay
import com.rivavafi.universal.ui.components.RivavaLoadingOverlay
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpVerificationScreen(
    onNavigateNext: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    var otpCode by remember { mutableStateOf("") }
    var timeLeft by remember { mutableStateOf(30) }
    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            if (it.isNotEmpty()) {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    LaunchedEffect(timeLeft) {
        if (timeLeft > 0) {
            delay(1000)
            timeLeft -= 1
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        RivavaBrandDisplay(showQuote = true)

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Enter OTP",
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "We have sent a verification code to your number.",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(
            value = otpCode,
            onValueChange = {
                val digitsOnly = it.filter { char -> char.isDigit() }
                if (digitsOnly.length <= 6) {
                    otpCode = digitsOnly
                }
            },
            label = { Text("6-digit Code", color = Color.White.copy(0.7f)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00A3FF),
                unfocusedBorderColor = Color.White.copy(0.2f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (otpCode.length == 6) {
                    viewModel.verifyOtp(otpCode) {
                        onNavigateNext()
                    }
                } else {
                    Toast.makeText(context, "Please enter 6 digit code", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00A3FF),
                contentColor = Color.White,
                disabledContainerColor = Color(0xFF005D8A),
                disabledContentColor = Color.White.copy(alpha = 0.75f)
            ),
            enabled = otpCode.length == 6 && !isLoading,
            shape = RoundedCornerShape(20.dp)
        ) {
            Text(
                "Verify",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = {
                if (timeLeft == 0) {
                    viewModel.resendOtp()
                    timeLeft = 30
                }
            },
            enabled = timeLeft == 0 && !isLoading
        ) {
            Text(
                text = if (timeLeft > 0) "Resend code in ${timeLeft}s" else "Resend Code",
                color = if (timeLeft > 0) Color.Gray else Color(0xFF00A3FF),
                fontWeight = FontWeight.Medium
            )
        }
    }

    RivavaLoadingOverlay(isLoading = isLoading)
}
