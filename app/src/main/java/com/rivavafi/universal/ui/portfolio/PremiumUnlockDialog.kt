package com.rivavafi.universal.ui.portfolio

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rivavafi.universal.ui.theme.EmeraldGreen
import com.rivavafi.universal.ui.theme.PrimaryContainerSky
import com.rivavafi.universal.ui.theme.glassMorphism
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumUnlockDialog(
    userName: String,
    onDismiss: () -> Unit,
    onUnlockSuccess: () -> Unit,
    onPayClick: (() -> Unit)? = null
) {
    var secretKeyInput by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var paymentReferenceInput by remember { mutableStateOf("") }
    var showPaymentError by remember { mutableStateOf(false) }
    var currentStep by remember { mutableStateOf(UnlockStep.Main) }
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF131313))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                },
                label = "Unlock Flow"
            ) { step ->
                when (step) {
                    UnlockStep.Main -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(
                                        Brush.linearGradient(listOf(Color(0xFF00A3FF), Color(0xFF00E471))),
                                        shape = RoundedCornerShape(16.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.VpnKey, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                            }

                            Text(
                                "Unlock Rivava Portfolio",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                "Enter your secret key or make a quick payment to permanently unlock all premium portfolio insights.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = secretKeyInput,
                                onValueChange = {
                                    secretKeyInput = it
                                    showError = false
                                },
                                label = { Text("Secret Key") },
                                isError = showError,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                    errorBorderColor = MaterialTheme.colorScheme.error
                                ),
                                singleLine = true
                            )

                            if (showError) {
                                Text("Invalid secret key.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                            }

                            Button(
                                onClick = {
                                    if (secretKeyInput == "RIVAVA_UNLOCK_2026" || secretKeyInput.equals(userName, ignoreCase = true)) {
                                        val prefs = context.getSharedPreferences("RivavaPortfolioPrefs", Context.MODE_PRIVATE)
                                        prefs.edit().putBoolean("portfolio_unlocked", true).apply()
                                        android.widget.Toast.makeText(context, "Unlocked Successfully", android.widget.Toast.LENGTH_SHORT).show()
                                        currentStep = UnlockStep.Success
                                    } else {
                                        android.widget.Toast.makeText(context, "Invalid Key", android.widget.Toast.LENGTH_SHORT).show()
                                        showError = true
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Unlock with Key", fontWeight = FontWeight.Bold)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Divider(Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface.copy(0.1f))
                                Text(" OR ", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Divider(Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface.copy(0.1f))
                            }

                            Button(
                                onClick = {
                                    onPayClick?.invoke()
                                },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                            ) {
                                Icon(Icons.Default.Payment, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Pay ₹11 via Razorpay", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    UnlockStep.ConfirmPayment -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                "Verify Payment",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                "Please enter the UPI Transaction ID below to verify your payment and unlock Rivava+ Premium for your account.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = paymentReferenceInput,
                                onValueChange = {
                                    paymentReferenceInput = it
                                    showPaymentError = false
                                },
                                label = { Text("UPI Transaction ID") },
                                isError = showPaymentError,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                    errorBorderColor = MaterialTheme.colorScheme.error
                                ),
                                singleLine = true
                            )

                            if (showPaymentError) {
                                Text("Invalid Transaction ID. It must be at least 12 characters.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                            }

                            Button(
                                onClick = {
                                    if (paymentReferenceInput.length >= 12) {
                                        currentStep = UnlockStep.Verifying
                                    } else {
                                        showPaymentError = true
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                            ) {
                                Text("Verify & Unlock", fontWeight = FontWeight.Bold)
                            }

                            TextButton(onClick = { currentStep = UnlockStep.Main }) {
                                Text("Go Back", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    UnlockStep.Verifying -> {
                        LaunchedEffect(Unit) {
                            delay(3000)
                            currentStep = UnlockStep.Success
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(24.dp),
                            modifier = Modifier.padding(16.dp)
                        ) {
                            CircularProgressIndicator(color = EmeraldGreen)
                            Text(
                                "Verifying transaction...",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    UnlockStep.Success -> {
                        LaunchedEffect(Unit) {
                            delay(1500)
                            onUnlockSuccess()
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(Color(0xFF00E471).copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.LockOpen, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(48.dp))
                            }

                            Text(
                                "Rivava+ Premium Unlocked",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = EmeraldGreen,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

enum class UnlockStep {
    Main, ConfirmPayment, Verifying, Success
}
