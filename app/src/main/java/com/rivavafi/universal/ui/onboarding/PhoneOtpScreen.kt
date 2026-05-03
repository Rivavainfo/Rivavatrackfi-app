package com.rivavafi.universal.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rivavafi.universal.ui.theme.AmoledBlack
import com.rivavafi.universal.ui.theme.PrimarySky
import kotlinx.coroutines.delay

@Composable
fun PhoneOtpScreen(
    verificationId: String,
    phoneNumber: String,
    onNavigateNext: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    var otp by remember { mutableStateOf("") }
    var timer by remember { mutableStateOf(60) }
    var currentVerificationId by remember { mutableStateOf(verificationId) }

    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(timer) {
        if (timer > 0) {
            delay(1000)
            timer--
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            if (it.isNotEmpty()) {
                delay(3000)
                viewModel.clearError()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Verify Phone",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = PrimarySky
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Enter 6-digit OTP sent to $phoneNumber",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        OtpInputField(otpText = otp, onOtpChange = { otp = it })

        Spacer(modifier = Modifier.height(32.dp))

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
                if (otp.length == 6) {
                    viewModel.verifyOtp(
                        verificationId = currentVerificationId,
                        otp = otp,
                        phoneNumber = phoneNumber,
                        onSuccess = { onNavigateNext() },
                        onError = {}
                    )
                } else {
                    viewModel.setErrorMessage("Please enter all 6 digits")
                }
            },
            enabled = otp.length == 6 && !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = PrimarySky, contentColor = AmoledBlack),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = AmoledBlack, modifier = Modifier.size(24.dp))
            } else {
                Text("Verify OTP", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val context = androidx.compose.ui.platform.LocalContext.current
        TextButton(
            onClick = {
                timer = 60
                (context as? android.app.Activity)?.let { act ->
                    viewModel.resendOtp(phoneNumber, act) { newVerificationId ->
                        currentVerificationId = newVerificationId
                    }
                }
            },
            enabled = timer == 0
        ) {
            Text(
                text = if (timer > 0) "Resend OTP in ${timer}s" else "Resend OTP",
                color = if (timer > 0) Color.White.copy(alpha = 0.5f) else PrimarySky
            )
        }
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
                                color = if (isFocused) PrimarySky else Color.White.copy(alpha = 0.5f),
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
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    )
}
