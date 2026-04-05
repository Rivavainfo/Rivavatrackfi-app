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

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val smsGranted = permissions[Manifest.permission.READ_SMS] == true &&
                         permissions[Manifest.permission.RECEIVE_SMS] == true

        if (smsGranted) {
            viewModel.setSmsTrackingEnabled(true)
            viewModel.completeOnboarding()
            onNavigateNext(true)
        } else {
            // Check if we should show rationale or if it was permanently denied
            val shouldShowRationale = activity?.let {
                ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.READ_SMS)
            } ?: false

            if (!shouldShowRationale) {
                // Permanently denied
                showSettingsDialog = true
            } else {
                showDeniedMessage = true
            }
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
        Text(
            text = "Automatic Bank Tracking (Optional)",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "You can allow SMS access to automatically detect bank transactions.\n\n" +
                    "This is optional. All processing happens offline on your device.\n" +
                    "No data is uploaded anywhere.",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = {
                val shouldShowRationale = activity?.let {
                    ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.READ_SMS)
                } ?: false

                if (shouldShowRationale) {
                    showRationaleDialog = true
                } else {
                    val perms = mutableListOf(
                        Manifest.permission.READ_SMS,
                        Manifest.permission.RECEIVE_SMS,
                        Manifest.permission.READ_CONTACTS
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        perms.add(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    permissionLauncher.launch(perms.toTypedArray())
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Allow SMS Access", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = {
                viewModel.setSmsTrackingEnabled(false)
                viewModel.completeOnboarding()
                onNavigateNext(false)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Skip – I prefer manual tracking", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }

    if (showDeniedMessage) {
        LaunchedEffect(Unit) {
            Toast.makeText(context, "No problem. You can always enable SMS tracking later from Settings.", Toast.LENGTH_LONG).show()
            viewModel.setSmsTrackingEnabled(false)
            viewModel.completeOnboarding()
            onNavigateNext(false)
        }
    }

    if (showRationaleDialog) {
        AlertDialog(
            onDismissRequest = { showRationaleDialog = false },
            title = { Text("Permissions Needed") },
            text = { Text("TrackFi needs SMS permissions to automatically read your bank transactions and build your budget history. It operates strictly offline.") },
            confirmButton = {
                TextButton(onClick = {
                    showRationaleDialog = false
                    val perms = mutableListOf(
                        Manifest.permission.READ_SMS,
                        Manifest.permission.RECEIVE_SMS,
                        Manifest.permission.READ_CONTACTS
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        perms.add(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    permissionLauncher.launch(perms.toTypedArray())
                }) {
                    Text("Grant")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRationaleDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("Permission Denied") },
            text = { Text("You have permanently denied SMS permissions. If you want to use automatic tracking, please enable them in the app settings.") },
            confirmButton = {
                TextButton(onClick = {
                    showSettingsDialog = false
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSettingsDialog = false
                    viewModel.setSmsTrackingEnabled(false)
                    viewModel.completeOnboarding()
                    onNavigateNext(false)
                }) {
                    Text("Continue without tracking")
                }
            }
        )
    }
}
