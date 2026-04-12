package com.rivavafi.universal.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import com.rivavafi.universal.HomeActivity
import com.rivavafi.universal.ui.theme.AmoledBlack
import com.rivavafi.universal.ui.theme.PrimarySky
import com.rivavafi.universal.ui.theme.TrackFiTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class OtpActivity : ComponentActivity() {

    private val viewModel: AuthViewModel by viewModels()
    private var verificationId: String = ""
    private var phoneNumber: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        verificationId = intent.getStringExtra("verificationId") ?: ""
        phoneNumber = intent.getStringExtra("phone") ?: ""

        if (verificationId.isEmpty() || phoneNumber.isEmpty()) {
            Toast.makeText(this, "Missing phone info. Returning to login.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setContent {
            TrackFiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = AmoledBlack
                ) {
                    OtpScreenContent(
                        viewModel = viewModel,
                        verificationId = verificationId,
                        phoneNumber = phoneNumber,
                        onSuccess = { isNewUser ->
                            val intent = Intent(this, HomeActivity::class.java).apply {
                                putExtra("isNewUser", isNewUser)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                            startActivity(intent)
                        },
                        onResendSent = { newVerificationId ->
                            verificationId = newVerificationId
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun OtpScreenContent(
    viewModel: AuthViewModel,
    verificationId: String,
    phoneNumber: String,
    onSuccess: (Boolean) -> Unit,
    onResendSent: (String) -> Unit
) {
    var otp by remember { mutableStateOf("") }
    var timer by remember { mutableStateOf(60) }

    val authState by viewModel.authState.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isNewUser by viewModel.isNewUser.collectAsState()

    LaunchedEffect(timer) {
        if (timer > 0) {
            delay(1000)
            timer--
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            LaunchedEffect(errorMessage) {
                delay(3000)
                viewModel.setErrorMessage("")
            }
        }

        Button(
            onClick = {
                if (otp.length == 6) {
                    viewModel.verifyOtp(
                        verificationId = verificationId,
                        otp = otp,
                        onSuccess = { onSuccess(isNewUser ?: false) },
                        onError = {}
                    )
                } else {
                    viewModel.setErrorMessage("Please enter all 6 digits")
                }
            },
            enabled = otp.length == 6 && authState != AuthState.LOADING,
            colors = ButtonDefaults.buttonColors(containerColor = PrimarySky, contentColor = AmoledBlack),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            if (authState == AuthState.LOADING) {
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
                        onResendSent(newVerificationId)
                    }
                }
            },
            enabled = timer == 0
        ) {
            Text(
                text = if (timer > 0) "Resend OTP in ${timer}s" else "Resend OTP",
                color = if (timer > 0) MaterialTheme.colorScheme.onSurfaceVariant else PrimarySky
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
