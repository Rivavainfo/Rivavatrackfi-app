package com.rivavafi.universal.ui.onboarding

import android.Manifest
import android.os.Build
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rivavafi.universal.ui.components.RivavaLoadingOverlay
import com.rivavafi.universal.ui.components.RivavaBrandDisplay
import com.rivavafi.universal.domain.preferences.SmsTrackingMode

@Composable
fun SmsOptInScreen(
    onNavigateNext: (optedIn: Boolean) -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    var showDeniedMessage by remember { mutableStateOf(false) }
    var showRationaleDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    val isLoading by viewModel.isLoading.collectAsState()

    var selectedTrackingMode by remember { mutableStateOf(SmsTrackingMode.BOTH) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        Toast.makeText(context, "SMS tracking is currently unavailable per policy.", Toast.LENGTH_SHORT).show()
        viewModel.setSmsTrackingEnabled(false)
        viewModel.completeOnboarding()
        onNavigateNext(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Do you want to enable automatic message tracking?",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "What should Rivava count from SMS?",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            ),
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "You can change this anytime in Settings.",
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedTrackingMode == SmsTrackingMode.BOTH,
                        onClick = { selectedTrackingMode = SmsTrackingMode.BOTH },
                        colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                    )
                    Text("Credit and debits", style = MaterialTheme.typography.bodyLarge)
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedTrackingMode == SmsTrackingMode.CREDIT_ONLY,
                        onClick = { selectedTrackingMode = SmsTrackingMode.CREDIT_ONLY },
                        colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                    )
                    Text("Credit only", style = MaterialTheme.typography.bodyLarge)
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedTrackingMode == SmsTrackingMode.DEBIT_ONLY,
                        onClick = { selectedTrackingMode = SmsTrackingMode.DEBIT_ONLY },
                        colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                    )
                    Text("Debits only", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                Toast.makeText(context, "SMS tracking is currently unavailable per policy.", Toast.LENGTH_SHORT).show()
                viewModel.setSmsTrackingEnabled(false)
                viewModel.completeOnboarding()
                onNavigateNext(false)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Enable automatic tracking", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = {
                viewModel.saveSmsTrackingMode(selectedTrackingMode.name)
                viewModel.setSmsTrackingEnabled(false)
                viewModel.completeOnboarding()
                onNavigateNext(false)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isLoading
        ) {
            Text("Skip (I’ll do it manually)", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(modifier = Modifier.height(32.dp))
        RivavaBrandDisplay(showQuote = true)
    }

    if (showDeniedMessage) {
        LaunchedEffect(Unit) {
            Toast.makeText(context, "No problem. You can always enable SMS tracking later from Settings.", Toast.LENGTH_LONG).show()
            viewModel.setSmsTrackingEnabled(false)
            viewModel.completeOnboarding()
            onNavigateNext(false)
        }
    }


    RivavaLoadingOverlay(isLoading = isLoading)
}
