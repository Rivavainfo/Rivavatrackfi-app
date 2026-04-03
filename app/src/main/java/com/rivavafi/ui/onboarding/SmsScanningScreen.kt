package com.rivavafi.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SmsScanningScreen(
    onNavigateNext: () -> Unit,
    viewModel: ScanningViewModel = hiltViewModel()
) {
    val scanState by viewModel.scanState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!scanState.isComplete) {
            Text(
                text = "Scanning your messages",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            CircularProgressIndicator(
                modifier = Modifier.size(80.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 6.dp
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = scanState.message,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                textAlign = TextAlign.Center
            )

            if (scanState.totalCount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Analyzed ${scanState.processedCount} of ${scanState.totalCount} messages",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                scanState.banksDetected.forEach { bank ->
                    Text(
                        text = "$bank detected",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        } else {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(500))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (scanState.error != null) {
                        Text(
                            text = "Scan Failed",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = scanState.error ?: "Unknown error",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.error
                            )
                        )
                    } else {
                        Text(
                            text = "Scan Complete!",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "${scanState.banksDetected.size} Banks Found",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )

                        Text(
                            text = "${scanState.totalTransactionsFound} Transactions Analyzed",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(48.dp))

                    Button(
                        onClick = onNavigateNext,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text("Go to Home", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
